package edu.illinois.junit5;

import edu.illinois.CTest;
import edu.illinois.CTestClass;
import edu.illinois.CTestJUnit5Extension;
import edu.illinois.Configuration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/7/23
 */
@ExtendWith(CTestJUnit5Extension.class)
@CTestClass
public class TestBeforeAllAndBefore {
    Configuration conf = null;
    @BeforeAll
    public static void setUp() {
        Configuration conf = new Configuration();
        conf.get("beforeClass");
    }

    @BeforeEach
    public void setUp2() {
        conf = new Configuration();
        conf.get("before");
    }

    @CTest(value = {"beforeClass", "before", "param1"})
    public void test() {
        conf.get("param1");
    }

    @CTest(value = {"beforeClass", "before", "param2"})
    public void test2() {
        conf.get("param2");
    }
}
