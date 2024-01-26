package edu.illinois;

/**
 * Author: Shuai Wang
 * Date:  10/21/23
 */
public enum Modes {
    /** DEFAULT mode: run the config test with checking and injecting */
    DEFAULT,
    /** CHECKING mode: run the config test with parameter usage checking only */
    CHECKING,
    /** INJECTING mode: run the config test with parameter injection only */
    INJECTING,
    /** BASE mode: run the config test as a normal test */
    BASE
}
