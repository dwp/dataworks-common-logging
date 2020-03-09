# dataworks-common-logging
Kotlin utility library to be used in Dataworks applications to ensure common logging format.

## Log formatting
Dataworks common logging provides opinionated ways to write messages into log files, using [sl4j](http://www.slf4j.org/).

Out of the box, it provides functionality to convert log messages to JSON and appends a number of common fields. These can be found in the `LogField` enum class. Any `LogField` which is not found at runtime gets set to a default value, also defined in that enum. Some example variable values are as follows:

| Variable       | Example value  |
|----------------|----------------|
| environment    | development    |
| application    | my-special-api |
| app_version    | v1             |
| component      | read-from-x    |
| correlation_id | 1              |
| hostname       | 127.0.0.1      |

## Using in projects
To include this library in your project you will need to include the compiled this projects`.jar` file. You are also required to add a logback XML file in the resources for your project, and add the following code as an `appender`. This will inform the logging framework to use `LoggerLayoutAppender` to parse messages into our format.
```xml
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <!-- encoders are assigned the type ch.qos.logback.classic.encoder.PatternLayoutEncoder by default -->
        <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
            <layout class="app.utils.logging.LoggerLayoutAppender"/>
        </encoder>
    </appender>
```

## Example log calls
Below are some examples of how you can use the library to output logs.

##### Including a DataworksLogger in a class
```kotlin
companion object {
    val logger = DataworksLogger.getLogger("thisClass")
}
``` 

##### Writing a log at DEBUG for data output program
```kotlin
val logger = DataworksLogger.getLogger("thisClass")

// Using Kotlin-like syntax (preferred):
semiFormattedTuples("Written manifest", "attempt_number" to "${attempts + 1}", "manifest_size" to "$manifestSize", "s3_location" to "s3://$manifestBucket/$manifestPrefix/$manifestFileName")

// Using Java-like syntax
logger.debug("Written manifest",  Pair("attempt_number", "${attempts + 1}"), Pair("manifest_size", "$manifestSize"), Pair("s3_location", "s3://$manifestBucket/$manifestPrefix/$manifestFileName"))
``` 

## Outputs
Log lines resulting from this library are formatted as such:
```text
{"timestamp":"1970-04-25T07:29:03.210","log_level":"WARN","message":"some message about stuff","exception":"","thread":"my.thread.is.betty","logger":"logger.name.is.mavis","duration_in_milliseconds":"-1573885286308","environment":"test","application":"tests","app_version":"v1","component":"tests","correlation_id":"1","hostname":"127.0.0.1"}
```