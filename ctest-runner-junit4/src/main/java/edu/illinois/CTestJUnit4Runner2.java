package edu.illinois;


import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static edu.illinois.Names.CONFIG_MAPPING_DIR;
import static edu.illinois.Options.saveUsedParamToFile;
import static edu.illinois.Utils.getTestMethodFullName;

/**
 * Design B: Only CTestClass Annotation and treat all the test methods as @CTest
 * Author: Shuai Wang
 * Date:  11/10/23
 */
// TODO: Write test cases for this class
public class CTestJUnit4Runner2 extends BlockJUnit4ClassRunner implements CTestRunner {
    protected String classLevelConfigMappingFile;
    protected Set<String> classLevelParameters;
    protected Map<String, Set<String>> methodLevelParameters;
    protected final ConfigUsage configUsage = new ConfigUsage();
    protected final String testClassName = getTestClass().getJavaClass().getName();

    public CTestJUnit4Runner2(Class<?> klass) throws InitializationError, IOException {
        super(klass);
        initializeRunner(klass);
    }

    /**
     * Initialize the runner.
     * @CTestClass annotation is required and the class-level configuration file is required.
     */
    @Override
    public void initializeRunner(Class<?> klass) throws InitializationError, IOException {
        // Retrieve class-level parameters if present
        CTestClass cTestClass = klass.getAnnotation(CTestClass.class);
        if (cTestClass == null) {
            throw new InitializationError("CTestClass annotation is not present in class " + klass.getName());
        }

        // Get classLevel and methodLevel parameters from the mapping file
        // If the file is not specified, use the default file =>
        // System.getProperty("ctest.mapping.dir", "ctest/mapping") + "/" + testClassName + ".json"
        // If the default file is not present, throw an exception
        classLevelConfigMappingFile = cTestClass.configMappingFile();
        if (classLevelConfigMappingFile.isEmpty()) {
            classLevelConfigMappingFile = new File(CONFIG_MAPPING_DIR, testClassName + ".json").getAbsolutePath();
            if (classLevelConfigMappingFile.isEmpty()) {
                throw new InitializationError("Class-level configuration file is not specified.");
            }
        }
        // Retrieve class-level parameters if present
        classLevelParameters = getUnionClassParameters(new HashSet<>(Arrays.asList(cTestClass.value())), classLevelConfigMappingFile, "");
        // Retrieve method-level parameters if present
        methodLevelParameters = getAllMethodLevelParametersFromMappingFile(classLevelConfigMappingFile);
        // Set the current test class name
        ConfigTracker.setCurrentTestClassName(klass.getName());
    }

    /**
     * Get all the test methods with @Test and @CTest annotations.
     * @return a list of test methods.
     */
    @Override
    protected List<FrameworkMethod> computeTestMethods() {
        // Return both methods with @Test and @CTest annotations
        List<FrameworkMethod> methods = new ArrayList<>();
        methods.addAll(super.computeTestMethods());
        methods.addAll(getTestClass().getAnnotatedMethods(CTest.class));
        return Collections.unmodifiableList(methods);
    }

    /**
     * @CTest would override the expected exception from @Test annotation.
     */
    @Override
    protected Statement possiblyExpectingExceptions(FrameworkMethod method, Object test, Statement next) {
        // From @CTest annotation
        CTest cTest = method.getAnnotation(CTest.class);
        if (cTest != null) {
            Class<? extends Throwable> configTestExpectedException = cTest.expected();
            if (configTestExpectedException != CTest.None.class) {
                return new ExpectException(next, configTestExpectedException);
            }
        }
        return super.possiblyExpectingExceptions(method, test, next);
    }

