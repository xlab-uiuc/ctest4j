package edu.illinois;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: Shuai Wang
 * Date:  10/14/23
 */
public class Utils {

    /**
     * Write instrumented class bytes to a file in a new thread
     * @param path the path of the file
     * @param bytes the bytes to write
     */
    public static void writeBytesToFile(final String path, final byte[] bytes) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    File file = new File(path);
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                    }
                    Files.write(Paths.get(path), bytes);
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
        }).start();
    }

    public static List<String> getMethodDescriptorsFromString(String methodDescriptors) {
        if (methodDescriptors != null && !methodDescriptors.isEmpty()) {
            // Split with comma
            return new ArrayList<>(List.of(methodDescriptors.split(",")));
        }
        return null;
    }
}
