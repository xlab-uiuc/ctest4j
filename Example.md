# Example of Applying Ctest4J to Hadoop Common

## Build the Ctest4J

```bash
$ mvn clean install -DskipTests
```

## Clone the Hadoop repository

```bash
$ git clone git@github.com:apache/hadoop.git
```

## Add the runner dependency to Hadoop Common
Add the following Ctest4J dependency to the `pom.xml` file of Hadoop Common module.

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
For a project that uses JUnit5 or TestNG, add `ctest4j-junit5` or `ctest4j-testng` dependency instead.

## Instrument the Configuration APIs
Ctest4J provides an automatic instrumentation for configuration APIs to enable configuration testing.
Follow the [instrumentation guidance](Instrumentation.md) to automatically instrument the configuration APIs.

User can also manually instrument the configuration APIs by adding the following code to the configuration APIs:
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
To track the configuration parameters that are set by the test, add the `ConfigTracker.markParamAsSet(paramName)` to the setter methods.
```java
  public void set(String name, String value, String source) {
+   ConfigTracker.markParmaAsSet(name);
    Preconditions.checkArgument(
        name != null,
        "Property name must not be null");
    Preconditions.checkArgument(
        value != null,
        "The value of property %s must not be null", name);
    name = name.trim();
    DeprecationContext deprecations = deprecationContext.get();
    if (deprecations.getDeprecatedKeyMap().isEmpty()) {
      getProps();
    }
    getOverlay().setProperty(name, value);
    getProps().setProperty(name, value);
    String newSource = (source == null ? "programmatically" : source);

    if (!isDeprecated(name)) {
      putIntoUpdatingResource(name, new String[] {newSource});
      String[] altNames = getAlternativeNames(name);
      if(altNames != null) {
        for(String n: altNames) {
          if(!n.equals(name)) {
+           ConfigTracker.markParmaAsSet(n);
            getOverlay().setProperty(n, value);
            getProps().setProperty(n, value);
            putIntoUpdatingResource(n, new String[] {newSource});
          }
        }
      }
    }
    else {
      String[] names = handleDeprecation(deprecationContext.get(), name);
      String altSource = "because " + name + " is deprecated";
      for(String n : names) {
+       ConfigTracker.markParmaAsSet(n);
        getOverlay().setProperty(n, value);
        getProps().setProperty(n, value);
        putIntoUpdatingResource(n, new String[] {altSource});
      }
    }
  }
```

### Add Ctest4J configuration injector to the Configuration constructor
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

## Write and Run Configuration Tests
You can follow the [write_and_run_ctest.md](write_and_run_ctest.md) to write and run configuration tests with Ctest4J.