import os, time, sys
from utils import LOG, is_proj_supported, get_poject_vanilla_branch, get_proj_ctest_branch, get_proj_path, mvn_clean_and_build_cmd, vanilla_mvn_cmd, mapping_collection_mvn_cmd, ctest_runner_mvn_cmd
from pathlib import Path

CUR_DIR = Path.cwd()
MVN_COMMAND = 'mvn -B clean test'


def run_vanilla_test(branch):
    # checkout to the branch
    LOG('[VANILLA-RND] Checkout to branch {}'.format(branch))
    os.system('git checkout {}'.format(branch))
    # mvn clean and build
    os.system(mvn_clean_and_build_cmd())
    # run the test
    start_time = time.time()
    os.system(vanilla_mvn_cmd())
    end_time = time.time()
    return end_time - start_time


def run_ctest_test(branch):
    # checkout to the branch
    LOG('[CTEST-RND] Checkout to branch {}'.format(branch))
    os.system('git checkout {}'.format(branch))
    # mvn clean and build
    os.system(mvn_clean_and_build_cmd())
    
    # run the test
    start_time = time.time()
    os.system(mapping_collection_mvn_cmd())
    end_time = time.time()
    annotation_time = end_time - start_time
    
    # run the test with ctest runner
    start_time = time.time()
    os.system(ctest_runner_mvn_cmd())
    end_time = time.time()
    ctest_time = end_time - start_time
    return annotation_time, ctest_time


# Step1: checkout to the vanilla branch and run the test suite once to get the time
def run(target_proj):
    if not is_proj_supported(target_proj):
        print('Project {} is not supported'.format(target_proj))
        return

    proj_vanilla_branch = get_poject_vanilla_branch(target_proj)
    proj_ctest_branch = get_proj_ctest_branch(target_proj)
    proj_path = get_proj_path(target_proj)
    proj_abs_path = CUR_DIR / proj_path

    if not proj_abs_path.exists():
        print('Project {} does not exist'.format(target_proj))
        return

    # run vanilla test
    vanilla_time = run_vanilla_test(proj_vanilla_branch)
    # TODO: run script to add all annotations

    # run mapping collection and ctest-runner test
    annotation_time, ctest_time = run_ctest_test(proj_ctest_branch)
    return vanilla_time, annotation_time, ctest_time


if __name__ == '__main__':
    if (len(sys.argv) != 2):
        print('Usage: python run.py <target-project>')
        exit(1)
    target_proj = sys.argv[1]
    run(target_proj)
