package uk.gov.dwp.dataworks.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.LayoutBase
import org.apache.commons.text.StringEscapeUtils
import java.net.InetAddress
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Formats a string with the following rules:
 * * Replaces line endings (Windows & Unix) with space delimited pipes (`" | "`)
 * * Replaces tab chars (`\t`) with a space
 * * If `text` is null return the literal string "null"
 */
fun flattenString(text: String?): String {
    return text
            ?.replace("\r\n", " | ")
            ?.replace("\n", " | ")
            ?.replace("\t", " ")
            ?: "null"
}

/**
 * Converts an [IThrowableProxy] object to a single line representation of it's Stack Trace. Returns a blank string if
 * `event` is empty.
 */
fun throwableProxyEventToString(event: ILoggingEvent): String {
    val throwableProxy = event.throwableProxy ?: return ""

    val stackTrace = ThrowableProxyUtil.asString(throwableProxy)
    return StringEscapeUtils.escapeJson(flattenString(stackTrace))
}

/**
 * Converts a message and a set of Tuples to an _almost_ formatted Json string. Tuples are first parsed as follows:
 * ```
 * "TupleKey1":"TupleValue1","TupleKey2":"TupleValue2"
 * ```
 * Then the input message is escaped as per [StringEscapeUtils.escapeJson] and prepended with quotes. The resulting
 * output will look like the following:
 * ```
 * "input message contents","TupleKey1":"TupleValue1","TupleKey2":"TupleValue2"
 * ```
 */
fun semiFormattedTuples(message: String, vararg tuples: Pair<String, String>): String {
    if (tuples.isEmpty()) {
        return """ "$message" """.trim()
    }
    val formattedTuples = tuples.joinToString(
            separator = """ "," """.trim(),
            transform = { """${it.first}":"${StringEscapeUtils.escapeJson(it.second)}""" })
    return """ "${StringEscapeUtils.escapeJson(message)}","$formattedTuples" """.trim()
}

//fun test() {
//    "key" to "value"
//    semiFormattedTuples("Written manifest", "attempt_number", "${attempts + 1}", "manifest_size", "$manifestSize", "s3_location", "s3://$manifestBucket/$manifestPrefix/$manifestFileName")
//    semiFormattedTuples("Written manifest", "attempt_number" to "${attempts + 1}", "manifest_size" to "$manifestSize", "s3_location" to "s3://$manifestBucket/$manifestPrefix/$manifestFileName")
//    semiFormattedTuples("Written manifest", Pair("attempt_number", "${attempts + 1}"), Pair("manifest_size", "$manifestSize"), Pair("s3_location", "s3://$manifestBucket/$manifestPrefix/$manifestFileName"))
//}