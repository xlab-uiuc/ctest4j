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
import subprocess
from typing import List, Dict

# Constant section
PROJECTS_SUPPORTED = ["hadoop-common"]

TEST_MODULES_SUPPORTED = ["junit4"]

TEST_IMPORT = "import org.junit.runner.RunWith;\n\
import edu.illinois.CTestJUnit4Runner;\n\
import edu.illinois.CTestClass;\n\
import edu.illinois.CTest;\n\n"

ABSTRACT_IMPORT = "import edu.illinois.CTest;\n\n"

# Utility section
def print_log(message: str):
    print("<<<ctest-runner-script>>> " + message)

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

def get_config_file_by_method_name(target_dir, method_name):
    if not os.path.isdir(target_dir):
        return None
    entries = os.listdir(target_dir)
    for entry in entries:
        if method_name + ".json" in entry:
            return entry

def add_dependency(project, test_module):
    print_log("add dependency information for " + project + " with " + test_module)
    if project == "hadoop-common":
        with open("pom.xml", "r+") as f:
            contents = f.readlines()
            for index, content in enumerate(contents):
                if "</dependencies>" in content and "ctest-runner-junit4" not in contents[index - 4]:
                    contents[index] = "    <dependency>\n      <groupId>edu.illinois</groupId>\n      <artifactId>ctest-runner-junit4</artifactId>\n      <version>1.0-SNAPSHOT</version>\n      <scope>compile</scope>\n    </dependency>\n" + contents[index]
                    break
            contents = "".join(contents)
            f.seek(0)
            f.write(contents)

# Require file to be opened as r+ mode, require file to be written back by caller
def add_import_and_runwith(f=None, test_class=True):
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
    for content in contents:
        if re.search(" +abstract +class +\w+ +{", content) is not None:
            abstract_class = True
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
                    contents[index] = TEST_IMPORT + content
                if abstract_class and not test_class:
                    contents[index] = ABSTRACT_IMPORT + content
                import_added = True
            if package_or_import_seen and not import_added and re.match("\/\*\*", content) is not None:
                if not abstract_class and test_class:
                    contents[index] = TEST_IMPORT + content
                if abstract_class and not test_class:
                    contents[index] = ABSTRACT_IMPORT + content
                import_added = True
            # add @RunWith and import (rare case)
            if package_or_import_seen and re.match(".*class +\w+ +{", content) is not None:
                if not abstract_class and test_class:
                    contents[index] = "@RunWith(CTestJUnit4Runner.class)\n" + content
                if not import_added:
                    if not abstract_class and test_class:
                        contents[index] = TEST_IMPORT + contents[index]
                    if abstract_class and not test_class:
                        contents[index] = ABSTRACT_IMPORT + contents[index]
                    import_added = True
                break
        # contents = "".join(contents)
        # f.seek(0)
        # f.write(contents)
        print_log("test import and @RunWith added for " + f.name)
    return contents

def add_runwith_for_all(target_dir: str):
    print_log("add import information and @RunWith in " + target_dir)
    # search all test class
    if not os.path.isdir(target_dir):
        return None
    # entries = os.listdir(target_dir)
    # for entry in entries:
    #     if os.path.isdir(target_dir + entry)
    for dir_path, dir_name, files in os.walk(target_dir):
        # print(f"found dir: {dir_path}")
        for file_name in files:
            if ".java" in file_name:
                with open(dir_path + "/" + file_name, "r+") as f:
                    contents = add_import_and_runwith(f, True)
                    contents = "".join(contents)
                    f.seek(0)
                    f.write(contents)

def run_tests_to_track(output_dir: str, config_used_dir: str="src/test/resources/used_config"):
    print_log("compile source code")
    cmd = ["mvn", "clean", "test-compile"]
    with open(output_dir + "/mvn_compile.txt", "w") as f:
        child = subprocess.Popen(cmd, stdout=f)
        child.wait()
    print_log("output saved to " + output_dir + "/mvn_compile.txt")
    print_log("run test code")
    cmd = ["mvn", "surefire:test", "-Dmode=default", "-Dconfig.used.dir=" + config_used_dir, "-Dsave.used.config=true"]
    with open(output_dir + "/mvn_track.txt", "w") as f:
        child = subprocess.Popen(cmd, stdout=f)
        child.wait()
    print_log("output saved to " + output_dir + "/mvn_track.txt")

