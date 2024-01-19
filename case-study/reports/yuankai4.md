# Problem: Apply Ctest to the Kafka core module

# Summary: 
I add logging to generate the mapping of the configurations. I update the set configuration code to allow injection. I create a PR for `openctest` to add Kafka support based on the current structure as well as some helpful changes for future `Gradle` based projects.

# Results:
I am able to do Ctest on the Kafka core module. The [PR](https://github.com/xlab-uiuc/openctest/pull/11) I created for `openctest` is still pending. I didn't find any configuration errors in the tests I ran.

# Total Effort: The total is about 85 hours.
Initial setup, getting familiar with the repo, and injecting the configuration: 10 hours.\
Adding GET and SET API logs: 20 hours.\
Using `IDoft` to generate the mapping: 5 hours. \
Making Ctest runnable for Kafka using `IDoft` and actually doing some Ctest: 20 hours. \
Update and fix my previous work based on the result of the testing: 15 hours.\
Create the PR for `openctest` and make all the changes needed to match the expected final output: 15 hours.

# Ctest questions:

1. What are the pain points in working on the configuration project?

a. Adding getter and setter API for the project took me lots of effort. Originally I thought Kafka is a Java project. However, most of its configuration-related code is written in Scala. I would need to get familiar with the Scala syntax first. The good thing is that Scala is similar to Java. However, one major difference is that Scala tends to use public variables and the configuration variable are written in such a way. Therefore, it doesn't have a get or set API, because the variable is retrieved directly. After getting help from Darko, I understand that I could rewrite the variable to functions and insert the logs in it. However, it is still very time-consuming because I need to make all the changes manually.

b. The module I picked has 291 configuration parameters and 2752 tests. Therefore, running the programs for the whole module takes a lot of time. For example, while each test takes about 10 seconds to run, generating mapping takes about `2752*10/60/60 = 7.64` hours to finish. It feels bad when I found something wrong and regenerate the mapping. Our VM has a nightly shutdown, so sometimes, I hope to get a result by running it overnight, but it was killed when the VM is shut down.

c. I was pretty confused about how to see the print messages. Because Kafka uses `log4j`, I need to change the log print level and add `-i` in the command to print `info` level messages. I think this is worth mentioning for future Ctest projects that we need to find out how to show print messages in the first few steps. Talking about some common log libraries will also be helpful.

2. How much time does the project take in total and what is the most time-consuming part?

It takes about 85 hours in total. For the concept of being time-consuming, there are two cases. The most time-consuming part for me is adding getters and setters because there is much repeated manual work. The most time-consuming part for the VM is actually running the Ctest. Just like generating mapping, running all the tests after making a change on a config value also takes a long time.

3. What part of the original Ctest model (in the paper https://www.usenix.org/conference/osdi20/presentation/sun) works well and what works poorly?

The Ctest model about generating many tests from the current tests in the project works well. After I did the ctest logging and injection, I can easily test with different combinations of config values and use the original tests to check if the system will still work under that setup. While running the Ctests with some bad values, I saw some error messages about unreasonable config values. These are written by the original developer for the Kafka project and I am glad that our Ctest hit those cases.

The part that doesn't work so well is the assumption about developers could need little effort to add the ctest logging. This would really depend on how the project is written. Kafka is a bit different because that part is not written in Java. However, I could imagine that not all projects have a centralized get and set API and we might need to take some effort to figure out where are places we need to inject the log.

4. How good or bad is the Ctest prototype?

`IDoft` and `openctest` repo have similar codes and they both provide a good prototype. One thing that is needed for Ctest is that we would need to go to different folders during different steps of Ctest in order to run the commands. The repo builds a good way to jump among the folders. The structure of how to separate the tests and loop over them to get the result is also helpful.

One thing that can be improved is that currently, the readme focus on how to use it, and the developer guide is a high-level introduction. I guess it will work fine directly if it is a Maven project, but when I want to use Gradle and I need to understand the code in detail, it will be helpful to add something like a design doc about the structure of the classes and their responsibilities.

6. How does Ctest fit into different build/testing system beyond Maven (Ant / Gradle)?

Kafka is a Gradle project. In general, Ctest works well on Gradle, but there are areas that we need to dig into to improve the `openctest` repo. I feel that there are some things in common between Gradle and Maven like we can run tests by a command and it will print out the results. Therefore, I can utilize the main ideas from the current `openctest` repo. I also need to remove or skip the parts that are only for Maven, like surefire. Maven gets the time information from surefire, but I didn't find a good way to get the time from Gradle. Also, the test procedure is a bit different. Maven needs to go into the folder of the module, while Gradle is more likely to do testing from the root.

If we want to make the `openctest` repo to support Gradle, we would need to do some refactors to separate the differences and the common parts. Maybe make the build tool to be an enum and include different `program_input` variables under them. We also need to do some deep-dive to fully understand the Gradle report output to see if we can learn more information from there.

7. How does Ctest fit into other programing language (not Java)?

For Kafka, the injection part is Java code and the logging part is Scala code. So my project is somehow related to this question. Scala is similar to Java, so I feel there is not much difference in the understanding or the object-oriented design. However, as I mentioned previously about injecting get and set APIs. The way of writing config variables differs based on language features. It is reasonable to assume that well-maintained public Java projects won't just use public variables, but it is pretty common for other languages like Scala or Kotlin. Maybe we can write some script and use tools like `Java Reflection` for those languages to do the conversion.