import edu.illinois.ConfigTest;
import edu.illinois.ConfigTestRunner;
import edu.illinois.UnUsedConfigParamException;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Author: Shuai Wang
 * Date:  10/15/23
 */
@RunWith(ConfigTestRunner.class)
public class FromMethodTest {
    private Configuration conf;
    @Before
    public void setUp() {
        conf = new Configuration();
    }
    @ConfigTest({"parameter1", "parameter2"})
    public void test() {
        String value1 = conf.get("parameter1");
        String value2 = conf.get("parameter2");
        System.out.println("In test: " + " parameter1: " + value1 + " parameter2: " + value2);
    }

    @ConfigTest(value = {"parameter3", "parameter4"}, expected = UnUsedConfigParamException.class)
    public void test2() {
        String value3 = conf.get("parameter3");
        String file_value1 = conf.get("file-param1");
        System.out.println("In test2: " + " parameter3: " + value3 + " file-param1: " + file_value1);
        // This test should fail because it never uses parameter4.
    }

    @ConfigTest({"parameter1", "parameter2"})
    public void test3() {
        String value1 = conf.get("parameter1");
        String value2 = conf.get("parameter2");
        System.out.println("In test3: " + " parameter1: " + value1 + " parameter2: " + value2);
    }
}