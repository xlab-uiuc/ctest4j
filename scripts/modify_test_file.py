import os


def modify_file(test_file, method_list):
    print(f"================={test_file}=================")
    for method, required_config in method_list:
        print(f"-->{method}, {required_config}")
        




