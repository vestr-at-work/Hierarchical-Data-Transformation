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
        InArray,
        InValue;
    }

    private class StateHolder {
        public State state;
        public Boolean writeSeparator = false;
        public String tag;

        public StateHolder(State state, String tag) {
            this.tag = tag;
            this.state = state;
        }
    }

    private enum Token { Unknown, Type, Value }

    private String _key = null;
    private String _lastNonTypeKey = null;
    private Ur.Type _type = null;
    private State _state = State.Unknown;
    private Token _nextValue = Token.Unknown;
    private Stack<StateHolder> _nesting = new Stack<StateHolder>();
    private Boolean _prettyPrint;
    private StringBuilder _indentationPrefix = new StringBuilder();
    private Boolean _firstDocumentElement = true;

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
        writeNextLine();
        decreaseIndentation();
        writeIndentation();
        _writer.write("</");
        writeString(toBeClosed.tag);
        _writer.write(">");
    }

    @Override
    public void openArray() throws IOException {
        if (_key.equals(Ur.KEY_TYPE)) {
            _nextValue = Token.Type;
        }
        else if (_key.equals(Ur.KEY_VALUE)) {
            _nextValue = Token.Value;
        }
        else if (isNumeric(_key) && _nesting.peek().state == State.InArray) {
            // TODO dont write it but act accordingly
        }
        else {

            writeKey();
        }
    }

    private void writeKey() throws IOException {
        if (_key == null) {
            return;
        }
        if (!_firstDocumentElement) {
            _writer.write(">");
            writeNextLine();
            increaseIndentation();
        }
        _firstDocumentElement = false;
        writeIndentation();
        _writer.write("<");
        writeString(_key);
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
        if (!_key.equals(Ur.KEY_TYPE)) {
            _lastNonTypeKey = key;
        }
    }

    @Override
    public void writeValue(String value) throws IOException {
        switch (_nextValue) {
            case Type:
                _type = getType(value);
                updateOnNewType();
                break;
            case Value:
                writeValueToken(value);
                _type = null;
                _nextValue = Token.Unknown;
                break;
            default:
        }
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

    private void updateOnNewType() throws IOException {
        switch (_type) {
            case String:
            case Number:
            case Boolean:
                _nesting.push(new StateHolder(State.InValue, _lastNonTypeKey));
                break;
            case Object:
                _nesting.push(new StateHolder(State.InObject, _lastNonTypeKey));
                break;
            default:
                // impossible
        }
    }

    private void writeValueToken(String value) throws IOException {
        _writer.write(">");
        writeNextLine();
        increaseIndentation();
        writeIndentation();
        switch(_type) {
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