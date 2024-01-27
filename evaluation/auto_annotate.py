"""
1. Run all tests as normal tests to record parameter usage
2. Generate usage files
3. Parse usage files
4. Add import information to all test classes
5. Annotate test classes and methods with parsed information
"""
import os
import re
import sys
import time
import subprocess
from typing import List, Dict

# Constant section
# If you want to test more projects, add their names in the PROJECTS_POTENTIAL field and run the script with corresponding arguments.
PROJECTS_SUPPORTED = ["hadoop-common", "hadoop-hdfs"]
PROJECTS_POTENTIAL = ["mapreduce-client-core", "alluxio-core-common", "bookkeeper-common", "camel-core", "druid-processing", "flink-core", "hive-common", "kylin-core-common", "netty-common", "nifi-commons",\
                        "redisson", "rocketmq-common", "spark-core", "zeppelin-interpreter", "zookeeper-server"]

TEST_MODULES_SUPPORTED = ["junit4", "junit5", "testng"]

JAVA_DEPENDENCY = {"junit4": "    <dependency>\n      <groupId>edu.illinois</groupId>\n      <artifactId>ctest4j-junit4</artifactId>\n      <version>1.0-SNAPSHOT</version>\n      <scope>compile</scope>\n    </dependency>\n",
                   "junit5": "    <dependency>\n      <groupId>edu.illinois</groupId>\n      <artifactId>ctest4j-junit4</artifactId>\n      <version>1.0-SNAPSHOT</version>\n      <scope>compile</scope>\n    </dependency>\n",
                     "testng": "    <dependency>\n      <groupId>edu.illinois</groupId>\n      <artifactId>ctest4j-testng</artifactId>\n      <version>1.0-SNAPSHOT</version>\n      <scope>compile</scope>\n    </dependency>\n"
                   }

IMPORT_NORMAL = {"junit4": "import org.junit.runner.RunWith;\nimport edu.illinois.CTestJUnit4Runner;\nimport edu.illinois.CTestClass;\nimport edu.illinois.CTest;\n\n",
                 "junit5": "import org.junit.jupiter.api.extension.ExtendWith;\nimport edu.illinois.CTestJUnit5Extension;\nimport edu.illinois.CTestClass;\nimport edu.illinois.CTest;\n\n",
                 "testng": "import org.testng.annotations.Listeners;\nimport edu.illinois.CTestListener;\nimport edu.illinois.CTestClass;\nimport edu.illinois.CTest;\n\n"}

IMPORT_NORMAL_2 = {"junit4": "import org.junit.runner.RunWith;\nimport edu.illinois.CTestJUnit4Runner;\nimport edu.illinois.CTestClass;\nimport edu.illinois.CTest;\n\n",
                   "junit5": "import org.junit.jupiter.api.extension.ExtendWith;\nimport edu.illinois.CTestJUnit5Extension;\nimport edu.illinois.CTestClass;\nimport edu.illinois.CTest;\n\n",
                   "testng": "import org.testng.annotations.Listeners;\nimport edu.illinois.CTestListener;\nimport edu.illinois.CTestClass;\nimport edu.illinois.CTest;\n\n"}

IMPORT_ABSTRACT = {"junit4": "import edu.illinois.CTest;\n\n",
                   "junit5": "import edu.illinois.CTest;\n\n",
                   "testng": "import edu.illinois.CTest;\n\n"}

LOG_FILES = {"compile": "compile.txt", "track": "track.txt", "ctest": "ctest.txt"}

PRINT_LOG = False

# Utility section
def print_log(message: str):
    if PRINT_LOG:
        print("<<<ctest4j-script>>> " + message)

def change_working_dir(target_dir: str):
    current = os.getcwd()
    os.chdir(target_dir)
    print_log("change working directory: " + current + " -> " + os.getcwd())

