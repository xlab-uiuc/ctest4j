package edu.illinois.agent;

import edu.illinois.Config;
import org.objectweb.asm.*;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigTransformer implements ClassFileTransformer {

    /**
     * Method adapter for the Configuration class
     * Add the tracking method call to the beginning of the get() and set() method
     */
    private static class ConfigMethodAdapter extends MethodVisitor {
        public ConfigMethodAdapter(final MethodVisitor methodVisitor) {
            super(Config.ASMVersion, methodVisitor);
        }

        @Override
        public void visitCode() {
            mv.visitCode();
            mv.visitVarInsn(Opcodes.ALOAD, 0); // Assuming parameter of interest is always the first one
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Config.TRACKER_CLASS_NAME, Config.TRACKER_METHOD_NAME, Config.TRACKER_METHOD_SIGNATURE, false);
        }
    }

    /**
     * Class adapter for the Configuration class
     * Add the tracking method call to the beginning of the get() and set() method
     */
    private static class ConfigClassAdapter extends ClassVisitor {
        public ConfigClassAdapter(final ClassVisitor classVisitor) {
            super(Config.ASMVersion, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
            if ("get".equals(name) && "(Ljava/lang/String;)Ljava/lang/String;".equals(descriptor) ||
                    "set".equals(name) && "(Ljava/lang/String;)V".equals(descriptor)) {
                mv = new ConfigTransformer.ConfigMethodAdapter(mv);
            }
            return mv;
        }
    }

    /**
     * The implementation of the ClassFileTransformer interface.
     * Only transform the Configuration class.
     * @param loader                the defining loader of the class to be transformed,
     *                              may be {@code null} if the bootstrap loader
     * @param className             the name of the class in the internal form of fully
     *                              qualified class and interface names as defined in
     *                              <i>The Java Virtual Machine Specification</i>.
     *                              For example, <code>"java/util/List"</code>.
     * @param classBeingRedefined   if this is triggered by a redefine or retransform,
     *                              the class being redefined or retransformed;
     *                              if this is a class load, {@code null}
     * @param protectionDomain      the protection domain of the class being defined or redefined
     * @param classfileBuffer       the input byte buffer in class file format - must not be modified
     *
     * @return transformed configuration class, or null.
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        // TODO: Change the hardcode class to the class name from the annotation
        System.out.println("Loaded Class From transform(): " + className);
        if (className.contains("Configuration")) {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
            ClassVisitor visitor = new ConfigClassAdapter(classWriter);
            classReader.accept(visitor, 0);
            return classWriter.toByteArray();
        }
        return null;
    }


    // Internal

    public static void prepare(String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
    }

    public static void write(final String path,final byte[] bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    prepare(path);
                    Files.write(Paths.get(path), bytes);
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String args[]) throws MalformedURLException, ClassNotFoundException {
        // Load the Configuration.java file and transform it with the function above
        // Save the transformed file to a new file

    }
}
