## JUnit4 Configuration Test Runner

### Usage

#### Add Runner as dependency 
For maven project, add the following dependency to your pom.xml:
```xml
<dependencies>
    <dependency>
        <groupId>edu.illinois</groupId>
        <artifactId>CTestRunner</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
</dependencies>
```

##### Automatic Instrumentation Only：
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

#### Write Configuration Test
Use `@RunWith(ConfigTestRunner.class)` to specify the runner for your test class.
`@ConfigTestClass` specifies the configuration parameters that must be used in the all test methods in the test class.
For each configuration test method, use `@ConfigTest` to specify the configuration parameter that must be used in the test method.

There are two ways of specifying configuration parameters:
1. Directly write configuration parameter name in `@ConfigTestClass` and `@ConfigTest` annotation as value().
2. Put configuration parameter name in a file and specify the file path in `@ConfigTestClass` and `@ConfigTest` annotation as file().

Current runner support JSON file format for configuration parameter file, 
one can implement [ConfigurationParser](src/main/java/edu/illinois/parser/ConfigurationParser.java) to support 
other file format and override `getParser()` method in [ConfigTestRunner#getParser](src/main/java/edu/illinois/ConfigTestRunner.java).

```java
@RunWith(ConfigTestRunner.class)
@ConfigTestClass(value = {"parameter1"}, file = "config.json")
public class FromMethodTest {
    @ConfigTest({"parameter2"})
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