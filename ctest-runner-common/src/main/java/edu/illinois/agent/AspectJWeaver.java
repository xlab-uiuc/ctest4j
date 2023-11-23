package edu.illinois.agent;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import edu.illinois.ConfigTracker;
import static edu.illinois.Names.*;

@Aspect
public class AspectJWeaver {

    private static String[] config_getter_list;
    private static String[] config_setter_list;

    static {
        String config_getter_str = System.getProperty(CTEST_GETTER);
        String config_setter_str = System.getProperty(CTEST_SETTER);
        config_getter_list = config_getter_str != null ? config_getter_str.split(",") : new String[0];
        config_setter_list = config_setter_str != null ? config_setter_str.split(",") : new String[0];
    }

    @Pointcut("execution(public * *(String)) && args(name)")
    public void anyGetMethod(String name) {}

    @Before("anyGetMethod(name)")
    public void beforeAnyGetMethod(JoinPoint joinPoint, String name) {
        String methodSignature = joinPoint.getSignature().toString();
        if (Arrays.asList(config_getter_list).contains(methodSignature)) {
            ConfigTracker.markParamAsUsed(name);
        }
    }

    @Pointcut("execution(public * *(String, String)) && args(name, value)")
    public void anySetMethod(String name, String value) {}

    @Before("anySetMethod(name, value)")
    public void beforeAnySetMethod(JoinPoint joinPoint, String name, String value) {
        String methodSignature = joinPoint.getSignature().toString();
        if (Arrays.asList(config_setter_list).contains(methodSignature)) {
            ConfigTracker.markParmaAsSet(name);
        }
    }
}
