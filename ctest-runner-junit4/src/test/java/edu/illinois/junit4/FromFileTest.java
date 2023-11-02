package edu.illinois.junit4;

import edu.illinois.*;
import org.junit.runner.RunWith;

/**
 * config.json requires "file-param1" must be used in all the test methods.
 * Author: Shuai Wang
 * Date:  10/17/23
 */
@RunWith(CTestJUnit4Runner.class)
@CTestClass(file = "src/test/resources/config.json")
public class FromFileTest {
    @CTest()
    public void test() {
        Configuration conf = new Configuration();
        conf.get("file-param1");
    }

    /**
     * The test would fail because it never uses "file-param1".
     */
    @CTest(expected = UnUsedConfigParamException.class)
    public void testWouldFail() {
        Configuration conf = new Configuration();
        conf.get("file-param2");
    }
}
