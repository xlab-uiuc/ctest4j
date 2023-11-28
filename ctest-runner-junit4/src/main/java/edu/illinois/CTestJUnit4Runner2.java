package edu.illinois;


import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.AnnotationFormatError;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static edu.illinois.Names.CONFIG_SAVE_DIR;

/**
 * Design B: Only CTestClass Annotation and treat all the test methods as @CTest
 * Author: Shuai Wang
 * Date:  11/10/23
 */
public class CTestJUnit4Runner2 extends BlockJUnit4ClassRunner implements CTestRunner {
    protected Set<String> classLevelParameters;
    protected Map<String, Set<String>> methodLevelParametersFromMappingFile;
    protected final ConfigUsage configUsage = new ConfigUsage();
    protected final String testClassName = getTestClass().getJavaClass().getName();

    public CTestJUnit4Runner2(Class<?> klass) throws InitializationError, IOException {
        super(klass);
        initializeRunner(klass);
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
        validateNestedRunWith(errors);
        super.collectInitializationErrors(errors);
    }

    /**
     * Check if the parent class has another RunWith annotation.
     * @param errors
     */
    protected void validateNestedRunWith(List<Throwable> errors) {
        // If the parent class has another RunWith annotation, skip the validation
        Class<?> parentClass = getTestClass().getJavaClass().getSuperclass();
        if (parentClass.getAnnotation(RunWith.class) != null) {
            errors.add(new Exception("CTestJUnit4Runner does not support nested RunWith annotations"));
        }
    }

    @Override
    public void initializeRunner(Object context) throws AnnotationFormatError, IOException {
        Class<?> klass = (Class<?>) context;
        // Set the current test class name
        ConfigTracker.setCurrentTestClassName(klass.getName());
        // Retrieve class-level parameters if present
        CTestClass cTestClass = klass.getAnnotation(CTestClass.class);
        if (cTestClass == null) {
            throw new AnnotationFormatError("CTestClass annotation is not present in class " + klass.getName());
        }
        // Get classLevel and methodLevel parameters from the mapping file
        Object[] values = initalizeParameterSet(testClassName, cTestClass.configMappingFile(), cTestClass.value(), cTestClass.regex());
        classLevelParameters = (Set<String>) values[0];
        methodLevelParametersFromMappingFile = (Map<String, Set<String>>) values[1];
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
                Throwable fromTestThrowable = null;
                try {
                    originalStatement.evaluate();
                } catch (Throwable throwable) {
                    fromTestThrowable = throwable;
                } finally {
                    if (shouldThorwException(fromTestThrowable)) {
                        throw fromTestThrowable;
                    }
                    ConfigTracker.updateConfigUsage(configUsage, method.getName());
                    if (Options.mode == Modes.CHECKING || Options.mode == Modes.DEFAULT) {
                        CTest cTest = method.getAnnotation(CTest.class);
                        if (cTest != null) {
                            for (String param : getUnionMethodParameters(method.getName(), cTest.regex(),
                                    new HashSet<>(Arrays.asList(cTest.value())))) {
                                if (!ConfigTracker.isParameterUsed(param)) {
                                    if (isUnUsedParamException(cTest.expected())) {
                                        return;
                                    }
                                    throw new UnUsedConfigParamException(param + " was not used during the test.");
                                }
                            }
                        }
                        Test testAnnotation = method.getAnnotation(Test.class);
                        if (testAnnotation != null) {
                            for (String param : getUnionMethodParameters(method.getName(), "", new HashSet<>())) {
                                if (!ConfigTracker.isParameterUsed(param)) {
                                    if (isUnUsedParamException(testAnnotation.expected())) {
                                        return;
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
                    writeConfigUsageToJson(configUsage, new File(CONFIG_SAVE_DIR, testClassName + ".json"));
                }
            }
        };
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

}
