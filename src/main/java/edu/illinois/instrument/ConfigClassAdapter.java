package edu.illinois.instrument;

import edu.illinois.Config;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigClassAdapter extends ClassVisitor {
    public ConfigClassAdapter(final ClassVisitor classVisitor) {
        super(Config.ASMVersion, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
        if ("get".equals(name) && "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;".equals(descriptor) ||
                "set".equals(name) && "(Ljava/lang/String;)V".equals(descriptor)) {
            return new ConfigMethodAdapter(mv);
        }
        return mv;
    }
}