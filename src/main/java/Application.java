import lib.utils.Colors;
import lib.utils.ErrorMessage;
import models.Attribute;
import models.Model;
import org.w3c.dom.UserDataHandler;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class Application {

    public static File modelsFile;
    public static ArrayList<Model> models = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Retrieve the modelsFile then load all models.
            loadModelsFile();
            loadAllModels();




        } catch (Exception e) {
            System.out.println(new ErrorMessage("An error occurred: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // Load models file
    public static void loadModelsFile() {
        try {
            File modelsFileObj = new File("src/main/java/models/models.txt");

            if (modelsFileObj.createNewFile()) {
                System.out.println("Models file generated: " + modelsFileObj.getName());
            } else {
                System.out.println("Models file found.");
            }
            modelsFile = modelsFileObj;
        } catch (IOException e) {
            System.out.println(new ErrorMessage("An error occurred while loading the models file."));
        }
    }

    // Load all models from modelsFile then set them to the models arrayList
    public static void loadAllModels() throws FileNotFoundException {
        try {

            System.out.println("Loading Models...");
            ArrayList<Model> modelsList = new ArrayList<>();
            Scanner scanner = new Scanner(modelsFile);

            Stack<Character> bracketStack = new Stack<>();
            ArrayList<Attribute> attributes = new ArrayList<>();

            String className = "";
            String atbName = "";
            String atbType = "";

            // Parse modelsFile and add to modelsList
            while (scanner.hasNext()) {
                String next = scanner.next();

                // Handle Brackets
                for (int i = 0; i < next.length(); i++) {
                    if (next.charAt(i) == '{') {
                        bracketStack.push('{');
                    }
                    else if (next.charAt(i) == '}') {
                        bracketStack.pop();
                    }
                }

                // If at the end of an object
                if (bracketStack.isEmpty() && !className.isEmpty() && !attributes.isEmpty()) {
                    // Add Model and then reset values
                    Object[] atbs = attributes.toArray();

                    Model model = new Model(className, attributes.toArray(new Attribute[0]));
                    modelsList.add(model);

                    // Reset values
                    className = "";
                    attributes = new ArrayList<>();
                    atbName = "";
                    atbType = "";
                }

                // Inside an object, set name
                if (bracketStack.size() == 1) {
                    if (next.contains("name")) {
                        className = scanner.next().split("\"")[1];
                    }
                }

                // Inside an attribute,
                if (bracketStack.size() == 2) {
                    if (next.contains("atb_name")) {
                        atbName = scanner.next().split("\"")[1];
                    }

                    if (next.contains("type")) {
                        atbType = scanner.next().split("\"")[1];
                    }

                    if (!atbType.isEmpty() && !atbName.isEmpty()) {
                        attributes.add(new Attribute(atbName, atbType));
                        atbType = "";
                        atbName = "";
                    }
                }

            }

            System.out.println("Models Done Loading.");
            models = modelsList;
            System.out.println(models);


        } catch (Exception e) {
            System.out.println(Colors.ANSI_RED + "An error occurred loading all models: " + e.getMessage() + Colors.ANSI_RESET);
            e.printStackTrace();
            throw new FileNotFoundException();
        }
    }

    // Creates a new model using the provided class structure.
    // Only fields with getter methods following proper camel case will be created.
    public static void createModel(Class<?> clazz) throws Exception {
        try {
            String modelName = clazz.getName();

            // Check if model exists
            if (checkModelExists(modelName)) {
                throw new Exception("A Model with the name \"" + modelName + "\" already exists. A new model was not created.");
            }

            // Store all the fields that contain a getter method using camelCase
            Field[] allFields = clazz.getDeclaredFields();
            ArrayList<Field> fields = new ArrayList<>();

            for (Field field : allFields) {
                if (hasGetterMethod(clazz, field)) {
                    fields.add(field);
                }
            }

            Model model = new Model(modelName, fields);

            // Write model to modelsFile
            FileWriter writer = new FileWriter(modelsFile, true);
            String writeStr = "";

            if (modelsFile.length() != 0) {
                writeStr += ", ";
            }
            writeStr += model.toJSON();

            writer.write(writeStr);
            writer.close();

            // Add model to models list
            models.add(model);
        } catch (Exception e) {
            System.out.println(new ErrorMessage("An error occurred while creating a new model: " + e.getMessage(), Colors.ANSI_YELLOW));
        }
    }

    // Returns true if the model exists, false if not
    public static boolean checkModelExists(String modelName) {

        // Check each model in models arraylist and compare names.
        for (Model model : models) {
            if (model.name.equals(modelName)) {
                return true;
            }
        }

        return false;

    }

    // Check if the field has a getter method
    public static boolean hasGetterMethod(Class<?> clazz, Field field) {
        String fieldName = field.getName();
        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        try {
            clazz.getMethod(getterName);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

}
