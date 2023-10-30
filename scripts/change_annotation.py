import os, sys
import json
from util import read_json
from pathlib import Path
from typing import List
from collections import defaultdict
from modify_test_file import modify_file


"""Return a list of test "java" source code files in a directory"""
def get_test_class_file_dict(target_dir: Path) -> List[str]:
    """Find all tests in a directory"""
    test_file_dict = {}
    for root, dirs, files in os.walk(target_dir):
        for file in files:
            if file.endswith(".java") and "test" in file.lower():
                test_class = file.split(".")[0].split("/")[-1]
                test_file_dict[test_class] = os.path.join(root, file)
    return test_file_dict


"""Return the required configuration for a test"""
def get_config_from_json(test_json_file: str):
    json_data = read_json(test_json_file)
    return json_data.get("required", [])
    

"""Return all test "json" configuration files"""
def get_target_test_config_file_from_dir(test_config_dir: Path) -> List[str]:
    """Get the target test from the used config directory"""
    test_lists = []
    for root, dirs, files in os.walk(test_config_dir):
        for file in files:
            if file.endswith(".json"):
                test_lists.append(os.path.join(root, file))
    return test_lists


"""Return the test class and method based on the test file name"""
def get_test_class_and_method(test_file: str) -> tuple:
    """Get the test class and method from a test file"""
    parts = test_file.split("_")
    test_class = parts[0].split(".")[-1]
    test_method = parts[1].split(".")[0]
    return test_class, test_method


"""Return a dictionary of test source code file and its corresponding test methods"""
def construct_modification_target_dict(config_dir: Path, target_test_dir: Path):
    target_test_file_dict = defaultdict(list)

    test_config_json_file_list = get_target_test_config_file_from_dir(config_dir)
    test_code_file_dict = get_test_class_file_dict(target_test_dir)

    for test_config_json_file in test_config_json_file_list:
        test_class, test_method = get_test_class_and_method(test_config_json_file)
        required_config = get_config_from_json(test_config_json_file)
        test_file = test_code_file_dict.get(test_class, None)
        target_test_file_dict[test_file].append((test_method, required_config))

    return target_test_file_dict


def main(config_dir: Path, target_test_dir: Path):
    target_test_file_dict = construct_modification_target_dict(config_dir, target_test_dir)
    for test_file, method_list in target_test_file_dict.items():
        modify_file(test_file, method_list)


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python change_annotation.py <config_dir> <target_test_dir>")
        sys.exit(1)
    config_dir, target_test_dir = sys.argv[1:]
    main(Path(config_dir), Path(target_test_dir))


        




    
    
