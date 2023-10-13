package edu.illinois.instrument;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigClassLoader extends ClassLoader {
    private final String className;
    private final byte[] byteCode;

    public ConfigClassLoader(String className, byte[] byteCode, ClassLoader parent) {
        super(parent);
        this.className = className;
        this.byteCode = byteCode;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.equals(this.className)) {
            return defineClass(name, byteCode, 0, byteCode.length);
        }
        return super.findClass(name);
    }
}

