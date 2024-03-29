# Example of Applying Ctest4J to Hadoop Common

## Download and Build Ctest4J

```bash
$ git clone git@github.com:xlab-uiuc/ctest4j.git
$ cd ctest4j && mvn clean install -DskipTests && cd ..
```

## Clone the Hadoop repository

```bash
$ git clone git@github.com:apache/hadoop.git
$ git checkout rel/release-3.3.6 && git checkout -b ctest4j-example
```

## Add the runner dependency to Hadoop Common
We will work on the `hadoo-common-project/hadoop-common` module.\
First go to the `hadoop-common` module and add the following Ctest4J dependency to the `pom.xml` file.
```bash
$ cd hadoop-common-project/hadoop-common
$ emacs pom.xml  # or replace emacs with your favorite editor
```

Add the following Ctest4J dependency to the `pom.xml` file of Hadoop Common module.

```xml
<dependencies>
    ...
    <dependency>
        <groupId>edu.illinois</groupId>
        <artifactId>ctest4j-junit4</artifactId>
        <version>1.0.0</version>
    </dependency>
    ...
</dependencies>
```
For a project that uses JUnit5 or TestNG, add `ctest4j-junit5` or `ctest4j-testng` dependency instead.

## Instrument the Configuration APIs
Ctest4J provides an automatic instrumentation for configuration APIs to enable configuration testing.\
Follow the [instrumentation guidance](Instrumentation.md) to automatically instrument the configuration APIs. (Not recommend for now)\

User can manually instrument the configuration APIs by adding the following code to the configuration APIs:
### Add ctest-runner configuration tracker to the Configuration getter methods
The Configuration class of `Hadoop Common` is located at `hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/conf/Configuration.java`.\
Make sure you are under `hadoop/hadoop-common-project/hadoop-common` directory, and open the `Configuration.java` file.
```bash
$ emacs $(find . -name "Configuration.java")  # or replace emacs with your favorite editor
```
To track the usage of configuration parameters, add the `ConfigTracker.markParamAsUsed(paramName)` to the getter methods of the Configuration class.
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

### Add Ctest4J configuration value connector to the Configuration constructor
Add the `ConfigTracker.injectConfig(setterMethod)` to the end of the Configuration constructor.
The setterMethod takes two arguments, the first argument is the configuration parameter name, the second argument is the configuration parameter value.
This is used to inject the configuration value with the configuration setter method during the test execution.
```java
  /** A new configuration. */
   public Configuration() {
     this(true);
+    ConfigTracker.injectConfig((paramName, paramValue) -> set(paramName, (String) paramValue));
   }
```

### Build the Hadoop Common module
After adding the Ctest4J dependency and instrumenting the configuration APIs, build the Hadoop Common module.
```bash
$ mvn clean install -DskipTests
```

## Run an Example Ctest in Hadoop Common
Now your Hadoop Common module is ready for configuration testing.\
You can follow the [document here](run_hcommon_example_ctest.md) to run a simple Ctest example in Hadoop Common with Ctest4J.