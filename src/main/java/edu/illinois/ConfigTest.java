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

    /** Copied From @Test annotation */
    static class None extends Throwable {
        private static final long serialVersionUID = 1L;

        private None() {
        }
    }

    /**
     * Optionally specify <code>expected</code>, a Throwable, to cause a test method to succeed if
     * and only if an exception of the specified class is thrown by the method. If the Throwable's
     * message or one of its properties should be verified, the
     * {@link org.junit.rules.ExpectedException ExpectedException} rule can be used instead.
     */
    Class<? extends Throwable> expected() default None.class;

}