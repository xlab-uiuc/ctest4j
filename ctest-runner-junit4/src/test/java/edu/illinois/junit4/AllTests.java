package edu.illinois.junit4;

import edu.illinois.CTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Author: Shuai Wang
 * Date:  10/18/23
 */
@RunWith(CTestSuite.class)
@Suite.SuiteClasses({
        TestNormalOne.class,
        TestNormalTwo.class,
})
public class AllTests {
}
