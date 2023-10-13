package edu.illinois.agent;

import edu.illinois.Config;

import java.lang.instrument.Instrumentation;
import java.util.Objects;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigRunnerAgent {
    /** Name of the Agent */
    private static Instrumentation sInstrumentation;

    /**
     * This method is called before the application's main method is called.
     * Load the Configuration Test Runner Agent to Instrument the Configuration API for tracking config parameter usage.
     * @param options
     * @param instrumentation
     */
    public static void premain(String options, Instrumentation instrumentation) {
        if (Objects.equals(Config.AGENT_MODE, "JUNIT")) {
            sInstrumentation = instrumentation;
            instrumentation.addTransformer(new ConfigTransformer());
        }
    }

    public static void agentmain(String options, Instrumentation instrumentation) {
        if (Objects.equals(Config.AGENT_MODE, "JUNIT")) {
            sInstrumentation = instrumentation;
            instrumentation.addTransformer(new ConfigTransformer());
        }
    }

    public static Instrumentation getInstrumentation() {
        return sInstrumentation;
    }
}
