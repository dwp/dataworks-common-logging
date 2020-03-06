package uk.gov.dwp.dataworks.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LogLayoutAppenderTest {
    @Test
    fun testFormattedTimestamp_WillUseDefaultFormat_WhenCalled() {
        assertThat("1970-01-01T00:00:00.000").isEqualTo(LoggerLayoutAppender.epochToUTCString(0))
        assertThat("1973-03-01T23:29:03.210").isEqualTo(LoggerLayoutAppender.epochToUTCString(99876543210))
        assertThat("292278994-08-17T07:12:55.807").isEqualTo(LoggerLayoutAppender.epochToUTCString(Long.MAX_VALUE))
    }

    @Test
    fun testLoggerLayoutAppender_WillReturnEmpty_WhenCalledWithNothing() {
        val result = LoggerLayoutAppender.doLayout(null)
        Assertions.assertThat(result).isEqualTo("")
    }

    @Test
    fun testLoggerLayoutAppender_WillReturnSkinnyJson_WhenCalledWithEmptyEvent() {
        val result = LoggerLayoutAppender.doLayout(mock())
        val expected = """{ "timestamp":"1970-01-01T00:00:00.000", "log_level":"null", "message":"null", "thread":"null", "logger":"null", "duration_in_milliseconds":"-9876543000", "hostname":"test-host", "environment":"test-env", "application":"my-app", "app_version":"v1", "component":"tests", "correlation_id":"test1"}"""
        assertThat(result).isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun testLoggerLayoutAppender_WillFormatAsJson_WhenCalledWithVanillaMessage() {
        val mockEvent = mock<ILoggingEvent>()
        whenever(mockEvent.timeStamp).thenReturn(9876543210)
        whenever(mockEvent.level).thenReturn(Level.WARN)
        whenever(mockEvent.threadName).thenReturn("my.thread.is.betty")
        whenever(mockEvent.loggerName).thenReturn("logger.name.is.mavis")
        whenever(mockEvent.formattedMessage).thenReturn("some message about stuff")
        whenever(mockEvent.hasCallerData()).thenReturn(false)
        val expected = """{ "timestamp":"1970-04-25T07:29:03.210", "log_level":"WARN", "message":"some message about stuff", "thread":"my.thread.is.betty", "logger":"logger.name.is.mavis", "duration_in_milliseconds":"210", "hostname":"test-host", "environment":"test-env", "application":"my-app", "app_version":"v1", "component":"tests", "correlation_id":"test1"}"""
        val result = LoggerLayoutAppender.doLayout(mockEvent)
        assertThat(result).isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun testLoggerLayoutAppender_WillFlattenMultilineMessages_WhenCalledWithAnyMessage() {
        val mockEvent = mock<ILoggingEvent>()
        whenever(mockEvent.timeStamp).thenReturn(9876543210)
        whenever(mockEvent.level).thenReturn(Level.WARN)
        whenever(mockEvent.threadName).thenReturn("my.thread.is.betty")
        whenever(mockEvent.loggerName).thenReturn("logger.name.is.mavis")
        whenever(mockEvent.formattedMessage).thenReturn("some\nmessage\nabout\nstuff with\ttabs")
        whenever(mockEvent.hasCallerData()).thenReturn(false)

        val result = LoggerLayoutAppender.doLayout(mockEvent)
        val expected = """{ "timestamp":"1970-04-25T07:29:03.210", "log_level":"WARN", "message":"some | message | about | stuff with tabs", "thread":"my.thread.is.betty", "logger":"logger.name.is.mavis", "duration_in_milliseconds":"210", "hostname":"test-host", "environment":"test-env", "application":"my-app", "app_version":"v1", "component":"tests", "correlation_id":"test1"}"""
        assertThat(result).isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun testLoggerLayoutAppender_WillFormatAsJson_WhenCalledWithEmbeddedTuplesInMessage() {
        val mockEvent = mock<ILoggingEvent>()
        whenever(mockEvent.timeStamp).thenReturn(9876543210)
        whenever(mockEvent.level).thenReturn(Level.WARN)
        whenever(mockEvent.threadName).thenReturn("my.thread.is.betty")
        whenever(mockEvent.loggerName).thenReturn("logger.name.is.mavis")
        val embeddedTokens = semiFormattedTuples("some message about stuff", "key1" to "value1", "key2" to "value2")
        whenever(mockEvent.formattedMessage).thenReturn(embeddedTokens)
        whenever(mockEvent.hasCallerData()).thenReturn(false)

        val result = LoggerLayoutAppender.doLayout(mockEvent)
        val expected = """{ "timestamp":"1970-04-25T07:29:03.210", "log_level":"WARN", "message":"some message about stuff", "key1":"value1", "key2":"value2", "thread":"my.thread.is.betty", "logger":"logger.name.is.mavis", "duration_in_milliseconds":"210", "hostname":"test-host", "environment":"test-env", "application":"my-app", "app_version":"v1", "component":"tests", "correlation_id":"test1" }
"""
        assertThat(result).isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun testLoggerLayoutAppender_ShouldNotEscapeTheJsonMessage_AsThatWouldMessWithOurCustomStaticLogMethodsWhichDo() {
        val mockEvent = mock<ILoggingEvent>()
        whenever(mockEvent.timeStamp).thenReturn(9876543210)
        whenever(mockEvent.level).thenReturn(Level.WARN)
        whenever(mockEvent.threadName).thenReturn("my.thread.is.betty")
        whenever(mockEvent.loggerName).thenReturn("logger.name.is.mavis")
        whenever(mockEvent.formattedMessage).thenReturn("message-/:'!@")
        whenever(mockEvent.hasCallerData()).thenReturn(false)

        val result = LoggerLayoutAppender.doLayout(mockEvent)
        val expected = """{ "timestamp":"1970-04-25T07:29:03.210", "log_level":"WARN", "message":"message-/:'!@", "thread":"my.thread.is.betty", "logger":"logger.name.is.mavis", "duration_in_milliseconds":"210", "hostname":"test-host", "environment":"test-env", "application":"my-app", "app_version":"v1", "component":"tests", "correlation_id":"test1"}"""
        assertThat(result).isEqualToIgnoringWhitespace(expected)
    }

    @Test
    fun testLoggerLayoutAppender_ShouldAddExceptions_WhenProvided() {
        val mockEvent = mock<ILoggingEvent>()
        whenever(mockEvent.timeStamp).thenReturn(9876543210)
        whenever(mockEvent.level).thenReturn(Level.WARN)
        whenever(mockEvent.threadName).thenReturn("my.thread.is.betty")
        whenever(mockEvent.loggerName).thenReturn("logger.name.is.mavis")
        whenever(mockEvent.formattedMessage).thenReturn("some message about stuff")

        val stubThrowable = ThrowableProxy(catchMe1())
        ThrowableProxyUtil.build(stubThrowable, catchMe2(), ThrowableProxy(catchMe3()))
        whenever(mockEvent.throwableProxy).thenReturn(stubThrowable as IThrowableProxy)

        val result = LoggerLayoutAppender.doLayout(mockEvent)
        assertThat(result).containsPattern("\"exception\":\".*\"")
    }
}