package cz.cuni.mff.hdt.utils;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Helper class for parsing primitives like booleans, numbers, strings
 */
public class PrimitiveParser {
    public static Ur.Type getPrimitiveType(String value) {
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Ur.Type.Boolean;
        }
        else if (isNumeric(value)) {
            return Ur.Type.Number;
        }
        else {
            return Ur.Type.String;
        }
    }

    public static boolean isNumeric(String value) {
        if (value == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(value);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }
}
