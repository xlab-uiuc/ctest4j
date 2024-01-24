import json
import os
import sys


def list_json_files(directory_path):
    json_files = []
    for dirpath, dirnames, filenames in os.walk(directory_path):
        for file in filenames:
            if file.endswith(".json"):
                json_files.append(os.path.join(dirpath, file))
    return json_files


def parse_file(json_file):
    with open(json_file) as f:
        content = json.load(f)
    classLevelParams = content["classLevelParams"]
    methodLevelParamsMapping = content["methodLevelParams"]
    numOfCTestMethod = 0
    paramsSet = set(classLevelParams)
    for test_method, test_method_params in methodLevelParamsMapping.items():
        if len(test_method_params) > 0:
            numOfCTestMethod += 1
            paramsSet = paramsSet.union(set(test_method_params))
    if len(classLevelParams) > 0:
        numOfCTestMethod = len(methodLevelParamsMapping.items())
    return numOfCTestMethod, paramsSet


def counter(directory_path):
    json_files = list_json_files(directory_path)
    numOfCTestClass = 0
    numOfCTestMethod = 0
    ParamsSet = set()
    for json_file in json_files:
        numOfCTests, totalNumOfParamsSet = parse_file(json_file)
        if numOfCTests > 0:
            numOfCTestClass += 1
            numOfCTestMethod += numOfCTests
            ParamsSet = ParamsSet.union(totalNumOfParamsSet)
    return numOfCTestClass, numOfCTestMethod, len(ParamsSet)


if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Usage: python main.py <folder path>")
        exit(1)
    ncc, ncm, np = counter(sys.argv[1])
    print(f"{ncc},{ncm},{np}")
