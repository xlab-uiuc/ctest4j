package edu.illinois.junit5;

import edu.illinois.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/2/23
 */
@ExtendWith(CTestJunit5Extension.class)
@CTestClass({"class-parameter1", "class-parameter2"})
public class FromClassTest {
    @CTest
    public void test() {
        Configuration conf = new Configuration();
        conf.get("class-parameter1");
        conf.get("class-parameter2");
    }
}

