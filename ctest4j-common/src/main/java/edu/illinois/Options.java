package edu.illinois;

/**
 * Author: Shuai Wang
 * Date:  10/15/23
 */
public class Options {
    @Deprecated
    public static Boolean instrumentSetter = Boolean.valueOf(System.getProperty("instrumentSetter", "false"));

    public static Modes mode = Modes.valueOf(System.getProperty(Names.CTEST_MODE_PROPERTY, "DEFAULT").toUpperCase());

    public static Boolean saveUsedParamToFile = Boolean.valueOf(System.getProperty(Names.CONFIG_SAVE_PROPERTY, "false"));

    @Deprecated
    public static Boolean ctestSuiteTracking = Boolean.valueOf(System.getProperty("ctest.suite.tracking", "false"));
}