# DEPRECATED
def get_class_and_method_name(target_dir) -> Dict:
    if not os.path.isdir(target_dir):
        return None
    entries = os.listdir(target_dir)
    for entry in entries:
        if ".json" not in entry:
            entries.remove(entry)
    results = {}
    for entry in entries:
        tmp = entry.split("_", 1)
        if len(tmp) != 2:
            raise ValueError("Cannot resolve class and method name.")
        class_name = tmp[0]
        method_name = tmp[1]
        if class_name in results:
            results[class_name].append(method_name)
        else:
            results[class_name] = [].append(method_name)
    return results

def get_config_file_by_method_name(target_dir: str, method_name: str):
    if not os.path.isdir(target_dir):
        return None
    entries = os.listdir(target_dir)
    for entry in entries:
        if method_name + ".json" in entry:
            return entry
        
def check_or_create_dir(target_dir: str, exception: bool=False):
    if not os.path.isdir(target_dir):
        if not exception:
            os.makedirs(target_dir)
        else:
            raise ValueError("Incorrect path: " + target_dir)

def add_dependency(project: str, test_module: str):
    if project in PROJECTS_SUPPORTED or project in PROJECTS_POTENTIAL:
        print_log("add dependency information for " + project + " with " + test_module)
        with open("pom.xml", "r+") as f:
            added = False
            contents = f.readlines()
            for index, content in enumerate(contents):
                if "</dependencies>" in content and "dependencyManagement" not in contents[index + 1]:
                    added = True
                    if "ctest4j-junit4" not in contents[index - 4]:
                        contents[index] = JAVA_DEPENDENCY[test_module] + contents[index]
                        break
            if not added:
                for index, content in enumerate(contents):
                    if "<build>" in content:
                        contents[index] = "  <dependencies>\n" + JAVA_DEPENDENCY[test_module] + "  </dependencies>\n\n" + contents[index]
                        added = True
                        break
            if not added:
                for index, content in enumerate(contents):
                    if "</project>" in content:
                        contents[index] = "  <dependencies>\n" + JAVA_DEPENDENCY[test_module] + "  </dependencies>\n\n" + contents[index]
                        added = True
                        break
            contents = "".join(contents)
            f.seek(0)
            f.write(contents)

# Require file to be opened as r+ mode, require file to be written back by caller
def add_import_and_runwith(f=None, test_class: bool=True, test_module: str="junit4"):
    if f is None:
        print_log("file does not exist")
        return None
    if f.mode != "r+":
        print_log("file opened in wrong mode")
        return None
    f.seek(0)
    contents = f.readlines()
    # check existing @RunWith present or not, @Test present or not to select targets and add relavent information
    abstract_class = False
    target_file = True
    annotate_test = False
    ctest_annotation_seen = False
    for content in contents:
        if re.search(" +abstract +class +\w+ +{", content) is not None:
            abstract_class = True
        if "import edu.illinois.CTest" in content or "import edu.illinois.CTestClass" in content:
            ctest_annotation_seen = True 
        # if "@RunWith" in content
        if "@RunWith" in content:
            target_file = False
            break
        else:
            if "@Test" in content:
                annotate_test = True
    if target_file and annotate_test:
        import_added = False
        package_or_import_seen = False
        for index, content in enumerate(contents):
            # add import
            if not package_or_import_seen and (re.match("package .*", content) is not None or re.match("import (?:(?!static)).*", content) is not None):
                package_or_import_seen = True
            if package_or_import_seen and not import_added and re.match("import static .*", content) is not None:
                if not abstract_class and test_class:
                    # CHANGE content -> contents[index]
                    contents[index] = IMPORT_NORMAL[test_module] + content
                if abstract_class and not test_class and not ctest_annotation_seen:
                    # CHANGE content -> contents[index]
                    contents[index] = IMPORT_ABSTRACT[test_module] + content
                    print_log("abstract import added for " + f.name)
                import_added = True
            if package_or_import_seen and not import_added and re.match("\/\*\*", content) is not None:
                if not abstract_class and test_class:
                    # CHANGE content -> contents[index]
                    contents[index] = IMPORT_NORMAL[test_module] + content
                if abstract_class and not test_class and not ctest_annotation_seen:
                    # CHANGE content -> contents[index]
                    contents[index] = IMPORT_ABSTRACT[test_module] + content
                    print_log("abstract import added for " + f.name)
                import_added = True
            # add @RunWith and import (rare case)
            if package_or_import_seen and re.match(".*class +\w+.*{", content) is not None:
                if not abstract_class and test_class:
                    # CHANGE content -> contents[index]
                    contents[index] = "@RunWith(CTestJUnit4Runner.class)\n" + content
                    print_log("normal import and @RunWith added for " + f.name)
                if not import_added:
                    if not abstract_class and test_class:
                        contents[index] = IMPORT_NORMAL[test_module] + contents[index]
                    if abstract_class and not test_class and not ctest_annotation_seen:
                        contents[index] = IMPORT_ABSTRACT[test_module] + contents[index]
                        print_log("abstract import added for " + f.name)
                    import_added = True
                break
        # contents = "".join(contents)
        # f.seek(0)
        # f.write(contents)
        # print_log("test import and @RunWith added for " + f.name)
    return contents

