package edu.illinois.junit5;

import edu.illinois.*;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/2/23
 */
@ExtendWith(CTestJUnit5Extension.class)
@CTestClass(configMappingFile = "src/test/resources/ShouldFailTest.json")
public class ShouldFailTest {

    /**
     * The test would fail because it never uses "method-parameter2".
     */
    @CTest(value = {"method-parameter1", "method-parameter2"}, expected = UnUsedConfigParamException.class)
    public void testFailDueToMethodAnnotation() {
        Configuration conf = new Configuration();
        // From class annotation
        conf.get("class-parameter1");
        conf.get("class-parameter2");
        // From file path
        conf.get("file-param1");
        // From method annotation
        conf.get("method-parameter1");
        // Missing method-parameter2 so the test would fail
    }

    /**
     * The test would fail because it never uses "class-parameter2".
     */
    @CTest(value = {"method-parameter1", "method-parameter2"}, expected = UnUsedConfigParamException.class)
    public void testFailDueToClassAnnotation() {
        Configuration conf = new Configuration();
        // From class annotation
        conf.get("class-parameter1");
        // Missing class-parameter2 so the test would fail

        // From file path
        conf.get("file-param1");
        // From method annotation
        conf.get("method-parameter1");
        conf.get("method-parameter2");
    }

    /**
     * The test would fail because it never uses "file-param1".
     */
    @CTest(value = {"method-parameter1", "method-parameter2"}, expected = UnUsedConfigParamException.class)
    public void testFailDueToConfigFile() {
        Configuration conf = new Configuration();
        // From class annotation
        conf.get("class-parameter1");
        conf.get("class-parameter2");

        // From file path
        // Missing file-param1 so the test would fail

        // From method annotation
        conf.get("method-parameter1");
        conf.get("method-parameter2");
    }

    /**
     * The test would fail because it never uses "class-parameter2".
     */
    @CTest(expected = UnUsedConfigParamException.class)
    public void testShouldFail() {
        Configuration conf = new Configuration();
        conf.get("class-parameter1");
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
