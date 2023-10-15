package edu.illinois;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class Configuration {
    public String get(String name) {
        return "default";
    }
    public void set(String name, String value) {
        System.out.println("fake print");
    }
}