def add_import_and_runwith_2(f=None, test_class: bool=True, test_module: str="junit4"):
    if f is None:
        print_log("file does not exist")
        return None
    if f.mode != "r+":
        print_log("file opened in wrong mode")
        return None
    f.seek(0)
    contents = f.readlines()
    # check existing @RunWith present or not, @Test present or not to select targets and add relavent information
    abstract_class = False
    target_file = True
    annotate_test = False
    ctest_annotation_seen = False
    for content in contents:
        if re.search(" +abstract +class +\w+ +{", content) is not None:
            abstract_class = True
        if "import edu.illinois.CTest" in content or "import edu.illinois.CTestClass" in content:
            ctest_annotation_seen = True 
        # if "@RunWith" in content
        if "@RunWith" in content:
            target_file = False
            break
        else:
            if "@Test" in content:
                annotate_test = True
    if target_file and annotate_test:
        import_added = False
        package_or_import_seen = False
        for index, content in enumerate(contents):
            # add import
            """ Option 1
            if not package_or_import_seen and (re.match("package .*", content) is not None or re.match("import (?:(?!static)).*", content) is not None):
                package_or_import_seen = True
            if package_or_import_seen and not import_added and re.match("import static .*", content) is not None:
                if not abstract_class and test_class:
                    # CHANGE content -> contents[index]
                    contents[index] = IMPORT_NORMAL_2[test_module] + content
                if abstract_class and not test_class and not ctest_annotation_seen:
                    # CHANGE content -> contents[index]
                    contents[index] = IMPORT_ABSTRACT[test_module] + content
                    print_log("abstract import added for " + f.name)
                import_added = True
            if package_or_import_seen and not import_added and (re.match("\/\*\*", content) is not None or re.match("@Category", content) is not None):
                if not abstract_class and test_class:
                    # CHANGE content -> contents[index]
                    contents[index] = IMPORT_NORMAL_2[test_module] + content
                if abstract_class and not test_class and not ctest_annotation_seen:
                    # CHANGE content -> contents[index]
                    contents[index] = IMPORT_ABSTRACT[test_module] + content
                    print_log("abstract import added for " + f.name)
                import_added = True
            # add @RunWith and import (rare case)
            if package_or_import_seen and re.match(".*class +\w+.*{", content) is not None:
                if not abstract_class and test_class:
                    # CHANGE content -> contents[index]
                    contents[index] = "@RunWith(CTestJUnit4Runner.class)\n@CTestClass()\n" + content
                    print_log("normal import and @RunWith added for " + f.name)
                if not import_added:
                    if not abstract_class and test_class:
                        contents[index] = IMPORT_NORMAL_2[test_module] + contents[index]
                    if abstract_class and not test_class and not ctest_annotation_seen:
                        contents[index] = IMPORT_ABSTRACT[test_module] + contents[index]
                        print_log("abstract import added for " + f.name)
                    import_added = True
                break
            """
            """ Option 2 """
            if re.match("import .*;", content) is not None and not import_added:
                if not abstract_class and test_class:
                    contents[index] = IMPORT_NORMAL_2[test_module] + content
                if abstract_class and not test_class and not ctest_annotation_seen:
                    contents[index] = IMPORT_ABSTRACT[test_module] + content
                    print_log("abstract import added for " + f.name)
                import_added = True
            if re.match(".*class +\w+.*{", content) is not None:
                if not abstract_class and test_class:
                    # CHANGE content -> contents[index]
                    if test_module == "junit4":
                        contents[index] = "@RunWith(CTestJUnit4Runner.class)\n@CTestClass()\n" + content
                    elif test_module == "junit5":
                        contents[index] = "@ExtendWith(CTestJUnit5Extension.class)\n@CTestClass()\n" + content
                    elif test_module == "testng":
                        contents[index] = "@Listeners(CTestListener.class)\n@CTestClass()\n" + content
                    print_log("normal import and @RunWith added for " + f.name)
                if not import_added:
                    if not abstract_class and test_class:
                        contents[index] = IMPORT_NORMAL_2[test_module] + contents[index]
                    if abstract_class and not test_class and not ctest_annotation_seen:
                        contents[index] = IMPORT_ABSTRACT[test_module] + contents[index]
                        print_log("abstract import added for " + f.name)
                    import_added = True
                break
        # contents = "".join(contents)
        # f.seek(0)
        # f.write(contents)
        # print_log("test import and @RunWith added for " + f.name)
    return contents

