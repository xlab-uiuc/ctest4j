# This is the configuration file for building all the applications in docker 
import os
build_modules = {
    'hbase': ['ctest-eval', ['hbase-common', 'hbase-server']],
    'hadoop': ['ctest-eval', ['hadoop-common-project/hadoop-common', 
                            'hadoop-hdfs-project/hadoop-hdfs', 
                            'hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common',
                            'hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-core']],
    'hive': ['ctest-eval', ['common', 'ql']],
    'zeppelin': ['ctest-eval', ['zeppelin-common', 'zeppelin-interpreter', 'zeppelin-zengine']],
    'alluxio': ['ctest-eval', ['core/common']],
    'kylin': ['ctest-eval', ['core-common', 'core-cube', 'core-job', 'core-metadata', 'core-metrics', 'core-storage']],
    'flink': ['ctest-eval', ['flink-core']],
}
