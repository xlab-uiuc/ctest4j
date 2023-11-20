package edu.illinois.junit4.designA;

import edu.illinois.*;
import org.junit.runner.RunWith;

/**
 * Author: Shuai Wang
 * Date:  10/17/23
 */
@RunWith(CTestJUnit4Runner.class)
@CTestClass(value = {"class-parameter1", "class-parameter2"}, regex = "regex-parameter(1|2)", configMappingFile = "src/test/resources/config.json")
public class FromAllTest {
    @CTest(value = {"method-parameter1", "method-parameter2"}, regex = "method-regex(1|2)")
    public void test() {
        Configuration conf = new Configuration();
        // From class annotation
        conf.get("class-parameter1");
        conf.get("class-parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        // From file path
        conf.get("file-param1");
        // From method annotation
        conf.get("method-parameter1");
        conf.get("method-parameter2");
        conf.get("method-regex1");
        conf.get("method-regex2");
    }

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

    @CTest(value = {"method-parameter1", "method-parameter2"}, expected = UnUsedConfigParamException.class)
    public void testFailDueToClassRegex() {
        Configuration conf = new Configuration();
        // From class annotation
        conf.get("class-parameter1");
        conf.get("class-parameter2");
        conf.get("regex-parameter1");
        // The test would fail because it never uses "regex-parameter2".

        // From file path
        conf.get("file-param1");
        // From method annotation
        conf.get("method-parameter1");
        conf.get("method-parameter2");
    }

    @CTest(value = {"method-parameter1", "method-parameter2"}, regex = "method-regex(1|2)", expected = UnUsedConfigParamException.class)
    public void testFailDueToMethodRegex() {
        Configuration conf = new Configuration();
        // From class annotation
        conf.get("class-parameter1");
        conf.get("class-parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        // From file path
        conf.get("file-param1");
        // From method annotation
        conf.get("method-parameter1");
        conf.get("method-parameter2");

        // The test would fail because it never uses "method-regex2".
        conf.get("method-regex1");
    }

}
