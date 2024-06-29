package cz.cuni.mff.hdt.ur;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.transformation.TypedValue;
import cz.cuni.mff.hdt.path.UrPath;
import cz.cuni.mff.hdt.path.ArrayItemToken;
import cz.cuni.mff.hdt.path.PropertyToken;
import cz.cuni.mff.hdt.path.BaseUrPathToken;

/**
 * Unified representation of hierarchical data.
 */
public class Ur {
    public static final String KEY_TYPE = "@type";
    public static final String KEY_VALUE = "@value";
    public static final String KEY_CSV_ROWS = "@rows";
    public static final String KEY_CSV_HEADER = "@header";
    public static final String KEY_XML_VERSION = "@version";
    public static final String KEY_XML_ENCODING = "@encoding";
    public static final String KEY_XML_ATTRIBUTES = "@attributes";
    public static final String KEY_RDF_ID = "@id";
    public static final String KEY_RDF_LANGUAGE = "@language";

    public static final String VALUE_OBJECT = "object";
    public static final String VALUE_ARRAY = "array";
    public static final String VALUE_STRING = "string";
    public static final String VALUE_NUMBER = "number";
    public static final String VALUE_BOOLEAN = "boolean";

    public static final String VALUE_STRING_URI = "http://www.w3.org/2001/XMLSchema#string";
    public static final String VALUE_LANG_STRING_URI = "http://www.w3.org/1999/02/22-rdf-syntax-ns#langString";
    public static final String VALUE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#integer";
    public static final String VALUE_BOOLEAN_URI = "http://www.w3.org/2001/XMLSchema#boolean";
    public static final String VALUE_ANY_URI = "http://www.w3.org/2001/XMLSchema#anyURI";

    /**
     * Possible type in a Unified representation.
     */
    public static enum Type { Object, Array, String, Number, Boolean };

    protected JSONObject innerRepresentation;

    /**
     * Constructs a new {@code Ur} object with the specified JSON representation.
     * 
     * @param object the JSON object representing the hierarchical data
     */
    public Ur(JSONObject object) {
        this.innerRepresentation = object;
    } 

    /**
     * Returns the inner JSON representation of the hierarchical data.
     * 
     * @return the inner JSON object
     */
    public JSONObject getInnerRepresentation() {
        return innerRepresentation;
    }

    /**
     * Constructs a new {@code Ur} primitive value from a {@code TypedValue}.
     * 
     * @param typedValue the TypedValue containing type and value
     * @return a new Ur primitive value
     */
    public static Ur getTypedValueUr(TypedValue typedValue) {
        return new Ur(
            new JSONObject()
                .put(KEY_TYPE, new JSONArray().put(0, typedValue.type()))
                .put(KEY_VALUE, new JSONArray().put(0, typedValue.value()))
        );
    }

    /**
     * Returns the primitive type as a string for the given value.
     * 
     * @param value the value to check
     * @return the string representation of the type
     * @throws IOException if the value type is unsupported
     */
    public static String getPrimitiveUrString(Object value) throws IOException {
        if (value instanceof String) {
            return VALUE_STRING;
        }
        else if (value instanceof Boolean) {
            return VALUE_BOOLEAN;
        }
        else if (value instanceof Number) {
            return VALUE_NUMBER;
        }
        else {
            throw new IOException("Unsupported value type.");
        }
    }

    /**
     * Returns the set of keys in the inner JSON representation.
     * 
     * @return the set of keys or null if Ur is primitive type
     */
    public Set<String> getKeys() {
        // Is primitive
        if (innerRepresentation.has(Ur.KEY_VALUE) && innerRepresentation.has(Ur.KEY_TYPE)) {
            return null;
        }

        var keys = new HashSet<String>();
        for (var key : innerRepresentation.keySet()) {
            if (key.equals(Ur.KEY_TYPE)) {
                continue;
            }
            keys.add(key);
        }

        return keys;
    }

    /**
     * Retrieves the {@code Ur} object at the specified path.
     * 
     * @param path the path to the Ur object
     * @return the Ur object at the specified path
     * @throws IOException if the path is invalid
     */
    public Ur get(UrPath path) throws IOException {
        var outputInner = getInner(path);
        var outputInnerCopy = new JSONObject(outputInner.toMap()); 
        return new Ur(outputInnerCopy);
    }

