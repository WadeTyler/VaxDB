import lib.utils.JSON;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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

    public ArrayList<String> selectAll() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ArrayList<String> list = new ArrayList<>();


        for (String key : data.keySet()) {
            String str = key + ": ";
            str += JSON.toJSON(data.get(key));
            str += "\n";
            list.add(str);
        }
        return list;
    }

    public String select(String key) throws Exception {
        if (data.get(key) == null) {
            throw new Exception("No entry with the key '" + key + "' found.");
        }

        return key + ": " + JSON.toJSON(data.get(key));
    }

    public void addEntry(String key, Object obj) throws Exception {
        if (!obj.getClass().getSimpleName().equals(model.name)) {
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
