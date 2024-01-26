package edu.illinois;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Shuai Wang
 * Date:  11/1/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CTestClass {
    /** A shared list of configuration parameter name for all methods in the test class. */
    String[] value() default {};
    String[] optional() default {};
    String configMappingFile() default "";
    String regex() default "";
}