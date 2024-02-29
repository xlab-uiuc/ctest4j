# Run a Ctest Example in HCommon

We will use the Hadoop Common module as an example to demonstrate how to run a Ctest with Ctest4J in HCommon.\
Here is the full modification of the HCommon module of this example: [code link](https://github.com/ctest-repos/hadoop/commit/3bc2003c784fa8872d6a6b63928f961a6f821e98). 


### Step1: Support Ctest4J in HCommon

Before running the Ctest example, make sure you have followed the [instructions here](Example.md) to support the Ctest4J in HCommon.\
The example in this document is based on the Hadoop 3.3.6 release. \
If you are using a different version of Hadoop, please checkout to the Hadoop 3.3.6 release, run the following command:

```bash
$ cd $HADOOP_HOME # HADOOP_HOME is the root directory of the Hadoop source code
$ git checkout rel/release-3.3.6 && git checkout -b ctest4j-example
```

### Step2: Add Ctest4J Annotations to the Test Class
We will use the test `org.apache.hadoop.crypto.TestCryptoStreams#testAvailable` in the HCommon module as an example to demonstrate how to add Ctest4J annotations to the test class.\
Open the file `hadoop-common-project/hadoop-common/src/test/java/org/apache/hadoop/crypto/TestCryptoStreams.java`, and add the following Ctest4J annotations to the test class:

```bash
$ cd $HADOOP_HOME/hadoop-common-project/hadoop-common
$ emacs $(find . -name "TestCryptoStreams.java")
```

```java
+ import org.junit.runner.RunWith;                                                                                                      
+ import edu.illinois.CTestClass;                                                                                                       
+ import edu.illinois.CTest;                                                                                                            
+ import edu.illinois.CTestJUnitRunner;
 
+ @RunWith(CTestJUnitRunner.class)                                                                                                      
+ @CTestClass                                                                                                                           
public class TestCryptoStreams extends CryptoStreamsTestBase {
  ...
}
```

Build the HCommon module:
```bash
$ mvn clean install -DskipTests
```

### Step3: Run the Ctest
Run the Ctest example with the following command:
```bash
$ mvn surefire:test -Dtest=org.apache.hadoop.crypto.TestCryptoStreams#testAvailable
```

### Step4: Save the Used Configuration Parameter Information
To save the used parameter information, run the following command:
```bash
$ mvn surefire:test -Dtest=org.apache.hadoop.crypto.TestCryptoStreams#testAvailable -Dctest.config.save
```
Then check the `ctest` directory to see the saved parameter information.
```bash
$ cat ctest/saved_mapping/org.apache.hadoop.crypto.TestCryptoStreams.json
```
The JSON file that saves the used configuration parameter information for test `TestCryptoStreams#testAvailable` should be similar to the following:
```json
{
  "classLevelParams":["hadoop.security.crypto.cipher.suite","hadoop.security.crypto.codec.classes.aes.ctr.nopadding","hadoop.security.crypto.jce.provider","hadoop.security.java.secure.random.algorithm"], 
  "methodLevelParams":{
    "org.apache.hadoop.crypto.TestCryptoStreams_testAvailable":[]
  }
}
```

### Step5: Run the Ctest with Another Configuration
To run the Ctest example with another configuration, run the following command:
```bash
$ mvn surefire:test -Dtest=org.apache.hadoop.crypto.TestCryptoStreams#testAvailable -Dconfig.inject.cli="hadoop.security.crypto.cipher.suite=AES/CTR/NoPadding"
```
The argument `config.inject.cli` is used to connect the configuration parameter `hadoop.security.crypto.cipher.suite` with the value `AES/CTR/NoPadding` to the test `TestCryptoStreams#testAvailable`.

To run a bad value of the configuration parameter, run the following command:
```bash
$ mvn surefire:test -Dtest=org.apache.hadoop.crypto.TestCryptoStreams#testAvailable -Dconfig.inject.cli="hadoop.security.crypto.cipher.suite=AES/CTR/Padding"
```
The value `AES/CTR/Padding` is a bad value for the configuration parameter `hadoop.security.crypto.cipher.suite`. The test should fail with the following message:
```
[INFO] Running org.apache.hadoop.crypto.TestCryptoStreams
[ERROR] Tests run: 1, Failures: 0, Errors: 1, Skipped: 0, Time elapsed: 0.187 s <<< FAILURE! - in org.apache.hadoop.crypto.TestCryptoStreams
[ERROR] org.apache.hadoop.crypto.TestCryptoStreams  Time elapsed: 0.186 s  <<< ERROR!
java.lang.IllegalArgumentException: Invalid cipher suite name: AES/CTR/Padding
	at org.apache.hadoop.crypto.CipherSuite.convert(CipherSuite.java:90)
```

### Step6: More Arguments of Ctest4J
More arguments of Ctest4J can be found in the [Options](Options.md) document.