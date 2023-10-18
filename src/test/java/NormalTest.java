import edu.illinois.ConfigTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Author: Shuai Wang
 * Date:  10/17/23
 */
@RunWith(ConfigTestRunner.class)
public class NormalTest {
    @Test
    public void normalTest() {
        Configuration conf = new Configuration();
        conf.get("tracked-parameter");
    }
}
