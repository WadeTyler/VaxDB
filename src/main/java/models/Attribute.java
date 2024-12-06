package models;

public class Attribute {
    public String atb_name;
    public String type;

    public Attribute(String atb_name, String type) {
        this.atb_name = atb_name;
        this.type = type;
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "atb_name='" + atb_name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}