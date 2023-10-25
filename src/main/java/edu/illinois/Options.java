package edu.illinois;

/**
 * Author: Shuai Wang
 * Date:  10/15/23
 */
public class Options {
    public static Boolean instrumentSetter = Boolean.valueOf(System.getProperty("instrumentSetter", "false"));

    public static Modes mode = Modes.valueOf(System.getProperty("mode", "DEFAULT").toUpperCase());

    public static Boolean saveUsedParamToFile = Boolean.valueOf(System.getProperty("save.used.config", "true"));
}
