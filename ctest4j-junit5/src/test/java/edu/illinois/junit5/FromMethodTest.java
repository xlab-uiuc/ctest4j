package edu.illinois.junit5;

import edu.illinois.CTest;
import edu.illinois.CTestClass;
import edu.illinois.CTestJUnit5Extension;
import edu.illinois.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/1/23
 */
@ExtendWith(CTestJUnit5Extension.class)
@CTestClass
public class FromMethodTest {
    private Configuration conf;

    @BeforeEach
    public void setUp() {
        conf = new Configuration();
    }

    @CTest({"parameter1", "parameter2"})
    public void test() {
        String value1 = conf.get("parameter1");
        String value2 = conf.get("parameter2");
        System.out.println("In test: " + " parameter1: " + value1 + " parameter2: " + value2);
    }

    @CTest({"parameter1", "parameter2"})
    public void test3() {
        String value1 = conf.get("parameter1");
        String value2 = conf.get("parameter2");
        System.out.println("In test3: " + " parameter1: " + value1 + " parameter2: " + value2);
    }
}
