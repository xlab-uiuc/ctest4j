# Final Project

## 1. Problems
* We worked on accommodating CTEST prototypes on the Apache Kylin Repo.

## 2. Summary
  ### Result: 
  * We created two new branches based on 4.0.2 version of Kylin. 'test-logging' branch is for get/set APIs, while 'test-injection' is for the injection function. We also have our corresponding patch in our PR.
  * We submitted PR for openctest repo to accommodate configuration testing logic for four modules (common, storage, tool, cube) in Apache Kylin.
  * We also designed our own script(https://github.com/CarolSSS/Kylin_test_log_parser) for a specific version of a replacement for openctest that could run and test configuration parameters in all Kylin modules by running just one shell file. That script would also web scrapping the official website of Kylin and update the latest information and values of configuration parameters.

  ### Other efforts:
  * In the process of doing our projects, we found that kylin would crash when using sufire:test. After some research, we found that the cause of the problem was an undefined variable in the pom file. In order to make surefire work, we removed the variable and submmited the Pull Request.
  * We submit a PR to KYLIN about a bug we found in the surefire configuration.

## 3. Total Effort
* Both chens3 and rtao6 spend around 130 hours in total working for this project and 7hrs for report/pr(130 * 2 + 7= 267hrs).

## 4. Longer Description
  * Kylin is based on JDK 1.8 and requires maven to build. It has '.properties' files to input configuration parameters. We have worked on five important modules in the kylin repo to tackle the problems of configuration testing. 

  * Challenge:
   The injection function for four of the modules (common, storage, tool, cube) can fail tests with bad configuration values, but the query module is an exception.
   The most time-consuming part for us is to debug why after injecting bad-values, some tests in modules of Kylin still do not fail. We finally figure out that it is caused by two reasons: 1. Some modules (such as query) have functions to try and catch invalid configuration parameters. 2. There is only one module processing the configuration values, and all other modules are based on the jar of that module. Therefore, we need to rebuild that model every time before running our test in other modules.


## Other questions:
### 1. Pain Points
  * The structure of our project, Kylin, is different from many other Apache repos. One example would be: It only allows us to build and run tests in the root directory due to the `scalastyle_config.xml`. We spend some time accommodating the openctest code. Apart from these differences, we do not know if we need to modify the code from openctest repo before. Therefore, we wrote our code from scratch and only started to modify openctest in the very last two weeks. There are many compatible problems so we are quite struggling at that time. Thanks to TA who give us so much support and patience in the nick of time. We think TA could consider giving clearer instructions and pointing this mistake out in our progress report since at least 3+ groups are suffering from the same situation.

### 2. Original CTEST model Evaluation
  * We think `find and modify the GET/SET API` and `identify parameters` work well. The logic is very clear and are easy to use.
  * `generate ctest` and `run ctest` have many repeated parts so we think the code could be further modified and improve to be more automated.

### 3. How good or bad is the CTEST prototype?
  * Overall, we think the ctest prototype is very good.
  * After studying the code, we believe it covers everything a project needs for ctest.
  * However, we think it is very difficult for this prototype to find a bad value, for example, when a configure parameter has to be of type numeric, the interval of its good value is usually very large, so it is very difficult to find a bad value without multiple attempts. As a boss, I think I will spend some time supporting ctest. Configuration tests can help us find potential vulnerabilities, such as obvious bad values, but the tests still pass. It can also help us to find potential software security risks. For example, the single biggest vulnerability in log4j in the last decade can be solved by modifying the configuration file. The ctest code is difficult to generalize to all projects because the architecture of projects is so different. At the same time, each project's configuration parameters may have different types and more fine-grained data types that are not defined. So we think it is very difficult to apply ctest to more projects, but we think it is very good to develop one ctest for one project.

### 4. Our own script `KYLIN_PARSER`:
  * Our Logic behind `KYLIN_PARSER` & `OPENCTEST` are generally the same, since we have `read_properties`, `generate_ctests`, `mapping`, `generate_value`, `run_test scripts`. However, it is specially designed for Kylin so it is easier for KYLIN developers to test with only one click but not generalized to all other repos.
  * Since all modulus in Kylin only depends on the configuration file in core-common module, there is no needs for adding global variables for different modules in advanced. We collect all the values a configuration parameter has taken from running passed tests and use them as a criteria to generate. We also handle some outliers specifically for Kylin. It contains web-scraping feature that can catch update for default values in Kylin official website.
  * OPENCTEST has a much wider range of application situations and can handle more general outliers and different cases for different repos. Its value generation logic is also more comprehensive.