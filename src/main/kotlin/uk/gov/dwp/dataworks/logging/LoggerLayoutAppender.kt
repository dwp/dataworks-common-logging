package uk.gov.dwp.dataworks.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.LayoutBase
import java.time.Instant
import java.time.format.DateTimeFormatter

object LoggerLayoutAppender : LayoutBase<ILoggingEvent>() {

    var start_time_milliseconds = System.currentTimeMillis()

    fun getDurationInMilliseconds(epochTime: Long): String {
        val elapsedMilliseconds = epochTime - start_time_milliseconds
        return elapsedMilliseconds.toString()
    }

    fun epochToUTCString(epochTime: Long): String {
        return DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss.SSS").format(Instant.ofEpochSecond(epochTime))
    }

    override fun doLayout(event: ILoggingEvent?): String {
        if (event == null) {
            return ""
        }
        val builder = StringBuilder()
        builder.append("{ ")
        builder.append("\"timestamp\":\"")
        builder.append(epochToUTCString(event.timeStamp))
        builder.append("\", \"log_level\":\"")
        builder.append(event.level)
        builder.append("\", \"message\":\"")
        builder.append(flattenString(event.formattedMessage))
        builder.append("\", ")
        builder.append(throwableProxyEventToString(event))
        builder.append("\"thread\":\"")
        builder.append(event.threadName)
        builder.append("\", \"logger\":\"")
        builder.append(event.loggerName)
        builder.append("\", \"duration_in_milliseconds\":\"")
        builder.append(getDurationInMilliseconds(event.timeStamp))
        builder.append("\", ")
        builder.append(CommonLogFields.commonFieldsAsJson)
        builder.append(" }")
        builder.append(CoreConstants.LINE_SEPARATOR)
        return builder.toString()
    }
}
