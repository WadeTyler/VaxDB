package lib.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class JSON {


    // Convert All public getAttributes
    public static String toJSON(Object obj) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Field[] allFields = obj.getClass().getDeclaredFields();
        ArrayList<Field> fields = new ArrayList<>();

        String outputStr = "{";
        for (Field field : allFields) {
            if (hasGetterMethod(obj.getClass(), field)) {
                fields.add(field);
            }
        }

        // iterate over all fields that have a getMethod, and add their values to the outputStr
        for (Field field : fields) {
            String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
            Method m = obj.getClass().getDeclaredMethod(getterName);
            Object value = m.invoke(obj);
            outputStr += "\"" + field.getName() + "\": \"" + value + "\"";

            if (field != fields.get(fields.size() - 1)) {
                outputStr += ", ";
            }
        }

        outputStr += "},";

        return outputStr;
    }

    private static boolean hasGetterMethod(Class<?> clazz, Field field) {
        String fieldName = field.getName();
        String getterName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);

        try {
            clazz.getMethod(getterName);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }


}
