import os
import sys
import build_config

ROOT_DIR = "."

def build_app(app_name):
    if app_name == "hbase":
        return
    app_dir = os.path.join(ROOT_DIR, app_name)
    values = build_config.build_modules[app_name]
    branch = values[0]
    modules = values[1]
    module_str = ",".join(modules)
    mvn_cmd = "mvn -B clean install -o -DskipTests -am -pl {} -Drat.skip -Denforcer.skip -Dcheckstyle.skip -Dmaven.test.skip=true".format(module_str)
    build_cmd = "cd {} && git checkout {} && {}".format(app_dir, branch, mvn_cmd)
    print("CTest-Runner-Building " + app_name + "=======================================")
    print(build_cmd)
    os.system(build_cmd)


def build_hadoop_for_hbase():
    app_name = "hadoop"
    app_dir = os.path.join(ROOT_DIR, app_name)
    branch = "ctest-eval-hbase"
    module = 'hadoop-common-project/hadoop-common'
    mvn_cmd = "mvn -B clean install -o -DskipTests -Dmaven.test.skip=true -am -pl {}".format(module)
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
        build_app(app)

build()
