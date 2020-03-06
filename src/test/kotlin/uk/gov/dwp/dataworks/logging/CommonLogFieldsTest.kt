package uk.gov.dwp.dataworks.logging

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class CommonLogFieldsTest {
    @ParameterizedTest
    @EnumSource(LogField::class)
    fun testCommonLogFields_GetByLogField(logField: LogField) {
        val actual = CommonLogFields.get(logField)
        assertThat(actual)
            .withFailMessage("Expected value of %s to be %s but was %s", logField.systemPropName, logField.default, actual)
            .isEqualTo(logField.default)
    }

    @ParameterizedTest
    @EnumSource(LogField::class)
    fun testCommonLogFields_GetByKey(logField: LogField) {
        val actual = CommonLogFields.get(logField.systemPropName)
        assertThat(actual)
            .withFailMessage("Expected value of %s to be %s but was %s", logField.systemPropName, logField.default, actual)
            .isEqualTo(logField.default)
    }

    @ParameterizedTest
    @EnumSource(LogField::class)
    fun testConfigItems_WillPopulateStatically(logField: LogField) {
        assertThat(CommonLogFields.get(logField.systemPropName)).isEqualTo(logField.default)
    }

    @ParameterizedTest
    @EnumSource(LogField::class)
    fun testCommonFieldsAsJson_WillPopulateJsonStatically(logField: LogField) {
        assertThat(CommonLogFields.commonFieldsAsJson).contains(logField.systemPropName)
        assertThat(CommonLogFields.commonFieldsAsJson).contains(logField.default)
    }
}