def add_runwith_for_all(target_dir: str):
    modules = []
    if not os.path.isdir(target_dir + "/src/test"): # /src/test/java
        for i in os.listdir(target_dir):
            if os.path.isdir(i + "/src/test"):
                modules.append(i + "/src/test")
    if len(modules) == 0:
        if not os.path.isdir(target_dir + "/src/test"):
            raise ValueError("Does not support current project file hierarchy.")
        else:
            modules.append("src/test")
    for test_dir in modules:
        print_log("add import information and @RunWith in " + test_dir)
        # search all test class
        # entries = os.listdir(target_dir)
        # for entry in entries:
        #     if os.path.isdir(target_dir + entry)
        for dir_path, dir_name, files in os.walk(test_dir):
            # print(f"found dir: {dir_path}")
            for file_name in files:
                if ".java" in file_name:
                    with open(dir_path + "/" + file_name, "r+") as f:
                        contents = add_import_and_runwith(f, True)
                        contents = "".join(contents)
                        f.seek(0)
                        f.write(contents)

def add_runwith_for_all_2(target_dir: str, junit_version: str="junit4"):
    modules = []
    if not os.path.isdir(target_dir + "/src/test"): # /src/test/java
        for i in os.listdir(target_dir):
            if os.path.isdir(i + "/src/test"):
                modules.append(i + "/src/test")
    if len(modules) == 0:
        if not os.path.isdir(target_dir + "/src/test"):
            raise ValueError("Does not support current project file hierarchy.")
        else:
            modules.append(target_dir + "/src/test")
    for test_dir in modules:
        print_log("add import information and @RunWith in " + test_dir)
        for dir_path, dir_name, files in os.walk(test_dir):
            # print(f"found dir: {dir_path}")
            for file_name in files:
                if ".java" in file_name:
                    with open(dir_path + "/" + file_name, "r+") as f:
                        contents = add_import_and_runwith_2(f, True, junit_version)
                        contents = "".join(contents)
                        f.seek(0)
                        f.write(contents)

