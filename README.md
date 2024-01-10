# Configuration Test (CTest) Runner

## What is CTest Runner?
CTest Runner is a customized JUnit test runner that allows you to write and run configuration test (ctest) easily.

The runner provides a set of annotations that allow you to specify a ctest and the expected results, which is similar to parameterized tests.
CTest Runner executes ctest in parallel with the user-defined configuration and verifies the correctness of the given configuration.
CTest Runner also provides ctest selection feature that only runs the ctest that uses the given configuration under test to speed up the test execution.

## Download CTest Runner
### Build from source
```bash
$ git clone https://github.com/xlab-uiuc/ctest-runner.git && cd ctest-runner
$ mvn clean install
```

### Maven Central
Include the following dependency in your `pom.xml`:
For JUnit4:
```xml
<dependency>
    <groupId>edu.illinois</groupId>
    <artifactId>ctest-runner-junit4</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
For JUnit5:
```xml
<dependency>
    <groupId>edu.illinois</groupId>
    <artifactId>ctest-runner-junit5</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Quick Start
Please follow the [Guide to Get Started](example_with_hcommon.md) to run a simple example with CTest Runner.

Read the [Features & Options](Options.md) to learn more about all the features that CTest Runner supports.

## Supported Framework:
- [JUnit4](JUnit4.md): [Write and run ctest with JUnit4](write_and_run_ctest.md)
- [JUnit5](JUnit5.md) 
