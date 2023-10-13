package edu.illinois.instrument;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigClassAdapter extends ClassVisitor {

    public ConfigClassAdapter(final ClassVisitor classVisitor) {
        super(Opcodes.ASM9, classVisitor);
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