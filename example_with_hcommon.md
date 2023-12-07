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
Since HCommon uses JUnit4, add the junit4 runner dependency to the `pom.xml` file of HCommon module.

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
You can follow the [write_and_run_ctest.md](write_and_run_ctest.md) to write and run configuration tests.