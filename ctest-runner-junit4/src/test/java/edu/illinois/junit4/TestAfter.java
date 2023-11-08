package edu.illinois.junit4;

import edu.illinois.CTest;
import edu.illinois.CTestJUnit4Runner;
import edu.illinois.Configuration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * Author: Shuai Wang
 * Date:  11/7/23
 */
@RunWith(CTestJUnit4Runner.class)
public class TestAfter {
    Configuration conf = null;
    @Before
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

    @After
    public void after() {
        conf.get("after");
    }
}
