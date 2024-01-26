package edu.illinois.junit5;

import edu.illinois.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/2/23
 */
@ExtendWith(CTestJUnit5Extension.class)
@CTestClass(configMappingFile = "src/test/resources/FromFileTest.json")
public class FromFileTest {
    @CTest
    public void test() {
        Configuration conf = new Configuration();
        conf.get("class-parameter1");
        conf.get("class-parameter2");
        conf.get("method-parameter1");
        conf.get("method-parameter2");
    }

    @CTest(expected = UnUsedConfigParamException.class)
    public void testWouldFail() {
        Configuration conf = new Configuration();
        conf.get("class-parameter1");
        conf.get("class-parameter2");
        conf.get("method-parameter1");
        // Missing method-parameter2 so the test would fail
    }
}
