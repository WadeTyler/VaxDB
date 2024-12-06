package models;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class Model {

    public String name;
    public ArrayList<Attribute> attributes = new ArrayList<>();

    public Model(String name, ArrayList<Field> fields) {
        this.name = name;

        for (Field field : fields) {
            String type = field.getType().getSimpleName();
            Attribute atb = new Attribute(field.getName(), type);
            attributes.add(atb);
        }
    }

    public Model(String name, Attribute[] attributes) {
        this.name = name;
        for (Attribute atb : attributes) {
            this.attributes.add(atb);
        }
    }

    public String toJSON() {
        String str = "{" +
                "\"name\": \"" + name + "\", \"attributes\": [";
        for (Attribute atb : attributes) {
            str += "{\"atb_name\": \"" + atb.atb_name + "\", ";
            str += "\"type\": \"" + atb.type + "\" }";
            if (atb != attributes.get(attributes.size() - 1)) {
                str += ",";
            }
        }
        str += "]";
        str += "}";

        return str;

    }

    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}



