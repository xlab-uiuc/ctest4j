# CTest4j - Configuration Testing Framework for Java

## What is CTest4j?

CTest4j is a practical configuration testing framework for Java.
The goal of CTest4j is to help developers to write, run and maintain configuration tests easily and efficiently.

## Download CTest4j
### Build From Source
```bash
$ git clone https://github.com/xlab-uiuc/ctest4j.git && cd ctest4j
$ mvn clean install
```

### Add CTest4j to Your Project
Include the following dependency in your project build file:
#### Maven
```xml
<dependency>
    <groupId>edu.illinois</groupId>
    <artifactId>ctest4j-junit${junit-version}</artifactId> <!-- or ctest4j-testng --> 
    <version>1.0-SNAPSHOT</version>
</dependency>
```
#### Gradle
```groovy
dependencies {
    testImplementation 'edu.illinois:ctest4j-junit${junit-version}:1.0-SNAPSHOT'
}
```
Current CTest4j support JUnit4 and 5, and TestNG.

## Quick Start
Please follow the [Guide to Get Started](example_with_hcommon.md) to run a simple example with CTest4j.

Read the [Features & Options](Options.md) to learn more about all the features that CTest4j supports.

## Supported Framework:
- [JUnit4](JUnit4.md): [Write and run ctest with JUnit4](write_and_run_ctest.md)
- [JUnit5](JUnit5.md) 
- [TestNG](TestNG.md)
