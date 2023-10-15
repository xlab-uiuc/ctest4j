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

Specify Runner Agent and Configuration APIs in Maven Surefire plugin:
```xml
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <argLine>-javaagent:/Users/allenwang/.m2/repository/edu/illinois/CTestRunner/1.0-SNAPSHOT/CTestRunner-1.0-SNAPSHOT.jar</argLine>
          <systemPropertyVariables>
            <configurationClassName>org/apache/hadoop/conf/Configuration</configurationClassName>
            <configurationGetterMethod>get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;,get(Ljava/lang/String;)Ljava/lang/String;</configurationGetterMethod>
            <configurationSetterMethod>set(Ljava/lang/String;Ljava/lang/String;)V</configurationSetterMethod>
          </systemPropertyVariables>
        </configuration>
      </plugin>
```

#### Write Configuration Test
Use `@RunWith(ConfigTestRunner.class)` to specify the runner for your test class.
For each configuration test method, use `@ConfigTest` to specify the configuration parameter name.
```java
@RunWith(ConfigTestRunner.class)
public class ExampleTest {
    @ConfigTest({"parameter1", "parameter2"})
    public void test() {
        // test code
        // parameter1 and parameter2 must be used in this test method
        // otherwise UnUsedConfigParamException will be thrown
    }
}
```

#### Run An Example
Under the root directory of this project, run the following command:
```bash
$ mvn clean install -DskipTests
$ mvn surefire:test -Dtest=ExampleTest
```