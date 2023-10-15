import edu.illinois.ConfigTest;
import edu.illinois.ConfigTestRunner;
import edu.illinois.Configuration;
import org.junit.runner.RunWith;

/**
 * Author: Shuai Wang
 * Date:  10/15/23
 */
@RunWith(ConfigTestRunner.class)
public class ExampleTest {
    @ConfigTest({"parameter1", "parameter2"})
    public void test() {
        Configuration conf = new Configuration();
        conf.get("parameter1");
        conf.set("parameter2", "value2");
    }
}