def get_class_method_pair(target_dir: str):
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
                raise ValueError("Cannot resolve class and method name.")
            class_name = tmp[0]
            method_name = tmp[1][:-5]
            if class_name in class_method_pair:
                class_method_pair[class_name].append(method_name)
            else:
                class_method_pair[class_name] = [method_name]
    return class_method_pair

def annotate_test_method(class_method_pair: Dict, target_dir: str, config_used_dir: str):
    print_log("annotate ctests in " + target_dir)
    if class_method_pair is None or target_dir is None:
        return None
    super_class = []
    # handle normal class test annotation
    class_name_striped = {re.search("\.\w+$", i).group()[1:]: i for i in list(class_method_pair)}
    for dir_path, dir_name, files in os.walk(target_dir):
        for file_name in files:
            if file_name[:-5] in class_name_striped:
                # print(file_name)
                with open(dir_path + "/" + file_name, "r+") as f:
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
                    test_methods = class_method_pair[class_name_striped[file_name[:-5]]]
                    for index, content in enumerate(contents):
                        test_method_name = re.search(" test\w+\(", content)
                        if test_method_name is None:
                            continue
                        test_method_name = test_method_name.group()[1:-1]
                        if test_method_name in test_methods:
                            if re.search("@Test *\(", contents[index - 1]) is not None:
                                contents[index - 1] = contents[index - 1].replace("@Test(", "@CTest(file=\"" + config_used_dir + "/" + class_name_striped[file_name[:-5]] + "_" + test_method_name + ".json\", ")
                                class_method_pair[class_name_striped[file_name[:-5]]].remove(test_method_name)
                            elif "@Test" in contents[index - 1]:
                                contents[index - 1] = contents[index - 1].replace("@Test", "@CTest(file=\"" + config_used_dir + "/" + class_name_striped[file_name[:-5]] + "_" + test_method_name + ".json\")")
                                class_method_pair[class_name_striped[file_name[:-5]]].remove(test_method_name)
                    contents = "".join(contents)
                    f.seek(0)
                    f.write(contents)
    return class_method_pair

# Core section
def auto_annotate_script(project: str, test_module:str, test_file_dir: str, used_config_dir: str):
    # with open("test.java", "r+") as f:
    #     add_import(f)
    # print_log("import information added")
    print_log("======================================================================")
    if project == "hadoop-common":
        cwd = os.getcwd()
        change_working_dir(os.getcwd() + "/../app/hadoop/hadoop-common-project/hadoop-common")
        add_dependency(project, test_module)
        # add import and runwith to all test classes
        add_runwith_for_all(test_file_dir)
        # run all tests to track parameter usage, save those in "src/test/resources/used_config"
        run_tests_to_track(cwd, used_config_dir)
        # read used config dir and analyze json file in "src/test/resources/used_config"
        class_method_pair = get_class_method_pair(used_config_dir)
        # add corresponding @CTest annotation for child class and super class
        remaining = annotate_test_method(class_method_pair, test_file_dir, used_config_dir)
        print_log("remaining:")
        print(remaining)

def test():
    change_working_dir(os.getcwd() + "/../app/hadoop/hadoop-common-project/hadoop-common")
    # add_dependency("hadoop-common", "junit4")
    # add_runwith_for_all("src/test/java/org/apache/hadoop/crypto")
    class_method_pair = get_class_method_pair("src/test/resources/used_config")
    annotate_test_method(class_method_pair, "src/test/java/org/apache/hadoop/crypto", "src/test/resources/used_config")

# python auto_annotate.py hadoop-common junit4 src/test/java/org/apache/hadoop src/test/resources/used_config
if __name__ == "__main__":
    if len(sys.argv) != 5:
        print_log("usage $project $test_module $test_file_dir $used_config_dir")
    if sys.argv[1] not in PROJECTS_SUPPORTED:
        print_log("project not supported")
        exit
    if sys.argv[2] not in TEST_MODULES_SUPPORTED:
        print_log("test module not supported")
        exit
    # test()
    auto_annotate_script(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
