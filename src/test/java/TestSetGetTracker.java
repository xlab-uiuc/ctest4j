import edu.illinois.CTest;
import edu.illinois.CTestRunner;
import edu.illinois.ConfigTracker;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  10/31/23
 */
@RunWith(CTestRunner.class)
public class TestSetGetTracker {
    Configuration conf = null;
    @Before
    public void setUp() {
        conf = new Configuration();
    }

    @Test
    public void testGetTracker() {
        conf.get("param1");
        conf.get("param2");
        Set<String> expected = new HashSet<>();
        expected.add("param1");
        expected.add("param2");
        Assert.assertEquals(expected, ConfigTracker.getUsedParams());
    }

    /**
     * Test the setter tracker
     */
    @Test
    public void testSetTracker() {
        conf.set("param1", "value1");
        conf.set("param2", "value2");
        conf.set("param1", "new-value1");
        conf.get("param3");
        conf.get("param4");
        Set<String> expected = new HashSet<>();
        expected.add("param1");
        expected.add("param2");
        Assert.assertEquals(expected, ConfigTracker.getSetParams());
    }
}
