package cz.cuni.mff.hdt.utils;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Helper class for parsing primitives like booleans, numbers, strings
 */
public class ParserHelper {
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

    public static String[] getStringTokens(JSONArray array) throws IOException {
        ArrayList<String> tokenList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                tokenList.add(array.getString(i));
            }
            catch (JSONException e) {
                throw new IOException("Incorrect type of token in array");
            }
        }
        return tokenList.toArray(new String[tokenList.size()]);
    }
}
