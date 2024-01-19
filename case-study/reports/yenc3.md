# Final Report

## **Problem**

We work on configuration testing on Druid, focusing on the progressing module and the core module.

## **Summary**

1. **Results**
    
    For both modules, we instrumented GET/SET API, generated configuration mapping files, found a way to inject config value, generated valid values for each parameter, and generated ctest for each parameter. At last, we open PR for each module at openctest repo. ([Processing Module](https://github.com/xlab-uiuc/openctest/pull/19))([Core Module](https://github.com/xlab-uiuc/openctest/pull/36))
    

## **Total Effort**

1. Yen Chen (yenc3): From week 5 to the semester's end, I think I spend about 8 hours per week on the project on average. So it should be around 90~95 hours. 
2. Yijing Yang (yijingy2): I think I spend about 8 hours per week and with 2 weeks of sick leave, so it should be about 70 hours.

## Questions

1. What are the pain points in working on the configuration project?
Since we work on a huge open-source project, it is hard to fully understand and control the codes. As a result, we often encounter unexpected obstacles such as some codes being forbidden when adding code to the open-source project (Druid), and we have to refer to the other files in the project and find out the correct way to do it. Additionally, when a test fails, it is hard to figure out whether it is because of the misunderstanding of the documents that classify a bad value as a good one, or a configuration bug. It requires a thorough understanding of the project.
2. How much time does the project take in total and what is the most time-consuming part?
    
    For the processing module, we spend about 90 hours. And for the core module, because there are some similarities between the two modules, we spend about 70 hours.
    
    I think instrumenting GET/SET API is quite time-consuming. The reason is that instrumenting GET/SET API is the first step of the whole project. At first, we are not familiar with Druid and don’t know where to start, it takes us quite a while to understand Druid and find the correct place to instrument GET/SET API.  In our project, the configuration parameters are scattered in different classes, so identifying them is time-consuming. Additionally, unlike the sample projects where the configuration parameters are generated through a for loop, in our project, the GET/SET APIs are implemented per configuration parameter, so it takes more lines of instrumentation.
    
3. What part of the original Ctest model (in the paper [https://www.usenix.org/conference/osdi20/presentation/sun](https://www.usenix.org/conference/osdi20/presentation/sun)) works well and what works poorly?
    
    “Identify parameters” and  “generate ctest” work well. However, I think “generate value” is relatively limited. First, it requires users to collect descriptions and default values. If the project, which users implement is not documented well, this part will become quite challenging. Besides, there are also some cases that the default value in the documentation is hard to deal with, for example, the default value of “druid.global.http.numMaxThreads” is “max(10, ((number of cores * 17) / 16 + 2) + 30)”, and openctest is not able to handle well. Additionally, many configuration parameters in our project are defined as a class or enum, like “druid.global.http.compressionCodec”,  which cannot be identified by the auto-generation. Eventually, there was more than half of the parameters do not have a generated value.
    
4. How good or bad is the Ctest prototype?
    - How often/efficiently can Ctest pinpoint the BAD value of the parameters?
        - About 15%~20% of valid ctests fail when generating ctest. I think most of them are due to BAD value (a small amount of them are due to assertion error (around 3%)).
    - If you are the boss of one famous project (spark, nifi, etc...) Are you willing to spend some time supporting the configuration testing for the project? Why or Why not?
        - Yes. I think by doing configuration testing, possible bugs which can not be found by the normal unit test can be found. And ctest can detect configuration changes while existing misconfiguration detection techniques only check configuration values. Besides, I think openctest actually provide good guideline on how to apply ctest to new project, and by running the code in openctest such as “./generate_ctest.sh”, users can save a lot of time on implementing configuration test.
    - Is there any disadvantages of applying configuration testing to more projects?
        - I can not think of any… After all, it won’t harm to do more tests on projects.