package edu.illinois.agent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import edu.illinois.ConfigTracker;
import static edu.illinois.Names.*;

@Aspect
public class AspectJWeaver {
    public class WeaverUnit {
        private String signature;
        private int pos = 0;
        private String caller = null;

        /**
         * Constructor for WeaverUnit.
         *
         * @param args The arguments for initializing the WeaverUnit object.
         * @throws IllegalArgumentException if the input arguments are not valid.
         */
        public WeaverUnit(String[] args) throws Exception {
            validateArguments(args);
            signature = args[0];
            if (args.length == 2) {
                if (args[1].matches("\\d+")) {
                    pos = Integer.parseInt(args[1]);
                } else {
                    caller = args[1];
                }
            } else if (args.length == 3) {
                pos = Integer.parseInt(args[1]);
                caller = args[2];
            }
        }

        private void validateArguments(String[] args) throws Exception {
            if (args == null || args.length == 0 || args.length > 3) {
                throw new Exception("Invalid number of arguments. Expected 1 to 3 arguments.");
            }
        }

        public String getSignature() {
            return signature;
        }

        public int getPos() {
            return pos;
        }

        public String getCaller() {
            return caller;
        }
    }

    private static boolean isPropertySet = false;
    private static WeaverUnit[] config_getter_list;
    private static WeaverUnit[] config_setter_list;
    private static WeaverUnit[] config_injector_list;

    private void setProperty() {
        String config_getter_str = System.getProperty(CTEST_GETTER);
        String config_setter_str = System.getProperty(CTEST_SETTER);
        String config_injector_str = System.getProperty(CTEST_INJECTOR);
        // Split the strings and initialize the WeaverUnit arrays
        String[] getterStrings = config_getter_str != null ? config_getter_str.split(";") : new String[0];
        String[] setterStrings = config_setter_str != null ? config_setter_str.split(";") : new String[0];
        String[] injectorStrings = config_injector_str != null ? config_injector_str.split(";") : new String[0];
        config_getter_list = new WeaverUnit[getterStrings.length];
        config_setter_list = new WeaverUnit[setterStrings.length];
        config_injector_list = new WeaverUnit[injectorStrings.length];
        // Convert each string into a WeaverUnit
        for (int i = 0; i < getterStrings.length; i++) {
            try {
                config_getter_list[i] = new WeaverUnit(getterStrings[i].split("#"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        for (int i = 0; i < setterStrings.length; i++) {
            try {
                config_setter_list[i] = new WeaverUnit(setterStrings[i].split("#"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        for (int i = 0; i < injectorStrings.length; i++) {
            try {
                config_injector_list[i] = new WeaverUnit(injectorStrings[i].split("#"));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Pointcut("!within(edu.illinois..*) && execution(public String *..*(String))")
    public void anyGetMethod() {}

    @Before("anyGetMethod()")
    public void beforeAnyPublicGetMethod(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (isPropertySet == false && (System.getProperty(CTEST_GETTER) != null) || (System.getProperty(CTEST_SETTER) != null) || (System.getProperty(CTEST_INJECTOR) != null)) {
            setProperty();
            isPropertySet = true;
        }
        if (isPropertySet) {
            String methodSignature = joinPoint.getSignature().toString();
            for (WeaverUnit wunit : config_getter_list) {
                if (methodSignature.contains(wunit.getSignature())) {
                    Object[] args = joinPoint.getArgs();
                    if (wunit.getCaller() == null) {
                        ConfigTracker.markParamAsUsed((String) args[wunit.getPos()]);
                    } else {
                        Method getNameMethod = args[wunit.getPos()].getClass().getMethod(wunit.getCaller());
                        String name = (String) getNameMethod.invoke(args[wunit.getPos()]);
                        ConfigTracker.markParamAsUsed(name);
                    }
                }
            }
        }
    }

    @Pointcut("!within(edu.illinois..*) && execution(public void *..*(String, String))")
    public void anySetMethod() {}

    @Before("anySetMethod()")
    public void beforeAnyPublicSetMethod(JoinPoint joinPoint) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (isPropertySet == false && (System.getProperty(CTEST_GETTER) != null) || (System.getProperty(CTEST_SETTER) != null) || (System.getProperty(CTEST_INJECTOR) != null)) {
            setProperty();
            isPropertySet = true;
        }
        if (isPropertySet) {
            String methodSignature = joinPoint.getSignature().toString();
            for (WeaverUnit wunit : config_setter_list) {
                if (methodSignature.contains(wunit.getSignature())) {
                    Object[] args = joinPoint.getArgs();
                    if (wunit.getCaller() == null) {
                        ConfigTracker.markParamAsSet((String) args[wunit.getPos()]);
                    } else {
                        Method setNameMethod = args[wunit.getPos()].getClass().getMethod(wunit.getCaller());
                        String name = (String) setNameMethod.invoke(args[wunit.getPos()]);
                        ConfigTracker.markParamAsSet(name);
                    }
                }
            }
        }
    }


    @Pointcut("!within(edu.illinois..*) && initialization(*..new(..))")
    public void anyInjectMethod() {}

    @After("anyInjectMethod()")
    public void afterAnyPublicInjectMethod(JoinPoint joinPoint) throws Exception {
        if (!isPropertySet && ((System.getProperty(CTEST_GETTER) != null) || (System.getProperty(CTEST_SETTER) != null) || (System.getProperty(CTEST_INJECTOR) != null))) {
            setProperty();
            isPropertySet = true;
        }
        if (isPropertySet) {
            String methodSignature = joinPoint.getSignature().toString();
            for (WeaverUnit wunit : config_injector_list) {
                if (methodSignature.contains(wunit.getSignature())) {
                    Object[] args = joinPoint.getArgs();
                    if (wunit.getCaller() == null) {
                        throw new Exception("Invalid arguments. Caller should not be null");
                    } else {
                        Class<?> configClass = Class.forName(wunit.getSignature().split("\\(")[0]);
                        Method injectMethod = configClass.getMethod(wunit.getCaller(), String.class, String.class);
                        ConfigTracker.injectConfig((arg1, arg2) -> {
                            try {
                                injectMethod.invoke(joinPoint.getTarget(), arg1.toString(), arg2.toString());
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                throw new RuntimeException("Injection failed", e);
                            }
                        });
                    }
                }
            }
        }
    }
}
