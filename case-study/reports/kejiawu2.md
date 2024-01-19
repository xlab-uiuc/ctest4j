# **Done Report**
## **Problem**
The Problem (Project) I choose for *fall 2022 CS 527* course is to *Integrate Ctest into open source projects*:
1. Integrate Ctests into [`shenyu/shenyu-common`](https://github.com/apache/shenyu) , and open PR
2. Integrate Ctests into [`hadoop/hadoop-yarn/hadoop-yarn-timelineservice`](https://github.com/apache/hadoop) , and open PR
## **Summary**
* Abstraction:
    * I chose to work individually in this project. 
    * I chose [`shenyu/shenyu-common`](https://github.com/KobeNorris/shenyu) for the first project, which does not contain formal config file. I developed several `Python` scripts to instrumentalize config file, extract config params and test methods. 
    * I chose [`hadoop/hadoop-yarn/hadoop-yarn-timelineservice`](https://github.com/KobeNorris/hadoop) for the second project, which uses key-value format config and intialize using `xml` files. The previous `Python` extractors have been refactored and utilized.
* Results: 
    * Both projects have been completed: Been **intrumentalized**, **injectorized**, **integrated** into `IDoCT` and `openctest`, and **openned PRs** to `openctest` ( [`hadoop-yarn-tls-PR`](https://github.com/xlab-uiuc/openctest/pull/22) | [`shenyu-PR`](https://github.com/xlab-uiuc/openctest/pull/14) ).
## **Total Effort**
* Total Effort Amount: 【**23 + 107 = 130**】:
    * **Estimated**【**23**】Hours : Offline discussion with TA and prof, VM op consumption, and trivial time spent.
    * **Recorded**【**107**】Hours : See detailed records in [Work Log](https://gitlab.engr.illinois.edu/kejiawu2/cs527/-/blob/main/work_log.md).
## **Longer Description**
* Detail Work Log could be accessed [`HERE`](https://gitlab.engr.illinois.edu/kejiawu2/cs527/-/blob/main/work_log.md)

* High Level Accumulative Progress Log:

    * **17 / 09 / 2022 &nbsp; - &nbsp; 29 / 09 / 2022**:
        * Investigated Projects.
        * Picked [`shenyu/shenyu-common`](https://github.com/apache/shenyu) as first project.
        * Intrumentalized [`shenyu/shenyu-common`](https://github.com/KobeNorris/shenyu) by rewritting [`ShenyuConfig.java`](https://github.com/KobeNorris/shenyu/blob/master/shenyu-common/src/main/java/org/apache/shenyu/common/config/ShenyuConfig.java) using self-wrote `Python` script.
    * **29 / 09 / 2022 &nbsp; - &nbsp; 10 / 10 / 2022**:
        * Refactored [`IDoCT/generate-mapping`](https://github.com/KobeNorris/IDoCT/tree/main/generate_mapping) for [`shenyu/shenyu-common`](https://github.com/KobeNorris/shenyu).
    * **18 / 10 / 2022 &nbsp; - &nbsp; 22 / 10 / 2022**:
        * Refactored [`IDoCT/run_ctest`](https://github.com/KobeNorris/IDoCT/tree/main/run_ctest) for [`shenyu/shenyu-common`](https://github.com/KobeNorris/shenyu).
        * Developed [`CTestInjector.java`](https://github.com/KobeNorris/shenyu/blob/master/shenyu-common/src/main/java/org/apache/shenyu/common/config/CTestInjector.java) to enable Ctest injection for [`shenyu/shenyu-common`](https://github.com/KobeNorris/shenyu).
    * **31 / 10 / 2022 &nbsp; - &nbsp; 03 / 11 / 2022**:
        * Investigated Projects.
        * Picked [`hadoop/hadoop-yarn/hadoop-yarn-timelineservice`](https://github.com/apache/hadoop) as second project.
    * **03 / 11 / 2022 &nbsp; - &nbsp; 11 / 11 / 2022**:
        * Intrumentalized [`Configuration.java`](https://github.com/KobeNorris/hadoop/blob/trunk/hadoop-common-project/hadoop-common/src/main/java/org/apache/hadoop/conf/Configuration.java) of [`hadoop/hadoop-yarn/hadoop-yarn-timelineservice`](https://github.com/KobeNorris/hadoop) following Ctest prototype.
        * Refactored [`IDoCT/generate-mapping`](https://github.com/KobeNorris/IDoCT/tree/main/generate_mapping) for [`hadoop/hadoop-yarn/hadoop-yarn-timelineservice`](https://github.com/KobeNorris/hadoop).
    * **27 / 11 / 2022 &nbsp; - &nbsp; 28 / 11 / 2022**:
        * Refactored [`IDoCT/run_ctest`](https://github.com/KobeNorris/IDoCT/tree/main/run_ctest) for [`hadoop/hadoop-yarn/hadoop-yarn-timelineservice`](https://github.com/KobeNorris/hadoop).
        * Refactored [`YarnConfiguration.java`](https://github.com/KobeNorris/hadoop/blob/trunk/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-api/src/main/java/org/apache/hadoop/yarn/conf/YarnConfiguration.java) to enable Ctest injection for [`hadoop/hadoop-yarn/hadoop-yarn-timelineservice`](https://github.com/KobeNorris/hadoop).
    * **29 / 11 / 2022 &nbsp; - &nbsp; 01 / 12 / 2022**:
        * Developed and openned a Pull Request to [`openctest`](https://github.com/xlab-uiuc/openctest) for [`shenyu/shenyu-common`](https://github.com/xlab-uiuc/openctest/pull/14).
        * Developed and openned a Pull Request to [`openctest`](https://github.com/xlab-uiuc/openctest) for [`hadoop/hadoop-yarn/hadoop-yarn-timelineservice`](https://github.com/xlab-uiuc/openctest/pull/22).

## **Ctest Questions**
1. **What are the pain points in working on the configuration project?**

    * Find Proper Project: Spent several weeks, still hard to find a good one (like `Hadoop`) to integrate Ctest.
    * Instrumentalize Config File: Many projects do not provide unified `getter` and `setter`, some use auto-code generator.
    * Dev Injection Method: Since `shenyu-common` does not init config params with source file, I wrote a Java code to inject value during execution.

    ----

2. **How much time does the project take in total and what is the most time-consuming part?**

    * **130** hours of work in total ( **85** hours for `shenyu-common`, **45** hours for `hadoop-yarn-tls` ).
    * Developing proper injection method of `shenyu` is the most time-consuming part. Although I wrote a `Python` script to read and inject value, I still need to manually parse value from `String` to target type during injection. And for `Hadoop-yarn`

    ----

3. **What part of the original Ctest model (in the [paper](https://www.usenix.org/conference/osdi20/presentation/sun)) works well and what works poorly?**
    1. Config File Instrumentalization - find and modify the GET/SET API:
        * *Pros* : Efficient and clear for projects with good config structure like `hadoop`.
        * *Cons*: Painful and needs self-developed scripts for projects with irregular config structure like `shenyu`.
    2. Generate Mapping - identify parameters:
        * *Pros* : Easy and friendly with proper Instrumentalization of project config files. Might need few modifications to the codebase, but the experience is really smooth.
    3. Generate Value:
        * *Pros* : Nice and easy for projects with public default value documentation like `hadoop`, only need to develop script to extract test methods.
        * *Cons*: Painful for poorly documented codebase, and for some project, might need manual insertion and correction to get enough good values. Test methods could hardly be extracted using simple regex (some projects do not name all test suites as `test*`).
    4. Generate Ctest:
        * *Pros* : Efficiency has been further improved by extracting the meaningful test suites only. And it is easy to use with few modifications to the code.
    5. Run Ctest:
        * *Pros* : Good to work with bash scripts. I developed the demo smoothly using `run_single_ctest.py` and `run_ctest.py`
    ----

4. **How good or bad is the Ctest prototype?**
    * How often / efficiently can Ctest pinpoint the BAD value of the parameters?
        * Because Ctest mainly depends on test suites to detect failures, the test suites' quality affects Ctest performance significantly.
        * Therefore, good project choice (`hadoop`) could pinpoint meaningful parameter BAD values or (value sets) efficiently. 
        * But bad project choice (`shenyu`) could hardly find out meaningful values as their test suites are merely developed to pass test coverage.
    * If you are the boss of one famous project (spark, nifi, etc...) Are you willing to spend some time supporting the configuration testing for the project? Why or Why not?
        * I will consider applying Ctest as one of the supplementary test methods for release version of the codebase. 
        * Although Ctest is easy and smooth to deploy to project with good quality, it still takes programmers' manual efforts, and directly pollutes the codebase (i.e. insert loggers and injectors into source code).
        * So I will consider deploy Ctest and done extra config tests before releasing a new verison of the project.
    * Is there any disadvantages of applying the configuration testing to more projects?
        * **Generalizability** is one of my most concerned points of applying Ctest to more projects:
            1. As different project might be built on different frameworks (Maven, Ant, Gradle, and CMake ...) and different languages, `openctest` need to be partially refactored to enable self-defined building methods and programming languages.
            2. Also, the intrumentalization and injection method of different prohect might be different too, especially when developers are working on project of another language or another platform (e.g. someone want to deploy Ctest to IOS applications).
            3. The smoothness of deploying Ctest heavily depends on project quality, which is quite hard to resolve (as Ctest depends on project quality to detect BAD values). This feature might filter out many projects that might be able to utilize Ctest for config param value testing.
        * **Efficiency** is one of my most concerned points too:
            1. I wonder whether there will be circumstance that GOOD values for single param Ctest might pass, but their combination will fail the system. But the process of finding out those BAD value sets are time expensive.
            2. For each release version, the programmers are of high possibility to manually change some code in the codebase to support Ctest. It is quite unefficient as well, because Ctest could harldy work as a automatic testing plugin which could be easily integrated into CI/CD workflow.
    ----

5. **How does Ctest fit into projects that do not have key-value format configurations? How to fit those interfaces into Ctest? How to track those configuration parameters?**

    * Terrible experience working with `Shenyu` project, as it uses Java files to initialize config params which are Java Object's attributes.
    * I instrumentalized config files using a `Python` script, which detect all `getter` and `setter` methods, and insert the logging call. For injection, I wrote a Java class, which was called during each config object construction, then reads from a injection value file, and update corresponding params.
    * I simply track and verify those config params using loggers, which were injected into config files using `Python` script.

    ----
6. **How does Ctest fit into different build/testing system beyond Maven (Ant / Gradle)?**
    * Not applicable.
    ----
7. **How does Ctest fit into other programing language (not Java)?**
    * Not applicable.