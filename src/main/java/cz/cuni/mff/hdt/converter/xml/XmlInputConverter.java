package cz.cuni.mff.hdt.converter.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;

import javax.management.RuntimeErrorException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import cz.cuni.mff.hdt.converter.InputConverter;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.utils.PrimitiveParser;

/**
 * Converter implementation for converting XML to Unified representation (Ur).
 */
public class XmlInputConverter implements InputConverter {

    /**
     * Converts the XML as input stream into a Unified representation (Ur) object.
     *
     * @param input the input stream containing XML data
     * @return a Unified representation (Ur) object representing the XML data
     * @throws IOException if an I/O error occurs during conversion
     */
    @Override
    public Ur convert(InputStream input) throws IOException {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(input);
            doc.getDocumentElement().normalize();

            JSONObject resultJson = new JSONObject().put(Ur.KEY_TYPE, new JSONArray().put(0, Ur.VALUE_OBJECT));
            resultJson.put(Ur.KEY_XML_ENCODING, new JSONArray().put(0, doc.getXmlEncoding()));
            resultJson.put(Ur.KEY_XML_VERSION, new JSONArray().put(0, doc.getXmlVersion()));
            var rootInner = getXmlUrRecursive(doc.getDocumentElement());
            resultJson.put(doc.getDocumentElement().getTagName(), new JSONArray().put(0, rootInner));
            return new Ur(resultJson);
        }
        catch (ParserConfigurationException e) {
            throw new IOException("Error occured when loading the input");
        }
        catch (SAXException e) {
            throw new IOException("Error occured when loading the input");
        }
    }

    private static JSONObject getXmlUrRecursive(Element element) throws IOException {
        Dictionary<String, Integer> tagCounts = getTagCounts(element);
        Optional<JSONObject> attributes = getAttributesObject(element);

        JSONObject outputObject = new JSONObject();

        if (!attributes.isEmpty()) {
            outputObject.put(Ur.KEY_XML_ATTRIBUTES, new JSONArray().put(0, attributes.get()));
        }

        if (tagCounts.isEmpty()) {
            var textValue = element.getTextContent();
            String type = getPrimitiveValueType(textValue);
            outputObject.put(Ur.KEY_TYPE, new JSONArray().put(0, type));
            outputObject.put(Ur.KEY_VALUE, new JSONArray().put(0, textValue));
            return outputObject;
        }

        
        for (var tags = tagCounts.keys(); tags.hasMoreElements();) {
            var tag = tags.nextElement();
            var count = tagCounts.get(tag);

            if (count > 1) {
                JSONObject tagArrayObject = getArrayObject(element, tag);
                outputObject.put(tag, new JSONArray().put(0, tagArrayObject));
                continue;
            }
            var tagElement = getTagElement(element, tag);
            outputObject.put(tag, new JSONArray().put(0, getXmlUrRecursive(tagElement)));
        }

        outputObject.put(Ur.KEY_TYPE, new JSONArray().put(0, Ur.VALUE_OBJECT));
        return outputObject;
    }

    private static JSONObject getArrayObject(Element element, String tag) throws IOException {
        var nodes = element.getElementsByTagName(tag);
        var outputObject = new JSONObject().put(Ur.KEY_TYPE, new JSONArray().put(0, Ur.VALUE_ARRAY));
        for (Integer i = 0; i < nodes.getLength(); i++) {
            var node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                throw new IOException("Unsupported XML node type");
            }
            Element tagElement = (Element)node;
            outputObject.put(i.toString(), new JSONArray().put(0, getXmlUrRecursive(tagElement)));
        }
        return outputObject;
    }

    private static Dictionary<String, Integer> getTagCounts(Element element) {
        var dict = new Hashtable<String, Integer>();
        var childNodes = element.getChildNodes();

        for (int i = 0; i < childNodes.getLength(); i++) {
            var childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            var childElement = (Element)childNode;
            var tag = childElement.getNodeName();
            if (dict.containsKey(tag)) {
                dict.replace(tag, dict.get(tag) + 1);
            }
            else {
                dict.put(tag, 1);
            }
        }

        return dict;
    }

    private static Optional<JSONObject> getAttributesObject(Element element) throws IOException {
        var attributes = element.getAttributes();
        if (attributes.getLength() == 0) {
            return Optional.empty();
        }
        var attributesObject = new JSONObject().put(Ur.KEY_TYPE, new JSONArray().put(0, Ur.VALUE_OBJECT));
        for (int i = 0; i < attributes.getLength(); i++) {
            var attribute = attributes.item(i);
            var textValue = attribute.getNodeValue();
            var primitiveObject = new JSONObject();
            String type = getPrimitiveValueType(textValue);
            primitiveObject.put(Ur.KEY_TYPE, new JSONArray().put(0, type));
            primitiveObject.put(Ur.KEY_VALUE, new JSONArray().put(0, textValue));
            attributesObject.put(attribute.getNodeName(), new JSONArray().put(0, primitiveObject));
        }

        return Optional.of(attributesObject);
    }

    private static String getPrimitiveValueType(String value) throws IOException {
        var type = PrimitiveParser.getPrimitiveType(value);
        switch (type) {
            case Boolean:
                return Ur.VALUE_BOOLEAN;
            case Number:
                return Ur.VALUE_NUMBER;
            case String:
                return Ur.VALUE_STRING;
            default:
                throw new IOException("Value not a primitive type");
        }
    }

    private static Element getTagElement(Element element, String tag) {
        var nodes = element.getElementsByTagName(tag);
        if (nodes.item(0).getNodeType() == Node.ELEMENT_NODE) {
            return (Element)nodes.item(0);
        }
        throw new RuntimeErrorException(null, "Unreachable code.");
    }
}