    /**
     * Sets the value at the specified path.
     * 
     * @param path the path to set the value at
     * @param value the Ur value to set
     * @throws IOException if the path is invalid
     */
    public void set(UrPath path, Ur value) throws IOException {
        try {
            update(path, value);
        }
        catch (IOException e) {
            setNonExistant(path, value);
        }
    }

    /**
     * Updates the value at the specified path.
     * 
     * @param path the path to update the value at
     * @param value the Ur value to update
     * @throws IOException if the path is invalid or the value does not exist
     */
    public void update(UrPath path, Ur value) throws IOException {
        var innerValue = value.innerRepresentation; 
        if (path.length() == 0) {
            innerRepresentation = innerValue;
            return;
        }
        List<BaseUrPathToken> parentPath = path.tokens.subList(0, path.length() - 1);
        var parrentInner = getInner(new UrPath(parentPath));
        var childToken = path.tokens.get(path.length() - 1);
        String childKeyOrIndex = getTokenKeyOrIndexString(childToken);
        if (childKeyOrIndex == null) { 
            throw new IOException("Invalid array item index in UrPath. Can not be empty.");
        }
        if (!parrentInner.has(childKeyOrIndex)) {
            throw new IOException("Can not update non existent entity.");
        }
        parrentInner.put(childKeyOrIndex, new JSONArray().put(innerValue));
    }

    /**
     * Deletes the value at the specified path.
     * 
     * @param path the path to delete the value at
     * @throws IOException if the path is invalid or the root is being deleted
     */
    public void delete(UrPath path) throws IOException {
        if (path.length() == 0) {
            throw new IOException("Can not delete Ur root.");
        }
        List<BaseUrPathToken> parentPath = path.tokens.subList(0, path.length() - 1);
        var parrentInner = getInner(new UrPath(parentPath));
        var childToken = path.tokens.get(path.length() - 1);
        String childKeyOrIndex = getTokenKeyOrIndexString(childToken);
        if (childKeyOrIndex == null) { 
            throw new IOException("Invalid array item index in UrPath. Can not be empty.");
        }
        if (!parrentInner.has(childKeyOrIndex)) {
            throw new IOException("Can not delete non existent entity.");
        }
        parrentInner.remove(childKeyOrIndex);
    }

    /**
     * Checks if the value is present at the specified path.
     * 
     * @param path the path to check for presence
     * @return true if the value is present, false otherwise
     */
    public boolean isPresent(UrPath path) {
        JSONObject outputInner = innerRepresentation;
        for (int i = 0; i < path.length(); i++) {
            BaseUrPathToken pathToken = path.tokens.get(i);
            if (pathToken instanceof PropertyToken) {
                var property = (PropertyToken)pathToken;
                if (!outputInner.has(property.getKey())) {
                    return false;
                }
                outputInner = getChildEntity(outputInner, property.getKey());
            }
            else if (pathToken instanceof ArrayItemToken) {
                var arrayItem = (ArrayItemToken)pathToken;
                if (arrayItem.getIndex() == null) {
                    return false;
                }
                if (!outputInner.has(arrayItem.getIndex().toString())) {
                    return false;
                }
                outputInner = getChildEntity(outputInner, arrayItem.getIndex().toString());
            }
            else {
                throw new IllegalStateException("Unknown UrPath token.");
            }
        }
        return true;
    }

    protected JSONObject getChildEntity(JSONObject entity, String key) {
        var innerArray = (JSONArray)entity.get(key);
        var innerObject = (JSONObject)innerArray.get(0);
        return innerObject;
    }

