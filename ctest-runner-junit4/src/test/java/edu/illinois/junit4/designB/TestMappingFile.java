package edu.illinois.junit4.designB;

import edu.illinois.CTest;
import edu.illinois.CTestClass;
import edu.illinois.CTestJUnit4Runner2;
import edu.illinois.Configuration;
import org.junit.runner.RunWith;


/**
 * Author: Shuai Wang
 * Date:  11/15/23
 */
@RunWith(CTestJUnit4Runner2.class)
@CTestClass(value = {"class-param1"}, configMappingFile = "fake-file", regex = "regex-parameter(1|2)")
public class TestMappingFile {

    @CTest
    public void testFakeMappingFile() {
        Configuration conf = new Configuration();
        conf.get("class-param1");
        conf.get("regex-parameter1");
        conf.get("regex-parameter2");
    }
}
