package lib.utils;

public class ErrorMessage {

    private String color = Colors.ANSI_RED;
    private String message;

    public ErrorMessage(String message) {
        this.message = message;
    }

    public ErrorMessage(String message, String color) {
        this.message = message;
        this.color = color;
    }

    @Override
    public String toString() {
        return color + message + Colors.ANSI_RESET;
    }

}
