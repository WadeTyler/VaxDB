import lib.utils.JSON;
import models.Model;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class Table {

    private String tableName;
    private Model model;

    // Store Data as a hashmap with an id that points to a notation of the object
    public HashMap<String, Object> data = new HashMap<>();

    public Table(String tableName, Model model) {
        this.tableName = tableName;
        this.model = model;
    }

    public void addEntry(String key, Object obj) throws Exception {
        if (!obj.getClass().getName().equals(model.name)) {
            throw new Exception("The class " + obj.getClass().getName() + " does not match the model of the \"" + tableName + "\" table.");
        }

        // Check if key already exists
        if (data.containsKey(key)) {
            throw new Exception("A data entry with the key \"" + key + "\" already exists.");
        }

        data.put(key, obj);
    }

    public void removeEntry(String key) throws Exception {

        if (this.data.get(key) == null) {
            throw new Exception("No data entry with the key \"" + key + "\" exists.");
        }
        this.data.remove(key);
    }


    public String selectAll() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        String output = "";

        for (String key : data.keySet()) {
            output += key + ": " + JSON.toJSON(data.get(key)) + "\n";
        }

        return output;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

}
