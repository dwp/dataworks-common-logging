package uk.gov.dwp.dataworks.logging

import java.net.InetAddress

/**
 * Object to hold the Application specific and Common fields that should be added to each of the log lines output by
 * this library. Provides helper methods to return these fields as a set of json compliant Key:value pairs but without
 * any wrapping (i.e no `{}`).
 */
object LogFields {
    private val logFields = CommonLogFields.commonFields.toMutableMap()
    val asJson = logFields.map { """ "${it.key}":"${it.value}" """.trim() }.joinToString(separator = ",")

    fun get(logField: String): String {
        return logFields.getValue(logField)
    }

    fun get(logField: LogField): String {
        return get(logField.systemPropName)
    }

    fun put(logField: String, value: String) {
        if(logField.isBlank() || value.isBlank()) {
            throw IllegalArgumentException("Key and value must not be blank! Got: $logField:$value")
        }
        logFields[logField] = value
    }
}

// TODO handle env vars as well as system props
/**
 * Object which represents fields that are common across all applications. These are extracted from Environment or
 * Java System variables and used to prepend all log lines sent by this library.
 */
object CommonLogFields {
    val commonFields: Map<String, String> =
        LogField.values().associate {
            val value = System.getProperty(it.systemPropName) ?: it.default
            Pair(it.systemPropName, value)
        }
}

enum class LogField(val systemPropName: String, val default: String) {
    ENVIRONMENT("environment", "NOT_SET"),
    APPLICATION("application", "NOT_SET"),
    APP_VERSION("app_version", "NOT_SET"),
    COMPONENT("component", "NOT_SET"),
    CORRELATION_ID("correlation_id", "NOT_SET"),
    HOSTNAME("hostname", InetAddress.getLocalHost().hostName);
}