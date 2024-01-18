package edu.illinois;

import org.testng.*;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.AnnotationFormatError;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static edu.illinois.Names.CONFIG_SAVE_DIR;
import static edu.illinois.Names.TRACKING_LOG_PREFIX;

/**
 * Author: Shuai Wang
 * Date:  1/18/24
 */
public class CTestListener implements CTestRunner, IClassListener, IInvokedMethodListener, ITestListener {
    /** A list of configuration parameter name that all methods in the test class will use */
    protected Set<String> classLevelParameters;
    protected Map<String, Set<String>> methodLevelParametersFromMappingFile;
    protected final ConfigUsage configUsage = new ConfigUsage();
    /** The name of the test class */
    private String className;
    /** The name of the test method */
    private String methodName;
    /** The set of configuration parameters to be tested for runtime selection purpose. */
    protected final Set<String> selectionParams = new HashSet<>();

    @Override
    public void onBeforeClass(ITestClass testClass) {
        startTestClass(testClass.getName());
        try {
            initializeRunner(testClass);
        } catch (AnnotationFormatError | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod() || Options.mode == Modes.BASE) {
            return;
        }
        startTestMethod(method.getTestMethod().getTestClass().getName(), method.getTestMethod().getMethodName());
        ConfigTracker.startTestMethod(method.getTestMethod().getTestClass().getName(), method.getTestMethod().getMethodName());
        methodName = method.getTestMethod().getMethodName();
    }

    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (!method.isTestMethod() || Options.mode == Modes.BASE) {
            return;
        }
        ConfigUsage.bufferForUpdate(configUsage, className, methodName);
        if (Options.mode == Modes.CHECKING || Options.mode == Modes.DEFAULT) {
            // Retrieve method-level parameters
            CTest cTest = method.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(CTest.class);
            if (cTest != null) {
                boolean hasUnusedExpected = isUnUsedParamException(cTest.expected());
                boolean meetUnusedException = false;
                for (String param : getUnionMethodParameters(className, methodName, cTest.regex(),
                        classLevelParameters, methodLevelParametersFromMappingFile,
                        new HashSet<>(Arrays.asList(cTest.value())))) {
                    if (!ConfigTracker.isParameterUsed(className, methodName, param)) {
                        if (hasUnusedExpected) {
                            meetUnusedException = true;
                            break;
                        }
                        throw new UnUsedConfigParamException(param + " was not used during the test.");
                    }
                }
                if (hasUnusedExpected && !meetUnusedException) {
                    throw new RuntimeException("The test method " + methodName + " does not meet the expected exception " + cTest.expected());
                }
            }
            Test test = method.getTestMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class);
            if (test != null) {
                Log.INFO(TRACKING_LOG_PREFIX, className + "#" + methodName,
                        "uses configuration parameters: " + ConfigTracker.getAllUsedParams(className, methodName) + " and set parameters: " +
                                ConfigTracker.getAllSetParams(className, methodName));
                for (String param : getUnionMethodParameters(className, methodName, "",
                        classLevelParameters, methodLevelParametersFromMappingFile, new HashSet<>())) {
                    if (!ConfigTracker.isParameterUsed(className, methodName, param)) {
                        throw new UnUsedConfigParamException(param + " was not used during the test.");
                    }
                }
            }
        }
    }

    @Override
    public void onAfterClass(ITestClass testClass) {
        writeConfigUsageToJson(configUsage, new File(CONFIG_SAVE_DIR, className + ".json"));
    }

    @Override
    public Set<String> getUnionClassParameters(Set<String> classLevelParameters, String classConfigFile, String classRegex) throws IOException {
        classLevelParameters.addAll(getClasssParametersFromMappingFile(classConfigFile));
        if (!classRegex.isEmpty()) {
            classLevelParameters.addAll(getParametersFromRegex(classRegex));
        }
        return classLevelParameters;
    }

    @Override
    public void initializeRunner(Object context) throws AnnotationFormatError, IOException {
        ITestClass testClass = (ITestClass) context;
        className = testClass.getName();
        CTestClass cTestClass = testClass.getRealClass().getAnnotation(CTestClass.class);
        if (cTestClass == null) {
            // this class may extend from another class that has the @CTestClass annotation, check it
            Class<?> superClass = testClass.getRealClass().getSuperclass();
            if (superClass != null) {
                cTestClass = superClass.getAnnotation(CTestClass.class);
            }
            if (cTestClass == null) {
                throw new AnnotationFormatError("CTestClass annotation is not present in class " + className
                        + " or its super class.");
            }
        }

        // Get classLevel and methodLevel parameters from the mapping file
        Object[] values = initializeParameterSet(className, cTestClass.configMappingFile(), cTestClass.value(), cTestClass.regex());
        classLevelParameters = (Set<String>) values[0];
        methodLevelParametersFromMappingFile = (Map<String, Set<String>>) values[1];
        selectionParams.addAll(Utils.getSelectionParameters(
                System.getProperty(Names.CTEST_SELECTION_PARAMETER_PROPERTY)));
    }


    /**
     * Skip the test if it is not selected.
     */
    @Override
    public void onTestStart(ITestResult result) {
        if (isTestIgnored(result)) {
            throw new SkipException("CTestListener Skipping test: " + result.getMethod().getMethodName());
        }
    }

    private boolean isTestIgnored(ITestResult result) {
        if (selectionParams.isEmpty()) {
            return false;
        }
        String methodName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName();
        Set<String> usedParams = new HashSet<>();
        CTest cTest = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(CTest.class);
        if (cTest != null) {
            usedParams.addAll(getUnionMethodParameters(className, methodName, cTest.regex(),
                    classLevelParameters, methodLevelParametersFromMappingFile, new HashSet<>(Arrays.asList(cTest.value()))));
            return isCurrentTestIgnored(selectionParams, usedParams);
        }
        Test test = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Test.class);
        if (test != null) {
            usedParams.addAll(getUnionMethodParameters(className, methodName, "",
                    classLevelParameters, methodLevelParametersFromMappingFile, new HashSet<>()));
            return isCurrentTestIgnored(selectionParams, usedParams);
        }
        return false;
    }
}
