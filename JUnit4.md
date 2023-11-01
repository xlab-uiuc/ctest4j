## JUnit4 Configuration Test Runner

### Usage

#### Add Runner as dependency 
For maven project, add the following dependency to your pom.xml:
```xml
<dependencies>
    <dependency>
        <groupId>edu.illinois</groupId>
        <artifactId>junit4-ctest-runner</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

#### DO NOT USE IT FOR NOW: (Optional) Automatic Instrumentation Only：
Specify Runner Agent and Configuration APIs in Maven Surefire plugin:
```xml
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <argLine>-javaagent:~/.m2/repository/edu/illinois/CTestRunner/1.0-SNAPSHOT/CTestRunner-1.0-SNAPSHOT.jar</argLine>
          <systemPropertyVariables>
            <configurationClassName>${CONFIGURATION_CLASS}</configurationClassName>
            <configurationGetterMethod>${GETTER_METHOD_NAME_AND_DESCRIPTOR}</configurationGetterMethod>
            <configurationSetterMethod>${SETTER_METHOD_NAME_AND_DESCRIPTOR}</configurationSetterMethod>
          </systemPropertyVariables>
        </configuration>
      </plugin>
```

#### Manual Instrumentation:
To track configuration parameters, you need to add the following code to your project:
```java
public static <T> void injectConfig(BiConsumer<String, T> configSetterMethod) throws IOException
public static void markParamAsUsed(String param)
```
`injectConfig` method need to be added to the end of your Configuration class constructor to inject configuration value 
with configuration setter method. `markParamAsUsed` method need to be added to the end of your configuration getter method
to mark the configuration parameter as used.
Here is an example of the [instrumented configuration class](junit4-runner/src/test/java/Configuration.java).

#### Write Configuration Test
Use `@RunWith(ConfigTestRunner.class)` to specify the runner for your test class.
`@CTestClass` specifies the configuration parameters that must be used in the all test methods in the test class.
For each configuration test method, use `@CTest` to specify the configuration parameter that must be used in the test method.

There are two ways of specifying configuration parameters:
1. Directly write configuration parameter name in `@CTestClass` and `@CTest` annotation as value().
2. Put configuration parameter name in a file and specify the file path in `@CTestClass` and `@CTest` annotation as file().

Current runner support JSON file format for configuration parameter file, 
one can implement [ConfigurationParser](junit4-runner/src/main/java/edu/illinois/parser/ConfigurationParser.java) to support 
other file format and override `getParser()` method in [ConfigTestRunner#getParser](junit4-runner/src/main/java/edu/illinois/ConfigTestRunner.java).

```java
@RunWith(ConfigTestRunner.class)
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

#### Track Configuration Set with Existing Test
If you do not know which parameter is used in a test method, you can use `@RunWith(ConfigTestRunner.class)` and specify
your test method with normal `@Test`.
After you execute the test with the runner, the runner will print out the configuration parameters used in the test method.
```java
@Test
public void testTrack() {
    String value4 = conf.get("parameter-tracked");
}
```

#### Run An Example
Example dependency can be found at [pom.xml](pom.xml).
Under the root directory of this project, run the following command:
```bash
$ mvn clean install -DskipTests
$ mvn surefire:test
```


#### Configuration Injection
To test different configuration parameter values, the runner supports injecting configuration parameters into test class.
The runner supports two ways of injecting configuration parameters into test class:
1. Use command line arguments to specify configuration parameter values;
```bash
$ mvn surefire:test -Dtest=${configTestName} -Dconfig.inject="parameter1=value1,parameter2=value2..."

# For example
$ mvn surefire:test -Dtest=FromFileTest -Dconfig.inject="file-param1=value1,file-param2=value2"
$ 
```

2. Use json file to specify configuration parameter values. User can specify the file directory with `config.file.dir` property, and the file name should be in the format of `${configTestClassName}.json`.
```bash
$ mvn surefire:test -Dtest=${configTestName} -Dconfig.file.dir=${configFileDir}

# For example
$ mvn surefire:test -Dtest=FromFileTest -Dconfig.file.dir=src/test/resources
```

3. Combine the two ways above. If the same configuration parameter is specified in both command line arguments and json file, the value in command line arguments will be used.
```bash
$ mvn surefire:test -Dtest=${configTestName} -Dconfig.inject="parameter1=value1,parameter2=value2..." -Dconfig.file.dir=${configFileDir}

# For example
$ mvn surefire:test -Dtest=FromFileTest -Dconfig.inject="file-param1=value1,file-param2=value2" -Dconfig.file.dir=src/test/resources
```

#### Run several tests with JUnit Suite
To run several tests with Configuration Test Runner, you can use JUnit Suite with `@RunWith(ConfigTestSuite.class)`.
```java
@RunWith(ConfigTestSuite.class)
@Suite.SuiteClasses({
        TestNormalOne.class,        
        TestNormalTwo.class,
})
public class AllTests {
}
```
