package edu.illinois.junit4;

import edu.illinois.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Author: Shuai Wang
 * Date:  10/19/23
 */
@RunWith(CTestRunner.class)
public class TestConfigCaller {
    Configuration conf = null;
    @Before
    public void setUp() {
        conf = new Configuration();
    }

    @Test
    public void testFindCaller() {
        System.out.println(conf.get("param1"));
    }
}
