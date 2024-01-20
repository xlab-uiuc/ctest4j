SUPPORTED_PROJECTS = ['hbase', 'hcommon', 'hdfs', 'yarn', 'mapreduce', 'hive', 'zeppelin', 'alluxio', 'kylin', 'flink', 'camel', 'jmeter', 'paldb']

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
    'zookeeper': ['ctest-eval', ['zookeeper-server']],
    'jmeter': ['ctest-eval', ['']],
    'paldb': ['ctest-eval', ['']]
}

GRADLE_PROJECTS = {'jmeter', 'paldb'}
                  
PROJ_VANILLA_BRANCH_DICT = {
    'hbase': 'ctest/2.5.6',
    'hcommon': 'ctest-eval-vanilla',
    'hdfs': 'ctest-eval-vanilla',
    'yarn': 'ctest-eval-vanilla',
    'mapreduce': 'ctest-eval-vanilla',
    'hive': 'rel/release-3.1.3',
    'zeppelin': 'v0.10.1',
    'alluxio': 'ctest-eval-vanilla',
    'kylin': 'kylin-4.0.4',
    'flink': 'ctest-eval-vanilla',
    'camel': 'camel-3.21.2',
    'zookeeper': 'ctest-eval-vanilla',
    'jmeter': 'ctest-eval-vanilla',
    'paldb': 'ctest-eval-vanilla'
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
    'zookeeper': 'zookeeper/zookeeper-server',
    'jmeter': 'jmeter/src/core',
    'paldb': 'paldb/paldb'
}

PROJ_JUNIT_VERSION_DICT = {
    'hbase': 'junit4',
    'hcommon': 'junit4',
    'hdfs': 'junit4',
    'yarn': 'junit4',
    'mapreduce': 'junit4',
    'hive': 'junit4',
    'zeppelin': 'junit4',
    'alluxio': 'junit4',
    'kylin': 'junit4',
    'flink': 'junit4',
    'camel': 'junit5',
    'zookeeper': 'junit5',
    'jmeter': 'junit5',
    'paldb': 'testng'
}

   