def run_tests_to_track(project:str, output_dir: str, ctest_mapping_dir: str="ctest/mapping"):
    t1 = time.perf_counter()
    t2 = time.process_time()
    cmd = ["mvn", "-B", "clean", "test-compile"]
    print_log("compile test code: " + " ".join(cmd))
    log_file = LOG_FILES["compile"]
    tmp_index = log_file.index(".")
    t = time.localtime()
    log_file = project.replace("-", "_") + "_" + log_file[:tmp_index] + "_{:02d}{:02d}{:02d}{:02d}".format(t.tm_mon, t.tm_mday, t.tm_hour, t.tm_min) + log_file[tmp_index:]
    with open(output_dir + "/" + log_file, "w") as f:
        child = subprocess.Popen(cmd, stdout=f)
        child.wait()
    print_log("perf_conter() time: " + str(time.perf_counter() - t1) + "s")
    print_log("process_time() time: " + str(time.process_time() - t2) + "s")
    print_log("output saved to " + output_dir + "/" + log_file)

    t1 = time.perf_counter()
    t2 = time.process_time()
    cmd = ["mvn", "-B", "surefire:test", "-Dctest.mode=default", "-Dctest.mapping.dir=" + ctest_mapping_dir, "-Dctest.config.save=true"]
    print_log("run tests: " + " ".join(cmd))
    log_file = LOG_FILES["track"]
    tmp_index = log_file.index(".")
    t = time.localtime()
    log_file = project.replace("-", "_") + "_" + log_file[:tmp_index] + "_{:02d}{:02d}{:02d}{:02d}".format(t.tm_mon, t.tm_mday, t.tm_hour, t.tm_min) + log_file[tmp_index:]
    with open(output_dir + "/" + log_file, "w") as f:
        child = subprocess.Popen(cmd, stdout=f)
        child.wait()
    print_log("perf_conter() time: " + str(time.perf_counter() - t1) + "s")
    print_log("process_time() time: " + str(time.process_time() - t2) + "s")
    print_log("output saved to " + output_dir + "/" + log_file)

def get_class_method_pair(target_dir: str) -> Dict:
    print_log("get used config in " + target_dir)
    if not os.path.isdir(target_dir):
        return None
    class_method_pair = {}
    for dir_path, dir_name, files in os.walk(target_dir):
        for file_name in files:
            if ".json" not in file_name:
                continue
            tmp = file_name.split("_", 1)
            if len(tmp) != 2:
                continue
                # raise ValueError("Cannot resolve class and method name.")
            class_name = tmp[0]
            method_name = tmp[1][:-5]
            if class_name in class_method_pair:
                class_method_pair[class_name].append(method_name)
            else:
                class_method_pair[class_name] = [method_name]
    return class_method_pair

def get_class_list(target_dir: str) -> List:
    print_log("get used config in " + target_dir)
    if not os.path.isdir(target_dir):
        return None
    class_list = []
    for dir_path, dir_name, files in os.walk(target_dir):
        for file_name in files:
            # do not support class name with "_"
            if ".json" not in file_name or "_" in file_name:
                continue
            else:
                class_list.append(file_name[:-5])
    return class_list            

