package edu.illinois;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static edu.illinois.Names.TRACKING_LOG_PREFIX;
import static edu.illinois.Options.saveUsedParamToFile;
import static edu.illinois.Utils.getTestMethodFullName;

/**
 * Author: Shuai Wang
 * Date:  11/1/23
 */
public class CTestJunit5Extension implements CTestRunner, BeforeAllCallback,
        BeforeEachCallback, AfterEachCallback {

    /** A list of configuration parameter name that all methods in the test class will use */
    private Set<String> classLevelParameters = new HashSet<>();
    /** The name of the test class */
    private String className;
    /** The name of the test method */
    private String methodName;

    /**
     * Initialize the class-level parameters.
     * @param extensionContext
     * @throws Exception
     */
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        // Retrieve class-level parameters
        className = extensionContext.getRequiredTestClass().getName();
        CTestClass cTestClass = extensionContext.getRequiredTestClass().getAnnotation(CTestClass.class);
        if (cTestClass != null) {
            try {
                classLevelParameters = getAllClassParameters(new HashSet<>(Arrays.asList(cTestClass.value())), cTestClass.file());
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse configuration file from class " + className + " Annotation", e);
            }
        } else {
            classLevelParameters = new HashSet<>();
        }
        ConfigTracker.setCurrentTestClassName(className);
    }

    /**
     * Clean the configuration tracker.
     * @param extensionContext
     * @throws Exception
     */
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        if (Options.mode == Modes.BASE) {
            return;
        }
        ConfigTracker.startTestMethod();
        methodName = extensionContext.getRequiredTestMethod().getName();
    }

    /**
     * Check if the test method uses all the parameters specified in the @CTest and @CTestClass annotation.
     * @param extensionContext
     * @throws Exception
     */
    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        if (Options.mode == Modes.BASE) {
            return;
        }
        // Retrieve method-level parameters
        CTest cTest = extensionContext.getRequiredTestMethod().getAnnotation(CTest.class);
        if (cTest != null) {
            try {
                if (Options.mode == Modes.CHECKING || Options.mode == Modes.DEFAULT) {
                    Set<String> params = getAllMethodParameters(className, methodName,
                            cTest.file(), new HashSet<>(Arrays.asList(cTest.value())), classLevelParameters);
                    for (String param : params) {
                        if (!ConfigTracker.isParameterUsed(param)) {
                            Class<? extends Throwable> expected = cTest.expected();
                            if (expected != CTest.None.class) {
                                if (expected.isAssignableFrom(UnUsedConfigParamException.class)) {
                                    return;
                                } else {
                                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse configuration file from method " + methodName + " Annotation", e);
            }
        }
        Test test = extensionContext.getRequiredTestMethod().getAnnotation(Test.class);
        if (test != null) {
            Log.INFO(TRACKING_LOG_PREFIX, className + "#" + methodName,
                    "uses configuration parameters: " + ConfigTracker.getAllUsedParams() + " and set parameters: " +
                            ConfigTracker.getAllSetParams());
            if (saveUsedParamToFile) {
                ConfigTracker.writeConfigToFile(getTestMethodFullName(className, methodName));
            }
        }
    }
}
