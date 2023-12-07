package edu.illinois.junit4.designA;

import edu.illinois.Configuration;
import org.junit.Test;

/**
 * Author: Shuai Wang
 * Date:  10/18/23
 */
public class TestNormalTwo {
    @Test
    public void test2() {
        Configuration conf = new Configuration();
        conf.get("param1");
    }
}
