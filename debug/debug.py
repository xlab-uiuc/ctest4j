import os
import re

if __name__ == "__main__":
    passed = {"vanilla": [], "tracking": [], "checking": []}
    error = {"vanilla": [], "tracking": [], "checking": []}
    failure = {"vanilla": [], "tracking": [], "checking": []}
    # getting class
    for i in ["vanilla.log", "tracking.log", "checking.log"]:
        with open(i, "r") as f:
            contents = f.readlines()
            for index, content in enumerate(contents):
                if re.search("\[INFO\] Running", content) is not None:
                    if re.search("\[INFO\] Tests", contents[index + 1]) is not None:
                        passed[i[:-4]].append(content[15:-1])
                    if re.search("\[ERROR\]", contents[index + 1]) is not None:
                        if "<<< ERROR!" in contents[index + 2]:
                            error[i[:-4]].append(content[15:-1])
                        elif "<<< FAILURE!" in contents[index + 2]:
                            failure[i[:-4]].append(content[15:-1])

    # doing diff
    all_passed = set(passed["vanilla"] + passed["tracking"] + passed["checking"])
    print(len(all_passed))
    all_error = set(error["vanilla"] + error["tracking"] + error["checking"])
    print(len(all_error))
    all_failure = set(failure["vanilla"] + failure["tracking"] + failure["checking"])
    print(len(all_failure))