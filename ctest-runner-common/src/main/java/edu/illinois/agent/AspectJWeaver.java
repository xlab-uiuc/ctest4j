package edu.illinois.agent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import edu.illinois.ConfigTracker;
import static edu.illinois.Names.*;

@Aspect
public class AspectJWeaver {

    static String[] config_getter_list;
    static String[] config_setter_list;
    static {
        String config_getter_str = System.getProperty(CTEST_GETTER);
        String config_setter_str = System.getProperty(CTEST_SETTER);
        if (config_getter_str != null) {
            config_getter_list = config_getter_str.split(",");
        }
        if (config_setter_str != null) {
            config_setter_list = config_setter_str.split(",");
        }
    }

    @Pointcut("execution(public * *(String)) && args(name)")
    public void anyMethod(String name) {}

    @Before("anyMethod(name)")
    public void beforeAnyMethod(JoinPoint joinPoint, String name) {
        String methodSignature = joinPoint.getSignature().toString();
        if (config_getter_list != null) {
            if (Arrays.asList(config_getter_list).contains(methodSignature)) {
                ConfigTracker.markParamAsUsed(name);
            }
        }
        if (config_setter_list != null) {
            if (Arrays.asList(config_setter_list).contains(methodSignature)) {
                ConfigTracker.markParmaAsSet(name);
            }
        }
    }

}
