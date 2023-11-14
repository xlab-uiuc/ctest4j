package edu.illinois;

import org.junit.Test;
import org.junit.Assert;
import java.util.ArrayList;

/**
 * Author: Shuai Wang
 * Date:  11/14/23
 */
public class TestConfigRegex {
    @Test
    public void testGetParameters() {
        // The "." here does not mean the dot in regex, it means the dot in the parameter name.
        String configRegex = "ctest.conf.(param1|param2|param3)|ctest.thread.(number|size)|ctest.test.(test1|test2)";
        ConfigRegex configRegexObj = new ConfigRegex(configRegex);
        ArrayList<String> parameters = configRegexObj.getParameters();
        String [] expectedParameters = {"ctest.conf.param1", "ctest.conf.param2", "ctest.conf.param3",
                "ctest.thread.number", "ctest.thread.size", "ctest.test.test1", "ctest.test.test2"};
        Assert.assertArrayEquals(expectedParameters, parameters.toArray());
    }

    @Test
    public void testOneParameters() {
        String configRegex = "ctest.conf.(param1)";
        ConfigRegex configRegexObj = new ConfigRegex(configRegex);
        ArrayList<String> parameters = configRegexObj.getParameters();
        String [] expectedParameters = {"ctest.conf.param1"};
        Assert.assertArrayEquals(expectedParameters, parameters.toArray());
    }

    @Test
    public void testTwoParameters() {
        String configRegex1 = "ctest.conf.(param1|param2)";
        ConfigRegex configRegexObj1 = new ConfigRegex(configRegex1);
        ArrayList<String> parameters1 = configRegexObj1.getParameters();
        String [] expectedParameters1 = {"ctest.conf.param1", "ctest.conf.param2"};
        Assert.assertArrayEquals(expectedParameters1, parameters1.toArray());

        String configRegex2 = "ctest.conf.param1|ctest.conf.param2";
        ConfigRegex configRegexObj2 = new ConfigRegex(configRegex2);
        ArrayList<String> parameters2 = configRegexObj2.getParameters();
        String [] expectedParameters2 = {"ctest.conf.param1", "ctest.conf.param2"};
        Assert.assertArrayEquals(expectedParameters2, parameters2.toArray());
    }

    @Test
    public void testComplexRegex() {
        String configRegex = "ctest.conf.(param1|anotherconf.param1|anotherconf.param2)";
        ConfigRegex configRegexObj = new ConfigRegex(configRegex);
        ArrayList<String> parameters = configRegexObj.getParameters();
        String [] expectedParameters = {"ctest.conf.param1", "ctest.conf.anotherconf.param1", "ctest.conf.anotherconf.param2"};
        Assert.assertArrayEquals(expectedParameters, parameters.toArray());
    }
}
