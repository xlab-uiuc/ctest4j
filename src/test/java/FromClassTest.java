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
@CTestClass({"class-parameter1", "class-parameter2"})
public class FromClassTest {
    @CTest
    public void test() {
        Configuration conf = new Configuration();
        conf.get("class-parameter1");
        conf.get("class-parameter2");
    }

    /**
     * The test would fail because it never uses "class-parameter2".
     */
    @CTest(expected = UnUsedConfigParamException.class)
    public void testShouldFail() {
        Configuration conf = new Configuration();
        conf.get("class-parameter1");
    }
}