    /**
     * @CTest would override the timeout from @Test annotation.
     */
    @Override
    protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
        long timeout = 0;
        CTest cTest = method.getAnnotation(CTest.class);
        Test testAnnotation = method.getAnnotation(Test.class);
        if (cTest != null) {
            timeout = cTest.timeout();
        } else if (testAnnotation != null) {
            timeout = testAnnotation.timeout();
        }
        if (timeout <= 0) {
            return next;
        }
        return FailOnTimeout.builder()
                .withTimeout(timeout, TimeUnit.MILLISECONDS)
                .build(next);
    }

    /**
     * Start tracking the class-level parameters before running the @BeforeClass methods.
     * @param statement
     * @return
     */
    @Override
    protected Statement withBeforeClasses(Statement statement) {
        final Statement originalStatement = super.withBeforeClasses(statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ConfigTracker.startTestClass();
                originalStatement.evaluate();
            }
        };
    }

    /**
     * Start tracking the method-level parameters before running the @Before methods.
     */
    @Override
    protected Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        final Statement originalStatement = super.withBefores(method, target, statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                ConfigTracker.startTestMethod();
                originalStatement.evaluate();
            }
        };
    }

    /**
     * Check if the test method uses all the parameters for all the test methods in the class.
     */
    @Override
    protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
        final Statement originalStatement = super.withAfters(method, target, statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    originalStatement.evaluate();
                } finally {
                    ConfigTracker.updateConfigUsage(configUsage, method.getName());
                    if (Options.mode == Modes.CHECKING || Options.mode == Modes.DEFAULT) {
                        CTest cTest = method.getAnnotation(CTest.class);
                        if (cTest != null) {
                            for (String param : getUnionMethodParameters(getTestClass().getJavaClass().getName(),
                                    method.getName(), cTest.configMappingFile(), "")) {
                                if (!ConfigTracker.isParameterUsed(param)) {
                                    if (cTest.expected() != CTest.None.class) {
                                        if (cTest.expected().isAssignableFrom(UnUsedConfigParamException.class)) {
                                            return;
                                        }
                                    }
                                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                                }
                            }
                        }
                        Test testAnnotation = method.getAnnotation(Test.class);
                        if (testAnnotation != null) {
                            for (String param : getUnionMethodParameters(getTestClass().getJavaClass().getName(),
                                    method.getName(), "", "")) {
                                if (!ConfigTracker.isParameterUsed(param)) {
                                    if (testAnnotation.expected() != Test.None.class) {
                                        if (testAnnotation.expected().isAssignableFrom(UnUsedConfigParamException.class)) {
                                            return;
                                        }
                                    }
                                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    /**
     * Write the ConfigUsage object to a JSON file after running the @AfterClass methods.
     * @param statement
     * @return
     */
    @Override
    protected Statement withAfterClasses(Statement statement) {
        Statement originalStatement = super.withAfterClasses(statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    originalStatement.evaluate();
                } finally {
                    if (saveUsedParamToFile) {
                        ConfigUsage.writeToJson(configUsage, new File(CONFIG_MAPPING_DIR, testClassName + ".json"));
                    }
                }
            }
        };
    }

    @Override
    public Set<String> getUnionClassParameters(Set<String> classLevelParameters, String classConfigFile, String classRegex) throws IOException {
        classLevelParameters.addAll(getClasssParametersFromMappingFile(classConfigFile));
        return classLevelParameters;
    }

    public Set<String> getUnionMethodParameters(String className, String methodName, String methodLevelConfigMappingFile, String methodRegex) throws IOException {
        Set<String> allMethodLevelParameters = new HashSet<>();
        // Retrieve method-level parameters if present
        allMethodLevelParameters.addAll(this.methodLevelParameters.get(methodName));
        // Retrieve class-level parameters if present
        allMethodLevelParameters.addAll(this.classLevelParameters);

        // Retrieve file-level parameters if present
        if (!methodLevelConfigMappingFile.isEmpty()) {
            allMethodLevelParameters.addAll(getParametersFromMappingFile(methodLevelConfigMappingFile));
        } else {
            allMethodLevelParameters.addAll(getRequiredParametersFromDefaultFile(className, methodName));
        }
        return allMethodLevelParameters;
    }

}
