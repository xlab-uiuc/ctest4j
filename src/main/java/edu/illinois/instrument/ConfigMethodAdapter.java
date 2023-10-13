package edu.illinois.instrument;

import edu.illinois.Config;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigMethodAdapter extends MethodVisitor {

    public ConfigMethodAdapter(final MethodVisitor methodVisitor) {
        super(Config.ASMVersion, methodVisitor);
    }

    @Override
    public void visitCode() {
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0); // Assuming parameter of interest is always the first one
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "path/to/ConfigTracker", "markParamAsUsed", "(Ljava/lang/String;)V", false);
    }
}

