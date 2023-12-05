import config
import Path

def LOG(msg):
    print(f"[CTEST-RUNNER-EVAL] {msg}")

def is_proj_supported(proj) -> bool:
    return proj in config.PROJ_LIST


def get_poject_vanilla_branch(proj) -> str:
    return config.PROJ_VANILLA_BRANCH_DICT[proj]


def get_proj_ctest_branch(proj) -> str:
    return config.PROJ_PATH_DICT[proj][0]


def get_proj_path(proj) -> Path:
    return Path(config.PROJ_PATH_DICT[proj][1])


def mvn_clean_and_build_cmd() -> str:
    return 'mvn -B clean install -DskipTests -Dmaven.test.skip'


def vanilla_mvn_cmd() -> str:
    return 'mvn -B surefire:test'


def mapping_collection_mvn_cmd() -> str:
    return 'mvn -B surefire:test -Dctest.config.save'


def ctest_runner_mvn_cmd() -> str:
    return 'mvn -B surefire:test -Dctest.mapping.dir=ctest/saved_mapping/'
