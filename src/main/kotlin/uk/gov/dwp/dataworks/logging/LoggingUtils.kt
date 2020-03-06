package uk.gov.dwp.dataworks.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import ch.qos.logback.core.CoreConstants
import ch.qos.logback.core.LayoutBase
import org.apache.commons.text.StringEscapeUtils
import java.net.InetAddress
import java.time.Instant
import java.time.format.DateTimeFormatter

fun flattenString(text: String?): String {
    return text
        ?.replace("\r\n", " | ")
        ?.replace("\n", " | ")
        ?.replace("\t", " ")
        ?: "null"
}

fun throwableProxyEventToString(event: ILoggingEvent): String {
    val throwableProxy = event.throwableProxy
    return if (throwableProxy != null) {
        val stackTrace = ThrowableProxyUtil.asString(throwableProxy)
        val oneLineTrace = StringEscapeUtils.escapeJson(flattenString(stackTrace))
        "\"exception\":\"$oneLineTrace\", "
    } else {
        ""
    }
}

fun semiFormattedTuples(message: String, vararg tuples: Pair<String, String>): String {
    if(tuples.isNotEmpty()) {
        val formattedTuples = tuples.joinToString(separator = """ "," """.trim(), transform = {"""${it.first}":"${StringEscapeUtils.escapeJson(it.second)}"""})
        return """${StringEscapeUtils.escapeJson(message)}","$formattedTuples"""
    }
    return message
}

//fun semiFormattedTuples(message: String, vararg tuples: String): String {
//    val semiFormatted = StringBuilder(StringEscapeUtils.escapeJson(message))
//    if (tuples.size % 2 != 0) {
//        throw IllegalArgumentException("Must have equal number of key-value pairs but had ${tuples.size} argument(s)")
//    }
//
//    for (i in tuples.indices step 2) {
//
//        val key = tuples[i]
//        val value = tuples[i + 1]
//        val escapedValue = StringEscapeUtils.escapeJson(value)
//        semiFormatted.append("\", \"")
//        semiFormatted.append(key)
//        semiFormatted.append("\":\"")
//        semiFormatted.append(escapedValue)
//    }
//    return semiFormatted.toString()
//}

//fun test() {
//    "key" to "value"
//    semiFormattedTuples("Written manifest", "attempt_number", "${attempts + 1}", "manifest_size", "$manifestSize", "s3_location", "s3://$manifestBucket/$manifestPrefix/$manifestFileName")
//    semiFormattedTuples("Written manifest", "attempt_number" to "${attempts + 1}", "manifest_size" to "$manifestSize", "s3_location" to "s3://$manifestBucket/$manifestPrefix/$manifestFileName")
//    semiFormattedTuples("Written manifest", Pair("attempt_number", "${attempts + 1}"), Pair("manifest_size", "$manifestSize"), Pair("s3_location", "s3://$manifestBucket/$manifestPrefix/$manifestFileName"))
//}