package uk.gov.dwp.dataworks.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.apache.commons.text.StringEscapeUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class LoggingUtilsTest {
    @Test
    fun `semiFormattedTuples will format as partial JSON without matching key-value pairs`() {
        assertThat("my-message").isEqualToIgnoringWhitespace(semiFormattedTuples("my-message"))
    }

    @Test
    fun `semiFormattedTuples will format as partial JSON with matching key-value pairs`() {
        assertThat("\"my-message\", \"key1\":\"value1\", \"key2\":\"value2\"")
            .isEqualToIgnoringWhitespace(semiFormattedTuples("my-message", "key1" to "value1", "key2" to "value2"))
    }

    @Test
    fun `semiFormattedTuples will escape JSON in message and Tuple values`() {
        assertThat("\"message-\\/:'!@\\u00A3\$%^&*()\\n\\t\\r\", \"key-unchanged\":\"value-\\/:!@\\u00A3\$%^&*()\\n\\t\\r\"")
            .isEqualToIgnoringWhitespace(semiFormattedTuples("message-/:'!@£\$%^&*()\n\t\r", "key-unchanged" to "value-/:!@£\$%^&*()\n\t\r"))
    }

    @Test
    fun `inlineStackTrace removes tabs, newlines and escape chars`() {
        val stubThrowable = ThrowableProxy(catchMe1())
        ThrowableProxyUtil.build(stubThrowable, catchMe2(), ThrowableProxy(catchMe3()))

        val throwableStr = ThrowableProxyUtil.asString(stubThrowable)
        val result = StringEscapeUtils.escapeJson(flattenString(throwableStr))
        assertThat(result).contains("boom1 - \\/:'!@\\u00A3\$%^&*()")
        assertThat(result).doesNotContain("\n", "\t", "\r\n")
    }

    @Test
    fun `flattenMultipleLines will remove tabs and newlines but not escape chars`() {

        val trace = "java.lang.RuntimeException: boom1 - /:'!@£\$%^&*()\n" +
            "\tat app.utils.logging.LoggerUtilsTest\$MakeStacktrace2.callMe2(LoggerUtilsTest.kt:87)\n"

        val result = flattenString(trace)
        assertThat(result).contains("java.lang.RuntimeException: boom1 - /:'!@£\$%^&*()")
        assertThat(result).doesNotContain("\n", "\t", "\r\n")
    }

    @Test
    fun `throwableProxyEventToString embeds as JSON key`() {
        val mockEvent = mock<ILoggingEvent>()
        whenever(mockEvent.timeStamp).thenReturn(9876543210)
        whenever(mockEvent.level).thenReturn(Level.WARN)
        whenever(mockEvent.threadName).thenReturn("my.thread.is.betty")
        whenever(mockEvent.loggerName).thenReturn("logger.name.is.mavis")
        whenever(mockEvent.formattedMessage).thenReturn("some message about stuff")

        val stubThrowable = ThrowableProxy(catchMe1())
        ThrowableProxyUtil.build(stubThrowable, catchMe2(), ThrowableProxy(catchMe3()))
        whenever(mockEvent.throwableProxy).thenReturn(stubThrowable as IThrowableProxy)

        val result = throwableProxyEventToString(mockEvent)

        assertThat(result).contains("boom1 - \\/:'!@\\u00A3${'$'}%^&*()")
    }
}
