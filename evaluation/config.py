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
    'camel': ['ctest-eval', ['core/camel-base', 'core/camel-core']],
}

PROJ_VANILLA_BRANCH_DICT = {
    'hbase': 'rel/2.5.6',
    'hcommon': 'rel/release-3.3.6',
    'hdfs': 'rel/release-3.3.6',
    'yarn': 'rel/release-3.3.6',
    'mapreduce': 'rel/release-3.3.6',
    'hive': 'rel/release-3.1.3',
    'zeppelin': 'v0.10.1',
    'alluxio': 'v2.9.3',
    'kylin': 'kylin-4.0.4',
    'flink': 'release-1.18.0',
    'camel': 'camel-3.21.2'
}


PROJ_PATH_DICT = {
    'hbase': 'hbase/hbase-server',
    'hcommon': 'hadoop/hadoop-common-project/hadoop-common',
    'hdfs': 'hadoop/hadoop-hdfs-project/hadoop-hdfs',
    'yarn': 'hadoop/hadoop-yarn-project/hadoop-yarn/hadoop-yarn-common',
    'mapreduce': 'hadoop/hadoop-mapreduce-project/hadoop-mapreduce-client/hadoop-mapreduce-client-core',
    'hive': 'hive/ql',
    'zeppelin': 'zeppelin/zeppelin-zengine',
    'alluxio': 'alluxio/core/common',
    'kylin': 'kylin/core-metadata',
    'flink': 'flink/flink-core',
    'camel': 'camel/core/camel-core',
}

