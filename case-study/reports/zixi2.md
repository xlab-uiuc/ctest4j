1. Problem:

    Applying configuration testing for ``hadoop-yarn-common`` and ``hadoop-hdfs-rbf`` modules 

2. Summary:

    We were able to apply configuration testing for ``yarn-common`` and ``hdfs-rbf`` modules in Apache Hadoop. We followed the configuration test work flow, to first setup log output on get/set functions. Once parameters extracted successfully, we managed to perform injection through inputing additional ctest config file, together with log output in configuration get to verify the function of value injection. Once both extraction and injection are guaranteed to work, we integrated our changes into the openctest.

    Meanwhile, we devised a set of functions in order to help filter extracted parameters (submitted in separate PR: https://github.com/xlab-uiuc/openctest/pull/27)

    Result: we submitted PR for our openctest awaiting approval: [hadoop-hdfs-rbf](https://github.com/xlab-uiuc/openctest/pull/32), [hadoop-yarn-common](https://github.com/xlab-uiuc/openctest/pull/24)

3. What are the pain points in working on the configuration project?

    Throughout the whole project, we tried three modules. For the first project ``Pulsar``, the pain points were the incompatible version of java and maven in the project.
    
    For the second project ``felix-dev``, we were having the most trouble in identifying the GET/SET API because there were no evident Configuration.java files in the project. Though we extended the HashMap and Hashtable class with GET/SET API and substitute the extended child class into the tests, we still not get desirable parameters qualified for config test. After discussing with TA, we managed to intercept some parameters by overriding the Map and Dictionary classes.

    For the third project ``hadoop-yarn-common``/``hadoop-hdsf-rbf``, the main pain was the last step of generating the ctest as the amount of tests in the modules were large and the generate_ctest program took long time to finish, and sometimes will crash because of daily shutdown of VMs. When Oscar ran ``hadoop-hdfs-rbf``, he has encountered 3 occasions where the vm powers off by itself after 10-hour test run unfinished, and so he has to rerun it from the beginning of that 10 hours again. The injection for ``hadoop-distcp`` module ran successfully locally, but we were having trouble getting it through the openctest setup because it could not build for some reason. Due to the time limit at short notice, we had to remove the supporting for this module, but all the changes for distcp can be found at https://github.com/chrisshen98/openctest/tree/distcp.

4. How much time does the project take in total and what is the most time-consuming part?

    The project took around 180 hours. What took the most time was the generation of mapping file in the generate_ctest program. The most time-consuming part for actual programming was trying to override the HashMap/Hastable/Dictionary classes and try to substitute them into the tests when attempting the felix-dev repository.

5. What part of the original Ctest model (in the paper https://www.usenix.org/conference/osdi20/presentation/sun) works well and what works poorly?

    GET/SET API went well because hadoop has a well-setup foundation of configuration. By following the examples, the API was setup easily.

    Identify parameters went well as well because we set up some functions to simplify the process of generating the files required for the bash command. 

    generate value worked poorly because the generated value from the generate_value function contained a lot of SKIP value and SKIP tests. Generated values for some parameters with default value being true will get another value of SKIP instead of false value. 

    generate ctest went suprisingly well given that the generated value was strange. Although it took long time, it did produce great output in the end. Run ctest also went well: values could be successfully inserted when running tests, and bad values can lead to test failures.

6. How good or bad is the Ctest prototype?
    
    How often/efficiently can Ctest pinpoint the BAD value of the parameters?
    BAD value of the parameters can fail ALL tests using the parameter around 25% of the time. For around 50%, BAD value of the parameters can fail SOME tests using the parameter. For the rest 25% time, BAD value did not affect the test result.

    I would be willing to spend time supporting configuration testing. This is a very good practice of examining the prerequisite setup for the project. It can also potentially help identify security issues in the project. Moreover, it will be more convenient in the future if some default parameters are to be modified.

    From my perspective, applying configuration testings to more projects can only result in advantages and convenience for testers. The only thing that can be considered disadvantages is that when applying tests to modules that extends from other modules, we might need to take extra time differentiate where the injections come from and where they should go(in the child module or to the parent module as well).