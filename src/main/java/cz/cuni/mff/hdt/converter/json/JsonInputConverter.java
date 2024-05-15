package cz.cuni.mff.hdt.convertor.json;

import java.io.IOException;
import java.io.InputStream;

import javax.management.RuntimeErrorException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.cuni.mff.hdt.convertor.InputConvertor;
import cz.cuni.mff.hdt.ur.Ur;

public class JsonInputConvertor implements InputConvertor {

    @Override
    public Ur convert(InputStream input) throws IOException {
        String inputString = new String(input.readAllBytes());

        try {
            var object = new JSONObject(inputString);
            return getUr(object);
        }
        catch (JSONException e) {
            try {
                var array = new JSONArray(inputString);
                return getUr(array);
            }
            catch (JSONException ex) {
                throw new IOException("There is no JSON in the input.");
            }
        }
        finally {}
    }

    private static Ur getUr(JSONObject inputJson) {
        var urObject = getJsonUrRecursive(inputJson);
        return new Ur(urObject);
    }

    private static Ur getUr(JSONArray inputJson) {
        var urObject = getJsonUrRecursive(inputJson);
        return new Ur(urObject);
    }

    private static JSONObject getJsonUrRecursive(JSONObject object) {
        var urObject = new JSONObject();
        urObject.append(Ur.KEY_TYPE, Ur.VALUE_OBJECT);

        for (var key : object.keySet()) {
            Object value = object.get(key);

            if (value instanceof JSONObject) {
                urObject.append(key, arrayWrap(getJsonUrRecursive((JSONObject)value)));
            }
            else if (value instanceof JSONArray) {
                urObject.append(key, arrayWrap(getJsonUrRecursive((JSONArray)value)));
            }
            else { // is primitive
                urObject.append(key, arrayWrap(getUrPrimitive(value)));
            }
        }

        return urObject;
    }

    private static JSONObject getJsonUrRecursive(JSONArray array) {
        var urObject = new JSONObject();
        urObject.append(Ur.KEY_TYPE, Ur.VALUE_ARRAY);

        Integer index = 1;
        for (var value : array) {
            if (value instanceof JSONObject) {
                urObject.append(index.toString(), arrayWrap(getJsonUrRecursive((JSONObject)value)));
            }
            else if (value instanceof JSONArray) {
                urObject.append(index.toString(), arrayWrap(getJsonUrRecursive((JSONArray)value)));
            }
            else { // is primitive
                urObject.append(index.toString(), arrayWrap(getUrPrimitive(value)));
            }

            index++;
        }

        return urObject;
    }

    private static JSONObject getUrPrimitive(Object value) {
        if (value instanceof String) {
            return new JSONObject()
                .append(Ur.KEY_TYPE, Ur.VALUE_STRING)
                .append(Ur.KEY_VALUE, value.toString());
        }
        else if (value instanceof Boolean) {
            return new JSONObject()
                .append(Ur.KEY_TYPE, Ur.VALUE_BOOLEAN)
                .append(Ur.KEY_VALUE, value.toString());
        }
        else if (value instanceof Number) {
            return new JSONObject()
                .append(Ur.KEY_TYPE, Ur.VALUE_NUMBER)
                .append(Ur.KEY_VALUE, value.toString());
        }
        else {
            // TODO
            throw new RuntimeErrorException(null, "Unsupported type in JSON input.");
        }
    }

    // TODO move to somewhere else
    private static JSONArray arrayWrap(Object value) {
        return new JSONArray().put(value);
    }
    
}
