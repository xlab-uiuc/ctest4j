# Integrating AspectJ

## Introduction
AspectJ is an extension of Java that introduces 'aspects', making it easier to manage certain parts of your code. It enhances Java by providing a way to efficiently handle cross-cutting concerns. In our project, we use AspectJ to automatically instrument the Configuration APIs without modifying the source code.

AspectJ offers three primary modes to instrument classes:
- Compile-Time Weaving: Integrating aspects during the compilation process.
- Post-Compile Weaving: Adding aspects to compiled classes.
- **Load-Time Weaving**: Injecting aspects when classes are loaded into the JVM.

For our purposes, we've selected Load-Time Weaving. This approach allows us to retain the original class files without modifications.

## Arguments
| Arguments      | Purpose                                                                                                     | Supported Format                                                                                            |
|----------------|-------------------------------------------------------------------------------------------------------------|-------------------------------------------------------------------------------------------------------------|
| ctest.getter   | Specifies the method for getting the value of a configuration parameter.    | Signature1#(Optional)Pos1#(Optional)TransferMethod1;Signature2#(Optional)Pos2#(Optional)TransferMethod2;... |
| ctest.setter   | Specifies the method for setting the value of a configuration parameter.    | Signature1#(Optional)Pos1#(Optional)TransferMethod1;Signature2#(Optional)Pos2#(Optional)TransferMethod2;... |
| ctest.injecter | Specifies the method for injecting the value of a configuration parameter. | Signature1#(Optional)Pos1#(Optional)TransferMethod1;Signature2#(Optional)Pos2#(Optional)TransferMethod2;... |
Note:
- Pos: Index of the input to this method that will be the parameter name or will be used for TransferMethod.
- TransferMethod: Method name needed to convert the input to the parameter name.


## Example of Applying AspectJ to HCommon

### Build the CTest Runner
FFirst, compile the CTest Runner using Maven:
```bash
$ mvn clean install -DskipTests
```
This command compiles the CTest Runner while skipping the execution of tests.
### Clone the HCommon repository

```bash
$ git clone git@github.com:apache/hadoop.git
```

### Add dependencies to HCommon
#### a. JUnit4 Runner Dependency
Add the JUnit4 runner dependency to the pom.xml file of the HCommon module:

```xml
<dependencies>
    ...
    <dependency>
        <groupId>edu.illinois</groupId>
        <artifactId>ctest-runner-junit4</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>compile</scope>
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
### Add Instrumentation via CLI
```bash
mvn XXX -Dctest.getter="org.apache.hadoop.conf.Configuration.get(String)" -Dctest.setter="org.apache.hadoop.conf.Configuration.set(String,String)" -Dctest.injecter="org.apache.hadoop.conf.Configuration()#set"
```
This command specifies the methods AspectJ will use to get, set, and inject configuration parameters.
