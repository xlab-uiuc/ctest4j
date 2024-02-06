package edu.illinois;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.AnnotationFormatError;
import java.lang.reflect.Method;
import java.util.*;

import static edu.illinois.Names.*;

/**
 * Author: Shuai Wang
 * Date:  11/1/23
 */
public class CTestJUnit5Extension implements CTestRunner, ExecutionCondition,
        BeforeAllCallback, BeforeEachCallback, AfterEachCallback, AfterAllCallback {
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

    /**
     * Initialize the class-level parameters.
     * @param extensionContext
     * @throws Exception
     */
    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        startTestClass(extensionContext.getRequiredTestClass().getName());
        initializeRunner(extensionContext);
        //ConfigTracker.startTestClass();
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
        startTestMethod(extensionContext.getRequiredTestClass().getName(), extensionContext.getRequiredTestMethod().getName());
        ConfigTracker.startTestMethod(extensionContext.getRequiredTestClass().getName(), extensionContext.getRequiredTestMethod().getName());
        methodName = extensionContext.getRequiredTestMethod().getName();
    }

    /**
     * Check if the test method uses all the parameters specified in the @CTest and @CTestClass annotation.
     * @param extensionContext
     * @throws Exception
     */
    @Override
    public void afterEach(ExtensionContext extensionContext) throws IOException {
        endTestMethod(configUsage, className, methodName,
                extensionContext.getRequiredTestMethod().getAnnotation(CTest.class),
                extensionContext.getRequiredTestMethod().getAnnotation(Test.class),
                classLevelParameters, methodLevelParametersFromMappingFile);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        writeConfigUsageToJson(configUsage, new File(CONFIG_SAVE_DIR, className + ".json"));
    }

    @Override
    public Set<String> getUnionClassParameters(Set<String> classLevelParameters, String classConfigFile, String classRegex) throws IOException {
        classLevelParameters.addAll(getClassParametersFromMappingFile(classConfigFile));
        if (!classRegex.isEmpty()) {
            classLevelParameters.addAll(getParametersFromRegex(classRegex));
        }
        return classLevelParameters;
    }


    @Override
    public void initializeRunner(Object context) throws AnnotationFormatError, IOException {
        ExtensionContext extensionContext = (ExtensionContext) context;
        // Retrieve class-level parameters
        className = extensionContext.getRequiredTestClass().getName();
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
        Object[] values = initializeParameterSet(className, cTestClass.configMappingFile(), cTestClass.value(), cTestClass.regex());
        classLevelParameters = (Set<String>) values[0];
        methodLevelParametersFromMappingFile = (Map<String, Set<String>>) values[1];
        selectionParams.addAll(Utils.getSelectionParameters(
                System.getProperty(Names.CTEST_SELECTION_PARAMETER_PROPERTY)));
    }

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        Optional<Method> testMethod = extensionContext.getTestMethod();
        if (testMethod.isPresent()) {
            boolean ignoreTest = false;
            try {
                ignoreTest = isTestIgnored(extensionContext);
            } catch (IOException e) {
                Log.ERROR("Error while checking if test is ignored: " + e);
            }
            if (ignoreTest) {
                return ConditionEvaluationResult.disabled("CTest is disabled for " + testMethod.get().getName());
            }
        }
        return ConditionEvaluationResult.enabled("");
    }

    private boolean isTestIgnored(ExtensionContext extensionContext) throws IOException {
        if (selectionParams.isEmpty()) {
            return false;
        }
        methodName = extensionContext.getRequiredTestMethod().getName();
        Set<String> usedParams = new HashSet<>();
        CTest cTest = extensionContext.getRequiredTestMethod().getAnnotation(CTest.class);
        if (cTest != null) {
            usedParams.addAll(getUnionMethodParameters(className, methodName, cTest.regex(),
                    classLevelParameters, methodLevelParametersFromMappingFile, new HashSet<>(Arrays.asList(cTest.value()))));
            return isCurrentTestIgnored(selectionParams, usedParams);
        }
        Test test = extensionContext.getRequiredTestMethod().getAnnotation(Test.class);
        if (test != null) {
            usedParams.addAll(getUnionMethodParameters(className, methodName, "",
                    classLevelParameters, methodLevelParametersFromMappingFile, new HashSet<>()));
            return isCurrentTestIgnored(selectionParams, usedParams);
        }
        return false;
    }

}
