package edu.illinois;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Author: Shuai Wang
 * Date:  1/4/24
 */
public class NameInferTest {
    @Test
    public void nameInferTest() {
        try {
            String name = Utils.inferTestClassAndMethodNameFromStackTrace();
            Assert.assertEquals("edu.illinois.NameInferTest_nameInferTest", name);
        } catch (Exception e) {
            fail();
        }
    }
}
