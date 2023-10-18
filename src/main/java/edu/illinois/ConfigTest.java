package edu.illinois;
import org.junit.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Shuai Wang
 * Date: 10/13/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ConfigTest {
    /** A list of configuration parameter name as input for a single test method */
    String[] value() default {};
    String[] may() default {};
    String file() default "";
}