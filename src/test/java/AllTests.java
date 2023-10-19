import edu.illinois.ConfigTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Author: Shuai Wang
 * Date:  10/18/23
 */
@RunWith(ConfigTestSuite.class)
@Suite.SuiteClasses({
        TestNormalOne.class,
        TestNormalTwo.class,
})
public class AllTests {
}
