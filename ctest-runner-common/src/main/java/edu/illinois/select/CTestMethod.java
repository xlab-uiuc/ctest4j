package edu.illinois.select;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Shuai Wang
 * Date:  1/7/24
 */
public class CTestMethod {
    /** The name of the class containing the ctest method. */
    private String className;
    /** The name of the ctest method. */
    private String methodName;
    /** The set of configuration parameters used in the ctest method. */
    private final Set<String> usedParams;

    public CTestMethod(String className, String methodName, Set<String> usedParams) {
        this.className = className;
        this.methodName = methodName;
        this.usedParams = new HashSet<>(usedParams);
    }

    public CTestMethod(String className, String methodName) {
        this(className, methodName, new HashSet<>());
    }

    public CTestMethod() {
        this("", "");
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Set<String> getUsedParams() {
        return Collections.unmodifiableSet(usedParams);
    }

    public void addUsedParam(String param) {
        usedParams.add(param);
    }

    public void setUsedParams(Set<String> usedParams) {
        this.usedParams.clear();
        this.usedParams.addAll(usedParams);
    }

    public void clearUsedParams() {
        usedParams.clear();
    }

    public boolean isParameterUsed(String param) {
        return usedParams.contains(param);
    }
}
