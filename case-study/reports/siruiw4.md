# CS527 Project Final Report

## Problem
Implement the configuration testing on Apache RocketMQ by extending the IDoCT/openctest codebase. More specifically, implement ctest on acl module of RocketMQ.

## Summary
- Results
  - Created PR for openctest git repo. Sucessfully implemented all the required steps of the project. Make the openctest extended to RocketMQ acl module sucessfully.
  - All the work done fulfilled the goals mentioned in initial proposal.
  - RocketMQ repo is at https://github.com/Stranger3333/rocketmq/tree/ctest-intercept
  - openctest repo is at https://github.com/Stranger3333/openctest/tree/rocketmq 

## Total Effort
Spending totally around 80 to 90 hours on this project, 8 hours per week on average. Spent much time on finding the most appropriate project to work on, and later get back on rocketmq to work. Also, split from the team kind of slow the process.

## Details
- What are the pain points in working on the configuration project?
  - There are two pain points for me. Firstly, it is to identify if the project is appropriate for the ctest project and where those GETTER/SETTER for these configuration parameters. The other is to fully understand the logic of the project and modify the code for the ctest usage.
- How much time does the project take in total and what is the most time-consuming part?
  - I would say 8 hours per week and probably more time for the latter part of the project. The most time consuming part is to understand how to do the similar thing as hadoop project example on my own project. Every project differs a lot on their structure and code logic. Also, since I am not sure the module I will work on at the beginning, and the config parameters and their corresponding GETTER/SETTER are located in multiple files, I modified a lot of unused GETTER/SETTER and generate multiple mapping for different modules. Also, the value injection part is the most difficult to implement I believe, so it took me a lot of time as well.
- What part of the original Ctest model (in the paper https://www.usenix.org/conference/osdi20/presentation/sun) works well and what works poorly?
  - find and modify the GET/SET API
    - This step can greatly depend on the project we chose. For the hadoop, I believe that it is straightfoward and works well since it has a 'general' get/set api. Otherwise, you need to insert duplicated log warning code a lot of times.
  - identify parameters
    - This step really depends on the project as well. There are some projects that give you document in formats like xml or csv with all the parameters. In this case, you can directly parsed these files to generate a list of parameters. However, if not, we need to look file by file and to parse the code to get these parameters, which can be tricky. My project is kind of tricky for this step. I did not use the given shell script to do this step.
  - generate value
    - This is a step really simple and good. Once you made the previous steps correctly, this step will give you good results.
  - generate ctest
    - This step is similar to the previous step. I made a mistake for doing this step since I misunderstood part of the parse_output.py. Also, since my config file is in yaml type, I need to add the code to inject value in yaml file in the inject.py.
  - run ctest
    - For now. I think the run_single_test.py works really well. I can inject value into the ctest.yml and test if the injected parameter is a good value of not.
    - The run_ctest.sh does not really work. I think this is really similar to the generated_ctest folder. However, it does not generate any result in the run_ctest_result. I am not sure if I did something wrong.
- How good or bad is the Ctest prototype?
  - How often/efficiently can Ctest pinpoint the BAD value of the parameters?
    - In my case, it may never find the BAD value since the parameters used in the rocketmq are not defined in this way. They have some pre-check for the lenght or type for these parameters. Thus, if some type issue or value issue occurs, the project will have a build failure.
  - If you are the boss of one famous project (spark, nifi, etc...) Are you willing to spend some time supporting the configuration testing for the project? Why or Why not?
    - I would say yes or no. For the yes part, this is because that configuration testing is an area not many people would think about but also is an important area. Thus, supporting ctest for the project will be beneficial. For the no part, deploying the ctest can be time consuming and complicated. We need to firstly understand the logic for the ctest tool and potentially need to modify huge amount of code for the project you applied the configuration testing tool. Thus, this may not be an ideal situation for a boos of some projects. Also, the changes you made can vary case by case, which would not be effective.
  - Is there any disadvantages of applying the configuration testing to more projects?
    - From my observation, it can make the ctest tool less generalized. Everytime a new project is added for the ctest tool, one need to modify & add more code for the test tool. For example, the directory structure is different for each project. You need to specify the new path. Also, not all projects have files that contains the config parameters. Therefore, to enable the value injection step, probably more code needs to be modified.
    - From the project aspect, as I said previously, to make the ctest tool work on the project, you need to add many more log info and change some pom/gradle/ant file configurations. Thus, these changes can not be merged to the main branch and may harm other features. Also, I am not sure if the ctest tool needs to be run under some specific java version or specific tools like maven. If the project gets multiple changes or updates, the tool can fail, which means you have to make many more changes again.
## Addition
- I believe that the project I am working on is not suitable for the configuration testing. Here are some of the reasons based on my observations.
  1. The module I selected does not have many configurations. I added many CTEST GET/SET printout for different configurations files, which contains a lot of GETTER/SETTER. However, most of them are not used by any of the other files or functions.
  2. The project I selected is a message queue related project. I think most of the configurations are used for validation or verification. Thus, it has some hard-coded tests or code to check if some attribute has a specific value. Thus, in this kind of case, injecting value by using ctest.yml file will raise an error for such test.
  3. Also, most of the configuration parameters I had for the tested module are not like the parameters we saw in the hadoop. They are mainly strings, with limitations like length. Thus, they do not have BAD VALUES. Any value that violates the rule they defined will cause the build failure. Thus, I do not think I can find some BAD VALUE. Based on my observation from MP2, it is easier to find some BAD VALUE for some configruation parameter with number type, such as integer or long. 
  4. Overall, I think my project selection is not good. However, it is hard to detect some problems like this at the early stage. I realized these deficiencies of rocketmq after I understand multiple modules main code and test code structure. Also, I think the inject value part is the most important step of this ctest project.
