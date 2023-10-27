import edu.illinois.CTest;
import edu.illinois.CTestClass;
import edu.illinois.ConfigTestRunner;
import edu.illinois.UnUsedConfigParamException;
import org.junit.runner.RunWith;

/**
 * config.json requires "file-param1" must be used in all the test methods.
 * Author: Shuai Wang
 * Date:  10/17/23
 */
@RunWith(ConfigTestRunner.class)
@CTestClass(file = "src/test/resources/config.json")
public class FromFileTest {
    @CTest()
    public void test() {
        Configuration conf = new Configuration();
        conf.get("file-param1");
    }

    /**
     * The test would fail because it never uses "file-param1".
     */
    @CTest(expected = UnUsedConfigParamException.class)
    public void testWouldFail() {
        Configuration conf = new Configuration();
        conf.get("file-param2");
    }
}