    protected JSONObject getInner(UrPath path) throws IOException {
        JSONObject outputInner = innerRepresentation;
        for (int i = 0; i < path.length(); i++) {
            BaseUrPathToken pathToken = path.tokens.get(i);
            if (pathToken instanceof PropertyToken) {
                var property = (PropertyToken)pathToken;
                if (!outputInner.has(property.getKey())) {
                    throw new IOException("Invalid property in UrPath. No match in Ur.");
                }
                outputInner = getChildEntity(outputInner, property.getKey());
            }
            else if (pathToken instanceof ArrayItemToken) {
                var arrayItem = (ArrayItemToken)pathToken;
                if (arrayItem.getIndex() == null) {
                    throw new IOException("Invalid array item index in UrPath. Can not be empty.");
                }
                if (!outputInner.has(arrayItem.getIndex().toString())) {
                    throw new IOException("Invalid array item index in UrPath. No match in Ur.");
                }
                outputInner = getChildEntity(outputInner, arrayItem.getIndex().toString());
            }
            else {
                throw new IllegalStateException("Unknown UrPath token.");
            }
        }
        return outputInner;
    }

    /*
     * Returns null if token is array item without index
     */
    protected String getTokenKeyOrIndexString(BaseUrPathToken token) throws IOException {
        if (token instanceof PropertyToken) {
            var property = (PropertyToken)token;
            return property.getKey();
        }
        else if (token instanceof ArrayItemToken) {
            var arrayItem = (ArrayItemToken)token;
            if (arrayItem.getIndex() == null) {
                return null;
            }
            return arrayItem.getIndex().toString();
        }
        else {
            throw new IllegalStateException("Unknown UrPath token.");
        }
    }

    protected void setNonExistant(UrPath path, Ur value) throws IOException {
        var innerSubtree = innerRepresentation;
        for (int i = 0; i < path.length(); i++) {
            BaseUrPathToken pathToken = path.tokens.get(i);
            var key = getTokenKeyOrIndexString(pathToken);
            if (key == null) { // if array item with no index
                if (innerSubtree == innerRepresentation && innerSubtree.keySet().isEmpty()) { // if empty root
                    innerSubtree.put(KEY_TYPE, new JSONArray().put(0, VALUE_ARRAY));
                }
                Integer nextIndex = getMaxIndex(innerSubtree) + 1;
                var restOfPath = new UrPath(path.tokens.subList(i + 1, path.length()));
                innerSubtree.put(nextIndex.toString(), createSubtreeWithValue(restOfPath, value));
                return;
            }
            if (!innerSubtree.has(key)) {
                if (innerSubtree == innerRepresentation && innerSubtree.keySet().isEmpty()) { // if empty root
                    innerSubtree.put(KEY_TYPE, new JSONArray().put(0, VALUE_OBJECT));
                }
                var restOfPath = new UrPath(path.tokens.subList(i + 1, path.length()));
                innerSubtree.put(key, createSubtreeWithValue(restOfPath, value));
                return;
            }
            innerSubtree = getChildEntity(innerSubtree, key);
        }
        // unreachable
        throw new IllegalStateException("Unreachable code in set method in Ur.");
    }

    protected Integer getMaxIndex(JSONObject urArray) throws IOException {
        Integer maxIndex = 0;
        for (var key : urArray.keySet()) {
            if (key.equals(KEY_TYPE)) {
                continue;
            }

            try {
                var index = Integer.parseInt(key);
                maxIndex = index > maxIndex ? index : maxIndex;
            }
            catch (NumberFormatException e) {
                throw new IOException("Ur array not consisting of indexes only.");
            }
        }
        return maxIndex;
    }

    protected JSONArray createSubtreeWithValue(UrPath path, Ur value) throws IOException {
        var innerValue = value.innerRepresentation;
        for (int i = path.length() - 1; i >= 0; i--) {
            BaseUrPathToken pathToken = path.tokens.get(i);
            var key = getTokenKeyOrIndexString(pathToken);
            if (pathToken instanceof ArrayItemToken) {
                var tempObject = new JSONObject().put(KEY_TYPE, new JSONArray().put(VALUE_ARRAY));
                var index = key;
                if (key == null) {
                    index = "0";
                }
                innerValue = tempObject.put(index, new JSONArray().put(innerValue));
            }
            else if (pathToken instanceof PropertyToken) {
                var tempObject = new JSONObject().put(KEY_TYPE, new JSONArray().put(VALUE_OBJECT));
                innerValue = tempObject.put(key, new JSONArray().put(innerValue));
            }
        }
        return new JSONArray().put(innerValue);
    }
}