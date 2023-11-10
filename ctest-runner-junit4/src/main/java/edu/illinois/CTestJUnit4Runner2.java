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

import static edu.illinois.Names.USED_CONFIG_FILE_DIR;
import static edu.illinois.Options.saveUsedParamToFile;
import static edu.illinois.Utils.getTestMethodFullName;

/**
 * Design B: Only CTestClass Annotation and treat all the test methods as @CTest
 * Author: Shuai Wang
 * Date:  11/10/23
 */
// TODO: Write test cases for this class
public class CTestJUnit4Runner2 extends BlockJUnit4ClassRunner implements CTestRunner  {
    protected final String classLevelConfigMappingFile;
    protected final Set<String> classLevelParameters;
    protected Map<String, Set<String>> methodLevelParameters;
    protected final ConfigUsage configUsage = new ConfigUsage();
    protected final String testClassName = getTestClass().getJavaClass().getName();

    public CTestJUnit4Runner2(Class<?> klass) throws InitializationError {
        super(klass);
        // Retrieve class-level parameters if present
        CTestClass cTestClass = klass.getAnnotation(CTestClass.class);
        if (cTestClass == null) {
            throw new RuntimeException("CTestClass annotation is not present in class " + klass.getName());
        }
        try {
            classLevelConfigMappingFile = cTestClass.file();
            if (classLevelConfigMappingFile.isEmpty()) {
                throw new IOException("Class-level configuration file is not specified.");
            }
            classLevelParameters = getUnionClassParameters(new HashSet<>(Arrays.asList(cTestClass.value())), classLevelConfigMappingFile);
            methodLevelParameters = getAllMethodLevelParametersFromMappingFile(classLevelConfigMappingFile);
        } catch (IOException e) {
            throw new RuntimeException("Unable to parse configuration file from class " + klass.getName() + " Annotation", e);
        }
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


    protected Statement vanillaMethodInvoker(FrameworkMethod method, Object test) {
        return super.methodInvoker(method, test);
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
                                    method.getName(), cTest.file())) {
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
                                    method.getName(), "")) {
                                if (!ConfigTracker.isParameterUsed(param)) {
                                    if (testAnnotation.expected() != Test.None.class) {
                                        if (testAnnotation.expected().isAssignableFrom(UnUsedConfigParamException.class)) {
                                            return;
                                        }
                                    }
                                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                                }
                            }
                            if (saveUsedParamToFile) {
                                ConfigTracker.writeConfigToFile(getTestMethodFullName(method));
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
                    ConfigUsage.writeToJson(configUsage, new File(USED_CONFIG_FILE_DIR, testClassName + ".json"));
                }
            }
        };
    }

    @Override
    public Set<String> getUnionClassParameters(Set<String> classLevelParameters, String classConfigFile) throws IOException {
        classLevelParameters.addAll(getClasssParametersFromMappingFile(classConfigFile));
        return classLevelParameters;
    }

    public Set<String> getUnionMethodParameters(String className, String methodName, String methodLevelConfigMappingFile) throws IOException {
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
