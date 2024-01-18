package edu.illinois;

import org.testng.IClassListener;
import org.testng.IInvokedMethodListener;
import org.testng.ITestListener;

import java.io.IOException;
import java.lang.annotation.AnnotationFormatError;

/**
 * Author: Shuai Wang
 * Date:  1/18/24
 */
public class CTestListener implements IClassListener, IInvokedMethodListener, ITestListener, CTestRunner {
    @Override
    public void initializeRunner(Object context) throws AnnotationFormatError, IOException {

    }


}
