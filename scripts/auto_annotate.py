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

projects_supported = ["hadoop-common"]

test_import = "import org.junit.runner.RunWith;\n\
import edu.illinois.CTestJUnit4Runner;\n\
import edu.illinois.CTestClass;\n\
import edu.illinois.CTest;\n\n"

abstract_import = "\nimport edu.illinois.CTest;\n"

def print_log(message):
    print("<<<ctest-runner-script>>> " + message)

def change_working_dir(target_dir):
    current = os.getcwd()
    os.chdir(target_dir)
    print_log("change working directory: " + current + " -> " + os.getcwd())

# deprecated
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
            raise ValueError("Cannot resolve class and method name")
        class_name = tmp[0]
        method_name = tmp[1]
        if class_name in results:
            results[class_name].append(method_name)
        else:
            results[class_name] = [].append(method_name)
    return results

def run_tests_to_track(config_used_dir="src/test/resources/used_config"):
    print_log("compile source code")
    cmd = ["mvn", "clean", "test-compile"]
    child = subprocess.Popen(cmd)
    child.wait()
    print_log("run test code")
    cmd = ["mvn", "surefire:test", "-Dmode=default", "-Dconfig.used.dir=" + config_used_dir, "-Dsave.used.config=true"]
    child = subprocess.Popen(cmd)
    child.wait()

# Require file to be opened as r+ mode
def add_import(f=None, testClass=True):
    if f is None:
        print_log("file does not exist")
        return None
    contents = f.readlines()
    index = 0
    for line in contents:
        if "import static" in line:
            break
        else:
            index += 1
    if testClass:
        contents.insert(index, test_import)
    else:
        contents.insert(index, abstract_import)
    contents = "".join(contents)
    f.seek(0)
    f.write(contents)

def add_runwith_for_all(target_dir):
    # search all test class
    pending_files = []
    if not os.path.isdir(target_dir):
        return None
    # entries = os.listdir(target_dir)
    # for entry in entries:
    #     if os.path.isdir(target_dir + entry)
    for dir_path, dir_name, files in os.walk(target_dir):
        # print(f"found dir: {dir_path}")
        for f_name in files:
            if ".java" in f_name:
                pending_files.append(dir_path + "/" + f_name)
    # check existing @RunWith present or not, abstract or not, @Test present or not to select targets and add relavent information
    for file_name in pending_files:
        target_file = True
        annotate_test = False
        print(file_name)
        with open(file_name, "r+") as f:
            contents = f.readlines()
            for content in contents:
                if "@RunWith" in content or "abstract class" in content:
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
                    if re.match("package .*", content) is not None or re.match("import (?:(?!static)).*", content) is not None:
                        package_or_import_seen = True
                    if package_or_import_seen and not import_added and re.match("import static .*", content) is not None:
                        contents[index] = test_import + content
                        import_added = True
                    if package_or_import_seen and not import_added and re.match("\/\*\*", content) is not None:
                        contents[index] = test_import + content
                        import_added = True
                    # add @RunWith and import (rare case)
                    if package_or_import_seen and re.match(".*class .* {", content) is not None:
                        contents[index] = "@RunWith(CTestJUnit4Runner.class)\n" + content
                        if not import_added:
                            contents[index] = test_import + contents[index]
                            import_added = True
                contents = "".join(contents)
                f.seek(0)
                f.write(contents)
                print_log("test import and @RunWith added for " + file_name)

def get_class_method_pair(target_dir):
    if not os.path.isdir(target_dir):
        return None
    class_method_pair = {}
    for dir_path, dir_name, files in os.walk(target_dir):
        for file_name in files:
            if ".json" not in file_name:
                continue
            tmp = file_name.split("_")
            if len(tmp) != 2:
                raise ValueError("Cannot resolve class and method name.")
            class_name = tmp[0]
            method_name = tmp[1][:-5]
            if class_name in class_method_pair:
                class_method_pair[class_name].append(method_name)
            else:
                class_method_pair[class_name] = [method_name]
    return class_method_pair

def annotate_test_method(class_method_pair, target_dir, config_used_dir):
    if class_method_pair is None or target_dir is None:
        return None
    remaining = []
    class_name_striped = {re.search("\.\w+$", i).group()[1:]: i for i in list(class_method_pair)}
    for dir_path, dir_name, files in os.walk(target_dir):
        for file_name in files:
            if file_name[:-5] in class_name_striped:
                with open(dir_path + "/" + file_name, "r+") as f:
                    contents = f.readlines()
                    test_methods = class_method_pair[class_name_striped[file_name[:-5]]]
                    for index, content in enumerate(contents):
                        test_method_name = re.search(" test\w+\(", content)
                        if test_method_name is None:
                            continue
                        test_method_name = test_method_name.group()[1:-1]
                        if test_method_name in test_methods:
                            if "@Test" in contents[index - 1]:
                                contents[index - 1] = contents[index - 1].replace("@Test", "@CTest(file=\"" + config_used_dir + "/" + class_name_striped[file_name[:-5]] + "_" + test_method_name + ".json\")")
                                class_method_pair[class_name_striped[file_name[:-5]]].remove(test_method_name)
                    contents = "".join(contents)
                    f.seek(0)
                    f.write(contents)
    return class_method_pair


def auto_annotate_script(project, test_file_dir, used_config_dir):
    # with open("test.java", "r+") as f:
    #     add_import(f)
    # print_log("import information added")
    print_log("======================================================================")
    if project == "hadoop-common":
        change_working_dir(os.getcwd() + "/../app/hadoop/hadoop-common-project/hadoop-common")
    # add import and runwith to all test classes
    add_runwith_for_all(test_file_dir)
    # run all tests to track parameter usage, save those in "src/test/resources/used_config"
    run_tests_to_track(used_config_dir)
    # read used config dir and analyze json file in "src/test/resources/used_config"
    class_method_pair = get_class_method_pair(used_config_dir)
    # add corresponding @CTest annotation for child class and super class
    remaining = annotate_test_method(class_method_pair, test_file_dir, used_config_dir)
    print_log("remaining")
    print(remaining)

def test():
    # change_working_dir(os.getcwd() + "/app/hadoop/hadoop-common-project/hadoop-common")
    # run_tests_to_track()
    # add_runwith_for_all("app/hadoop/hadoop-common-project/hadoop-common/src/test/java/org/apache/hadoop/log")
    change_working_dir(os.getcwd() + "/app/hadoop/hadoop-common-project/hadoop-common")
    class_method_pair = get_class_method_pair("src/test/resources/used_config")
    annotate_test_method(class_method_pair, "src/test/java/org/apache/hadoop/log", "src/test/resources/used_config")

# python auto_annotate.py hadoop-common src/test/java/org/apache/hadoop src/test/resources/used_config
if __name__ == "__main__":
    # auto_annotate_script();
    if len(sys.argv) != 4:
        print_log("usage $project $test_file_dir $used_config_dir")
    if sys.argv[1] not in projects_supported:
        print_log("project not supported")
        exit
    auto_annotate_script(sys.argv[1], sys.argv[2], sys.argv[3])
