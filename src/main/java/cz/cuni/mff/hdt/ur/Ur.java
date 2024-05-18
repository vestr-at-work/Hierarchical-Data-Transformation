package cz.cuni.mff.hdt.ur;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.Property;

import cz.cuni.mff.hdt.ur.UrPath.ArrayItemToken;
import cz.cuni.mff.hdt.ur.UrPath.PropertyToken;
import cz.cuni.mff.hdt.ur.UrPath.Token;

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
    public static final String VALUE_INTEGER_URI = "http://www.w3.org/2001/XMLSchema#integer";
    public static final String VALUE_BOOLEAN_URI = "http://www.w3.org/2001/XMLSchema#boolean";

    public static enum Type { Object, Array, String, Number, Boolean };

    protected JSONObject innerRepresentation;

    public Ur(JSONObject object) {
        this.innerRepresentation = object;
    } 

    public JSONObject getInnerRepresentation() {
        return innerRepresentation;
    }

    public Set<String> getKeys() {
        return null;
    }

    public Ur get(UrPath path) throws IOException {
        var outputInner = getInner(path);
        return new Ur(outputInner);
    }

    public void set(UrPath path, Ur value) throws IOException {
        try {
            update(path, value);
        }
        catch (IOException e) {
            setNonExistant(path, value);
        }
    }

    public void update(UrPath path, Ur value) throws IOException {
        var innerValue = value.innerRepresentation; 
        if (path.length() == 0) {
            innerRepresentation = innerValue;
            return;
        }
        List<Token> parentPath = path.tokens.subList(0, path.length() - 1);
        var parrentInner = getInner(new UrPath(parentPath));
        var childToken = path.tokens.get(path.length() - 1);
        String childKeyOrIndex = getTokenKeyOrIndexString(childToken);
        if (childKeyOrIndex == null) { 
            throw new IOException("Invalid array item index in UrPath. Can not be empty.");
        }
        if (!parrentInner.has(childKeyOrIndex)) {
            throw new IOException("Can not update non existent entity.");
        }
        parrentInner.put(childKeyOrIndex, innerValue);
    }

    public void delete(UrPath path) throws IOException {
        if (path.length() == 0) {
            throw new IOException("Can not delete Ur root.");
        }
        List<Token> parentPath = path.tokens.subList(0, path.length() - 1);
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

    protected JSONObject getChildEntity(JSONObject entity, String key) {
        var innerArray = (JSONArray)entity.get(key);
        var innerObject = (JSONObject)innerArray.get(0);
        return innerObject;
    }

    protected JSONObject getInner(UrPath path) throws IOException {
        JSONObject outputInner = innerRepresentation;
        for (int i = 0; i < path.length(); i++) {
            Token pathToken = path.tokens.get(i);
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
    protected String getTokenKeyOrIndexString(Token token) throws IOException {
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
            Token pathToken = path.tokens.get(i);
            var key = getTokenKeyOrIndexString(pathToken);
            if (key == null) { // if array item with no index
                Integer nextIndex = getMaxIndex(innerSubtree) + 1;
                var restOfPath = new UrPath(path.tokens.subList(i + 1, path.length()));
                innerSubtree.put(nextIndex.toString(), createSubtreeWithValue(restOfPath, value));
                return;
            }
            if (!innerSubtree.has(key)) {
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
            Token pathToken = path.tokens.get(i);
            var key = getTokenKeyOrIndexString(pathToken);
            if (pathToken instanceof ArrayItemToken) {
                var tempObject = new JSONObject().put(KEY_TYPE, VALUE_ARRAY);
                var index = key;
                if (key == null) {
                    index = "0";
                }
                innerValue = tempObject.put(index, new JSONArray().put(0, innerValue));
            }
            else if (pathToken instanceof PropertyToken) {
                var tempObject = new JSONObject().put(KEY_TYPE, VALUE_OBJECT);
                innerValue = tempObject.put(key, new JSONArray().put(0, innerValue));
            }
        }
        return new JSONArray().put(0, innerValue);
    }
}