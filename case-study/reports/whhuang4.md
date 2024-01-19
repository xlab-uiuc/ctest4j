# done
## 1. Problem
<!-- Problem: <1-2 sentence brief summary describing your project> -->
To contribute to the open-source ctest project (openctest) and promote community growth, this project aims to add support for Apache Camel in openctest.  

## 2. Summary
<!-- Summary: <high-level summary, in the style of paper abstracts (i.e., a "breadth-first" overview, without going into "depth-first" details, e.g., one link okay but no multiple links)> -->
The aim of this project is to add support for Apache Camel in the openctest. The project involves instrumenting GET and SET APIs, generating a mapping file, and injecting config values. Additionally, the project aims to find any configuration bugs in this target project by injecting configs in tests. The outcome of this project is the successful integration of Apache Camel in openctest.

### Result
<!-- Results: <high-level summary of your successful results, e.g,. accepted PRs> -->
This project successfully added support for Apache Camel in openctest, as demonstrated by the accepted [PR](https://github.com/xlab-uiuc/openctest/pull/10).

## 3. Total Effort
<!-- Total Effort: <estimate your effort in terms of the number of hours you spent on the project for the entire semester> -->
about 120-144 hours.
## 4. Longer Description
<!-- [Optional] Longer Description: <longer description of the entire project, in the style of progress reports but describing cumulative progress since the start of the project>
Include links to the code/tests and other artifacts you wrote
Emphasize work done since the previous progress report
Note: we evaluate depth/amount of your work not length of your report (longer reports may hurt rather than help) -->
1. What are the pain points in working on the configuration project?  
    The pain points in working on a configuration project include:
    * Difficulty in validating the correctness of Getter and Setter of Configuration.  
    * Challenges in generating the mapping file, specifically generating surefire report and collecting tests and parameters.  
    * Difficulty in collecting and interpreting parameters in the target project.  

2. How much time does the project take in total and what is the most time-consuming part?   
    about 120-144 hours. The most time-consuming part is generating mapping (progress2 & progress3).

3. What part of the original Ctest model (in the paper https://www.usenix.org/conference/osdi20/presentation/sun) works well and what works poorly?  
    I think all of the steps involved in the model work well.    
    However, the preparation of Ctest requires a significant amount of manual work, which is a flaw in the model.  

4. How good or bad is the Ctest prototype?   
    * How often/efficiently can Ctest pinpoint the BAD value of the parameters?  
    In my project, it is difficult to find BAD value of the parameters, there are only few BAD values.  

    * If you are the boss of one famous project (spark, nifi, etc...) Are you willing to spend some time supporting the configuration testing for the project? Why or Why not?  
    Yes, it is a powerful tool to findout potential configuration bugs.

    * Is there any disadvantages of applying the configuration testing to more projects?  
        1. Supporting configuration testing requires a significant amount of manual work.  
        2. The effectiveness of applying configuration testing highly depends on the target project and its tests.

5. *How does Ctest fit into projects that do not have key-value format configurations? How to fit those interfaces into Ctest? How to track those configuration parameters?  
    My project have key-value format configurations.
6. *How does Ctest fit into different build/testing system beyond Maven (Ant / Gradle)?  
    My project is Maven build/testing system.
7. *How does Ctest fit into other programing language (not Java)?  
    My project uses Java.
