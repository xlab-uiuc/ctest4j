import config
from pathlib import Path

def LOG(msg):
    print(f"[CTest4J-EVAL] {msg}")


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
    return 'mvn -B surefire:test -Dctest.config.save' + ctest_exclude_parameters()


def ctest_runner_mvn_cmd() -> str:
    return 'mvn -B surefire:test -Dctest.mapping.dir=ctest/saved_mapping/' + ctest_exclude_parameters()


def ctest_exclude_parameters() -> str:
    return ' -Dctest.config.exclude=\"hbase.hregion.memstore.mslab.chunksize,ipc.client.connect.timeout,ipc\.\d+\..*,.*:\d+.*\"'

# For Gradle projects

def is_gradle_proj(proj: str) -> bool:
    return proj in config.GRADLE_PROJECTS


def gradle_clean_and_build_cmd(proj: str) -> str:
    return 'gradle clean build -x test -PchecksumIgnore' if proj == 'jmeter' else 'gradle clean build -x test -PchecksumIgnore'


def vanilla_gradle_cmd(proj: str) -> str:
    return 'gradle :src:core:test --no-rebuild -PchecksumIgnore --rerun-tasks' if proj == 'jmeter' else 'gradle test -PchecksumIgnore --no-rebuild --rerun-tasks'


def mapping_collection_gradle_cmd(proj: str) -> str:
    return 'gradle :src:core:test --no-rebuild -PchecksumIgnore --rerun-tasks -Pctest.config.save=true' if proj == 'jmeter' else 'gradle test -PchecksumIgnore --no-rebuild --rerun-tasks -Pctest.config.save=true'


def ctest_runner_gradle_cmd(proj: str) -> str:
    return 'gradle :src:core:test --no-rebuild -PchecksumIgnore --rerun-tasks -Pctest.mapping.dir=ctest/saved_mapping/' if proj == 'jmeter' else 'gradle test -PchecksumIgnore --no-rebuild --rerun-tasks -Pctest.mapping.dir=ctest/saved_mapping/'
    

