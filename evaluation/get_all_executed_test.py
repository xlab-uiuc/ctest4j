import os, sys
from pathlib import Path
from collections import defaultdict
from typing import List, Dict

def get_executed_tests(proj_name: str, log_file: Path):
    executed_tests = set()
    with open(log_file) as f:
        for line in f:
            if "\'ctest-eval\'" in line:
                return executed_tests
            if line.startswith("[INFO] Running") or line.startswith("Running"):
                executed_tests.add(line.split(" ")[-1].strip())
    return executed_tests
            

def get_log_files(data_dir: Path):
    log_files: Dict[str, List[Path]] = defaultdict(list)
    for root, dirs, files in os.walk(data_dir):
        for f in files:
            if f.endswith(".log"):
                project_name = f.split("-")[0]
                log_files[project_name].append(Path(root) / f)
    return log_files


def main(data_dir: Path, other_data_dir: Path):
    log_files = get_log_files(data_dir)
    executed_tests: Dict[str, set] = defaultdict(set)
    for proj_name in log_files:
        for log_file in log_files[proj_name]:
            executed_tests[proj_name].update(get_executed_tests(proj_name, log_file))
        print(proj_name, len(executed_tests[proj_name]))
        # do not write empty file
        if len(executed_tests[proj_name]) == 0:
            continue
        with open(other_data_dir / f"{proj_name}.txt", "w") as f:
            for test in executed_tests[proj_name]:
                f.write(test + "\n")

                
if __name__ == "__main__":
    data_dir = Path(sys.argv[1])
    output_dir = Path(sys.argv[2])
    main(data_dir, output_dir)

    
    

    
