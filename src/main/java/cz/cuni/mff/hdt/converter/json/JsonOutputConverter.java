package cz.cuni.mff.hdt.converter.json;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.converter.OutputConverter;
import cz.cuni.mff.hdt.ur.Ur;

/**
 * Converter implementation for converting Unified representation (Ur) to JSON format.
 */
public class JsonOutputConverter implements OutputConverter {

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
        if (typeValue.equals(Ur.VALUE_ARRAY)) {
            return convertArray(object);
        }
        else {
            return convertPrimitive(object);
        }
    }

    private JSONObject convertObject(JSONObject urObject) throws IOException {
        urObject.remove(Ur.KEY_TYPE);
        var outputObject = new JSONObject();
        for (var key : urObject.keySet()) {
            var value = urObject.get(key);
            assertArray(value);
            outputObject.put(key, convertRecursive((JSONArray)value));
        }
        return outputObject;
    }

    private JSONArray convertArray(JSONObject urArray) throws IOException {
        urArray.remove(Ur.KEY_TYPE);
        var outputArray = new JSONArray();
        for (var key : urArray.keySet()) {
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
    
    private Object convertPrimitive(JSONObject urPrimitive) throws IOException {
        var type = getTypeInnerValue(urPrimitive);
        var value = getValueInnerValue(urPrimitive);
        
        if (type.equals(Ur.VALUE_STRING) || type.equals(Ur.VALUE_STRING_URI)) {
            return value.toString();
        }
        else if (type.equals(Ur.VALUE_BOOLEAN) || type.equals(Ur.VALUE_BOOLEAN_URI)) {
            if (value.equals("True") || value.equals("true") || value.equals("TRUE")) {
                return true;
            }
            return false;
        }
        else if (type.equals(Ur.VALUE_NUMBER) || type.equals(Ur.VALUE_INTEGER_URI)) {
            try {
                return Integer.parseInt(value.toString());
            }
            catch (NumberFormatException e) {
                throw new IOException("Incorrect Unified representation provided. Number Primitive in incorrect format.");
            }
        }

        throw new IOException("Incorrect Unified representation provided. Unknown type of Ur Primitive.");
    }

    private JSONObject getInnerObject(JSONArray array) throws IOException {
        assertSingleElementArray(array);
        var potentialObject = array.get(0);
        if (!(potentialObject instanceof JSONObject)) {
            throw new IOException("Incorrect Unified representation provided. Values not wrapped in object.");
        }
        return (JSONObject)potentialObject;
    }

    private String getTypeInnerValue(JSONObject object) throws IOException {
        assertTypeKey(object);
        var type = object.get(Ur.KEY_TYPE);
        assertArray(type);
        var innerArray = (JSONArray)type;
        assertSingleElementArray(innerArray);
        var item = innerArray.get(0);
        return item.toString();
    }

    private String getValueInnerValue(JSONObject object) throws IOException {
        assertValueKey(object);
        var value = object.get(Ur.KEY_VALUE);
        assertArray(value);
        var innerArray = (JSONArray)value;
        assertSingleElementArray(innerArray);
        var item = innerArray.get(0);
        return item.toString();
    }

    private void assertSingleElementArray(JSONArray array) throws IOException {
        if (array.length() > 1 || array.isEmpty()) {
            throw new IOException("Incorrect Unified representation provided. Inner arrays in JSON Ur have to have only one element.");
        }
    }

    private void assertArray(Object potentialArray) throws IOException {
        if (!(potentialArray instanceof JSONArray)) {
            throw new IOException("Incorrect Unified representation provided. Entities not wrapped in array.");
        }
    }

    private void assertTypeKey(JSONObject object) throws IOException {
        if (!object.has(Ur.KEY_TYPE)) {
            throw new IOException("Incorrect Unified representation provided. Not all entities do have " + Ur.KEY_TYPE + " key.");
        }
    }

    private void assertValueKey(JSONObject object) throws IOException {
        if (!object.has(Ur.KEY_VALUE)) {
            throw new IOException("Incorrect Unified representation provided. Ur Primitive does not have " + Ur.KEY_VALUE + " key.");
        }
    } 
}
