package lib.dynamicgenerator;

import java.io.FileWriter;
import java.io.IOException;

public class DynamicClassGenerator {
    public static void createJavaFile(String className, String code) throws Exception {
        String fileName = "src/main/java/models/" + className + ".java";
        try {
            FileWriter writer = new FileWriter(fileName);

            writer.write(code);
            writer.flush();
            writer.close();

            System.out.println("Generated Java file: " + fileName);
        } catch (Exception e) {
            throw new Exception("Error while creating java file: " + e.getMessage());
        }
    }
}
