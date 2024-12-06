package lib.dynamicgenerator;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class CompilerUtil {
    public static void compileJavaFile(String fileName) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        int result = compiler.run(null, null, null, fileName);
        if (result == 0) {
            System.out.println("Compilation Successful.");
        } else {
            System.out.println("Compilation failed.");
        }
    }
}
