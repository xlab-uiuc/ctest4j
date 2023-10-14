package edu.illinois.agent;

import edu.illinois.Log;
import edu.illinois.Names;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static edu.illinois.Log.writeToFile;

/**
 * Author: Shuai Wang
 * Date:  10/13/23
 */
public class ConfigTransformer implements ClassFileTransformer {
    private static String configClassName;

    /** The map of getter/setter method names and their descriptors */
    private static Map<String, List<String>> configGetterMethodMap;
    private static Map<String, List<String>> configSetterMethodMap;

    public ConfigTransformer() {

    }

    /**
     * Method adapter for the Configuration class
     * Add the tracking method call to the beginning of the get() and set() method
     */
    private class ConfigMethodAdapter extends MethodVisitor {
        public ConfigMethodAdapter(final MethodVisitor methodVisitor) {
            super(Names.ASMVersion, methodVisitor);
        }

        @Override
        public void visitCode() {
            mv.visitCode();
            // Get the first parameter of the method and pass it to the tracking method
            mv.visitVarInsn(Opcodes.ALOAD, 1);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, Names.TRACKER_CLASS_NAME, Names.TRACKER_METHOD_NAME, Names.TRACKER_METHOD_SIGNATURE, false);
        }
    }

    /**
     * Class adapter for the Configuration class
     * Add the tracking method call to the beginning of the get() and set() method
     */
    private class ConfigClassAdapter extends ClassVisitor {
        public ConfigClassAdapter(final ClassVisitor classVisitor) {
            super(Names.ASMVersion, classVisitor);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
            /*if ("get".equals(name) && "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;".equals(descriptor) ||
                    "set".equals(name) && "(Ljava/lang/String;)V".equals(descriptor)) {
                mv = new ConfigMethodAdapter(mv);
            }*/
            if (isGetterOrSetter(name, descriptor)) {
                writeToFile("Instrumenting Method: " + name + ", Descriptor: " + descriptor);
                mv = new ConfigMethodAdapter(mv);
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
        parseGetterSetterDescriptor();
        if (!configClassName.equals("null")) {
            if (className.equals(configClassName)) {
                System.out.println("Loaded Class From transform(): " + className);
                ClassReader classReader = new ClassReader(classfileBuffer);
                ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
                ClassVisitor visitor = new ConfigClassAdapter(classWriter);
                classReader.accept(visitor, 0);
                return classWriter.toByteArray();
            }
        }
        return null;
    }

    // Internal

    /**
     * Parse the getter/setter method signature from the system properties
     * The format is "MethodName1(Argument1;Argument2;...;)ReturnType1;,MethodName2(Argument1;Argument2;...;)ReturnType2;..."
     * The example of the getter/setter method signature is:
     * Getter: "get(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;,get(Ljava/lang/String;)Ljava/lang/String;"
     * Setter: "set(Ljava/lang/String;)V"
     */
    private void parseGetterSetterDescriptor() {
        configClassName = System.getProperty("configurationClassName", "null").replaceAll("\\.", "/");
        if (configClassName.equals("null")) {
            return;
        }
        configGetterMethodMap = genTargetMethodMap(System.getProperty("configurationGetterMethod"));
        configSetterMethodMap = genTargetMethodMap(System.getProperty("configurationSetterMethod"));
/*
        if (configClassName.equals("null") || configGetterMethodMap.isEmpty() || configSetterMethodMap.isEmpty()) {
            throw new RuntimeException("To run with ConfigTest.class, " +
                    "please set the configuration class name and the getter/setter method " +
                    "signature in the system properties with the name as " +
                    "configurationClassName, configurationGetterMethod, configurationSetterMethod>.");
        }
*/
    }

    /**
     * Generate a map of method name and method descriptor from the method signature string
     * @param methodSignatures
     * @return
     */
    private Map<String, List<String>> genTargetMethodMap(String methodSignatures) {
        if (methodSignatures == null) {
            return new HashMap<>();
        }
        Map<String, List<String>> targetMethodMap = new HashMap<>();
        List<String> methodSignatureList = new ArrayList<>(List.of(methodSignatures.split(",")));
        for (String methodSignature : methodSignatureList) {
            // find the first index of "("
            int firstIndex = methodSignature.indexOf("(");
            if (firstIndex == -1) {
                System.out.println("Invalid method signature: " + methodSignature);
                continue;
            }
            String name = methodSignature.substring(0, firstIndex).trim();
            String descriptor = methodSignature.substring(firstIndex).trim();
            // add to the map
            if (targetMethodMap.containsKey(name)){
                targetMethodMap.get(name).add(descriptor);
            } else {
                targetMethodMap.put(name, new ArrayList<>(List.of(descriptor)));
            }
        }
        return targetMethodMap;
    }

    private boolean isGetterOrSetter(String methodName, String description) {
        writeToFile("Checking Method Name: " + methodName + ", Description: " + description);
        if (configGetterMethodMap.containsKey(methodName)) {
            return configGetterMethodMap.get(methodName).contains(description);
        } else if (configSetterMethodMap.containsKey(methodName)) {
            return configSetterMethodMap.get(methodName).contains(description);
        } else {
            return false;
        }
    }

}
