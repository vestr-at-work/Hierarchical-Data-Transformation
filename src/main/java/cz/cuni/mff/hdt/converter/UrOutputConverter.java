package cz.cuni.mff.hdt.converter;

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Abstract class holding most of the common operations needed for Ur OutputConverters
 */
public abstract class UrOutputConverter implements OutputConverter {

    protected Object convertPrimitive(JSONObject urPrimitive) throws IOException {
        var type = getTypeInnerValue(urPrimitive);
        var value = getValueInnerValue(urPrimitive);
        
        if (type.equals(Ur.VALUE_BOOLEAN) 
            || type.equals(Ur.VALUE_BOOLEAN_URI)) {
            if (value.equals("True") || value.equals("true") || value.equals("TRUE")) {
                return true;
            }
            return false;
        }
        else if (type.equals(Ur.VALUE_NUMBER) 
            || type.equals(Ur.VALUE_INTEGER_URI)) {
            try {
                return Integer.parseInt(value.toString());
            }
            catch (NumberFormatException e) {
                throw new IOException("Incorrect Unified representation provided. Number Primitive in incorrect format.");
            }
        }
        else if (type.equals(Ur.VALUE_STRING) 
            || type.equals(Ur.VALUE_STRING_URI) 
            || type.equals(Ur.VALUE_LANG_STRING_URI) 
            || type.equals(Ur.VALUE_ANY_URI)) {

            return value.toString();
        }
        else {
            return value.toString();
        }

        //throw new IOException("Incorrect Unified representation provided. Unknown type of Ur Primitive.");
    }

    protected JSONObject getInnerObject(JSONArray array) throws IOException {
        assertSingleElementArray(array);
        var potentialObject = array.get(0);
        if (!(potentialObject instanceof JSONObject)) {
            throw new IOException("Incorrect Unified representation provided. Values not wrapped in object.");
        }
        return (JSONObject)potentialObject;
    }

    protected JSONObject getProperty(JSONObject parent, String propertyName) throws IOException {
        var propertyObject = parent.get(propertyName);
        assertArray(propertyObject);
        var property = getInnerObject((JSONArray)propertyObject);
        return property;
    }

    protected String getTypeInnerValue(JSONObject object) throws IOException {
        assertTypeKey(object);
        var type = object.get(Ur.KEY_TYPE);
        assertArray(type);
        var innerArray = (JSONArray)type;
        assertSingleElementArray(innerArray);
        var item = innerArray.get(0);
        return item.toString();
    }

    protected String getValueInnerValue(JSONObject object) throws IOException {
        assertValueKey(object);
        var value = object.get(Ur.KEY_VALUE);
        assertArray(value);
        var innerArray = (JSONArray)value;
        assertSingleElementArray(innerArray);
        var item = innerArray.get(0);
        return item.toString();
    }

    protected void assertSingleElementArray(JSONArray array) throws IOException {
        if (array.length() > 1 || array.isEmpty()) {
            throw new IOException("Incorrect Unified representation provided. Inner arrays in JSON Ur have to have only one element.");
        }
    }

    protected void assertArray(Object potentialArray) throws IOException {
        if (!(potentialArray instanceof JSONArray)) {
            throw new IOException("Incorrect Unified representation provided. Entities not wrapped in array.");
        }
    }

    protected void assertObject(Object potentialJSONObject) throws IOException {
        if (!(potentialJSONObject instanceof JSONObject)) {
            throw new IOException("Incorrect Unified representation provided. Entity not in object.");
        }
    }

    protected void assertTypeKey(JSONObject object) throws IOException {
        if (!object.has(Ur.KEY_TYPE)) {
            throw new IOException("Incorrect Unified representation provided. Not all entities do have " + Ur.KEY_TYPE + " key.");
        }
    }

    protected void assertValueKey(JSONObject object) throws IOException {
        if (!object.has(Ur.KEY_VALUE)) {
            throw new IOException("Incorrect Unified representation provided. Ur Primitive does not have " + Ur.KEY_VALUE + " key.");
        }
    }
}
