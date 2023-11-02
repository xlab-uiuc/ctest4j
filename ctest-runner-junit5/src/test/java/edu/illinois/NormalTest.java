package edu.illinois;

import org.junit.jupiter.api.Test;

/**
 * Author: Shuai Wang
 * Date:  11/1/23
 */
public class NormalTest {
    @CTest
    public void test() {
        Configuration conf = new Configuration();
        conf.get("tracked-parameter");
    }

    @Test
    public void test2() {
        Configuration conf = new Configuration();
        conf.get("tracked-parameter");
    }
}
