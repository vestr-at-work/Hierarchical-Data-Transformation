package cz.cuni.mff.hdt.sink.xml;

import cz.cuni.mff.hdt.sink.*;
import cz.cuni.mff.hdt.ur.Ur;

import java.io.Writer;
import java.io.IOException;
import java.util.Stack;

/**
 * XML format specific implementation of Sink.
 */
public class XmlSink extends UrSink {
    private enum State {
        Unknown,
        InObject,
        InValue,
        InAttributes;
    }

    private class StateHolder {
        public State state;
        public Ur.Type type;
        public String tag;

        public StateHolder(State state, Ur.Type type, String tag) {
            this.state = state;
            this.type = type;
            this.tag = tag;
        }
    }

    private enum Token { Unknown, Type, Value, VersionValue, EncodingValue }

    private String key = null;
    private String lastNonControlKey = null;
    private Token nextValue = Token.Unknown;
    private Stack<StateHolder> nesting = new Stack<StateHolder>();

    private Boolean prettyPrint;
    private StringBuilder indentationPrefix = new StringBuilder();
    private Boolean firstDocumentElement = true;
    private Boolean lastTagEndTag = false;

    public XmlSink(Writer writer, Boolean prettyPrint) {
        this.writer = writer;
        this.prettyPrint = prettyPrint;
    }

    public XmlSink(Writer writer) {
        this(writer, false);
    }

    @Override
    public void openObject() throws IOException {}

    @Override
    public void closeObject() throws IOException {
        if (nesting.empty()) {
            return;
        }
        var toBeClosed = nesting.pop();
        if (toBeClosed.state == State.InAttributes) {
            lastNonControlKey = toBeClosed.tag;
            return;
        }

        if (toBeClosed.state == State.InValue
            && nesting.peek().state == State.InAttributes) {
            return;
        }

        writeNextLine();
        decreaseIndentation();
        writeIndentation();
        writer.write("</");
        writeString(toBeClosed.tag);
        writer.write(">");
        lastTagEndTag = true;
    }

    @Override
    public void openArray() throws IOException {
        if (key.equals(Ur.KEY_TYPE)) {
            nextValue = Token.Type;
        }
        else if (key.equals(Ur.KEY_VALUE)) {
            nextValue = Token.Value;
        }
        else if (key.equals(Ur.KEY_XML_ATTRIBUTES)) {
            nesting.push(new StateHolder(State.InAttributes, null, lastNonControlKey));
        }
        else if (key.equals(Ur.KEY_XML_VERSION)) {
            nextValue = Token.VersionValue;
        }
        else if (key.equals(Ur.KEY_XML_ENCODING)) {
            nextValue = Token.EncodingValue;
        }
        // if key is Ur XML list key e.g. "@2:item"
        else if (key.split(":")[0].charAt(0) == '@' && isNumeric(key.split(":")[0].substring(1))) {
            key = key.split(":")[1];
            lastNonControlKey = key;
            writeKey();
        }
        else if (!nesting.empty() && nesting.peek().state == State.InAttributes) {
            writeAttribute();
        }
        else {
            writeKey();
        }
    }

    private void writeAttribute() throws IOException {
        writer.write(" ");
        writeString(key);
        writer.write("=");
    }

    private void writeKey() throws IOException {
        if (key == null) {
            return;
        }
        if (!firstDocumentElement) {
            if (!lastTagEndTag) {
                writer.write(">");
                increaseIndentation();
            }
            writeNextLine();
        }
        firstDocumentElement = false;
        writeIndentation();
        writer.write("<");
        writeString(key);
        lastTagEndTag = false;
    }

    // TODO should be somewhere else
    private static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            int d = Integer.parseInt(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    @Override
    public void closeArray() throws IOException {}

    @Override
    public void setNextKey(String key) throws IOException {
        this.key = key;
        if (key.charAt(0) != '@') {
            lastNonControlKey = key;
        }
    }

    @Override
    public void writeValue(String value) throws IOException {
        switch (nextValue) {
            case Type:
                updateOnNewType(getType(value));
                break;
            case Value:
                processValueToken(value);
                break;
            case VersionValue:
                writeVersion(value);
                nextValue = Token.Unknown;
                break;
            case EncodingValue:
                writeEncoding(value);
                nextValue = Token.Unknown;
                break;
            default:
        }
    }

    private void processValueToken(String value) throws IOException {
        var lastStateHolder = nesting.pop();
        boolean valueIsAttribute = !nesting.empty() && nesting.peek().state == State.InAttributes;
        nesting.push(lastStateHolder);
        nextValue = Token.Unknown;

        if (valueIsAttribute) {
            writeAttributeValueToken(value);
            return;
        }
        
        writeValueToken(value);
    }

    private void writeAttributeValueToken(String value) throws IOException {
        writer.write("\"");
        writeBareValueToken(value);
        writer.write("\"");
    }

    private void writeValueToken(String value) throws IOException {
        writer.write(">");
        writeNextLine();
        increaseIndentation();
        writeIndentation();
        writeBareValueToken(value);
        lastTagEndTag = false;
    }

    private void writeVersion(String version) throws IOException {
        writer.write("<?xml");
        writer.write(" version=\"" + version + "\"");
    }

    private void writeEncoding(String encoding) throws IOException {
        writer.write(" encoding=\"" + encoding + "\"?>");
        writeNextLine();
    }

    private void writeIndentation() throws IOException {
        if (prettyPrint) {
            writer.write(indentationPrefix.toString());
        }
    }

    private void increaseIndentation() {
        if (prettyPrint) {
            indentationPrefix.append("  ");
        }
    }

    private void decreaseIndentation() {
        if (prettyPrint) {
            indentationPrefix.setLength(indentationPrefix.length() - 2);
        }
    }

    private void updateOnNewType(Ur.Type type) throws IOException {
        switch (type) {
            case String:
            case Number:
            case Boolean:
                nesting.push(new StateHolder(State.InValue, type, lastNonControlKey));
                break;
            case Object:
                nesting.push(new StateHolder(State.InObject, type, lastNonControlKey));
                break;
            default:
                // impossible
        }
    }

    private void writeBareValueToken(String value) throws IOException {
        switch(nesting.peek().type) {
            case String:
                writeString(value);
                break;
            case Number:
                writeNumber(value);
                break;
            case Boolean:
                writeBoolean(value);
                break;
            default:
                throw new IOException("Can not write value \"" + value + "\"" + " without a set type");
        }
    }

    private void writeNextLine() throws IOException {
        if (prettyPrint) {
            writer.write("\n");
        }
    }

    @Override
    protected void writeString(String value) throws IOException {
        value.replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("&","&amp;")
            .replace("'","&apos;")
            .replace("\"", "&quot;");
        writer.write(value);
    }

    @Override
    public void flush() {
        try {
            writer.flush();
        } catch (IOException ex) {
            // TODO throw custom exception
        }
    }
}