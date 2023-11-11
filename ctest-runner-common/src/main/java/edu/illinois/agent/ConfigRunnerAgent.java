package edu.illinois.agent;

import edu.illinois.Names;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Objects;

import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
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
        ClassPreProcessorAgentAdapter transformer = new ClassPreProcessorAgentAdapter();
        instrumentation.addTransformer(transformer);
    }

    public static void agentmain(String options, Instrumentation instrumentation) {
/*
        if (Objects.equals(Names.AGENT_MODE, "JUNIT")) {
            sInstrumentation = instrumentation;
            instrumentation.addTransformer(new ConfigTransformer());
        }
*/
    }

    public static void reloadClass(String className, byte[] newClassBytes) {
        try {
            // Use the Instrumentation API to redefine the class
            sInstrumentation.redefineClasses(new ClassDefinition(Class.forName(className), newClassBytes));
        } catch (Exception e) {
            // Handle exceptions
        }
    }

    public static Instrumentation getInstrumentation() {
        return sInstrumentation;
    }
}
