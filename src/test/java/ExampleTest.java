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
        String value1 = conf.get("parameter1");
        String value2 = conf.get("parameter2");
        System.out.println("In test: " + " parameter1: " + value1 + " parameter2: " + value2);
    }
}
