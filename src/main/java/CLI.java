import lib.utils.ErrorMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Stack;

public class CLI {

    private static boolean cliStarted = false;

    public static void startCLI() {
        cliStarted = true;

        Scanner scanner = new Scanner(System.in);

        String userInput = "";
        while (!userInput.split("\\s+")[0].equals("exit")) {
            System.out.print("VaxDB % ");

            userInput = scanner.nextLine();
            // check for ';'
            while (userInput.charAt(userInput.length() - 1) != ';') {
                userInput += " ";
                System.out.print("VaxDB -> ");
                userInput += scanner.nextLine();
            }

            System.out.println("Input: " + userInput);
            // Remove ;
            userInput = userInput.substring(0, userInput.length() - 1);
            if (!userInput.split("\\s+")[0].equals("exit")) parseUserInput(userInput);
        }
    }

    private static void parseUserInput(String userInput) {
        try {
            String[] splitInput = userInput.split("\\s+");

            switch (splitInput[0].toLowerCase()) {
                case "create":
                    parseCreateCommand(splitInput);
                    break;
                case "remove":
                    parseRemoveCommand(splitInput);
                    break;
                case "entry":
                    parseEntryCommand(splitInput);
                    break;
                case "reset":
                    handleReset();
                    break;
                default:
                    System.out.println("Unknown command: '" + splitInput[0] + "'.");
                    break;
            }
        } catch (Exception e) {
            System.out.println("An exception occurred: " + e.getMessage());
        }


    }

    private static void parseCreateCommand(String[] splitInput) {
        switch (splitInput[1].toLowerCase()) {
            case "table":

                if (splitInput.length < 5) {
                    System.out.println("Invalid command. Expected 5 inputs: 'create table \"table_name\" -> \"model_name\" ;'.");
                    return;
                }

                // Check for quotes
                if (!isQuoted(splitInput[2])) {
                    System.out.println("Unexpected input: '" + splitInput[2] + "'. Expected \"table_name\" encased in quotes.");
                    return;
                }

                String tableName = splitInput[2].substring(1, splitInput[2].length() - 1);

                // Check for ->
                if (!splitInput[3].equals("->")) {
                    System.out.println(new ErrorMessage("Unexpected input: '" + splitInput[3] + "'. Expected '->'."));
                    return;
                }

                // Check for quotes
                if (!isQuoted(splitInput[4])) {
                    System.out.println("Unexpected input: '" + splitInput[4] + "'. Expected \"model_name\" encased in quotes.");
                    return;
                }

                String modelName = splitInput[4].substring(1, splitInput[4].length() - 1);

                createTable(tableName, modelName);
                break;

            default:
                System.out.println("Unknown input: '" + splitInput[1] + "'.");
                break;
        }
    }

    private static void parseRemoveCommand(String[] splitInput) {
        switch (splitInput[1].toLowerCase()) {
            case "model":

                if (splitInput.length < 3) {
                    System.out.println("Expected 3 arguments: remove model \"model_name\";");
                    return;
                }
                handleRemoveModel(splitInput);
                break;

            case "table":
                handleRemoveTable(splitInput);
                break;

            default:
                System.out.println("Unknown input: " + splitInput[1]);
        }
    }


