package edu.illinois;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

/**
 * Author: Shuai Wang
 * Date:  10/14/23
 */
public class Log {
    private static final Logger logger = LogManager.getLogger(Log.class);
    private static final File logFile = new File("CTestRunnerLog.log");

    public static void DEBUG(String ... msg){
        StringBuilder sb = new StringBuilder();
        for (String s : msg) {
            sb.append(s).append(" ");
        }
        logger.debug(sb.toString());
    }

    public static void INFO(String ... msg){
        StringBuilder sb = new StringBuilder();
        for (String s : msg) {
            sb.append(s).append(" ");
        }
        logger.info(sb.toString());
    }

    public static void WARN(String ... msg){
        StringBuilder sb = new StringBuilder();
        for (String s : msg) {
            sb.append(s).append(" ");
        }
        logger.warn(sb.toString());
    }

    public static void ERROR(String ... msg){
        StringBuilder sb = new StringBuilder();
        for (String s : msg) {
            sb.append(s).append(" ");
        }
        logger.error(sb.toString());
    }

    public static void writeToFile(String ... msg){
        StringBuilder sb = new StringBuilder();
        for (String s : msg) {
            sb.append(s).append(" ");
        }
        // Write sb to file logFile
        try {
            FileWriter fw = new FileWriter(logFile, true);
            fw.write(sb+ "\n");
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
