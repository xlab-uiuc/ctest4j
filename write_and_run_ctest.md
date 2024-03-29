# Write and Run Configuration Test with Ctest4J

## Write Configuration Test
The following example provides a glimpse at the minimum requirements for writing a configuration test with Ctest4J.

```java
import java.util.Map;

@RunWith(CTestJUnitRunner.class) // For JUnit4
// @ExtendWith(CTestJUnit5Extension.class) // For JUnit5
// @Listeners(CTestListener.class) // For TestNG
@CTestClass(value = {"server.ip.address", "server.port"}) // "value" field is optional for @CTestClass
public class ExampleCTest {
    Configuration conf;
    Server server;

    @Before
    public void before() {
        conf = new Configuration();
        String ipAddress = conf.get("server.ip.address");
        int port = conf.getInt("server.port", 8080);
        server = new Server(ipAddress, port);
    }

    @CTest({"server.max.connections"}) // "value" field is also optional for @CTest
    public void testServerMaxConnections() {
        int maxConnections = conf.getInt("server.max.connections", 100);
        server.setMaxConnections(maxConnections);
        server.start();
        try {
            // Connect more than 100 connections and expect an exception
            for (int i = 0; i < 101; i++) {
                ServerConnection connection = server.connect();
            }
            fail("Should exceed the max connections and throw an exception");
        } catch (MaxConnectionsException e) {
            // Expected
            Assert.assertEquals(100, server.getMaxConnections());
        }
    }
}

```

### Annotations
Ctest4J provides 3 annotations to help developers to write configuration tests, which are listed below.

| Annotations | Descriptions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|-------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| @CTest      | Denotes that a method is a configuration test method. It can take 5 arguments: value(), optional(), regex(), expected(), and timeout(). `value()` specifies the "required" configuration parameters as a list that the test must use during its execution. `optional()` specifies the "optional" configuration parameters that can be used during the test execution. `regex()` takes a regex string that represents the "required" configuration paraemters, it can be used alone or together with `value()` and `optional()`. `expected()` and `timeout()` are extends from `@Test` annotation, which defines the expected failure/exception during the test execution and the timeout of the test. |
| @CTestClass | Denotes that a class is a configuration test class. It can take 4 arguments: value(), optional(), regex(), and file(). These 4 arguments have the same feature as the ones in `@CTest` annotation but behave at the class level, for example, the `value()` specifies the configuration parameters that must be used by all configuration test methods in the class. `file()` takes a JSON file that contains the class-level and method-level "required" configuration parameters, one example is shown above in `config.json`.                                                                                                                                                                      |
| @Test       | All @Test under @CTestClass would perform the same way as @CTest, expect it can't specify the method-level arguments in the annotation but purely rely on the class-level values.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |

## Run Configuration Test
Running a configuration test is the same as running a normal test.
For example with Maven Surefire, you can run a configuration test with the following command:
```bash
$ mvn surefire:test -Dtest=ExampleCTest
```

The example command for running the above Ctest is:
```bash
$ mvn surefire:test -Dtest=ExampleCTest -Dconfig.inject.cli="server.ip.address=127.0.0.1,server.port=8080,server.max.connections=50"
```
This command will connect the configuration under test through the Ctest4J `config.inject.cli` argument.

Ctest4J can also take other arguments to control the behavior of the test, please refer to [Options](Options.md) for more details.
