package uk.gov.dwp.dataworks.logging

import java.net.InetAddress

// TODO handle env vars as well as system props
object CommonLogFields {
    private val configItems: Map<String, String> =
        LogField.values().associate {
            val value = System.getProperty(it.systemPropName) ?: it.default
            Pair(it.systemPropName, value)
        }

    val commonFieldsAsJson = configItems.map { """ "${it.key}":"${it.value}" """.trim() }.joinToString(separator = ",")

    fun get(logField: String): String {
        return configItems.getValue(logField)
    }

    fun get(logField: LogField): String {
        return get(logField.systemPropName)
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