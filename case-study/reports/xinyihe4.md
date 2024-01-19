# Problem
Applying ctest to modules of two opensource projects: Apache NiFi and Apache Hive. 
# Summary
We successfully applied ctest to **Apache NiFi's nifi-commons** and **Apache Hive' common** modules. We wrote python scrips to extract testes, configuration parameters mapping, and run ctest with injected configuration values for Nifi. We also modified the openctest repo to accomodate for both nifi-commons and hive's common.
# Results
Successful application of ctest into Nifi nifi-commons and Hive common. 
# Total Effort
Since we are not familar with ctest (or even mvn, pom.xml, surefire-reports, git patch, etc) before this project. We spend a lot of time learning these topics.  
For each of the progress report we did about 12 hours of per person per progress report. During the last 2 weeks of the semester, we did approximately 30 hours of work per person per week. Thus, the total work hours is approximately 216 hours.
# Longer Description
## NIFI
(All of Nifi's accomodation for openctest were done after our previous progress report, previously we )
- Moved to the newest released version 1.19.1: see [pom.xml](https://github.com/lilacyl/nifi/blob/ctest-injection/pom.xml)
- Get & Set: see branch [ctest-logging](https://github.com/lilacyl/nifi/tree/ctest-logging). [Patches](https://github.com/Xinyihe123/openctest/tree/nifi/core/patch/nifi-commons/nifi-logging-patches) to the original hive branch (branch-3.1) can be found in our commits to the openctest folder
- Inject: see branch [ctest-injection](https://github.com/lilacyl/nifi/tree/ctest-injection). [Patches](https://github.com/Xinyihe123/openctest/blob/nifi/core/patch/nifi-commons/injection.patch) to the original hive branch (branch-3.1) can be found in our commits to the openctest folder
- Edited openctest to accomodate for ctest on hive/common. See detail in our [pull request](https://github.com/xlab-uiuc/openctest/pull/35).
- [**Demo Video**](https://illinois.zoom.us/rec/share/fShYS7UFdDmzWeLjzlTIyJn13G4Q_Rt5fs11ZLgsWIq5se4TCuE9UL9-vOxI-Jmo.jUubH1IzCzlvCocr?startTime=1670808287000): since we ran out of time during the demo on Dec 6th, we have recorded videos demos as well to demonstrate the functionality of our project.
## HIVE
- Moved to the newest released version 3.1.3: see [pom.xml](https://github.com/lilacyl/hive/blob/ctest-injection/pom.xml)
- Get & Set: see branch [ctest-logging](https://github.com/lilacyl/hive/tree/ctest-logging). [Patches](https://github.com/Xinyihe123/openctest/tree/hive/core/patch/hive-common/hive-logging-patches) to the original hive branch (branch-3.1) can be found in our commits to the openctest folder
- (After Last Progress Report) Inject: see branch [ctest-injection](https://github.com/lilacyl/hive/tree/ctest-injection). [Patches](https://github.com/Xinyihe123/openctest/tree/hive/core/patch/hive-common/hive-injection-patches) to the original hive branch (branch-3.1) can be found in our commits to the openctest folder
- Edited openctest to accomodate for ctest on hive/common. See detail in our [pull request](https://github.com/xlab-uiuc/openctest/pull/34).
- [**Demo Video**](https://illinois.zoom.us/rec/share/fShYS7UFdDmzWeLjzlTIyJn13G4Q_Rt5fs11ZLgsWIq5se4TCuE9UL9-vOxI-Jmo.jUubH1IzCzlvCocr?startTime=1670808461000): since we ran out of time during the demo on Dec 6th, we have recorded videos demos as well to demonstrate the functionality of our project.
# Our Other effors
Originally, we thought should not use the openctest repo but should implement scripts ourserlves, so we wrote the following python scrips:
- [genmap.py](https://github.com/lilacyl/IDoCT_nifi/blob/56529624ed4d0085d3939399466a8f15dcbc1d8d/nifi/genmap.py) generate mapping from surefire outputs. Result of our genmap can be found [here](https://github.com/lilacyl/IDoCT_nifi/blob/56529624ed4d0085d3939399466a8f15dcbc1d8d/nifi/result/param_map.json). Different from our version of genmap differ from openctest's in that it also display testcases that did not get or set configuration parameters, so you will see that there are some tests that have an empty configuration param array (example: "ResultSetRecordSetTest#testCreateRecord": []).
- [findTestsInAllFiles.py](https://github.com/lilacyl/IDoCT_nifi/blob/main/nifi/findTestsInAllFiles.py) goes through all files in a folder and find all thes tests. findTestsInAllFiles.py calls on our helper script [findTests.py](https://github.com/lilacyl/IDoCT_nifi/blob/main/nifi/findTests.py) which parses each file to looks for the "@test" tag to find unit tests.
- [extract_conf_params.py](https://github.com/lilacyl/IDoCT_nifi/blob/main/hive/extract_conf_params.py) we used this to extract all of the configuration paramters and their values from by parsing [HiveConf.java](https://github.com/lilacyl/hive/blob/master/common/src/java/org/apache/hadoop/hive/conf/HiveConf.java) which is where all configuration paramters & values are stored for hive/common. We wrote this script for hive but not for nifi because hive has a lot more configuration parameters (over 800) compaired to nifi. The generated result JSON file can be used for openctest.
- [run_ctests.py](https://github.com/lilacyl/IDoCT_nifi/blob/GM_NIFI/nifi/run_ctests.py) uses the results from the previous scripts to automatically runs ctest all tests & default values
- [run_single_ctest.py](https://github.com/lilacyl/IDoCT_nifi/blob/GM_NIFI/nifi/run_single_ctest.py) we also support running a single ctest. This is where we intentially put in bad config values and expect the ctests to fail (sometiems fail).
- [formatMethodList.py](https://github.com/lilacyl/IDoCT_nifi/blob/main/hive/formatMethodList.py) Used the previous results from findTestsInAllFiles.py and generate a test methods list JSON file that can be used for openctest.

# Extra Questions Required by CampusWire
1. *What are the pain points in working on the configuration project?*  
Debugging injection. The injection was one of the hardest part of us in hive, hive/common reading of configuration value was done in a 7000+ lines file ([HiveConf.java](https://github.com/lilacyl/hive/blob/master/common/src/java/org/apache/hadoop/hive/conf/HiveConf.java)). On top of that, hive also has both .properties and .xml properties files. So it took us a long time to read, understand, find, and debug our injection for hive. 
2. *How much time does the project take in total and what is the most time-consuming part?*  
Like estimated above, we took around 200 hours for these two projects. Time was spent pretty evenly on each part of the project (understanding code, get&set, inject, apply openctest). But overally the most time-consuming task is debugging our code when it written for larger repository that we are not super familiar with. For instance, debugging injection into hive was a big time consuming task. Another example would be when we debugging our use of openctest. For example, we were stuck on using openctest's identify_param for a while because we were unaware of the error in the "mvn surefire:test" in [runner.py](https://github.com/Xinyihe123/openctest/blob/main/core/identify_param/runner.py) because "mvn surefire:test" was done in a subprocess and its output were not shown by default.
3. *What part of the original Ctest model (in the paper https://www.usenix.org/conference/osdi20/presentation/sun) works well and what works poorly?*. 
- Mainly refer to the different steps in ctest. For example, there are some folders openctest repo:
    - find and modify the GET/SET API: Worked well overall. I think this is a great and straight forward way to find whether each test is related to a configuration parameter.
    - identify parameters: Worked great! Also straightforward and effective in identifying which config params are related to which tests. I think even if the developer of a project does not run ctest, they may use this parammeter map to identify underlying bugs related to config values when a unit test failed.
    - generate value: Not great. It seems to only generate values for numbers and booleans. 
    - generate ctest: Great overall. I think this is a great & efficient way to find underlying bugs. 
    - run ctest: Great overall. Similar to generate ctest.
4. How good or bad is the Ctest prototype?
- How often/efficiently can Ctest pinpoint the BAD value of the parameters?
I think it was not really great at pinpointing bad values. There are actually quite some bad values (ones that we very intentionally made to be bad) that can still pass test.
- If you are the boss of one famous project (spark, nifi, etc...) Are you willing to spend some time supporting the configuration testing for the project? Why or Why not?
If the project has lots of configuration paramters and value, I do think it is a good idea to support configuration testing. For a project such as Hive like we worked on, there are so many configuration parameters that I feel like it would be near impossible to write tests for each configuration parameters, so there could be deep underlying bugs. But using a configuration testing framework like openctests allows for testing with configuration parameters using the existing testcases and one run of the python script.
-  Is there any disadvantages of applying the configuration testing to more projects?
I think the current mode of openctest might not scale very well for many more projects. A lot of the logic are currently "hardcoded" (this may be the only way to go for now since each project is different). So the openctest code will become a lot more messy and hard to read & debug as more projects are added.  

*Question 5-7 Did not apply to our Project*

