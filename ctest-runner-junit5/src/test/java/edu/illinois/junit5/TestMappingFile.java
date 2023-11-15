package edu.illinois.junit5;

import edu.illinois.CTest;
import edu.illinois.CTestClass;
import edu.illinois.CTestJunit5Extension;
import edu.illinois.Configuration;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Author: Shuai Wang
 * Date:  11/15/23
 */
@ExtendWith(CTestJunit5Extension.class)
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