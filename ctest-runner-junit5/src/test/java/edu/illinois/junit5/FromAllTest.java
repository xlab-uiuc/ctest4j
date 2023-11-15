package edu.illinois.junit5;

import edu.illinois.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/2/23
 */
@ExtendWith(CTestJunit5Extension.class)
@CTestClass(configMappingFile = "src/test/resources/FromAllTest.json",
        value = {"parameter1", "parameter2"}, regex = "regex-parameter(1|2)")
public class FromAllTest {
    Configuration conf = null;
    @BeforeAll
    public static void beforeClass() {
        Configuration conf = new Configuration();
        conf.get("beforeClass");
    }

    @BeforeEach
    public void before() {
        conf = new Configuration();
        conf.get("before");
    }

    @CTest
    public void testTestAnnotation() {
        conf.get("testTestAnnotation");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @CTest
    public void testCTestAnnotation() {
        conf.get("testCTestAnnotation");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @CTest(expected = UnUsedConfigParamException.class)
    public void testTestAnnotationFail() {
        conf.get("testTestAnnotationFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        // "never-used" from the mapping file is never used so the test would fail
    }

    @CTest(expected = UnUsedConfigParamException.class)
    public void testCTestAnnotationFail() {
        Configuration conf = new Configuration();
        conf.get("testCTestAnnotationFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        // "never-used" from the mapping file is never used so the test would fail
    }

    @CTest(expected = UnUsedConfigParamException.class)
    public void testTestClassAnnotationFail() {
        conf.get("testTestClassAnnotationFail");
        // "parameter1" is never used so the test would fail
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @CTest(expected = UnUsedConfigParamException.class)
    public void testCTestClassAnnotationFail() {
        conf.get("testCTestClassAnnotationFail");
        // "parameter1" is never used so the test would fail
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @CTest(expected = UnUsedConfigParamException.class)
    public void testTestClassRegexFail() {
        conf.get("testTestClassRegexFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        // "regex-parameter2" is never used so the test would fail
    }

    @CTest(expected = UnUsedConfigParamException.class)
    public void testCTestClassRegexFail() {
        conf.get("testCTestClassRegexFail");
        conf.get("parameter1");
        conf.get("parameter2");
        // "regex-parameter1" is never used so the test would fail
        conf.get("regex-parameter2");
    }

    @CTest(value = {"method-parameter"}, expected = UnUsedConfigParamException.class)
    public void testCTestMethodAnnotationFail() {
        conf.get("testCTestMethodAnnotationFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        // "method-parameter" is never used so the test would fail
    }

    @CTest(value = {"method-parameter"}, regex = "method-regex(1|2)", expected = UnUsedConfigParamException.class)
    public void testCTestMethodRegexFail() {
        conf.get("testCTestMethodRegexFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        conf.get("method-parameter");
        conf.get("method-regex1");
        // "method-regex2" is never used so the test would fail
    }
}
