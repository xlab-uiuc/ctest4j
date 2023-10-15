import edu.illinois.ConfigTest;
import edu.illinois.ConfigTestRunner;
import edu.illinois.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Author: Shuai Wang
 * Date:  10/15/23
 */
@RunWith(ConfigTestRunner.class)
public class ExampleTest {
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

    @ConfigTest({"parameter3", "parameter4"})
    public void test2() {
        String value3 = conf.get("parameter3");
        System.out.println("In test2: " + " parameter3: " + value3);
        // This test should fail because it never uses parameter4.
    }

    @Test
    public void testTrack() {
        String value4 = conf.get("parameter-tracked");
    }
}