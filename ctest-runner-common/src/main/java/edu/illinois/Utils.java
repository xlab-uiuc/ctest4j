package edu.illinois;
import org.junit.runners.model.FrameworkMethod;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  10/14/23
 */
public class Utils {

    public static String readStringFromFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * Write a string to a file
     * @param path
     * @param content
     */
    public static void writeStringToFile(final String path, final String content) {
        // If the file dir does not exist, create it
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Write bytes to a file in a new thread
     * @param path the path of the file
     * @param bytes the bytes to write
     */
    public static void writeBytesToFile(final String path, final byte[] bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    File file = new File(path);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                    }
                    Files.write(Paths.get(path), bytes);
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }).start();
    }

    public static List<String> getMethodDescriptorsFromString(String methodDescriptors) {
        if (methodDescriptors != null && !methodDescriptors.isEmpty()) {
            // Split with comma
            return new ArrayList<>(List.of(methodDescriptors.split(",")));
        }
        return null;
    }

    /**
     * Get the file type from the file name
     * @param file the file name
     * @return the file type
     * @throws IOException if the file name is invalid
     */
    public static String getFileType(String file)  {
        if (file != null && !file.isEmpty()) {
            String[] fileSplit = file.split("\\.");
            if (fileSplit.length > 1) {
                return fileSplit[fileSplit.length - 1];
            }
        }
        return null;
    }

    public static void writeParamSetToJson(Set<String> usedParam, Set<String> setParam, File targetJsonFile) {
        // No need to write if the set is empty
        if (usedParam.isEmpty() && setParam.isEmpty()) {
            return;
        }
        StringBuilder paramStr = new StringBuilder("{\"required\": [");
        for (String param : usedParam) {
            paramStr.append("\"").append(param).append("\",");
        }
        if (!usedParam.isEmpty()) {
            paramStr = new StringBuilder(paramStr.substring(0, paramStr.length() - 1));
        }
        paramStr.append("], \"set\": [");

        for (String param : setParam) {
            paramStr.append("\"").append(param).append("\",");
        }
        if (!setParam.isEmpty()) {
            paramStr = new StringBuilder(paramStr.substring(0, paramStr.length() - 1));
        }
        paramStr.append("]}");
        writeStringToFile(targetJsonFile.getAbsolutePath(), paramStr.toString());
    }

    /**
     * Get the full name of a test method as the format of "testClassName_testMethodName"
     * @param method
     * @return
     */
    public static String getTestMethodFullName(FrameworkMethod method) {
        return method.getMethod().getDeclaringClass().getName() + Names.TEST_CLASS_METHOD_SEPERATOR + method.getName();
    }

    public static String getTestMethodFullName(String className, String methodName) {
        return getFullTestName(className, methodName);
    }

    private static boolean isLibraryClass(String className) {
        return className.startsWith("edu.illinois.CTest")
                || className.startsWith("edu.illinois.select.CTest") || className.startsWith("edu.illinois.track.CTest")
                || className.startsWith("java.") || className.startsWith("sun.") || className.startsWith("jdk.")
                || className.startsWith("org.junit.") || className.startsWith("org.hamcrest.")
                || className.startsWith("org.gradle.")
                || className.startsWith("org.apache.maven.") || className.startsWith("org.apache.tools.")
                || className.startsWith("org.codehaus.plexus.") || className.startsWith("org.eclipse.")
                || className.startsWith("org.pitest.") || className.startsWith("org.slf4j.")
                || className.startsWith("org.xmlpull.") || className.startsWith("org.yaml.")
                || className.startsWith("com.intellij.") || className.startsWith("com.sun.")
                || className.startsWith("com.google.") || className.startsWith("com.jcraft.");
    }

    public static String inferTestClassNameFromStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String threadName = String.valueOf(Thread.currentThread().getId());
        String retClassName = threadName;
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            // This logic is purely based on the JUnit name convention
            // find the last class name that contains "test"
            if (className.toLowerCase().contains("test") && !isLibraryClass(className)) {
                retClassName = className;
            }
        }
        if (retClassName.equals(threadName)) {
            throw new RuntimeException("Cannot infer test class and method name from stack trace");
        }
        return retClassName;
    }

    /**
     * Infer the test name from the stack trace
     * @return the test name
     */
    public static String inferTestClassAndMethodNameFromStackTrace() throws IOException {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        String threadName = String.valueOf(Thread.currentThread().getId());
        String retClassName = threadName;
        String retMethodName = threadName;
        for (StackTraceElement element : stackTrace) {
            String className = element.getClassName();
            String methodName = element.getMethodName();
            // This logic is purely based on the JUnit name convention
            if ((methodName.toLowerCase().startsWith("test") || methodName.toLowerCase().endsWith("test"))
                    && className.toLowerCase().contains("test")) {
                if (!isLibraryClass(className)) {
                    retClassName = className;
                    retMethodName = methodName;
                }
            }
        }
        if (retClassName.equals(threadName) || retMethodName.equals(threadName)) {
            throw new IOException("Cannot infer test class and method name from stack trace");
        }
        return retClassName + Names.TEST_CLASS_METHOD_SEPERATOR + retMethodName;
    }

    /**
     * Return (1) class name and (2) ${testClassName_testMethodName}
     */
    public static String[] getTestClassAndMethodName() throws IOException {
        String fullName = inferTestClassAndMethodNameFromStackTrace();
        String className = fullName.split(Names.TEST_CLASS_METHOD_SEPERATOR)[0];
        return new String[]{className, fullName};
    }

    /**
     * Return the full name of a test method as the format of "testClassName_testMethodName"
     */
    public static String getFullTestName(String className, String methodName) {
        // If this is already a full name with the separator, return it directly
        if (methodName.contains(Names.TEST_CLASS_METHOD_SEPERATOR)) {
            return methodName;
        }
        return className + Names.TEST_CLASS_METHOD_SEPERATOR + methodName;
    }

    /**
     * Get the set of configuration parameters specified for ctest selection.
     * @return null if the parameter list is not specified or empty; otherwise, return the set of parameters
     */
    public static Set<String> getSelectionParameters(String parameterStr) {
        if (parameterStr == null || parameterStr.isEmpty()) {
             return new HashSet<>();
        }
        Set<String> selectionParams = new HashSet<>();
        // If the parameter list starts with "@", it means that the parameter list is stored in a file
        if (parameterStr.startsWith("@")) {
            selectionParams.addAll(
                    getConfigParameterListFromFile(new File(parameterStr.substring(1))));
        } else {
            selectionParams.addAll(List.of(parameterStr.split(",")));
        }
        // Remove a possible empty string in the set.
        selectionParams.remove("");
        return selectionParams;
    }

    /**
     * Get the set of configuration parameters from the file
     * Each line of the file should be a configuration parameter
     */
    public static Set<String> getConfigParameterListFromFile(File file) {
        Set<String> configParameterList = new HashSet<>();
        // Read the file line by line
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    configParameterList.add(line);
                }
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Cannot find the target configuration parameter list file "
                    + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException("Error while reading the target configuration parameter list file "
                    + file.getAbsolutePath());
        }
        return configParameterList;
    }
}
