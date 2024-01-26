import edu.illinois.TestClassSelector;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  1/8/24
 */
public class SelectorTest {

    @Test
    public void testTestSelector() {
        URL url = this.getClass().getClassLoader().getResource("mapping");
        assert url != null;
        File mappingDir = new File(url.getFile());
        testSelector(new String[] {"California"}, new String[]{"TestCalifornia"}, mappingDir);
        testSelector(new String[] {"Illinois", "California"}, new String[]{"TestCalifornia", "TestIllinois"}, mappingDir);
        testSelector(new String[] {"Illinois", "Chicago"}, new String[]{"TestIllinois"}, mappingDir);
        testSelector(new String[] {"LA", "Riverside"}, new String[]{"TestCalifornia"}, mappingDir);
        testSelector(new String[] {"LA", "Chicago"}, new String[]{"TestIllinois", "TestCalifornia"}, mappingDir);
    }

    private void testSelector(String[] param, String[] expected, File mappingDir) {
        Set<String> paramSet = new HashSet<>(List.of(param));
        TestClassSelector selector = new TestClassSelector(mappingDir, paramSet);
        Set<String> selected = selector.select();
        Set<String> expectedSet = new HashSet<>(List.of(expected));
        Assert.assertEquals(expectedSet, selected);
    }
}
