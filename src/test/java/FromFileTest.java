import edu.illinois.ConfigTest;
import edu.illinois.ConfigTestClass;
import edu.illinois.ConfigTestRunner;
import edu.illinois.UnUsedConfigParamException;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * config.json requires "file-param1" must be used in all the test methods.
 * Author: Shuai Wang
 * Date:  10/17/23
 */
@RunWith(ConfigTestRunner.class)
@ConfigTestClass(file = "src/test/resources/config.json")
public class FromFileTest {
    @ConfigTest()
    public void test() {
        Configuration conf = new Configuration();
        conf.get("file-param1");
    }

    /**
     * The test would fail because it never uses "file-param1".
     */
    @ConfigTest()
    @Test(expected = UnUsedConfigParamException.class)
    public void testWouldFail() {
        Configuration conf = new Configuration();
        conf.get("file-param2");
    }
}
