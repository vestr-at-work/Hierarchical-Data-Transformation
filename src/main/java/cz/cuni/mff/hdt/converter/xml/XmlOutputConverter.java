package cz.cuni.mff.hdt.converter.xml;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.converter.UrOutputConverter;
import cz.cuni.mff.hdt.ur.Ur;

/**
 * Converter implementation for converting Unified representation (Ur) to XML format.
 */
public class XmlOutputConverter extends UrOutputConverter {
    private static final String XML_PROLOG_VERSION = "<?xml version=\"";
    private static final String XML_PROLOG_ENCODING = "\" encoding=\"";
    private static final String XML_PROLOG_ENDING = "\"?>";
    private static final String XML_OPENING_TAG_START = "<";
    private static final String XML_CLOSING_TAG_START = "</";
    private static final String XML_TAG_END = ">";

    protected String END_OF_LINE = "\n";

    protected Boolean prettyPrint = false;
    protected String indentation;

    public XmlOutputConverter(Boolean prettyPrint, int indentation) {
        this.prettyPrint = prettyPrint;
        var newIndentation = new StringBuilder();
        for (int i = 0; i < indentation; i++) {
            newIndentation.append(" ");
        }
        this.indentation = newIndentation.toString();
    }

    public XmlOutputConverter() {
        this.prettyPrint = true;
        var indentation = 2;
        var newIndentation = new StringBuilder();
        for (int i = 0; i < indentation; i++) {
            newIndentation.append(" ");
        }
        this.indentation = newIndentation.toString();
    }

    /**
     * Converts the Unified representation (Ur) data to XML format.
     *
     * @param data the Unified representation (Ur) data to be converted
     * @return a string representing the data in XML format
     * @throws IOException if an I/O error occurs during conversion
     */
    @Override
    public String convert(Ur data) throws IOException {
        var xmlBuilder = new StringBuilder();
        try {       
            var innerJson = data.getInnerRepresentation();
            var typeValue = getTypeInnerValue(innerJson);

            if (!typeValue.equals(Ur.VALUE_OBJECT)) {
                throw new IOException("Incorrect Unified representation provided. Wrong type of root");
            }

            var rootName = getRootName(innerJson);
            if (rootName == null) {
                throw new IOException("Incorrect Unified representation provided. No root element");
            }
            var version = getVersion(innerJson);
            var encoding = getEncoding(innerJson);

            if (version.isPresent() && encoding.isPresent()) {
                xmlBuilder.append(XML_PROLOG_VERSION).append(version.get())
                    .append(XML_PROLOG_ENCODING).append(encoding.get())
                    .append(XML_PROLOG_ENDING);
                if (prettyPrint) {
                    xmlBuilder.append(END_OF_LINE);
                }
            }

            var rootObject = innerJson.get(rootName);
            assertArray(rootObject);
            writeObject(getInnerObject((JSONArray)rootObject), xmlBuilder, rootName , 0);

            return xmlBuilder.toString();
        }
        catch (IOException e) {
            System.out.println(xmlBuilder);
            throw new IOException(e.getMessage());
        }
    }

    // TODO this two functions should be merged into one
    private Optional<String> getVersion(JSONObject root) throws IOException {
        if (!root.has(Ur.KEY_XML_VERSION)) {
            return Optional.empty();
        }

        var versionArray = root.get(Ur.KEY_XML_VERSION);
        assertArray(versionArray);
        var versionValueArray = (JSONArray)versionArray;
        assertSingleElementArray(versionValueArray);
        var versionPrimitive = versionValueArray.get(0);
        assertObject(versionPrimitive);
        var value = getValueInnerValue((JSONObject)versionPrimitive);
        return Optional.of(value);
    }

    private Optional<String> getEncoding(JSONObject root) throws IOException {
        if (!root.has(Ur.KEY_XML_ENCODING)) {
            return Optional.empty();
        }

        var encodingObject = root.get(Ur.KEY_XML_ENCODING);
        assertArray(encodingObject);
        var encodingValueArray = (JSONArray)encodingObject;
        assertSingleElementArray(encodingValueArray);
        var encodingPrimitive = encodingValueArray.get(0);
        assertObject(encodingPrimitive);
        var value = getValueInnerValue((JSONObject)encodingPrimitive);
        return Optional.of(value);
    }

