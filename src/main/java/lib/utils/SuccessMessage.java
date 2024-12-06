package lib.utils;

public class SuccessMessage {

    private String color = Colors.ANSI_GREEN;
    private String message;

    public SuccessMessage(String color, String message) {
        this.color = color;
        this.message = message;
    }

    public SuccessMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return color + message + Colors.ANSI_RESET;
    }
}
