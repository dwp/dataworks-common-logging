package uk.gov.dwp.dataworks.logging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.lang.IllegalArgumentException

class LogFieldsTest {
    @ParameterizedTest
    @EnumSource(LogField::class)
    fun `Common log fields returns values using LogField object`(logField: LogField) {
        val actual = LogFields.get(logField)
        assertThat(actual)
            .withFailMessage("Expected value of %s to be %s but was %s", logField.systemPropName, logField.default, actual)
            .isEqualTo(logField.default)
    }

    @ParameterizedTest
    @EnumSource(LogField::class)
    fun `Common log fields returns values using String`(logField: LogField) {
        val actual = LogFields.get(logField.systemPropName)
        assertThat(actual)
            .withFailMessage("Expected value of %s to be %s but was %s", logField.systemPropName, logField.default, actual)
            .isEqualTo(logField.default)
    }

    @ParameterizedTest
    @EnumSource(LogField::class)
    fun `Common log fields are populated statically`(logField: LogField) {
        assertThat(LogFields.get(logField.systemPropName)).isEqualTo(logField.default)
    }

    @ParameterizedTest
    @EnumSource(LogField::class)
    fun `LogFields statically populates JSON string`(logField: LogField) {
        assertThat(LogFields.asJson).contains(logField.systemPropName)
        assertThat(LogFields.asJson).contains(logField.default)
        assertThat(LogFields.asJson).contains(""" "${logField.systemPropName}":"${logField.default}" """.trim())
    }

    @Test
    fun `Log field can set and retrieve custom values`() {
        val expectedValue = "customValue"
        val expectedKey = "customKey"
        LogFields.put(expectedKey, expectedValue)
        assertThat(LogFields.get(expectedKey)).isEqualTo(expectedValue)
    }

    @Test
    fun `Log field throws exception when custom key is blank`() {
        assertThrows<IllegalArgumentException> {  LogFields.put("", "value") }
    }

    @Test
    fun `Log field throws exception when custom value is blank`() {
        assertThrows<IllegalArgumentException> {  LogFields.put("key", "") }
    }
}