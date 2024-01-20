import os
import sys
from build_config import build_modules, is_gradle_project

ROOT_DIR = "."

def build_maven_app(app_name):
    if app_name == "hbase":
        return
    app_dir = os.path.join(ROOT_DIR, app_name)
    values = build_config.build_modules[app_name]
    branch = values[0]
    modules = values[1]
    module_str = ",".join(modules)
    mvn_cmd = "mvn -B clean install -DskipTests -am -pl {} -Drat.skip -Denforcer.skip -Dcheckstyle.skip".format(module_str)
    build_cmd = "cd {} && git checkout {} && {}".format(app_dir, branch, mvn_cmd)
    print("CTest-Runner-Building " + app_name + "=======================================")
    print(build_cmd)
    os.system(build_cmd)

    
def build_gradle_app(app_name):
    app_dir = os.path.join(ROOT_DIR, app_name)
    values = build_config.build_modules[app_name]
    branch = values[0]
    gradle_cmd = "./gradlew clean build -x test -PchecksumIgnore"
    if app_name == "paldb":
        gradle_cmd = "gradle clean build -x test -PchecksumIgnore"
    build_cmd = "cd {} && git checkout {} && {}".format(app_dir, branch, gradle_cmd)
    print("CTest-Runner-Building " + app_name + "=======================================")
    print(build_cmd)
    os.system(build_cmd)

    
def build_hadoop_for_hbase():
    app_name = "hadoop"
    app_dir = os.path.join(ROOT_DIR, app_name)
    branch = "ctest-eval-hbase"
    module = 'hadoop-common-project/hadoop-common'
    mvn_cmd = "mvn -B clean install -DskipTests -am -pl {}".format(module)
    build_cmd = "cd {} && git checkout {} && {}".format(app_dir, branch, mvn_cmd)
    print("CTest-Runner-Building HCommon for HBase =======================================")
    print(build_cmd)
    os.system(build_cmd)


def build_hbase():
    build_hadoop_for_hbase()
    app_name = "hbase"
    app_dir = os.path.join(ROOT_DIR, app_name)
    values = build_config.build_modules[app_name]
    branch = values[0]
    modules = values[1]
    build_cmd = "cd {} && git checkout {} && cd {} && mvn -B clean install -DskipTests && cd - && cd {} && mvn -B clean install -DskipTests".format(app_dir, branch, modules[0], modules[1])
    print("CTest-Runner-Building " + app_name + "=======================================")
    print(build_cmd)
    os.system(build_cmd)


def build():
    apps = build_config.build_modules.keys()
    build_hbase()
    for app in apps:
        if is_gradle_project(app):
            build_gradle_app(app)
        else:
            build_maven_app(app)

build()
