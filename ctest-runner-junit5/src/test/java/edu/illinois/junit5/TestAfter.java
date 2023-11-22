package edu.illinois.junit5;

import edu.illinois.CTest;
import edu.illinois.CTestClass;
import edu.illinois.CTestJunit5Extension;
import edu.illinois.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/7/23
 */
@ExtendWith(CTestJunit5Extension.class)
@CTestClass
public class TestAfter {
    Configuration conf = null;

    @BeforeEach
    public void before() {
        conf = new Configuration();
    }

    @CTest({"param1", "after"})
    public void test() {
        conf.get("param1");
    }

    @CTest({"param2", "after"})
    public void test2() {
        conf.get("param2");
    }

    @AfterEach
    public void after() {
        conf.get("after");
    }

}