def annotate_test_method(class_method_pair: Dict, target_dir: str, ctest_mapping_dir: str) -> Dict:
    print_log("annotate ctests in " + target_dir)
    if class_method_pair is None or not os.path.isdir(ctest_mapping_dir):
        raise ValueError("Could not get class_method_pair or ctest_mapping_dir not available")
    # handle normal class test annotation
    # class_name_striped = {re.search("\.\w+$", i).group()[1:]: i for i in list(class_method_pair)}
    for dir_path, dir_name, files in os.walk(target_dir):
        for file_name in files:
            full_file_name = dir_path + "/" + file_name
            class_name = None
            for i in list(class_method_pair):
                if re.search(i + "$", full_file_name.replace("/", ".")[:-5]) is not None:
                    class_name = i
                    break
            if class_name is not None:
                # print(file_name)
                with open(full_file_name, "r+") as f:
                    contents = f.readlines()
                    # handle super class import imformation
                    abstract_class = False
                    for content in contents:
                        if re.search(" +abstract +class +\w+ +{", content) is not None:
                            abstract_class = True
                    if abstract_class:
                        contents = add_import_and_runwith(f, False)
                        if contents is None:
                            raise ValueError("Function add_import_and_runwith() failed: Abstract class already has @CTest")
                    test_methods = class_method_pair[class_name]
                    for index in range(len(contents) - 2):
                        content = contents[index]
                    # for index, content in enumerate(contents):
                        # test_method_name = re.search(" test\w+\(.*\).*{", content)
                        test_method_name = re.search("\w+\(.*\).*{", content[:-1] + contents[index + 1][:-1] + contents[index + 2][:-1])
                        if test_method_name is None:
                            continue
                        test_method_name = test_method_name.group()
                        test_method_name = test_method_name[:test_method_name.index("(")]
                        if test_method_name in test_methods:
                            # loop back to search for @Test
                            offset = 0
                            for i in range(index):
                                offset = i
                                if "@Test" in contents[index - offset]:
                                    break
                                if offset == 0:
                                    continue
                                if "@Override" in contents[index - offset] or "@SuppressWarnings" in contents[index - offset] or "/**" in contents[index - offset] or "/*" in contents[index - offset] or " *" in contents[index - offset] or "*/" in contents[index - offset]:
                                    continue
                                else:
                                    break
                            match_str = re.search("@Test *\( *\w+", contents[index - offset])
                            if match_str is not None:
                                match_str = match_str.group()
                                contents[index - offset] = contents[index - offset].replace(match_str[:match_str.index("(") + 1], "@CTest(file=\"" + ctest_mapping_dir + "/" + class_name + "_" + test_method_name + ".json\", ")
                                class_method_pair[class_name].remove(test_method_name)
                            elif "@Test" in contents[index - offset]:
                                match_str = re.search("@Test *\( *\)", contents[index - offset])
                                if match_str is None:
                                    match_str = "@Test"
                                else:
                                    match_str = match_str.group()
                                contents[index - offset] = contents[index - offset].replace(match_str, "@CTest(file=\"" + ctest_mapping_dir + "/" + class_name + "_" + test_method_name + ".json\")")
                                class_method_pair[class_name].remove(test_method_name)
                    if len(class_method_pair[class_name]) == 0:
                        del class_method_pair[class_name]
                    contents = "".join(contents)
                    f.seek(0)
                    f.write(contents)
    return class_method_pair

def annotate_test_method_2(class_list: List, target_dir: str, ctest_mapping_dir: str) -> List:
    print_log("annotate ctests in " + target_dir)
    if class_list is None or not os.path.isdir(ctest_mapping_dir):
        raise ValueError("Could not get class_method_pair or ctest_mapping_dir not available")
    # handle normal class test annotation
    # class_name_striped = {re.search("\.\w+$", i).group()[1:]: i for i in list(class_method_pair)}
    for dir_path, dir_name, files in os.walk(target_dir):
        for file_name in files:
            full_file_name = dir_path + "/" + file_name
            class_name = None
            for i in class_list:
                if re.search(i + "$", full_file_name.replace("/", ".")[:-5]) is not None:
                    class_name = i
                    break 
            if class_name is not None:
                # print(file_name)
                with open(full_file_name, "r+") as f:
                    contents = f.readlines()
                    for index, content in enumerate(contents):
                        # if "import edu.illinois.CTestJUnit4Runner;" in content:
                        #     contents[index] = contents[index].replace("import edu.illinois.CTestJUnit4Runner", "import edu.illinois.CTestJUnit4Runner")
                        if "@CTestClass()" in content:
                            contents[index] = content.replace("@CTestClass()", "@CTestClass(configMappingFile=\"" + ctest_mapping_dir + "/" + class_name + ".json\")")
                            class_list.remove(class_name)
                    contents = "".join(contents)
                    f.seek(0)
                    f.write(contents)
    return class_list

