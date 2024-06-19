package cz.cuni.mff.hdt.converter.json;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.converter.UrOutputConverter;
import cz.cuni.mff.hdt.ur.Ur;

/**
 * Converter implementation for converting Unified representation (Ur) to JSON format.
 */
public class JsonOutputConverter extends UrOutputConverter {

    /**
     * Converts the Unified representation (Ur) data to JSON format.
     *
     * @param data the Unified representation (Ur) data to be converted
     * @return a string representing the data in JSON format
     * @throws IOException if an I/O error occurs during conversion
     */
    @Override
    public String convert(Ur data) throws IOException {
        var innerJson = data.getInnerRepresentation();
        var typeValue = getTypeInnerValue(innerJson);
        if (typeValue.equals(Ur.VALUE_OBJECT)) {
            var outputObject = convertObject(innerJson);
            return outputObject.toString(2);
        }
        else if (typeValue.equals(Ur.VALUE_ARRAY)) {
            var outputArray = convertArray(innerJson);
            return outputArray.toString(2);
        }

        throw new IOException("Incorrect Unified representation provided. Wrong type of root.");
    }

    private Object convertRecursive(JSONArray array) throws IOException {
        var object = getInnerObject(array);
        var typeValue = getTypeInnerValue(object);

        if (typeValue.equals(Ur.VALUE_OBJECT)) {
            return convertObject(object);
        }
        else if (typeValue.equals(Ur.VALUE_ARRAY)) {
            return convertArray(object);
        }
        else {
            return convertPrimitive(object);
        }
    }

    private JSONObject convertObject(JSONObject urObject) throws IOException {
        var outputObject = new JSONObject();
        for (var key : urObject.keySet()) {
            if (key.equals(Ur.KEY_TYPE) || key.equals(Ur.KEY_RDF_ID)) {
                continue;
            }
            var value = urObject.get(key);
            assertArray(value);
            outputObject.put(key, convertRecursive((JSONArray)value));
        }
        return outputObject;
    }

    private JSONArray convertArray(JSONObject urArray) throws IOException {
        var outputArray = new JSONArray();
        for (var key : urArray.keySet()) {
            if (key.equals(Ur.KEY_TYPE) || key.equals(Ur.KEY_RDF_ID)) {
                continue;
            }
            var value = urArray.get(key);
            assertArray(value);
            // TODO regex might not be effective
            if (!key.matches("^(0|[1-9][0-9]*)$")) {
                throw new IOException("Incorrect Unified representation provided. Ur array has to have only index keys.");
            }
            outputArray.put(Integer.parseInt(key), convertRecursive((JSONArray)value));
        }
        return outputArray;
    }
}
