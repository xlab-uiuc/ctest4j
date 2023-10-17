package edu.illinois;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Shuai Wang
 * Date:  10/17/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigTestClass {
    /** A shared list of configuration parameter name for all methods in the test class. */
    String[] value() default {};
    String[] may() default {};
    String file() default "";
}