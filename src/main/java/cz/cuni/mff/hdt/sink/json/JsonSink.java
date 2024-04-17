package cz.cuni.mff.hdt.sink.json;

import cz.cuni.mff.hdt.sink.*;
import cz.cuni.mff.hdt.ur.Ur;

import java.io.Writer;
import java.io.IOException;
import java.util.Stack;

/**
 * JSON format specific implementation of Sink.
 */
public class JsonSink extends UrSink {
    private enum State {
        Unknown,
        InObject,
        InArray,
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

    private enum Token { Unknown, Type, Value }

    private class ValueStateAcumulator {
        public Token nextValue = Token.Unknown;
        public String valueString;
        public Ur.Type type;
    }

    private String key = null;
    private ValueStateAcumulator acumulator = new ValueStateAcumulator();
    private Stack<StateHolder> nesting = new Stack<StateHolder>();
    
    private Boolean prettyPrint;
    private StringBuilder indentationPrefix = new StringBuilder();
    private Boolean afterKey = false;

    public JsonSink(Writer writer, Boolean prettyPrint) {
        this.writer = writer;
        this.prettyPrint = prettyPrint;
        nesting.push(new StateHolder(State.Unknown, null));
    }

    public JsonSink(Writer writer) {
        this(writer, false);
    }

    @Override
    public void openObject() throws IOException {}

    @Override
    public void closeObject() throws IOException {
        var stateToBeClosed = nesting.pop().state;
        switch (stateToBeClosed) {
            case InObject:
                writeCloseObject();
                break;
            case InArray:
                writeCloseArray();
                break;
            default:
        }
    }

    @Override
    public void openArray() throws IOException {
        if (key.equals(Ur.KEY_TYPE)) {
            acumulator.nextValue = Token.Type;
        }
        else if (key.equals(Ur.KEY_VALUE)) {
            acumulator.nextValue = Token.Value;
        }
        else if (isNumeric(key) && nesting.peek().state == State.InArray) {
            // TODO dont write it but act accordingly
        }
        else {
            writeKey();
        }
    }

    private void writeKey() throws IOException {
        if (key == null) {
            return;
        }
        writeSeparator();
        writeIndentation();
        writeString(key);
        writer.write(":");
        afterKey = true;
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
    }

    @Override
    public void writeValue(String value) throws IOException {
        switch (acumulator.nextValue) {
            case Type:
                var type = getType(value);
                updateOnNewType(type);
                acumulator.type = type;

                if (acumulator.valueString != null) {
                    writeValueToken(acumulator.valueString);
                    acumulator = new ValueStateAcumulator();
                }
                break;
            case Value:
                if (acumulator.type == null) {
                    acumulator.valueString = value;
                    break;
                }
                
                writeValueToken(value);
                acumulator = new ValueStateAcumulator();
                break;
            default:
        }
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
                nesting.push(new StateHolder(State.InValue, type));
                break;
            case Object:
                writeOpenObject();
                nesting.push(new StateHolder(State.InObject, type));
                break;
            case Array:
                writeOpenArray();
                nesting.push(new StateHolder(State.InArray, type));
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

    private void writeOpenArray() throws IOException {
        writeSeparator();
        writeIndentation();
        writer.write("[");
        increaseIndentation();
    }

    private void writeCloseObject() throws IOException {
        writeNextLine();
        decreaseIndentation();
        writeIndentation();
        writer.write("}");
    }

    private void writeCloseArray() throws IOException {
        writeNextLine();
        decreaseIndentation();
        writeIndentation();
        writer.write("]");
    }

    private void writeValueToken(String value) throws IOException {
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
        afterKey = false;
    }

    private void writeSeparator() throws IOException {
        var stateHolder = nesting.peek();
        if (stateHolder.state == State.Unknown) {
            return;
        }
        if (stateHolder.writeSeparator && !afterKey) {
            writer.write(",");
        }
        writeNextLine();
        stateHolder.writeSeparator = true;
        afterKey = false;
    }

    private void writeNextLine() throws IOException {
        if (prettyPrint) {
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