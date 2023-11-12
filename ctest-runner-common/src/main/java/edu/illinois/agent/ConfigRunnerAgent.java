package edu.illinois.agent;

import org.aspectj.weaver.loadtime.Agent;
import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
import java.lang.instrument.Instrumentation;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigRunnerAgent {

    /**
     * This method is called before the application's main method is called.
     * Load the Configuration Test Runner Agent to Instrument the Configuration API for tracking config parameter usage.
     * @param options
     * @param instrumentation
     */
    public static void premain(String options, Instrumentation instrumentation) {
        Agent.premain(options, instrumentation);
        ClassPreProcessorAgentAdapter adapter = new ClassPreProcessorAgentAdapter();
        instrumentation.addTransformer(adapter, true);
    }

}

