package edu.illinois;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.AnnotationFormatError;
import java.util.*;

import static edu.illinois.Names.*;

/**
 * Author: Shuai Wang
 * Date:  11/1/23
 */
public class CTestJunit5Extension implements CTestRunner, BeforeAllCallback,
        BeforeEachCallback, AfterEachCallback, AfterAllCallback {
    /** A list of configuration parameter name that all methods in the test class will use */
    protected Set<String> classLevelParameters;
    protected Map<String, Set<String>> methodLevelParametersFromMappingFile;
    protected final ConfigUsage configUsage = new ConfigUsage();
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
        initializeRunner(extensionContext);
        ConfigTracker.startTestClass();
    }

    /**
     * Clean the configuration tracker.
     * @param extensionContext
     * @throws Exception
     */
    @Override
    public void beforeEach(ExtensionContext extensionContext) {
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
    public void afterEach(ExtensionContext extensionContext) throws IOException {
        if (Options.mode == Modes.BASE) {
            return;
        }
        ConfigTracker.updateConfigUsage(configUsage, methodName);
        // Retrieve method-level parameters
        CTest cTest = extensionContext.getRequiredTestMethod().getAnnotation(CTest.class);
        if (cTest != null) {
            try {
                if (Options.mode == Modes.CHECKING || Options.mode == Modes.DEFAULT) {
                    boolean hasUnusedExpected = isUnUsedParamException(cTest.expected());
                    boolean meetUnusedException = false;
                    for (String param : getUnionMethodParameters(methodName, cTest.regex(),
                            new HashSet<>(Arrays.asList(cTest.value())))) {
                        if (!ConfigTracker.isParameterUsed(param)) {
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
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse configuration file from method " + methodName + " Annotation", e);
            }
        }
        Test test = extensionContext.getRequiredTestMethod().getAnnotation(Test.class);
        if (test != null) {
            Log.INFO(TRACKING_LOG_PREFIX, className + "#" + methodName,
                    "uses configuration parameters: " + ConfigTracker.getAllUsedParams() + " and set parameters: " +
                            ConfigTracker.getAllSetParams());
            for (String param : getUnionMethodParameters(methodName, "", new HashSet<>())) {
                if (!ConfigTracker.isParameterUsed(param)) {
                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                }
            }
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
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

    public Set<String> getUnionMethodParameters(String methodName, String methodRegex,
                                                Set<String> methodLevelParamsFromAnnotation) throws IOException {
        Set<String> allMethodLevelParameters = new HashSet<>();
        // Retrieve class-level parameters if present
        allMethodLevelParameters.addAll(this.classLevelParameters);
        // Retrieve method-level parameters if present
        Set<String> methodLevelParameters = this.methodLevelParametersFromMappingFile.get(methodName);
        if (methodLevelParameters != null) {
            allMethodLevelParameters.addAll(methodLevelParameters);
        }
        allMethodLevelParameters.addAll(methodLevelParamsFromAnnotation);

        // Retrieve regex-level parameters if present
        if (!methodRegex.isEmpty()) {
            allMethodLevelParameters.addAll(getParametersFromRegex(methodRegex));
        }
        return allMethodLevelParameters;
    }


    @Override
    public void initializeRunner(Object context) throws AnnotationFormatError, IOException {
        ExtensionContext extensionContext = (ExtensionContext) context;
        // Retrieve class-level parameters
        className = extensionContext.getRequiredTestClass().getName();
        // Set the current test class name
        ConfigTracker.setCurrentTestClassName(className);
        CTestClass cTestClass = extensionContext.getRequiredTestClass().getAnnotation(CTestClass.class);
        if (cTestClass == null) {
            // this class may extend from another class that has the @CTestClass annotation, check it
            Class<?> superClass = extensionContext.getRequiredTestClass().getSuperclass();
            if (superClass != null) {
                cTestClass = superClass.getAnnotation(CTestClass.class);
            }
            if (cTestClass == null) {
                throw new AnnotationFormatError("CTestClass annotation is not present in class " + className
                        + " or its super class.");
            }
        }

        // Get classLevel and methodLevel parameters from the mapping file
        Object[] values = initalizeParameterSet(className, cTestClass.configMappingFile(), cTestClass.value(), cTestClass.regex());
        classLevelParameters = (Set<String>) values[0];
        methodLevelParametersFromMappingFile = (Map<String, Set<String>>) values[1];
    }
}
