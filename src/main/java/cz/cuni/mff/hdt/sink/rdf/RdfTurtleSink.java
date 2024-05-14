package cz.cuni.mff.hdt.sink.rdf;

import cz.cuni.mff.hdt.sink.*;
import cz.cuni.mff.hdt.ur.Ur;

import java.io.Writer;
import java.io.IOException;
import java.util.Stack;

/**
 * RDF Turtle format specific implementation of Sink.
 */
public class RdfTurtleSink extends UrSink {
    private enum State {
        Unknown,
        InObject,
        InValue;
    }

    private class StateHolder {
        public State state;
        public Boolean writeSeparator = false;
        public Ur.Type type;

        public StateHolder(State state, Ur.Type type) {
            this.state = state;
            this.type = type;
        }
    }

    private enum Token { Unknown, Type, Value, Language, Identificator }

    private String _key = null;
    private Token _nextValue = Token.Unknown;
    private Stack<StateHolder> _nesting = new Stack<StateHolder>();
    
    private Boolean _prettyPrint;
    private StringBuilder _indentationPrefix = new StringBuilder();
    private Boolean _afterKey = false;

    public RdfTurtleSink(Writer writer, Boolean prettyPrint) {
        writer = writer;
        _prettyPrint = prettyPrint;
        _nesting.push(new StateHolder(State.Unknown, null));
    }

    public RdfTurtleSink(Writer writer) {
        this(writer, false);
    }

    @Override
    public void openObject() throws IOException {}

    @Override
    public void closeObject() throws IOException {
        var stateToBeClosed = _nesting.pop().state;
        switch (stateToBeClosed) {
            case InObject:
                writeCloseObject();
                break;
            default:
        }
    }

    @Override
    public void openArray() throws IOException {
        if (_key.equals(Ur.KEY_TYPE)) {
            _nextValue = Token.Type;
        }
        else if (_key.equals(Ur.KEY_VALUE)) {
            _nextValue = Token.Value;
        }
        else if (_key.equals(Ur.KEY_RDF_ID)) {
            _nextValue = Token.Identificator;
        }
        else if (_key.equals(Ur.KEY_RDF_LANGUAGE)) {
            _nextValue = Token.Language;
        }
        else {
            writeKey();
        }
    }

    private void writeKey() throws IOException {
        if (_key == null) {
            return;
        }
        writeSeparator();
        writeIndentation();
        writer.write("<");
        writeString(_key);
        writer.write(">");
        _afterKey = true;
    }

    @Override
    public void closeArray() throws IOException {}

    @Override
    public void setNextKey(String key) throws IOException {
        _key = key;
    }

    @Override
    public void writeValue(String value) throws IOException {
        switch (_nextValue) {
            case Type:
                updateOnNewType(getType(value));
                break;
            case Value:
                writeValueToken(value);
                _nextValue = Token.Unknown;
                break;
            default:
        }
    }

    private void writeIndentation() throws IOException {
        if (_prettyPrint) {
            writer.write(_indentationPrefix.toString());
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
                _nesting.push(new StateHolder(State.InValue, type));
                break;
            case Object:
                writeOpenObject();
                _nesting.push(new StateHolder(State.InObject, type));
                break;
            default:
                // impossible
        }
    }

    private void writeOpenObject() throws IOException {
        writeSeparator();
        writeIndentation();
        writer.write("{");
        increaseIndentation();
    }

    private void writeCloseObject() throws IOException {
        writeNextLine();
        decreaseIndentation();
        writeIndentation();
        writer.write("}");
    }

    private void writeValueToken(String value) throws IOException {
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
        _afterKey = false;
    }

    private void writeSeparator() throws IOException {
        var stateHolder = _nesting.peek();
        if (stateHolder.state == State.Unknown) {
            return;
        }
        if (stateHolder.writeSeparator && !_afterKey) {
            writer.write(",");
        }
        writeNextLine();
        stateHolder.writeSeparator = true;
        _afterKey = false;
    }

    private void writeNextLine() throws IOException {
        if (_prettyPrint) {
            writer.write("\n");
        }
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