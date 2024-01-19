## Problem

Large software systems have many configuration items, and changes to some configuration items can cause system errors. Although the introduction of ctest makes it possible to detect configuration changes that cause failures, many projects like [Jetty](https://github.com/eclipse/jetty.project) are not yet compatible with the ctest tool and have not yet experienced such testing.



## Summary

### 1. Results

A PR has been submitted to openctest repo, which adds support for jetty-servlet, a submofule of Eclipse Jetty. 

This PR has modified the following submodules:

- `core/identify_param`: support identifying jetty-servlet parmas exercised in tests.
- `core/generate_ctests`: support generating parameter sets for Ctests of jetty-servlet.
- `core/generate_value`: support generating valid values for jetty-servlet conf params.
- `core/run_ctest`: support running generated ctests of jetty-servlet against configuration files.

### 2. Related questions:

1. What are the pain points in working on the configuration project?

- The specific values of the configuration in the project need to be obtained by referring to the documentation, which is sometimes not detailed enough.
- Many configuration items are linked and can affect each other. The failure of one configuration value may be due to the influence of other associated configuration values.
- Difficulty in determining the best best configuration value in a group of configuration value meshes



2. How much time does the project take in total and what is the most time-consuming part?

I spend an average of 6-8 hours per week on the project.

The most time consuming parts are.

- Finding the code about the configuration in each submodule and finding the appropriate submodule for configuration testing from there.
- Modifying the GET and SET API and testing that I found the right place.



3. What part of the original Ctest model works well and what works poorly?

Works well:

- Find and modify the GET/SET API
- Identify configuration parameters exercised by an existing test

- Generating parameter sets for ctests
- Running generated ctests

Works poorly:

- Generating valid values for configuration parameters



4. How good or bad is the Ctest prototype?

- >  How often/efficiently can Ctest pinpoint the BAD value of the parameters?

  In real-world projects, Ctest has difficulty finding bad values outside of bool types, and the bad values it does find are usually inferred through experience as a programmer. For example, at most time, converting an integer string outside the INT range to a number will cause the test to fail.

  

- > If you are the boss of one famous project (spark, nifi, etc...) Are you willing to spend some time supporting the configuration testing for the project? Why or Why not?

  In the current state, I don't tend to deploy Ctest in unit tests, because most of the bad values found at this point are due to type conversion errors, and many are values that would not be used in normal practice. But I might deploy Ctest in integration tests as part of CI/CD, so as to avoid configuration errors during the release process.

  

- > Is there any disadvantages of applying the configuration testing to more projects?

  Ctest relies on modifications to the GET/SET API for configuration items. For projects that do not have a common GET/SET API, such modifications are time consuming and easy to miss, and it is difficult to continue to maintain compatible support for Ctest in subsequent development.



5. How does Ctest fit into projects that do not have key-value format configurations? How to fit those interfaces into Ctest? How to track those configuration parameters?

In my project, there is a way to use IOC for configuration initialization. Jetty comes with this powerful XML parser. Jetty XML syntax is a direct mapping of XML elements to Java API, so one can instantiate POJO and call getters, setters and methods. It is very similar to inversion of control (IOC) or dependency injection (DI) frameworks like Spring or Plexus.

For example

```xml
<Configure id="conf" class="com.eclipse.jetty-servlet.Conf">
  <Set name="name">demo</Set>
</Configure>
```

Equivalent to

```java
com.eclipse.jetty-servlet.Conf conf = new com.eclipse.jetty-servlet.Conf();
conf.setName("demo");
```

Using POJO can do very complicated initialization. It is another way of writing code with configuration xml files. Modifying the configuration in this way is very reliant on the programmer's knowledge of the code implementation, so it is not suitable for integration with existing openctest tools. In the end I gave up this solution and wrote my own parser to align with the xml configuration format in openctest.



### Total Effort

I spend an average of 6-8 hours per week on my project. The total time spent on the project was about 70-80 hours.



### Longer Description

The complete log of using a good value and bad value to run ctest can be found here: [google drive](https://drive.google.com/file/d/1jytC6ACKvFs2qkdkuCsgL7ZbcCWg5kRN/view?usp=sharing)

The  PR to openctest is https://github.com/xlab-uiuc/openctest/pull/18

