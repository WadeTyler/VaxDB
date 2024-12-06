import lib.utils.Colors;
import lib.utils.ErrorMessage;
import lib.utils.SuccessMessage;
import models.Attribute;
import models.Model;
import org.w3c.dom.UserDataHandler;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class Application {

    // Files
    public static File modelsFile;
    public static ArrayList<File> tableFiles = new ArrayList<>();

    // Data Structures
    public static ArrayList<Model> models = new ArrayList<>();
    public static HashMap<String, Table> tables = new HashMap<>();      // <tableName, table>

    public static void main(String[] args) {
        try {
            // Retrieve the modelsFile then load all models.
            loadModelsFile();
            loadAllModels();

            loadAllTables();

        } catch (Exception e) {
            System.out.println(new ErrorMessage("An error occurred: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    public static String getTimestamp() {
        return LocalDateTime.now().toLocalTime().toString() + " - ";
    }


    // ---------------------------- MODELS ----------------------------

    // Load models file
    public static void loadModelsFile() {
        try {
            File modelsFileObj = new File("src/main/java/models/models.txt");

            if (modelsFileObj.createNewFile()) {
                System.out.println(getTimestamp() + "Models file created: " + modelsFileObj.getName());
            } else {
                System.out.println(getTimestamp() + "Models file found.");
            }
            modelsFile = modelsFileObj;
        } catch (IOException e) {
            System.out.println(getTimestamp() + new ErrorMessage("An error occurred while loading the models file."));
        }
    }

    // Load all models from modelsFile then set them to the models arrayList
    public static void loadAllModels() throws FileNotFoundException {
        try {

            System.out.println(getTimestamp() + "Loading Models...");
            ArrayList<Model> modelsList = new ArrayList<>();
            Scanner scanner = new Scanner(modelsFile);

            Long modelsFileLength = modelsFile.length();
            int counter = 0;

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
                        counter++;
                    }
                }

                // Inside an attribute,
                if (bracketStack.size() == 2) {
                    if (next.contains("atb_name")) {
                        atbName = scanner.next().split("\"")[1];
                        counter++;
                    }

                    if (next.contains("type")) {
                        atbType = scanner.next().split("\"")[1];
                        counter++;
                    }

                    if (!atbType.isEmpty() && !atbName.isEmpty()) {
                        attributes.add(new Attribute(atbName, atbType));
                        atbType = "";
                        atbName = "";
                    }
                }


                counter++;
                System.out.print("\r" + getTimestamp() + "Loading Models: " + Math.floor(((counter / (double) modelsFileLength) * 1000)) + "%     ");
            }

            System.out.println("\n" + getTimestamp() + new SuccessMessage("Models Done Loading."));
            models = modelsList;

        } catch (Exception e) {
            System.out.println(getTimestamp() + Colors.ANSI_RED + "An error occurred loading all models: " + e.getMessage() + Colors.ANSI_RESET);
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
            System.out.println(getTimestamp() + new ErrorMessage("An error occurred while creating a new model: " + e.getMessage(), Colors.ANSI_YELLOW));
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

    // GetModelFromName
    public static Model getModelFromName(String modelName) throws Exception {
        for (Model model : models) {
            if (model.name.equals(modelName)) {
                return model;
            }
        }

        throw new Exception("Error getting model from name: No model with the name \"" + modelName + "\" found.");
    }


    // ---------------------------- TABLES ----------------------------

    // Create table
    public static void createTable(String tableName, String modelName) {
        try {
            if (checkTableFileExists(tableName)) {
                System.out.println(getTimestamp() + new ErrorMessage("A table with the name \"" + tableName + "\" already exists. No table was created."));
                return;
            }

            if (!checkModelExists(modelName)) {
                System.out.println(getTimestamp() + new ErrorMessage("A model with the name \"" + modelName + "\" does not exists. No table was created."));
                return;
            }

            // Create the file
            File tableFile = new File("src/main/java/tables/" + tableName + ".table.txt");
            if (tableFile.createNewFile()) {
                System.out.println("Table created: " + tableFile.getAbsolutePath());
            } else {
                System.out.println(getTimestamp() + new ErrorMessage("A table with the name \"" + tableName + "\" already exists. No table was created."));
                return;
            }

            // Write to file
            FileWriter writer = new FileWriter(tableFile, true);
            writer.append("TABLENAME: \"" + tableName + "\"\n");
            writer.append("MODEL: \"" + modelName + "\"");

            writer.close();

        } catch (Exception e) {
            System.out.println(getTimestamp() + new ErrorMessage("An error occurred creating a table: " + e.getMessage()));
            e.printStackTrace();
        }
    }


    // Load Tables
    public static void loadAllTables() {
        try {

            System.out.println(getTimestamp() + "Loading Tables...");

            // Iterate over all table files
            File folder = new File("src/main/java/tables");
            File[] listOfFiles = folder.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {

                File currentFile = listOfFiles[i];
                Scanner scanner = new Scanner(currentFile);

                // Line 1 is tableName, Line 2 is modelName

                String[] splitLine1 = scanner.nextLine().split("\"");
                String tableName = splitLine1[splitLine1.length - 1];

                String[] splitLine2  = scanner.nextLine().split("\"");
                String modelName = splitLine2[splitLine2.length - 1];

                // Get Model from Model name
                Model model = getModelFromName(modelName);

                // Create Table object
                Table table = new Table(tableName, model);

                // Add data from file to table.data

                tables.put(tableName, table);

                scanner.close();
            }

            System.out.println(getTimestamp() + new SuccessMessage("Tables Done Loading."));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Returns true if the table exists, false if not.
    public static boolean checkTableFileExists(String tableName) {
        for (String key : tables.keySet()) {
            if (key.equals(tableName)) return true;
        }

        return false;
    }




    public static void createTable(Model model) {
        try {
            File folder = new File("src/main/java/tables");
            File[] listOfFiles = folder.listFiles();
        } catch (Exception e) {

        }
    }


}
