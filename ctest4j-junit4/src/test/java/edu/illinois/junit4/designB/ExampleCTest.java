package edu.illinois.junit4.designB;

import edu.illinois.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Author: Shuai Wang
 * Date:  11/29/23
 */
@RunWith(CTestJUnitRunner.class)
@CTestClass(
        value = {"parameter1", "parameter2"},
        regex = "regex-parameter(1|2)",
        configMappingFile = "src/test/resources/designB/ExampleCTest.json")

public class ExampleCTest {
    Configuration conf = null;

    @BeforeClass
    public static void beforeClass() {
        Configuration conf = new Configuration();
        conf.get("beforeClass-parameter");
    }

    @Before
    public void before() {
        conf = new Configuration();
        conf.get("before-parameter");
    }

    @Test
    public void testTestAnnotation() {
        conf.get("testTestAnnotation");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }

    @Test(expected = UnUsedConfigParamException.class)
    public void testTestClassAnnotationFail() {
        conf.get("testTestClassAnnotationFail");
        // Missing parameter1 so the test would fail
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

    @CTest(value = {"method-parameter"}, expected = UnUsedConfigParamException.class)
    public void testCTestMethodAnnotationFail() {
        conf.get("testCTestMethodAnnotationFail");
        conf.get("parameter1");
        conf.get("parameter2");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
        // "method-parameter" is never used so the test would fail
    }
}
