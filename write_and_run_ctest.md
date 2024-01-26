# Write and Run Configuration Test with CTest Runner

## Write Configuration Test
The following example provides a glimpse at the minimum requirements for writing a configuration test in JUnit4 with CTest Runner.

#### DesignA:
```java
@RunWith(CTestJUnit4Runner.class)
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

#### DesignB:

```json
{
  "_comment": "This is the config.json file that is specified in the class annotation",
  "classLevelParams":["beforeClass", "before"],
  "methodLevelParams":
  {
    "testTestAnnotation":["testTestAnnotation"],
    "testTestClassAnnotationFail":["testTestClassAnnotationFail"],
    "testCTestAnnotation": ["testCTestAnnotation"],
    "testCTestMethodAnnotationFail": ["testCTestMethodAnnotationFail"],
    "testCTestMethodRegexFail": ["testCTestMethodRegexFail"]
  }
}
```

```java
@RunWith(CTestJUnit4Runner2.class)
@CTestClass(configMappingFile = "config.json",
        value = {"parameter1", "parameter2"}, regex = "regex-parameter(1|2)")
public class ExampleCTest {
    @BeforeClass
    public static void beforeClass() {
        Configuration conf = new Configuration();
        conf.get("beforeClass");
    }

    @Before
    public void before() {
        conf = new Configuration();
        conf.get("before");
    }

    /** ======================= All test method with @Test will be treated as @CTest ======================= */ 
    
    /**
     * The test would pass because it uses all the "required" configuration parameters from class annotation, file path
     */
    @Test
    public void testTestAnnotation() {
        conf.get("testTestAnnotation");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    /**
     * The test would fail because it never uses "parameter1".
     */
    @Test(expected = UnUsedConfigParamException.class)
    public void testTestClassAnnotationFail() {
        conf.get("testTestClassAnnotationFail");
        // Missing parameter1 so the test would fail
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    /** ======================= All test method with @CTest are also CTests ======================= */

    /**
     * The test would pass because it uses all the "required" configuration parameters from class annotation, file path, and method annotation.
     */
    @CTest
    public void testCTestAnnotation() {
        conf.get("testCTestAnnotation");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @CTest(value = {"method-parameter"}, expected = UnUsedConfigParamException.class)
    public void testCTestMethodAnnotationFail() {
        conf.get("testCTestMethodAnnotationFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        // "method-parameter" is never used so the test would fail
    }

    @CTest(value = {"method-parameter"}, regex = "method-regex(1|2)", expected = UnUsedConfigParamException.class)
    public void testCTestMethodRegexFail() {
        conf.get("testCTestMethodRegexFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        conf.get("method-parameter");
        conf.get("method-regex1");
        // "method-regex2" is never used so the test would fail
    }

}

```

### Annotations
#### DesignA:
| Annotations | Descriptions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
|---|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| @CTest | Denotes that a method is a configuration test method. It can take 5 arguments: value(), optional(), file(), expected(), and timeout(). `value()` specifies the "required" configuration parameters as a list that the test must use during its execution. `optional()` specifies the "optional" configuration parameters that can be used during the test execution. `file()` takes a file that contains the "required" and/or "optional" configuration parameters, it can be used alone or together with `value()` and `optional()`. `expected()` and `timeout()` are extends from `@Test` annotation, which defines the expected failure/exception during the test execution and the timeout of the test. |
| @CTestClass | Denotes that a class is a configuration test class. It can take 3 arguments: value(), optional(), and file(). These 3 arguments have the same feature as the ones in `@CTest` annotation but behave at the class level, for example, the `value()` specifies the configuration parameters that must be used by all configuration test methods in the class.                                                                                                                                                                                                                                                                                                                                                 |
| @Test | Denotes that a method is a normal test. If a test method is run with ctest runner and `@Test` annotation, the runner would track the configuration usage during the test execution and output the used configuration parameter to a JSON file named as the test method name.                                                                                                                                                                                                                                                                                                                                                                                                                                |

#### DesignB:
| Annotations | Descriptions                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
|---|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| @CTest | Denotes that a method is a configuration test method. It can take 5 arguments: value(), optional(), regex(), expected(), and timeout(). `value()` specifies the "required" configuration parameters as a list that the test must use during its execution. `optional()` specifies the "optional" configuration parameters that can be used during the test execution. `regex()` takes a regex string that represents the "required" configuration paraemters, it can be used alone or together with `value()` and `optional()`. `expected()` and `timeout()` are extends from `@Test` annotation, which defines the expected failure/exception during the test execution and the timeout of the test. |
| @CTestClass | Denotes that a class is a configuration test class. It can take 4 arguments: value(), optional(), regex(), and file(). These 4 arguments have the same feature as the ones in `@CTest` annotation but behave at the class level, for example, the `value()` specifies the configuration parameters that must be used by all configuration test methods in the class. `file()` takes a JSON file that contains the class-level and method-level "required" configuration parameters, one example is shown above in `config.json`.                                                                                                                                                                      |
| @Test | All @Test under @CTestClass would perform the same way as @CTest, expect it can't speicify the method-level arguments in the annotation but purely rely on the class-level values.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |

## Run Configuration Test
Running a configuration test is the same as running a normal test, except that the runner needs to be specified as `ConfigTestRunner.class`. The runner can take a few arguments to control the behavior of the test, which are listed below.

### Arguments
Please refer to [Options](Options.md) for more details about the arguments.

One exmaple of using Maven Surefire to run configuration test is:
```bash
$ mvn surefire:test -Dmode=default -Dconfig.inject.dir=src/test/resources/inject_config -Dconfig.used.dir=src/test/resources/used_config -Dsave.used.config=true
```

## Run Configuration Test with CTest Suite Runner
CTest Suite Runner is a runner that allows user to run a suite of configuration tests.
An example of using CTest Suite Runner is:
```java
@RunWith(CTestJUnit4Suite.class)
@Suite.SuiteClasses({
        TestNormalOne.class,
        TestNormalTwo.class,
})
public class AllTests {
}
```
The test in the suite does not need `@CTest` annotation but can still perform as a configuration test. The runner would automatically inject configuration value to the test and check whether all required configuration parameters are used after the test execution. The runner can take the same arguments as above to control the behavior of the test.
One specific argument for CTest Suite is `ctest.suite.tracking`. If this argument is set to true, then the runner would use `@Test` annotation to track the configuration usage during the test execution and save to `ctest.mapping.dir`; otherwise all `@Test` would also perform like `@CTest`.
To run a CTest suite, one can use the following command:
```bash
# track configuration usage and save used parmas to ctest.mapping.dir
$ mvn surefire:test -Dtest=AllTests -Dconfig.used.dir=src/test/resources -Dctest.suite.tracking
# run every test method as a configuration test
$ mvn surefire:test -Dtest=AllTests -Dconfig.used.dir=src/test/resources
```