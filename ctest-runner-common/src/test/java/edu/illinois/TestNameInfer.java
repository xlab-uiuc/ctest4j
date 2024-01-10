package edu.illinois;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Author: Shuai Wang
 * Date:  1/4/24
 */
public class TestNameInfer {
   @Test
   public void testNameInferAPI() {
       try {
           String name = Utils.inferTestClassAndMethodNameFromStackTrace();
           Assert.assertEquals("edu.illinois.TestNameInfer_testNameInferAPI", name);
       } catch (Exception e) {
           fail();
       }
   }
}
