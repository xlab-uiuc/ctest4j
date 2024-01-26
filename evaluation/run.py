import os, time, sys
from auto_annotate import auto_annotate_script_2
from utils import LOG, is_proj_supported, get_poject_vanilla_branch, get_proj_ctest_branch, get_proj_path, get_proj_junit_version, get_proj_abs_path, mvn_clean_and_build_cmd, vanilla_mvn_cmd, mapping_collection_mvn_cmd, ctest_runner_mvn_cmd, append_to_file, get_junit_version
from pathlib import Path

CUR_DIR = Path.cwd()


def add_ctest_annotation(target_proj):
    LOG('[ANNOTATION] Checkout to branch ctest-eval')
    proj_abs_path = get_proj_abs_path(target_proj, CUR_DIR)
    proj_ctest_branch = get_proj_ctest_branch(target_proj)
    checkout_to_branch(proj_abs_path, proj_ctest_branch)
    junit_version = get_junit_version(target_proj)
    
    os.chdir(proj_abs_path)    
    LOG('[ANNOTATION] Add annotation')
    start_time = time.time()
    auto_annotate_script_2(target_proj, str(proj_abs_path), junit_version)
    end_time = time.time()
    return end_time - start_time

    
def checkout_to_branch(proj_path, branch):
    LOG('[CHECKOUT] Checkout to branch {}'.format(branch))
    os.system('cd {} && git checkout -f {}'.format(proj_path, branch))

    
def run_vanilla_test(proj_path, branch):
    os.chdir(proj_path)
    os.system('pwd')

    # checkout to the branch
    LOG('[VANILLA-RND] Checkout to branch {}'.format(branch))
    checkout_to_branch(proj_path, branch)

    # mvn clean and build
    LOG('[VANILLA-RND] mvn clean and build')
    os.system(mvn_clean_and_build_cmd())
    # run the test
    LOG('[VANILLA-RND] Run the test')
    start_time = time.time()
    os.system(vanilla_mvn_cmd())
    end_time = time.time()
    return end_time - start_time


def run_ctest_test(proj_path, branch):
    os.chdir(proj_path)
    LOG('[CTEST-RND-COLLECTION] mvn clean and build')
    os.system(mvn_clean_and_build_cmd())

    # run the test
    LOG('[CTEST-RND-COLLECTION] Run the collection phase')
    start_time = time.time()
    os.system(mapping_collection_mvn_cmd())
    end_time = time.time()
    collection_time = end_time - start_time
    
    LOG('[CTEST-RND] mvn clean and build')
    os.system(mvn_clean_and_build_cmd())
    
    # run the test with ctest runner
    LOG('[CTEST-RND] Run the test with ctest runner')
    start_time = time.time()
    os.system(ctest_runner_mvn_cmd())
    end_time = time.time()
    ctest_time = end_time - start_time
    return collection_time, ctest_time


def run(target_proj):
    if not is_proj_supported(target_proj):
        print('Project {} is not supported'.format(target_proj))
        exit(1)

    proj_vanilla_branch = get_poject_vanilla_branch(target_proj)
    proj_ctest_branch = get_proj_ctest_branch(target_proj)
    proj_path = get_proj_path(target_proj)
    proj_abs_path = get_proj_abs_path(target_proj, CUR_DIR)

    if not proj_abs_path.exists():
        print('Project {} does not exist'.format(target_proj))
        exit(1)

    # run vanilla test
    vanilla_time = run_vanilla_test(proj_abs_path, proj_vanilla_branch)

    
    # run script to add all annotations
    annotation_time = add_ctest_annotation(target_proj)

    # run mapping collection and ctest-runner test
    collection_time, ctest_time = run_ctest_test(proj_abs_path, proj_ctest_branch)
    os.chdir(CUR_DIR)
    return vanilla_time, annotation_time, collection_time, ctest_time


if __name__ == '__main__':
    if (len(sys.argv) != 2):
        print('Usage: python run.py <target-project>')
        exit(1)
    target_proj = sys.argv[1]
    vanilla_time, annotation_time, collection_time, ctest_time = run(target_proj)
    cur_time = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime())
    append_to_file(f'{target_proj}-time.tsv', 'Start_Date:{}\tVanilla_Test_Time:{}\tAnnotation_Time:{}\tCollection_Time:{}\tCTest_Time:{}'.format(cur_time, vanilla_time, annotation_time, collection_time, ctest_time))
    

