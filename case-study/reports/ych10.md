# Final Report
**Team: Yifan Jiang, Yung Chieh Huang**

## Problem
We added Apache Flink and Apache Hudi into the openctest framework.

## Summary
Since Ctest does not support Flink and Hudi, we tried to deploy configuration testing (Ctest), a test that could detect failures caused by configuration changes, to Flink (flink-core) and Hudi (hudi-common). The versions we focused on are Flink release-1.16 and Hudi 0.12.1. In the end, we successfully opened a [PR for Flink](https://github.com/xlab-uiuc/openctest/pull/6) and a [PR for Hudi](https://github.com/xlab-uiuc/openctest/pull/26) to the openctest framework. We also help improved a tiny problem for the openctest repo and got [that PR](https://github.com/xlab-uiuc/openctest/pull/7) accepted.  

## What are the pain points in working on the configuration project?
One of the pain points is to double-check whether our injection/get-set APIs work or not. Those were really hard times when we were not familiar with the process, so we needed to read the maven reports one by one after each trial of modification. Sometimes even though we thought those get-set APIs did their job, we still needed to modify them to fit the openctest framework, like Log formats and spacings. 

## How much time does the project take in total and what is the most time-consuming part?
The time we spent depended on our work for that week. If that week required lots of code reading, like the times when we were doing injection, those weeks typically took about a whole day of work. Otherwise, it was usually testing and fixing implementation problems, which took about 6 to 7 hours per week. Many times we had to sit by the computer screen to wait for those maven test results and prepare for possible errors. Other than that, the most time-consuming part must be the times when we were reading the original codes of those open-source projects to figure out a general structure of how their configurations work.

## What part of the original Ctest model (in the paper https://www.usenix.org/conference/osdi20/presentation/sun) works well and what works poorly?
Overall, we think openctest is a great tool for conducting configuration testing. The Ctest model not only made configuration testing simplier but also standartized the procedure. The different steps in openctest repo (`identify_param`, `generate_value`, etc) are fairly easy to understand. Initially, we were a bit unsure about the reasons behind obtaining those GET and SET logs. But after the responses through Campuswire, we got to better understand of how `identify_param` works and was able to finish the task as described in the procedure. 

With that being said, we do find `generate_ctest` a little weird. According to the paper, the purpose of `generate_ctest` is to detect hard-coded tests with implicit assumptions. It is done by running the tests with valid values. A test failing on different but valid values indicates that it assumes specific values. However, the failure could also be an indicator of bugs. Programmers would have to manually check the tests to see whether the failures are caused by bugs in the tests or some implicit assumptions. Unfortunately, we couldn't come up with a better solution. Our only suggestion is the Ctest model inform programmers that such failures can potentially be configuration bugs.

## How good or bad is the Ctest prototype?
In general, the Ctest prototype is great. One issue we encountered was that during the `generate_ctest` phase, some of the non-trivial inputs were skipped because Ctest could not assign values to those non-trivial configuration values. This means some manual effort is still required. There might be a solution to make it into a fully automated process if some high-level parsing technique is being incorporated during this `generate_ctest` step so that the program could find some valid values through parsing the configuration documentation. 

Conducting configuration testing is beneficial. Configuration files are easy to modify and easy to use, but they are rarely tested. As a result, projects can run into fatal errors when programmers abuse configuration parameters. If I were in charge of a big project, I would definitely apply the Ctest prototype to my project.
