package edu.illinois.junit4.designB;

import edu.illinois.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Author: Shuai Wang
 * Date:  11/10/23
 */
@RunWith(CTestJUnit4Runner2.class)
@CTestClass(configMappingFile = "src/test/resources/designB/FromAllTest.json")
public class FromAllTest {
    Configuration conf = null;
    @BeforeClass
    public static void beforeClass() {
        Configuration conf = new Configuration();
        conf.get("beforeClass");
    }

    @Before
    public void before() {
        conf = new Configuration();
        conf.get("before");
    }

    @Test
    public void testTestAnnotation() {
        conf.get("testTestAnnotation");
    }

    @CTest
    public void testCTestAnnotation() {
        conf.get("testCTestAnnotation");
    }

    @Test(expected = UnUsedConfigParamException.class)
    public void testTestAnnotationFail() {
        conf.get("testTestAnnotationFail");
    }

    @CTest(expected = UnUsedConfigParamException.class)
    public void testCTestAnnotationFail() {
        Configuration conf = new Configuration();
        conf.get("testCTestAnnotationFail");
    }
}