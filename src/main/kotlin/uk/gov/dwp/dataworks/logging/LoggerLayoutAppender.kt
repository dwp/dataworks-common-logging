package uk.gov.dwp.dataworks.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.LayoutBase

/**
 * Implementation of [LayoutBase] which allows us to parse events and reformat into JSON structured messages.
 */
object LoggerLayoutAppender : LayoutBase<ILoggingEvent>() {

    private var start_time_milliseconds = System.currentTimeMillis()

    private fun getDurationInMilliseconds(epochTime: Long): String {
        val elapsedMilliseconds = epochTime - start_time_milliseconds
        return elapsedMilliseconds.toString()
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
