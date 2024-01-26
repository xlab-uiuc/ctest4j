package edu.illinois;

import org.junit.Assert;
import org.junit.Test;

import java.lang.management.ManagementFactory;

/**
 * Author: Shuai Wang
 * Date:  1/11/24
 */
public class PTidTest {
    @Test
    public void testPTid() {
        // Current PID
        long pid = ManagementFactory.getRuntimeMXBean().getPid();
        // Current thread's ID
        long threadId = Thread.currentThread().getId();
        Assert.assertEquals(pid + Names.PID_TID_SEPARATOR + threadId, Utils.getPTid());
    }

    @Test
    public void testPTidProperty() {
        Utils.setCurTestClassNameToPTid(Utils.getPTid(), "PTidTest");
        Assert.assertEquals("PTidTest", Utils.getCurTestClassNameFromPTid(Utils.getPTid()));
        Utils.setCurTestFullNameToPTid(Utils.getPTid(), "PTidTest", "testPTidProperty");
        Assert.assertEquals("PTidTest" + Names.TEST_CLASS_METHOD_SEPARATOR +"testPTidProperty",
                Utils.getCurTestFullNameFromPTid(Utils.getPTid()));
    }
}
