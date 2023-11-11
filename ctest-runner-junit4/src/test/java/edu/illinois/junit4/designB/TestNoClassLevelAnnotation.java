package edu.illinois.junit4.designB;

import edu.illinois.CTestJUnit4Runner2;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Author: Shuai Wang
 * Date:  11/10/23
 */
@RunWith(CTestJUnit4Runner2.class)
public class TestNoClassLevelAnnotation {
    @Test
    public void test(){
        System.out.println("test");
    }
}
