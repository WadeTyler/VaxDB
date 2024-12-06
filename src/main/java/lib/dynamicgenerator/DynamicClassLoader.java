package lib.dynamicgenerator;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class DynamicClassLoader {
    public static Class<?> loadClass(String className, String classDirectory) throws Exception {
        File classFile = new File(classDirectory, className + ".class");
        URL classUrl = classFile.toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{classUrl});

        // Load the class
        return classLoader.loadClass(className);
    }
}