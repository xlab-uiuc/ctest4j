# Write and Run Configuration Test with CTest Runner

## Write Configuration Test
The following example provides a glimpse at the minimum requirements for writing a configuration test in JUnit4 with CTest Runner.

```java
@RunWith(ConfigTestRunner.class)
@CTestClass(value = {"class-parameter1", "class-parameter2"}, file = "src/test/resources/config.json")
public class FromAllTest {
    /**
     * The test would pass because it uses all the "required" configuration parameters from class annotation, file path, and method annotation.
     */
    @CTest({"method-parameter1", "method-parameter2"})
    public void test() {
        Configuration conf = new Configuration();
        // From class annotation
        conf.get("class-parameter1");
        conf.get("class-parameter2");
        // From file path
        conf.get("file-param1");
        // From method annotation
        conf.get("method-parameter1");
        conf.get("method-parameter2");
    }

    /**
     * The test would fail because it never uses "method-parameter2".
     */
    @CTest(value = {"method-parameter1", "method-parameter2"}, expected = UnUsedConfigParamException.class)
    public void testFailDueToMethodAnnotation() {
        Configuration conf = new Configuration();
        // From class annotation
        conf.get("class-parameter1");
        conf.get("class-parameter2");
        // From file path
        conf.get("file-param1");
        // From method annotation
        conf.get("method-parameter1");
        // Missing method-parameter2 so the test would fail
    }
}
```

### Annotations
| Annotations | Descriptions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|---|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| @CTest | Denotes that a method is a configuration test method. It can take 5 arguments: value(), optional(), file(), expected(), and timeout(). `value()` specifies the "required" configuration parameters as a list that the test must use during its execution. `optional()` specifies the "optional" configuration parameters that can be used during the test execution. `file()` takes a file that contains the "required" and/or "optional" configuration parameters, it can be used alone or together with `value()` and `optional()`. `expected()` and `timeout()` are extends from `@Test` annotation, which defines the expected failure/exception during the test execution and the timeout of the test. |
| @CTestClass | Denotes that a class is a configuration test class. It can take 3 arguments: value(), optional(), and file(). These 3 arguments have the same feature as the ones in `@CTest` annotation but behave at the class level, for example, the `value()` specifies the configuration parameters that must be used by all configuration test methods in the class.                                                                                                                                                                                                                                                                                                                                                 |
| @Test | Denotes that a method is a normal test. If a test method is run with ctest runner and `@Test` annotation, the runner would track the configuration usage during the test execution and output the used configuration parameter to a JSON file named as the test method name.                                                                                                                                                                                                                                                                                                                                                                                                                                |

## Run Configuration Test
Running a configuration test is the same as running a normal test, except that the runner needs to be specified as `ConfigTestRunner.class`. The runner can take a few arguments to control the behavior of the test, which are listed below.

### Arguments
| Arguments            | Description                                                                                                                                                                                                                                                                                                    | Supported Value                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     |
|----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| mode                 | The mode of running configuration test                                                                                                                                                                                                                                                                         | (1) default: this is the default mode of ctest runner, where the runner first inject the configuration value to the test and checks whether all required configuration paraemters are used after the test execution. (2) checking: this mode only checks whether all required configuration parameters are used, but does not inject configuration value to the test and use default configuration value to run every test. (3) injecting: this mode only injects configuration value to the test but does not check required configuration usage. (4) base: this mode switches back to use a default non-ctest runner to run the test as a normal test, no injecting and checking. |
| config.inject.dir    | A directory that contains configuration files that under injection for each configuration test class. Each file should named as the same as the configuration test class. Currently JSON and XML files are supported.                                                                                          | Directory path                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| config.inject.cli    | An argument that allows user to set configuration key value pairs directly.                                                                                                                                                                                                                                    | Configuration key value pairs seperated by comma. For example "param1=value1,param2=value2,...,paramN=valueN"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| config.used.dir      | This is a directory that contains all files that contains "required"/"optional" configuration parameters for each test. Each file name should be the same as the configuration method name. The runner will automatically search for the file under this direcotry if speicified and checks usage accrodingly. | Directory path                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| save.used.config     | For tests with @Test, whether to save the tracked configuration parameter into a JSON file. This is useful when one does not know what configuraiton parameters are used in the test.                                                                                                                          | True / False                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| ctest.suite.tracking | Use only with CTestSuiteRunner. With this set to true, @Test would perform normally, track the usage of configuration parameters during the test execution, and save to `config.used.dir`; otherwise all @Test would also perform like @CTest.                                                                 | True / False                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |


One exmaple of using Maven Surefire to run configuration test is:
```bash
$ mvn surefire:test -Dmode=default -Dconfig.inject.dir=src/test/resources/inject_config -Dconfig.used.dir=src/test/resources/used_config -Dsave.used.config=true
```

## Run Configuration Test with CTest Suite Runner
CTest Suite Runner is a runner that allows user to run a suite of configuration tests.
An example of using CTest Suite Runner is:
```java
@RunWith(CTestSuite.class)
@Suite.SuiteClasses({
        TestNormalOne.class,
        TestNormalTwo.class,
})
public class AllTests {
}
```
The test in the suite does not need `@CTest` annotation but can still perform as a configuration test. The runner would automatically inject configuration value to the test and check whether all required configuration parameters are used after the test execution. The runner can take the same arguments as above to control the behavior of the test.
One specific argument for CTest Suite is `ctest.suite.tracking`. If this argument is set to true, then the runner would use `@Test` annotation to track the configuration usage during the test execution and save to `config.used.dir`; otherwise all `@Test` would also perform like `@CTest`.
To run a CTest suite, one can use the following command:
```bash
# track configuration usage and save used parmas to config.used.dir
$ mvn surefire:test -Dtest=AllTests -Dconfig.used.dir=src/test/resources -Dctest.suite.tracking
# run every test method as a configuration test
$ mvn surefire:test -Dtest=AllTests -Dconfig.used.dir=src/test/resources
```