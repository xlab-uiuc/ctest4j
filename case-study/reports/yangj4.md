# Questions specified in assignment description

1. Problem
    
    Let `dropwizard-health` module support CTest and open a PR to the openctest repository.

2. Summary

    a. Result: 
    
    1. Instrumented `get` and `set` methods for `dropwizard-health`
    2. Implemented code for configuration value injection
    3. Updated openctest to let it support `dropwizard-health`. Created ctest mapping for 11 parameters (the module has 17 parameters in total, I also tested the rest 6
    parameters but they don't have any ctest mapping. That is, the test is hardcoded to 
    a specific value, or not being read/write during any test)
    4. Opened a [RP](https://github.com/xlab-uiuc/openctest/pull/8) to openctest

    b. Other Effort: 

    1. Instrumented `get` and `set` methods for `dropwizard-client` module ([code](https://github.com/dropwizard/dropwizard/commit/878e6d39c563162e52d827dcd64bf7dfcc4f7ff5))

3. Total Effort

    I spend about 7 hrs/week on the project. We have about 10 weeks to work on the project, that brings total time spend on the project to around 70 hours.

# Questions required by TA

1. What are the pain points in working on the configuration project?

   Working on the project on the provided VM is somewhat painful, since the VM is slow. Working locally
   on a docker container is much easier and faster.

2. How much time does the project take in total and what is the most time-consuming part?

    Around 70 hrs. Figuring out how to do value injection and writing the code to do it are the most time-consuming parts.

3. What part of the original Ctest model (in the paper https://www.usenix.org/conference/osdi20/presentation/sun) works well and what works poorly?

    All of them work well for my project.

4. How good or bad is the Ctest prototype?

    - How often/efficiently can Ctest pinpoint the BAD value of the parameters?

        Moderately efficient. Some tests only read configurations and does not use it during the test,
        which means they won't fail even a bad value is provided.

    - If you are the boss of one famous project (spark, nifi, etc...) Are you willing to spend some time supporting the configuration testing for the project? Why or Why not?

        I will consider it as a supplement for a decent configuration validator. Ctest cannot guarantee it can capture all bad values and hence a validator is still necessary. However, Ctest is capable of finding good configuration values that might fail the test/system, which is a feature validators cannot provide.

    - Is there any disadvantages of applying the configuration testing to more projects?

        Not much, aside from it might require additional maintenance. 

5. How does Ctest fit into projects that do not have key-value format configurations? How to fit those interfaces into Ctest? How to track those configuration parameters?

    Does not applicable to my project

6. How does Ctest fit into different build/testing system beyond Maven (Ant / Gradle)?

    Does not applicable to my project

7. How does Ctest fit into other programing language (not Java)?

    Does not applicable to my project