def run_ctests(project:str, output_dir: str, ctest_mapping_dir: str="ctest/mapping"):
    # recompile ctests
    t1 = time.perf_counter()
    t2 = time.process_time()
    cmd_1 = ["mvn", "-B", "clean", "test-compile"]
    cmd_2 = ["mvn", "-B", "surefire:test", "-Dctest.mode=default", "-Dctest.mapping.dir=" + ctest_mapping_dir, "-Dctest.config.save=false"]
    print_log("run CTests: " + " ".join(cmd_2))
    log_file = LOG_FILES["ctest"]
    tmp_index = log_file.index(".")
    t = time.localtime()
    log_file = project.replace("-", "_") + "_" + log_file[:tmp_index] + "_{:02d}{:02d}{:02d}{:02d}".format(t.tm_mon, t.tm_mday, t.tm_hour, t.tm_min) + log_file[tmp_index:]
    with open(output_dir + "/" + log_file, "w") as f:
        child = subprocess.Popen(cmd_1, stdout=f)
        child.wait()
        child = subprocess.Popen(cmd_2, stdout=f)
        child.wait()
    print_log("perf_conter() time: " + str(time.perf_counter() - t1) + "s")
    print_log("process_time() time: " + str(time.process_time() - t2) + "s")
    print_log("output saved to " + output_dir + "/" + log_file)

# Core section
def auto_annotate_script(project: str, test_module:str, project_dir: str, project_test_dir: str, ctest_mapping_dir: str):
    # with open("test.java", "r+") as f:
    #     add_import(f)
    # print_log("import information added")
    print_log("======================================================================")
    if project in PROJECTS_SUPPORTED or project in PROJECTS_POTENTIAL:
        log_dir = os.getcwd() + "/log"
        check_or_create_dir(log_dir)
        check_or_create_dir(project_dir, True)
        change_working_dir(project_dir)
        add_dependency(project, test_module)
        # add import and runwith to all test classes
        check_or_create_dir(project_test_dir, True)
        add_runwith_for_all(project_test_dir)
        # run all tests to track parameter usage, save those in "ctest/mapping"
        check_or_create_dir(ctest_mapping_dir)
        run_tests_to_track(project, log_dir, ctest_mapping_dir)
        # read used config dir and analyze json file in "ctest/mapping"
        class_method_pair = get_class_method_pair(ctest_mapping_dir)
        # add corresponding @CTest annotation for child class and super class
        remaining = annotate_test_method(class_method_pair, project_test_dir, ctest_mapping_dir)
        print_log("remaining:")
        for k, v in remaining.items():
            print(k, "->", v)
        run_ctests(project, log_dir, ctest_mapping_dir)

        
def auto_annotate_script_2(project: str, project_dir: str, junit_version: str):
    print_log("======================================================================")
    print_log("project: " + project)
    log_dir = os.getcwd() + "/log"
    check_or_create_dir(log_dir)
    check_or_create_dir(project_dir, True)
    change_working_dir(project_dir)

    # In our evaluation we already have the dependency added
    # add_dependency(project, test_module)
    # add import and runwith to all test classes
    print_log("add import and runwith to all test classes")
    check_or_create_dir(project_dir, True)
    add_runwith_for_all_2(project_dir, junit_version)

        
def test(project: str, test_module: str, project_dir: str, project_test_dir: str, ctest_mapping_dir: str):
    log_dir = os.getcwd() + "/log"
    check_or_create_dir(log_dir)
    change_working_dir(project_dir)
    add_dependency(project, "junit4")
    add_runwith_for_all(project_test_dir)

# python auto_annotate.py hadoop-common junit4 ../app/hadoop/hadoop-common-project/hadoop-common . ctest/saved_mapping
# python auto_annotate.py hadoop-hdfs junit4 ../app/hadoop/hadoop-hdfs-project/hadoop-hdfs . ctest/saved_mapping
if __name__ == "__main__":
    if len(sys.argv) != 6:
        print_log("usage $project $test_module $project_dir $project_test_dir $ctest_mapping_dir")
        exit(1)
    if sys.argv[1] not in PROJECTS_SUPPORTED and sys.argv[1] not in PROJECTS_POTENTIAL:
        print_log("project not supported")
        exit(1)
    if sys.argv[2] not in TEST_MODULES_SUPPORTED:
        print_log("test module not supported")
        exit(1)
    # test(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4], sys.argv[5])
    auto_annotate_script_2(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