    private static void parseEntryCommand(String[] splitInput) throws Exception {

        if (splitInput.length < 2) {
            System.out.println("Invalid Command.");
            return;
        }

        switch (splitInput[1].toLowerCase()) {
            case "add":
                // Add entry

                if (splitInput.length < 7) {
                    System.out.println("Expected at least 7 fields. entry add \"table_name\" -> \"key\" -> atb:<val> atb:<val>;");
                }

                // splitInput[2] = "table_name"
                if (!isQuoted(splitInput[2])) {
                    outputUnexpectedInput(splitInput[2], "\"table_name\"");
                    return;
                }
                String tableName = splitInput[2].substring(1, splitInput[2].length() - 1);

                // GET TABLE CLASS

                Table table = VaxDB.tables.get(tableName);

                if (table == null) {
                    System.out.println("No table with the name '" + tableName + "' found.");
                    return;
                }

                Model model = table.getModel();
                String modelName = model.name;

                Class<?> clazz = Class.forName("models." + modelName);
                Object obj = clazz.getDeclaredConstructor().newInstance();

                // splitInput[3] = "->"
                if (!isNextKey(splitInput[3])) {
                    outputUnexpectedInput(splitInput[3], "->");
                    return;
                }

                // splitInput[4] = "key"
                if (!isQuoted(splitInput[4])) {
                    outputUnexpectedInput(splitInput[4], "\"key\"");
                    return;
                }

                String key = splitInput[4].substring(1, splitInput[4].length() - 1);

                // splitInput[5] = "->"
                if (!isNextKey(splitInput[5])) {
                    outputUnexpectedInput(splitInput[5], "->");
                    return;
                }

                // the rest of the values are equal to atb:<val>

                String attributesStr = "";

                for (int i = 6; i < splitInput.length; i++) {
                    attributesStr += splitInput[i];
                }

                String[] attributes = attributesStr.split(",");

                // Set the values in the object
                for (String attribute : attributes) {

                    String atb = attribute.split(":")[0];
                    String val = attribute.split(":")[1];

                    if (!isQuoted(val)) {
                        outputUnexpectedInput(val, "\"val\" enclosed in quotes.");
                        return;
                    }

                    val = val.substring(1, val.length() - 1);

                    String setterName = "set" + Character.toUpperCase(atb.charAt(0)) + atb.substring(1);

                    Method setter = clazz.getMethod(setterName, String.class);
                    setter.invoke(obj, val);

                }

                VaxDB.createEntry(tableName, key, obj);
                break;

            case "select":

                if (splitInput.length < 4) {
                    System.out.println("Expected 4 fields: entry select \"key|all\" \"table_name\"");
                    return;
                }
                if (!isQuoted(splitInput[2])) {
                    outputUnexpectedInput(splitInput[2], "\"key\" enclosed in quotes.");
                    return;
                }

                if (!isQuoted(splitInput[3])) {
                    outputUnexpectedInput(splitInput[3], "\"table_name\" enclosed in quotes.");
                    return;
                }

                String table_name = splitInput[3].substring(1, splitInput[3].length() - 1);

                Table t = VaxDB.tables.get(table_name);
                if (t == null) {
                    System.out.println("Table '" + table_name + "' not found.");
                    return;
                }

                String entry_key = splitInput[2].substring(1, splitInput[2].length() - 1);



                if (entry_key.equals("all")) {
                    ArrayList<String> entries = VaxDB.selectAllEntires(table_name);
                    for (String entry : entries) {
                        System.out.println(entry);
                    }
                }

                else System.out.println(VaxDB.selectEntry(table_name, entry_key));;
                break;

            case "remove":

                if (splitInput.length < 5) {
                    System.out.println("Invalid input. Expected 5 arguments: entry remove \"table_name\" -> \"key\";");
                    return;
                }

                if (!isQuoted(splitInput[2])) {
                    outputUnexpectedInput(splitInput[2], "\"table_name\"");
                    return;
                }

                String table_name_remove = splitInput[2].substring(1, splitInput[2].length() - 1);

                if (!isNextKey(splitInput[3])) {
                    outputUnexpectedInput(splitInput[2], "->");
                    return;
                }

                if (!isQuoted(splitInput[4])) {
                    outputUnexpectedInput(splitInput[4], "\"key\"");
                    return;
                }

                String key_remove = splitInput[4].substring(1, splitInput[4].length() - 1);


                VaxDB.removeEntry(table_name_remove, key_remove);


                break;
            default:
                System.out.println("Unknown input '" + splitInput[1] + "'.");
                break;
        }
    }

    private static void createTable(String tableName, String modelName) {
        VaxDB.createTable(tableName, modelName);
    }

    private static void handleReset() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("You are about to reset VaxDB, deleting all of your tables and models. [y/n]: ");
        String input = scanner.next();
        if (!input.equalsIgnoreCase("y")) {
            System.out.println("\nReset cancelled.");
            return;
        }

        VaxDB.resetDB();
    }

    private static void handleRemoveModel(String[] splitInput) {

        if (splitInput.length < 3) {
            System.out.println("Expected 3 arguments: remove model \"model_name\";");
            return;
        }

        if (!isQuoted(splitInput[2])) {
            outputUnexpectedInput(splitInput[2], "\"table_name\"");
            return;
        }

        String tableName = splitInput[2].substring(1, splitInput[2].length() - 1);
        VaxDB.removeModel(tableName);
    }

    private static void handleRemoveTable(String[] splitInput) {

        if (splitInput.length < 3) {
            System.out.println("Expected 3 arguments: remove table \"table_name\";");
            return;
        }

        if (!isQuoted(splitInput[2])) {
            outputUnexpectedInput(splitInput[2], "\"table_name\"");
            return;
        }

        String tableName = splitInput[2].substring(1, splitInput[2].length() - 1);
        VaxDB.removeTable(tableName);
    }


    // ----------------- UTIL -----------------

    private static boolean isQuoted(String str) {
        return str.charAt(0) == '\"' && str.charAt(str.length() - 1) == '\"';
    }

    private static boolean isNextKey(String str) {
        return str.equals("->");
    }

    private static void outputUnexpectedInput(String unexpected, String expected) {
        System.out.println("Unexpected input: '" + unexpected + "'. Expected: '" + expected + "'.");
    }


}
