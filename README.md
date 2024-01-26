# CTest4J - Configuration Testing Framework for Java

## What is CTest4J?

CTest4J is a practical configuration testing framework for Java.
The goal of CTest4J is to help developers to write, run and maintain configuration tests easily and efficiently.

## Download CTest4J
### Build From Source
```bash
$ git clone https://github.com/xlab-uiuc/ctest4j.git && cd ctest4j
$ mvn clean install
```

### Add CTest4J to Your Project
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
    testImplementation 'edu.illinois:ctest4j-junit${junit-version}:1.0-SNAPSHOT' // or ctest4j-testng
}
```
Current CTest4J support JUnit4 and 5, and TestNG.

## Quick Start
Please follow the [Guide to Get Started](Example.md) to run a simple example with CTest4J.

Read the [Features & Options](Options.md) to learn more about all the features that CTest4J supports.

Follow the [Guide to Write and Run CTest](write_and_run_ctest.md) to write and run your own configuration tests.

## Supported Framework:
- JUnit4
- JUnit5
- TestNG
