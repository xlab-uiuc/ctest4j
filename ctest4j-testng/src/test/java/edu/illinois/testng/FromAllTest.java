package edu.illinois.testng;

import edu.illinois.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Author: Shuai Wang
 * Date:  11/2/23
 */
@Listeners(CTestListener.class)
@CTestClass(configMappingFile = "src/test/resources/FromAllTest.json",
        value = {"parameter1", "parameter2"}, regex = "regex-parameter(1|2)")
public class FromAllTest {
    Configuration conf = null;
    @BeforeClass
    public static void beforeClass() {
        Configuration conf = new Configuration();
        conf.get("beforeClass");
    }

    @BeforeMethod
    public void before() {
        conf = new Configuration();
        conf.get("before");
    }

    @Test
    @CTest
    public void testTestAnnotation() {
        conf.get("testTestAnnotation");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @Test
    @CTest
    public void testCTestAnnotation() {
        conf.get("testCTestAnnotation");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @Test
    @CTest(expected = UnUsedConfigParamException.class)
    public void testTestAnnotationFail() {
        conf.get("testTestAnnotationFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        // "never-used" from the mapping file is never used so the test would fail
    }

    @Test
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

    @Test
    @CTest(expected = UnUsedConfigParamException.class)
    public void testTestClassAnnotationFail() {
        conf.get("testTestClassAnnotationFail");
        // "parameter1" is never used so the test would fail
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @Test
    @CTest(expected = UnUsedConfigParamException.class)
    public void testCTestClassAnnotationFail() {
        conf.get("testCTestClassAnnotationFail");
        // "parameter1" is never used so the test would fail
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @Test
    @CTest(expected = UnUsedConfigParamException.class)
    public void testTestClassRegexFail() {
        conf.get("testTestClassRegexFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        // "regex-parameter2" is never used so the test would fail
    }

    @Test
    @CTest(expected = UnUsedConfigParamException.class)
    public void testCTestClassRegexFail() {
        conf.get("testCTestClassRegexFail");
        conf.get("parameter1");
        conf.get("parameter2");
        // "regex-parameter1" is never used so the test would fail
        conf.get("regex-parameter2");
    }

    @Test
    @CTest(value = {"method-parameter"}, expected = UnUsedConfigParamException.class)
    public void testCTestMethodAnnotationFail() {
        conf.get("testCTestMethodAnnotationFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        // "method-parameter" is never used so the test would fail
    }

    @Test
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