    private String getRootName(JSONObject rootObject) throws IOException {
        String rootName = null;
        for (var key : rootObject.keySet()) {
            if (key.equals(Ur.KEY_TYPE) || key.equals(Ur.KEY_XML_ENCODING) || key.equals(Ur.KEY_XML_VERSION)) {
                continue;
            }
            if (rootName != null) {
                throw new IOException("Multiple root elements in Unified representation");
            }
            rootName = key;
        }
        return rootName;
    }
    
    private void writeRecursive(JSONObject urObject, StringBuilder xmlBuilder, String tag, int indentLevel) throws IOException {
        var typeValue = getTypeInnerValue(urObject);

        if (typeValue.equals(Ur.VALUE_OBJECT)) {
            writeObject(urObject, xmlBuilder, tag, indentLevel);
        }
        else if (typeValue.equals(Ur.VALUE_ARRAY)) {
            writeArray(urObject, xmlBuilder, tag, indentLevel);
        }
        else {
            writePrimitive(urObject, xmlBuilder, tag, indentLevel);
        }
    }

    private void writeAttributes(JSONObject urAttributes, StringBuilder xmlBuilder) throws IOException {
        for (var attributeName : urAttributes.keySet()) {
            if (attributeName.equals(Ur.KEY_TYPE)) {
                continue;
            }
            var attribute = urAttributes.get(attributeName);
            assertArray(attribute);
            var value = getValueInnerValue(getInnerObject((JSONArray)attribute));
            xmlBuilder.append(" ")
                .append(attributeName)
                .append("=\"")
                .append(value)
                .append("\"");  
        }
    }

    private void writePrimitive(JSONObject urPrimitive, StringBuilder xmlBuilder, String tag, int indentLevel) throws IOException {
        addIndentation(xmlBuilder, indentLevel);

        xmlBuilder.append(XML_OPENING_TAG_START).append(tag);
        if (urPrimitive.has(Ur.KEY_XML_ATTRIBUTES)) {
            var attributes = urPrimitive.get(Ur.KEY_XML_ATTRIBUTES);
            assertArray(attributes);
            writeAttributes(getInnerObject((JSONArray)attributes), xmlBuilder);
        }
        xmlBuilder.append(XML_TAG_END);

        xmlBuilder.append(getValueInnerValue(urPrimitive));

        xmlBuilder.append(XML_CLOSING_TAG_START).append(tag).append(XML_TAG_END);
    }

    private void writeArray(JSONObject urArray, StringBuilder xmlBuilder, String tag, int indentLevel) throws IOException {
        for (var item : urArray.keySet()) {
            if (item.equals(Ur.KEY_TYPE)) {
                continue;
            }
            var value = urArray.get(item);
            assertArray(value);
            writeRecursive(getInnerObject((JSONArray)value), xmlBuilder, tag, indentLevel);

            if (prettyPrint) {
                xmlBuilder.append(END_OF_LINE);
            }
        }

        if (prettyPrint) {
            xmlBuilder.setLength(xmlBuilder.length() - END_OF_LINE.length());
        }
    }

    private void writeObject(JSONObject urObject, StringBuilder xmlBuilder, String tag, int indentLevel) throws IOException {
        addIndentation(xmlBuilder, indentLevel);

        xmlBuilder.append(XML_OPENING_TAG_START).append(tag);
        if (urObject.has(Ur.KEY_XML_ATTRIBUTES)) {
            var attributes = urObject.get(Ur.KEY_XML_ATTRIBUTES);
            assertArray(attributes);
            writeAttributes(getInnerObject((JSONArray)attributes), xmlBuilder);
        }
        xmlBuilder.append(XML_TAG_END);

        var elementHasChildren = false;
        for (var key : urObject.keySet()) {
            if (key.equals(Ur.KEY_TYPE) || key.equals(Ur.KEY_XML_ATTRIBUTES)) {
                continue;
            }
            elementHasChildren = true;
            xmlBuilder.append(END_OF_LINE);
            var value = urObject.get(key);
            assertArray(value);
            writeRecursive(getInnerObject((JSONArray)value), xmlBuilder, key, indentLevel + 1);
        }


        if (elementHasChildren && prettyPrint) {
            xmlBuilder.append(END_OF_LINE);
            addIndentation(xmlBuilder, indentLevel);
        }
        xmlBuilder.append(XML_CLOSING_TAG_START).append(tag).append(XML_TAG_END);
    }
    
    private void addIndentation(StringBuilder xmlBuilder, int indentLevel) {
        if (!prettyPrint) {
            return;
        }
        for (int i = 0; i < indentLevel; i++) {
            xmlBuilder.append(indentation);
        }
    }
}
