package edu.illinois.junit4;

import edu.illinois.CTest;
import edu.illinois.CTestJUnit4Runner;
import edu.illinois.Configuration;
import edu.illinois.UnUsedConfigParamException;
import org.junit.runner.RunWith;

/**
 * Author: Shuai Wang
 * Date:  11/8/23
 */
@RunWith(CTestJUnit4Runner.class)
public class VmVmTest {
    public static String VmVmStaticField = null;

    // This ctest sets the value of configuration parameter "static-field" to VmVmStaticField
    @CTest({"static-field"})
    public void ctestSetStaticField() {
        Configuration conf = new Configuration();
        VmVmStaticField = conf.get("static-field", "VmVm");
    }

    // This ctest actually also used the configuration parameter "static-field"
    @CTest(value = {"static-field"}, expected = UnUsedConfigParamException.class)
    public void ctestGetStaticField() {
        System.out.println(VmVmStaticField);
    }
}
