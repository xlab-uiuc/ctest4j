package edu.illinois.junit4;

import edu.illinois.Configuration;
import org.junit.Test;

/**
 * Author: Shuai Wang
 * Date:  10/17/23
 */
public class TestNormalOne {
    @Test
    public void normalTest() {
        Configuration conf = new Configuration();
        conf.get("tracked-parameter");
    }
}
