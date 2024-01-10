package edu.illinois.junit5;

import edu.illinois.CTestClass;
import edu.illinois.CTestJUnit5Extension;
import edu.illinois.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/1/23
 */
@ExtendWith(CTestJUnit5Extension.class)
@CTestClass
public class NormalTest {
    @Test
    public void test() {
        Configuration conf = new Configuration();
        conf.get("tracked-parameter1");
    }

    @Test
    public void test2() {
        Configuration conf = new Configuration();
        conf.get("tracked-parameter2");
    }
}
