package edu.illinois;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 * The exception thrown when a configuration parameter is not used in the test.
 */
public class UnUsedConfigParamException extends RuntimeException {
    public UnUsedConfigParamException(String message) {
        super(message);
    }
}

