package edu.illinois.junit5;

import edu.illinois.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/2/23
 */
@ExtendWith(CTestJunit5Extension.class)
@CTestClass(file = "src/test/resources/config.json")
public class FromFileTest {
    @CTest()
    public void test() {
        Configuration conf = new Configuration();
        conf.get("file-param1");
    }


}
