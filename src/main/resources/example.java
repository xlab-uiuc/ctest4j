import edu.illinois.ConfigMetadata;
import edu.illinois.ConfigTestRunner;
import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import edu.illinois.ConfigTest;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
@RunWith(ConfigTestRunner.class)
public class TestRunner {
    private Configuration conf = null;
    @Before
    public void setUp() {
        conf = new Configuration();
    }

    @ConfigTest({"parameter1", "parameter2"})
    public void configTestShouldFail() {
        conf.get("parameter1", "default");
    }

    @ConfigTest({"parameter1", "parameter2", "parameter3"})
    public void configTestShouldPass() {
        conf.get("parameter1", "default");
        conf.get("parameter2");
        conf.set("parameter3", "value3");
    }

    @Test
    public void normalTest() {
        System.out.println("This is a normal test.");
    }
}