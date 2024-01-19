# CS527 Final Project Report
## Problem:

We added support to run configuration tests on two modules in the Netty project - an event-driven asynchronous network application framework.

## Summary:
In this project, we decided to extend the functionality of the xlab-uiuc/openctest repository to make it support Netty. We followed the procedure described in the openctest repository to implement configuration parameters identification & injection and added support for value & ctest generation as well as for the run_ctest program for netty/transport and netty/transport-udt modules. Finally, we opened a request for each module we worked on. Besides, we discovered a bug in the Netty repository then opened a pull request to fix it, and it has been accepted & merged to the original Netty repository.

## Total Effort:
See in Ctest-project-specific Questions 2.

## Longer Description:
Overall, we forked and modified the Netty repository （version 4.1.85.Final） to add functions of ctest_logging （https://github.com/HongxuMeng/netty/tree/ctest-logging） and then ctest_injection (https://github.com/HongxuMeng/netty/tree/ctest-injection) on transport and transport-udt modules. Then, we started to migrate our work to make it compatible with the openctest repository. In the nearest recent work iteration, we mainly focused on finalizing our commits to the openctest repo. For example, we updated our forked project to a formal release instead of a snapshot, we adjusted the GOOD values in the generate_value folder for some values that can’t be generated automatically, and we also created patches for the changes we made in the forked Netty repository and fixed some minor bugs.

For netty/transport, there are 13 parameters in total. From all 321 tests in this module, we identified 13 parameters that are called with get() and 8 parameter that is called with set(). In the generate_ctest step, we successfully generated ctests for all 13 logged parameters. In addition, the case for parameter “autoRead” is a little bit special. We have tried a number of other values and found many tests getting autoRead can only pass when it is set to the default value “1”, so we modified its two GOOD values to (1, SKIP).

For netty/transport-udt (https://github.com/zazf/openctest/tree/netty-transport-udt), there are 22 parameters in total. From all 9 tests in this module, we identified 15 parameters that are called with get() and 1 parameter that is called with set(). In the generate_ctest step, we successfully generated ctests for all 15 logged parameters.

For netty/transport-sctp (https://github.com/zazf/openctest/tree/netty-transport-sctp), another module we initially planned to work on, we have completed the ctest_logging part. However, we noticed that the configuration class was never initiated with the only 3 existing tests in that module. Due to this fact and the very limited number of tests, we have discontinued our work on this module.

In addition, this is a link to the accepted and merged pull request for bug fix in the original Netty repository: https://github.com/netty/netty/pull/13031


## Ctest-project-specific Questions:
1. **What are the pain points in working on the configuration project?** 
We believe the pain point of this project is that each project can handle the configuration settings in quite different ways, so understanding how some projects load the configurations may not necessarily help to work on our project. For example, in most of the supported projects in the openctest repository, configuration parameters are read and loaded from a file while parameters are defined directly as variables in Netty. Therefore, we had to implement injection for each parameter because we will need to convert the injected value from string to the expected type. This became a problem for us when handling object-type parameters, and we spent a lot of time on it.

2. **How much time does the project take in total and what is the most time-consuming part?**
We estimated that we spent 6-8 hours each week, except the weeks we had presentations and were so busy that we asked for extension. We are estimating 80 hours per person in total. We believe the most time-consuming part are the ctest injection and the compatibility support with openctest repository. The ctest injection part required us to look in depth of the implementation of the open-source project. The openctest part required us to read most of the code in the openctest repository, and make changes so that it can work with the open-source project we had chosen. And because some of the BAD values of some parameter, we had to run generate_ctest several times and spent hours waiting before the results are out for us to move forward.

3. **What part of the original Ctest model works well and what works poorly?**
Some parts we believe worked well are identify_param, generate_ctest, and run_ctest. However, some of them did require modifications specific to each project.  For example, when checking the stack trace to determine whether to include a setter, I believe some projects may handle this differently. 
Some parts we believe did work very well are generate_values and some redundant codes. We noticed that generate_value has a limited ability for non-primitive type parameters. This may not matter a lot if most parameters are in primitive type, but in our project we have some object-type parameters. Also, we believe there are some redundant code such as add_project.sh and constant.py where we need to define the same variable multiple times and we believe this can be improved somehow. Moreover, we found that with some wrong parameter values, there are chances that some tests are unable to finish. In this case, generate_ctest and run_ctest could not efficiently identify this issue and continue hanging.

4. **How good or bad is the Ctest prototype?**

    **i. How often/efficiently can Ctest pinpoint the BAD value of the parameters?**
    In our project, Ctest almost always catches the BAD values for parameters. We found that the Ctest prototype can catch a BAD value for most of the time if it leads to an error in the test case. However, we also encountered some cases where the test itself never stops running, causing generate_ctest and run_ctest never stops as well when inserted with a BAD value. In this case, both generate_ctest and run_ctest failed to identify the BAD value, and corrupted the following tests.
    
    **ii. If you are the boss of one famous project (spark, nifi, etc...) Are you willing to spend some time supporting the configuration testing for the project? Why or Why not?**
    It depends on how complex the project is, how many parameters are used, and how parameters are used in the project. For example, if the project is relatively simple, with a very limited number of parameters, and with most parameters fixed or with limited choices, I would not consider supporting the configuration testing for the project. Because it takes time and knowledge of both the Ctest prototype and the projects to be applied on for the set up, and is simply not worth the extra effort. Otherwise, I think it is worth the extra effort to support the configuration testing for the project, because after it is properly set up, it is quite efficient and helpful for the testing process.
    
    **iii. Is there any disadvantages of applying the configuration testing to more projects?**
    As mentioned above, the disadvantages include the need for extra efforts that will not be paid off for projects with a relative non-urgent need for configuration testing. Also, adding new projects will require modify multiple files, and some of the changes are redundant and may lead to merge conflicts. We believe a better approach can be having a isolated configuration file for each project and adding project will be easier and with fewer conflicts.
