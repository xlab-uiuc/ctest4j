package edu.illinois;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class Configuration {
    public String get(String parameter1) {
        return "default";
    }
    public void set(String parameter1, String value) {
        System.out.println("fake print");
    }
}
