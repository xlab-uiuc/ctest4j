import os, sys, time
from pathlib import Path
from collections import defaultdict
from config import OLD_CTEST_PROJECTS
from utils import LOG, get_proj_abs_path, append_to_file

CUR_DIR = Path.cwd()


def get_test_list(target_proj: str) -> list:
    # check whetehr there is a directory named 'executed_tests':
    if not os.path.exists('executed_tests'):
        raise Exception('No executed_tests directory found!')
    with open(f'executed_tests/{target_proj}.txt', 'r') as f:
        test_list = f.readlines()
    test_list = [test.strip() for test in test_list]
    return test_list


def run(target_proj: str, proj_path: Path, test_list: list):
    os.chdir(proj_path)
    LOG('[OLD-CTEST-RND] Start running old ctestrunner...')
    start_time = time.time()
    if target_proj == 'zookeeper':
        os.system('git checkout vanilla && mvn -B clean install -DskipTests')
    for test in test_list:
        mvn_cmd = f"mvn -B surefire:test -Dtest={test}"
        #print(f'Running command: {mvn_cmd} in {proj_path}')
        os.system(mvn_cmd)
    end_time = time.time()
    LOG(f'[OLD-CTEST-RND] Finish running old ctestrunner. Time elapsed: {end_time - start_time} seconds.')
    os.chdir(CUR_DIR)
    return end_time - start_time


def run_old_ctestrunner(target_proj: str):
    test_list = get_test_list(target_proj)
    proj_abs_path = get_proj_abs_path(target_proj, CUR_DIR)
    time_elapsed = run(target_proj, proj_abs_path, test_list)
    return time_elapsed
    

if __name__ == '__main__':
    if (len(sys.argv) != 2):
        print('Usage: python run.py <target-project>')
        exit(1)
    target_proj = sys.argv[1]
    if target_proj not in OLD_CTEST_PROJECTS:
        print(f'Unknown target project: {target_proj}')
        exit(1)
    start_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())        
    time_elapsed = run_old_ctestrunner(target_proj)
    append_to_file(f'{target_proj}-oldctest-time.tsv', f'Start_time:{start_time}\tOldCtestRunnerTime:{time_elapsed} seconds\n')
