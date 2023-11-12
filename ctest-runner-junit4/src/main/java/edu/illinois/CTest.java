package edu.illinois;

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
public @interface CTest {
    /** A list of configuration parameter name as input for a single test method */
    String[] value() default {};
    String[] optional() default {};
    String configMappingFile() default "";

    /** =============================== Copied From @Test annotation =================================== */
    static class None extends Throwable {
        private static final long serialVersionUID = 1L;

        private None() {
        }
    }

    Class<? extends Throwable> expected() default None.class;

    long timeout() default 0L;
}