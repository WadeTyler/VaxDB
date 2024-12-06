import models.Model;

import java.util.ArrayList;
import java.util.HashMap;

public class Table {

    private String tableName;
    private Model model;

    // Store Data as a hashmap with an id that points to a notation of the object
    public HashMap<String, Object> data;

    public Table(String tableName, Model model) {
        this.tableName = tableName;
        this.model = model;
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
