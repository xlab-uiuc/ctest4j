package edu.illinois.junit5;

import edu.illinois.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/2/23
 */
@ExtendWith(CTestJunit5Extension.class)
@CTestClass(value = {"class-parameter1", "class-parameter2"}, file = "src/test/resources/config.json")
public class FromAllTest {
    @CTest({"method-parameter1", "method-parameter2"})
    public void test() {
        Configuration conf = new Configuration();
        // From class annotation
        conf.get("class-parameter1");
        conf.get("class-parameter2");
        // From file path
        conf.get("file-param1");
        // From method annotation
        conf.get("method-parameter1");
        conf.get("method-parameter2");
    }
}
