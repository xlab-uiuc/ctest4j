## JUnit5 CTest4J Runner

### Usage

#### Add Runner As Dependency
For maven project, add the following dependency to your pom.xml:
```xml
<dependencies>
    <dependency>
        <groupId>edu.illinois</groupId>
        <artifactId>ctest4j-junit5</artifactId>
        <version>${ctest4j-version}</version>
    </dependency>
</dependencies>
```

#### Manual Instrumentation:
To track configuration parameters, you need to add the following code to your project:
```java
public static <T> void injectConfig(BiConsumer<String, T> configSetterMethod) throws IOException;
public static void markParamAsUsed(String param);
public static void markParamAsSet(String param);
```
`injectConfig` method need to be added to the end of your Configuration class constructor to inject configuration value
with configuration setter method. `markParamAsUsed` method need to be added to the end of your configuration getter method
to mark the configuration parameter as used.
`markParamAsSet` method need to be added to the end of your configuration setter method to mark the configuration parameter as set.
Here is an example of the [instrumented configuration class](junit4-runner/src/test/java/Configuration.java).

#### Write Configuration Test
Use `@ExtendWith(CTestJunit5Extension.class)` to specify the JUnit5 extension for your test class.
`@CTestClass` specifies the configuration parameters that must be used in the all test methods in the test class.
For each configuration test method, use `@CTest` to specify the configuration parameter that must be used in the test method.

There are two ways of specifying configuration parameters:
1. Directly write configuration parameter name in `@CTestClass` and `@CTest` annotation as value().
2. Put configuration parameter name in a file and specify the file path in `@CTestClass` and `@CTest` annotation as file().

Current runner support JSON file format for configuration parameter file,
one can implement [ConfigurationParser](junit4-runner/src/main/java/edu/illinois/parser/ConfigurationParser.java) to support
other file format and override `getParser()` method in [ConfigTestRunner#getParser](junit4-runner/src/main/java/edu/illinois/ConfigTestRunner.java).

```java
@ExtendWith(CTestJunit5Extension.class)
@CTestClass(value = {"parameter1"}, file = "config.json")
public class FromMethodTest {
    @CTest({"parameter2"})
    public void test() {
        // Assume parameter3 is specified in config.json
        // Then parameter1, parameter2 and parameter3 must be used in this test method
        // Otherwise UnUsedConfigParamException will be thrown
    }
}
```
