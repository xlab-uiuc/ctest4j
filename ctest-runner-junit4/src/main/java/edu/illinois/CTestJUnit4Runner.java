package edu.illinois;

import org.junit.Test;
import org.junit.internal.runners.statements.ExpectException;
import org.junit.internal.runners.statements.FailOnTimeout;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static edu.illinois.Names.TRACKING_LOG_PREFIX;
import static edu.illinois.Utils.getTestMethodFullName;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class CTestJUnit4Runner extends BlockJUnit4ClassRunner implements CTestRunner {
    private final Set<String> classLevelParameters;

    public CTestJUnit4Runner(Class<?> klass) throws InitializationError {
        super(klass);
        // Retrieve class-level parameters if present
        CTestClass cTestClass = klass.getAnnotation(CTestClass.class);
        if (cTestClass != null) {
            try {
                classLevelParameters = getAllClassParameters(new HashSet<>(Arrays.asList(cTestClass.value())), cTestClass.file());
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse configuration file from class " + klass.getName() + " Annotation", e);
            }
        } else {
            classLevelParameters = new HashSet<>();
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

    /**
     * Invoke a vanilla method.
     * @param method
     * @param test
     * @return
     */
    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test) {
        Statement base = super.methodInvoker(method, test);
        if (Options.mode == Modes.BASE) {
            return base;
        }
        CTest cTest = method.getAnnotation(CTest.class);
        if (cTest != null) {
            try {
                return new CTestJUnit4Statement(base, getAllMethodParameters(getTestClass().getJavaClass().getName(),
                        method.getName(), cTest.file(), new HashSet<>(Arrays.asList(cTest.value())), classLevelParameters));
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse configuration file from method " + method.getName() + " Annotation", e);
            }
        }
        Test testAnnotation = method.getAnnotation(Test.class);
        if (testAnnotation != null) {
            return new ConfigTrackJUnit4Statement(base, method);
        }
        return base;
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
        // From @Test annotation
        Test testAnnotation = method.getAnnotation(Test.class);
        if (testAnnotation != null) {
            Class<? extends Throwable> testExpectedException = testAnnotation.expected();
            if (testExpectedException != Test.None.class) {
                return new ExpectException(next, testExpectedException);
            }

        }
        return next;
    }

    /**
     * @CTest would override the timeout from @Test annotation.
     */
    @Override
    protected Statement withPotentialTimeout(FrameworkMethod method, Object test, Statement next) {
        long timeout = 0;
        CTest cTest = method.getAnnotation(CTest.class);
        if (cTest != null) {
            timeout = cTest.timeout();
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
                System.out.println("In CTestJUnit4Runner.withBefores() for method" + method.getName());
                originalStatement.evaluate();
            }
        };
    }

/*    @Override
    protected Statement withAfters(FrameworkMethod method, Object target, Statement statement) {
        final Statement originalStatement = super.withAfters(method, target, statement);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    originalStatement.evaluate();
                    System.out.println("In CTestJUnit4Runner.withAfters()");
                    CTest cTest = method.getAnnotation(CTest.class);
                    if (cTest != null) {
                        checkCTestParameterUsage(getAllMethodParameters(getTestClass().getJavaClass().getName(),
                                method.getName(), cTest.file(), new HashSet<>(Arrays.asList(cTest.value())), classLevelParameters));
                        return;
                    }
                    if (method.getAnnotation(Test.class) != null) {
                        Log.INFO(TRACKING_LOG_PREFIX, method.getDeclaringClass().getCanonicalName() + "#" + method.getName(),
                                "uses configuration parameters: " + ConfigTracker.getAllUsedParams() + " and set parameters: " +
                                        ConfigTracker.getAllSetParams());
                        writeConfigToFile(getTestMethodFullName(method));
                    }
                } catch (Exception e) {
                  e.printStackTrace();
                }
            }
        };
    }*/
}