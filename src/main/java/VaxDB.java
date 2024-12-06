import lib.utils.Colors;
import lib.utils.ErrorMessage;
import lib.utils.JSON;
import lib.utils.SuccessMessage;
import models.Attribute;
import models.Model;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;

public class VaxDB {

    // Files
    public static File modelsFile;
    public static ArrayList<File> tableFiles = new ArrayList<>();
    public static String dir_tables = "src/main/java/tables";
    public static String dir_models = "src/main/java/models/models.txt";

    // Data Structures
    public static ArrayList<Model> models2 = new ArrayList<>();
    public static HashMap<String, Model> models = new HashMap<>();      // <modelName, model>
    public static HashMap<String, Table> tables = new HashMap<>();      // <tableName, table>

    public static void main(String[] args) {
        try {
            // Retrieve the modelsFile then load all models.
            loadModelsFile();
            loadAllModels();
            loadAllTables();

            removeEntry("history_books", "2");

            System.out.println(tables.get("history_books").selectAll());



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
            File modelsFileObj = new File(dir_models);

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


            // Assign in hashmap
            for (Model model : modelsList) {
                models.put(model.name, model);
            }

            System.out.println("\n" + getTimestamp() + new SuccessMessage("Models Done Loading."));
            System.out.println(getTimestamp() + "---------- MODELS ----------");

            for (String key : models.keySet()) {
                System.out.println(getTimestamp() + key);
            }

            System.out.println(getTimestamp() + "----------------------------");

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
            models.put(model.name, model);
        } catch (Exception e) {
            System.out.println(getTimestamp() + new ErrorMessage("An error occurred while creating a new model: " + e.getMessage(), Colors.ANSI_YELLOW));
        }
    }

    // Returns true if the model exists, false if not
    public static boolean checkModelExists(String modelName) {
        return models.get(modelName) != null;
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
    public static Model getModelFromName(String modelName) {
        return models.get(modelName);
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
                System.out.println(getTimestamp() + new SuccessMessage("Table Created with the name: " + tableName));
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
            File folder = new File(dir_tables);
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
                ArrayList<Object> objects = new ArrayList<>();
                Class<?> clazz = Class.forName(model.name);
                while (scanner.hasNextLine()) {

                    String line = scanner.nextLine();
                    if (line.isEmpty()) continue;

                    String key = line.split(": ", 2)[0].trim();

                    String jsonPart = line.split(": ", 2)[1].trim();
                    jsonPart = jsonPart.substring(1, jsonPart.length() - 1);

                    String[] keyValuePairs = jsonPart.split(", ");
                    Object obj = clazz.getDeclaredConstructor().newInstance();

                    for (String pair : keyValuePairs) {
                        String[] keyValue = pair.split(":");
                        String keyField = keyValue[0].trim().replaceAll("\"", "");
                        String value = keyValue[1].trim().replaceAll("\"", "");

                        // Set Values
                        String setterName = "set" + Character.toUpperCase(keyField.charAt(0)) + keyField.substring(1);
                        Method setter = clazz.getMethod(setterName, String.class);
                        setter.invoke(obj, value);


                    }

                    table.addEntry(key, obj);

                }

                tables.put(tableName, table);

                scanner.close();
            }

            System.out.println(getTimestamp() + new SuccessMessage("Tables Done Loading."));
            System.out.println(getTimestamp() + "---------- TABLES ----------");

            for (String key : tables.keySet()) {
                System.out.println(getTimestamp() + "Table Name: " + tables.get(key).getTableName() + " - Model: " + tables.get(key).getModel().name);
            }

            System.out.println(getTimestamp() + "----------------------------");

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

    // Create Entry to a Table
    public static void createEntry(String tableName, String key, Object obj) {
        try {

            // Check key value
            if (key.isEmpty()) {
                throw new Exception("Empty key value not allowed.");
            }

            if (key.contains(" ")) {
                throw new Exception("Space(s) not allowed in key value.");
            }

            // Check if table exists
            if (!checkTableFileExists(tableName)) {
                throw new Exception("No table with the name \"" + tableName + "\" exists.");
            }

            // Add data to table
            Table table = tables.get(tableName);
            table.addEntry(key, obj);

            // Add data to tablefile
            File tableFile = new File(dir_tables + "/" + tableName + ".table.txt");


            System.out.println(JSON.toJSON(obj));

            FileWriter writer = new FileWriter(dir_tables + "/" + tableName + ".table.txt", true);

            writer.write("\n" + key + ": " + JSON.toJSON(obj));
            writer.close();

            System.out.println(getTimestamp() + new SuccessMessage("New data entry added to " + tableName + " with key: \"" + key + "\""));
        } catch (Exception e) {
            System.out.println(getTimestamp() + new ErrorMessage("An error occurred while creating a data entry: " + e.getMessage()));
        }
    }


    // Remove Entry from a table
    public static void removeEntry(String tableName, String key) {
        try {

            System.out.println("Attempting to remove data entry with the key \"" + key + "\"");
            // Remove from table structure
            Table table = tables.get(tableName);

            if (table == null) {
                throw new Exception("No table with the name \"" + tableName + "\" exists.");
            }

            table.removeEntry(key);


            // Rewrite file without the entry
            String filePath = dir_tables + "/" + tableName + ".table.txt";
            Path path = Paths.get(filePath);

            BufferedReader reader = new BufferedReader(new FileReader(dir_tables + "/" + tableName + ".table.txt"));
            StringBuilder newFileContent = new StringBuilder();

            String line;

            while ((line = reader.readLine()) != null) {
                if (line.split(": ")[0].trim().equals(key)) continue;
                newFileContent.append(line).append(System.lineSeparator());
            }
            reader.close();

            Files.write(path, newFileContent.toString().getBytes());
            System.out.println(getTimestamp() + new SuccessMessage("Data Entry Removed"));

        } catch (Exception e) {
            System.out.println(getTimestamp() + new ErrorMessage("An exception occurred while removing an entry from " + tableName + ": " + e.getMessage(), Colors.ANSI_YELLOW));
        }
    }


}
