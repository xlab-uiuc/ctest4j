import config
from pathlib import Path

def LOG(msg):
    print(f"[CTEST-RUNNER-EVAL] {msg}")


def get_junit_version(proj) -> str:
    return config.PROJ_JUNIT_VERSION_DICT[proj]

def write_to_file(file_path, content):
    with open(file_path, 'w') as f:
        f.write(content)


def append_to_file(file_path, content):
    with open(file_path, 'a') as f:
        f.write(content)


def is_proj_supported(proj) -> bool:
    return proj in config.PROJ_PATH_DICT.keys()


def get_poject_vanilla_branch(proj) -> str:
    return config.PROJ_VANILLA_BRANCH_DICT[proj]


def get_proj_ctest_branch(proj) -> str:
    return 'ctest-eval'


def get_proj_path(proj) -> Path:
    return Path(config.PROJ_PATH_DICT[proj])


def get_proj_abs_path(proj, cur_dir: Path) -> Path:
    return cur_dir.parent.parent / get_proj_path(proj)


def get_proj_junit_version(proj) -> str:
    return config.PROJ_JUNIT_VERSION_DICT[proj]


def mvn_clean_and_build_cmd() -> str:
    return 'mvn -B clean install -DskipTests -Drat.skip -Denforcer.skip -Dcheckstyle.skip'


def vanilla_mvn_cmd() -> str:
    return 'mvn -B surefire:test'


def mapping_collection_mvn_cmd() -> str:
    return 'mvn -B surefire:test -Dctest.config.save'


def ctest_runner_mvn_cmd() -> str:
    return 'mvn -B surefire:test -Dctest.mapping.dir=ctest/saved_mapping/'
