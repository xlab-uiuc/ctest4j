package edu.illinois.agent;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.annotation.AfterReturning;

import edu.illinois.ConfigTracker;

@Aspect
public class AspectJWeaver {

    @Pointcut("execution(public String org.apache.hadoop.conf.Configuration.get(String)) && args(name)")
    public void getPointCut(String name) {}
    
    @Pointcut("cflow(execution(public String org.apache.hadoop.conf.Configuration.get(String)))")
    public void inGet() {}

    @Before("getPointCut(name)")
    public void beforeGetMethod(JoinPoint joinPoint, String name) {
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        System.out.println("Class Name: " + className + " ---- Method Name: " + methodName + " ---- Input Value: " + name);
        ConfigTracker.markParamAsUsed(name);
    }

    @Pointcut("execution(public String org.apache.hadoop.conf.Configuration.getRaw(String)) && args(name)")
    public void getRawPointCut(String name) {}
    
    @Pointcut("cflow(execution(public String org.apache.hadoop.conf.Configuration.getRaw(String)))")
    public void inGetRaw() {}
    
    @Before("getRawPointCut(name)")
    public void beforeGetRawMethod(JoinPoint joinPoint, String name) {
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        System.out.println("Class Name: " + className + " ---- Method Name: " + methodName + " ---- Input Value: " + name);
        ConfigTracker.markParamAsUsed(name);
    }

    @Pointcut("execution(public String org.apache.hadoop.conf.Configuration.get(String, String)) && args(name, defaultValue)")
    public void getWithDefaultPointCut(String name, String defaultValue) {}
    
    @Pointcut("cflow(execution(public String org.apache.hadoop.conf.Configuration.get(String, String)))")
    public void inGetWithDefault() {}
    
    @Before("getWithDefaultPointCut(name, defaultValue)")
    public void beforeGetRawMethod(JoinPoint joinPoint, String name, String defaultValue) {
        String className = joinPoint.getSignature().getDeclaringType().getName();
        String methodName = joinPoint.getSignature().getName();
        System.out.println("Class Name: " + className + " ---- Method Name: " + methodName + " ---- Input Value: " + name + " ---- Default Value: " + defaultValue);
        ConfigTracker.markParamAsUsed(name);
    }

    @Pointcut("execution(private String[] org.apache.hadoop.conf.Configuration.handleDeprecation(..))")
    public void handleDeprecationPointcut() {};

    @Pointcut("inGet() || inGetRaw() || inGetWithDefault()")
    public void handleDeprecationPointcutWithinGet() {};

    @AfterReturning(pointcut = "handleDeprecationPointcutWithinGet() && handleDeprecationPointcut()", returning = "names")
    public void afterReturningHandleDeprecation(String[] names) {
        for(String n : names) {
            ConfigTracker.markParamAsUsed(n);
        }
    }

    // @Pointcut("execution(org.apache.hadoop.conf.Configuration.new(..))")
    // public void configurationConstructor() {}

    // @After("configurationConstructor() && this(config)")
    // public void afterConfigurationConstructor(JoinPoint joinPoint, Configuration config) {
    //     ConfigTracker.injectConfig((arg1, arg2) -> config.set(arg1, (String) arg2));
    // }
}

