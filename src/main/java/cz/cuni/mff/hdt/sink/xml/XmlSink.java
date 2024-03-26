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
        public Boolean writeSeparator = false;

        public StateHolder(State state, Ur.Type type, String tag) {
            this.state = state;
            this.type = type;
            this.tag = tag;
        }
    }

    private enum Token { Unknown, Type, Value, VersionValue, EncodingValue }

    private String _key = null;
    private String _lastNonControlKey = null;
    private State _state = State.Unknown;
    private Token _nextValue = Token.Unknown;
    private Stack<StateHolder> _nesting = new Stack<StateHolder>();
    
    private Boolean _prettyPrint;
    private StringBuilder _indentationPrefix = new StringBuilder();
    private Boolean _firstDocumentElement = true;
    private Boolean _lastTagEndTag = false;

    public XmlSink(Writer writer, Boolean prettyPrint) {
        _writer = writer;
        _prettyPrint = prettyPrint;
    }

    public XmlSink(Writer writer) {
        this(writer, false);
    }

    @Override
    public void openObject() throws IOException {}

    @Override
    public void closeObject() throws IOException {
        if (_nesting.empty()) {
            return;
        }
        var toBeClosed = _nesting.pop();
        if (toBeClosed.state == State.InAttributes) {
            _lastNonControlKey = toBeClosed.tag;
            return;
        }

        if (toBeClosed.state == State.InValue
            && _nesting.peek().state == State.InAttributes) {
            return;
        }

        writeNextLine();
        decreaseIndentation();
        writeIndentation();
        _writer.write("</");
        writeString(toBeClosed.tag);
        _writer.write(">");
        _lastTagEndTag = true;
    }

    @Override
    public void openArray() throws IOException {
        if (_key.equals(Ur.KEY_TYPE)) {
            _nextValue = Token.Type;
        }
        else if (_key.equals(Ur.KEY_VALUE)) {
            _nextValue = Token.Value;
        }
        else if (_key.equals(Ur.KEY_XML_ATTRIBUTES)) {
            _nesting.push(new StateHolder(State.InAttributes, null, _lastNonControlKey));
        }
        else if (_key.equals(Ur.KEY_XML_VERSION)) {
            _nextValue = Token.VersionValue;
        }
        else if (_key.equals(Ur.KEY_XML_ENCODING)) {
            _nextValue = Token.EncodingValue;
        }
        // if key is Ur XML list key e.g. "@2:item"
        else if (_key.split(":")[0].charAt(0) == '@' && isNumeric(_key.split(":")[0].substring(1))) {
            _key = _key.split(":")[1];
            _lastNonControlKey = _key;
            writeKey();
        }
        else if (!_nesting.empty() && _nesting.peek().state == State.InAttributes) {
            writeAttribute();
        }
        else {
            writeKey();
        }
    }

    private void writeAttribute() throws IOException {
        _writer.write(" ");
        writeString(_key);
        _writer.write("=");
    }

    private void writeKey() throws IOException {
        if (_key == null) {
            return;
        }
        if (!_firstDocumentElement) {
            if (!_lastTagEndTag) {
                _writer.write(">");
                increaseIndentation();
            }
            writeNextLine();
        }
        _firstDocumentElement = false;
        writeIndentation();
        _writer.write("<");
        writeString(_key);
        _lastTagEndTag = false;
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
        _key = key;
        if (_key.charAt(0) != '@') {
            _lastNonControlKey = key;
        }
    }

    @Override
    public void writeValue(String value) throws IOException {
        switch (_nextValue) {
            case Type:
                updateOnNewType(getType(value));
                break;
            case Value:
                processValueToken(value);
                break;
            case VersionValue:
                writeVersion(value);
                _nextValue = Token.Unknown;
                break;
            case EncodingValue:
                writeEncoding(value);
                _nextValue = Token.Unknown;
                break;
            default:
        }
    }

    private void processValueToken(String value) throws IOException {
        var lastStateHolder = _nesting.pop();
        boolean valueIsAttribute = !_nesting.empty() && _nesting.peek().state == State.InAttributes;
        _nesting.push(lastStateHolder);
        _nextValue = Token.Unknown;

        if (valueIsAttribute) {
            writeAttributeValueToken(value);
            return;
        }
        
        writeValueToken(value);
    }

    private void writeAttributeValueToken(String value) throws IOException {
        _writer.write("\"");
        writeBareValueToken(value);
        _writer.write("\"");
    }

    private void writeValueToken(String value) throws IOException {
        _writer.write(">");
        writeNextLine();
        increaseIndentation();
        writeIndentation();
        writeBareValueToken(value);
        _lastTagEndTag = false;
    }

    private void writeVersion(String version) throws IOException {
        _writer.write("<?xml");
        _writer.write(" version=\"" + version + "\"");
    }

    private void writeEncoding(String encoding) throws IOException {
        _writer.write(" encoding=\"" + encoding + "\"?>");
        writeNextLine();
    }

    private void writeIndentation() throws IOException {
        if (_prettyPrint) {
            _writer.write(_indentationPrefix.toString());
        }
    }

    private void increaseIndentation() {
        if (_prettyPrint) {
            _indentationPrefix.append("  ");
        }
    }

    private void decreaseIndentation() {
        if (_prettyPrint) {
            _indentationPrefix.setLength(_indentationPrefix.length() - 2);
        }
    }

    private void updateOnNewType(Ur.Type type) throws IOException {
        switch (type) {
            case String:
            case Number:
            case Boolean:
                _nesting.push(new StateHolder(State.InValue, type, _lastNonControlKey));
                break;
            case Object:
                _nesting.push(new StateHolder(State.InObject, type, _lastNonControlKey));
                break;
            default:
                // impossible
        }
    }

    private void writeBareValueToken(String value) throws IOException {
        switch(_nesting.peek().type) {
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
        if (_prettyPrint) {
            _writer.write("\n");
        }
    }

    @Override
    protected void writeString(String value) throws IOException {
        value.replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("&","&amp;")
            .replace("'","&apos;")
            .replace("\"", "&quot;");
        _writer.write(value);
    }

    @Override
    public void flush() {
        try {
            _writer.flush();
        } catch (IOException ex) {
            // TODO throw custom exception
        }
    }
}