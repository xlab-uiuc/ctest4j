package edu.illinois;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConfigMetadata {
    String configClassName();
    String getConfigMethodSignature() default "get(String,String):String";
    String setConfigMethodSignature() default "set(String):void";
}

