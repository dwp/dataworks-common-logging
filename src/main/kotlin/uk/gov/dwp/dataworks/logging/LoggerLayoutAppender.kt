package uk.gov.dwp.dataworks.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.LayoutBase
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

object LoggerLayoutAppender : LayoutBase<ILoggingEvent>() {

    var start_time_milliseconds = System.currentTimeMillis()

    fun getDurationInMilliseconds(epochTime: Long): String {
        val elapsedMilliseconds = epochTime - start_time_milliseconds
        return elapsedMilliseconds.toString()
    }

    fun epochToUTCString(epochTime: Long): String {
        val dtf = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss.SSS")
        return dtf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(epochTime), ZoneOffset.UTC))
    }

    override fun doLayout(event: ILoggingEvent?): String {
        if (event == null) {
            return ""
        }
        return """
            |{
                |"timestamp":"${epochToUTCString(event.timeStamp)}",
                |"log_level":"${event.level}",
                |"message":${flattenString(event.formattedMessage)},
                |"exception":"${throwableProxyEventToString(event)}",
                |"thread":"${event.threadName}",
                |"logger":"${event.loggerName}",
                |"duration_in_milliseconds":"${getDurationInMilliseconds(event.timeStamp)}",
                |${LogFields.asJson}
            |}
        """.trimMargin("|").replace("\n", "")
    }
}
