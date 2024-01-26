package edu.illinois;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  10/31/23
 */
public class TestSetGetTracker {
    Configuration conf = null;
    String className = "edu.illinois.TestSetGetTracker";
    @Before
    public void setUp() {
        conf = new Configuration();
    }

    @Test
    public void testGetTracker() {
        Utils.setCurTestFullNameToPTid(Utils.getPTid(), className, "testGetTracker");
        conf.get("param1");
        conf.get("param2");
        Set<String> expected = new HashSet<>();
        expected.add("param1");
        expected.add("param2");
        Assert.assertEquals(expected, ConfigTracker.getAllUsedParams(className, "testGetTracker"));
    }

    /**
     * Test the setter tracker
     */
    @Test
    public void testSetTracker() {
        Utils.setCurTestFullNameToPTid(Utils.getPTid(), className, "testSetTracker");
        conf.set("param1", "value1");
        conf.set("param2", "value2");
        conf.set("param1", "new-value1");
        conf.get("param3");
        conf.get("param4");
        Set<String> expected = new HashSet<>();
        expected.add("param1");
        expected.add("param2");
        Assert.assertEquals(expected, ConfigTracker.getAllSetParams(className, "testSetTracker"));
    }

    @Test
    public void testGetInjectParams() {
        String value = ConfigTracker.getConfigParamValue("param1", "not-injected");
        System.out.println("value: " + value);
    }
}
