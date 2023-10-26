# Example of Applying CTest Runner to HCommon

## Build the CTest Runner

```bash
$ mvn clean install -DskipTests
```

## Clone the HCommon repository

```bash
$ git clone git@github.com:apache/hadoop.git
```

## Add the runner dependency to HCommon
Add the dependency to the `pom.xml` file of HCommon module.

```xml
<dependencies>
    ...
    <dependency>
        <groupId>edu.illinois</groupId>
        <artifactId>CTestRunner</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
    ...
</dependencies>
```

## Instrument the Configuration class

### Add ctest-runner configuration tracker to the Configuration getter methods
To track the usage of configuration parameters, add the `ConfigTracker.markParamAsUsed(paramName)` to the getter methods.
```java
   public String get(String name, String defaultValue) {
+    ConfigTracker.markParamAsUsed(name);     
     String[] names = handleDeprecation(deprecationContext.get(), name);
     String result = null;
     for(String n : names) {
+      ConfigTracker.markParamAsUsed(n);
       result = substituteVars(getProps().getProperty(n, defaultValue));
     }
     return result;
   }

   public String get(String name) {
+    ConfigTracker.markParamAsUsed(name);
     String[] names = handleDeprecation(deprecationContext.get(), name);
     String result = null;
     for(String n : names) {
+      ConfigTracker.markParamAsUsed(n);
       result = substituteVars(getProps().getProperty(n));
     }
     return result;
   }
```

### Add ctest-runner configuration injector to the Configuration constructor
Add the `ConfigTracker.injectConfig(setterMethod)` to the end of the Configuration constructor.
The setterMethod takes two arguments, the first argument is the configuration parameter name, the second argument is the configuration parameter value.
This is used to inject the configuration value with the configuration setter method during the test execution.
```java
  /** A new configuration. */
   public Configuration() {
     this(true);
+    ConfigTracker.injectConfig((arg1, arg2) -> set(arg1, (String) arg2));
   }
```

### Write and Run Configuration Tests
You can follow the [example_ctest.md](example_ctest.md) to write and run configuration tests.