# Ctest4J - Configuration Testing Framework for Java

## What is Ctest4J?

Ctest4J is a practical configuration testing framework for Java.
The goal of Ctest4J is to help developers write, run and maintain configuration tests easily and efficiently.

## Download Ctest4J
### Build From Source
```bash
$ git clone https://github.com/xlab-uiuc/ctest4j.git && cd ctest4j
$ mvn clean install
```

### Add Ctest4J to Your Project
Include the following dependency in your project build file:
#### Maven
```xml
<dependency>
    <groupId>edu.illinois</groupId>
    <artifactId>ctest4j-junit${junit-version}</artifactId> <!-- or ctest4j-testng --> 
    <version>${ctest4j-version}</version>
</dependency>
```
#### Gradle
Current Ctest4J supports JUnit4 and 5, and TestNG.

## Quick Start
Please follow the [Guide to Support Ctest4J](Example.md) and [Guide to Run Ctest with Ctest4J](run_hcommon_example_ctest.md) to run a simple Ctest example in Hadoop Common with Ctest4J.

Read the [Features & Options](Options.md) to learn more about all the features that Ctest4J supports.

Follow the [Guide to Write and Run CTest](write_and_run_ctest.md) to write and run your own configuration tests.

## Demo Video
[Click to view the demo video](https://github.com/xlab-uiuc/ctest4j/blob/main/demo-video.md)


## Supported Framework:
- JUnit4
- JUnit5
- TestNG
