package cz.cuni.mff.hdt.ur;

import java.util.Set;

import org.json.JSONObject;

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

    public Ur get(UrPath path) {
        // TODO
        return null;
    }

    public void set(UrPath path, Ur value) {
        // TODO

    }

    public void update(UrPath path, Ur value) {
        // TODO
    }

    public void delete(UrPath path) {
        // TODO
    }
}