package org.apache.hadoop.testrunner;

import edu.illinois.ConfigMetadata;
import edu.illinois.ConfigTestRunner;
import org.apache.hadoop.conf.Configuration;
import org.junit.runner.RunWith;
import edu.illinois.ConfigTest;

import java.lang.management.ManagementFactory;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
@RunWith(ConfigTestRunner.class)
@ConfigMetadata(configClassName = "org.apache.hadoop.conf.Configuration",
        getConfigMethodSignature = "get(String,String):String",
        setConfigMethodSignature = "set(String):void")
public class TestRunner {
    static {
        System.out.println("TestRunner has been loaded!");
        // print thread id
        System.out.println("Thread id: " + Thread.currentThread().getId());
        ClassLoader loader = TestRunner.class.getClassLoader();
        if (loader != null) {
            System.out.println("Loaded by: " + loader.toString());
        } else {
            System.out.println("Loaded by Bootstrap ClassLoader");
        }
    }

    @ConfigTest("parameter1")
    public void test() {
        // Print the current JVM id
        System.out.println("JVM id: " + ManagementFactory.getRuntimeMXBean().getName());
        Configuration conf = new Configuration();
        conf.get("parameter1", "default");
    }
}
