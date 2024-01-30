# Automatic Configuration API Instrumentation with AspectJ

## Introduction
Ctest4J provides automatic instrumentation for configuration APIs to enable configuration testing. 
It utilizes AspectJ Load-Time Weaving to inject instrumentation code into the configuration API methods.

## Arguments
To enable automatic instrumentation, specify the following arguments when running the Ctest4J:
| Arguments      | Purpose                                                                                                     | Supported Format                                                                                            |
|----------------|-------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| ctest.getter   | Specifies the method for getting the value of a configuration parameter.    | Signature1#(Optional)Pos1#(Optional)TransferMethod1;Signature2#(Optional)Pos2#(Optional)TransferMethod2;... |
| ctest.setter   | Specifies the method for setting the value of a configuration parameter.    | Signature1#(Optional)Pos1#(Optional)TransferMethod1;Signature2#(Optional)Pos2#(Optional)TransferMethod2;... |
| ctest.injector | Specifies the method for injecting the value of a configuration parameter. | Signature1#(Optional)Pos1#(Optional)TransferMethod1;Signature2#(Optional)Pos2#(Optional)TransferMethod2;... |
Note:
- Pos: Index of the input to this method that will be the parameter name or will be used for TransferMethod.
- TransferMethod: Method name needed to convert the input to the parameter name.

## Example of Applying AspectJ to HCommon
Here we show an example with Hadoop Common to enable configuration testing.

### Build the Ctest4J
First, compile the Ctest4J using Maven:
```bash
$ mvn clean install -DskipTests
```
### Clone the HCommon repository
```bash
$ git clone git@github.com:apache/hadoop.git
```

### Add dependencies to HCommon
#### a. Ctest4J Dependency
Add the Ctest4J dependency to the pom.xml file of the HCommon module:

```xml
<dependencies>
    ...
    <dependency>
        <groupId>edu.illinois</groupId>
        <artifactId>ctest4j-junit4</artifactId>
        <version>${ctest4j-version}</version>
    </dependency>
    ...
</dependencies>
```

#### b. AspectJ Dependency
Include the AspectJ dependency in the same pom.xml file:

```xml
<plugins>
    ...
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-surefire-plugin</artifactId>
        <configuration>
          <argLine>
            -javaagent:/{PATH_TO_M2_REPO}/.m2/repository/org/aspectj/aspectjweaver/1.9.7/aspectjweaver-1.9.7.jar
          </argLine>
        </configuration>
     </plugin>
    ...
</plugins>
```
This addition enables AspectJ's load-time weaving.
### Add Instrumentation Arguments via CLI 
```bash
mvn XXX -Dctest.getter="org.apache.hadoop.conf.Configuration.get(String)" -Dctest.setter="org.apache.hadoop.conf.Configuration.set(String,String)" -Dctest.injector="org.apache.hadoop.conf.Configuration()#set"
```
This command specifies the methods AspectJ will use to get, set, and inject configuration parameters.
