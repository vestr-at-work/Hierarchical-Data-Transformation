package cz.cuni.mff.hdt.sink.csv;

import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.ur.Ur;

import java.io.Writer;
import java.io.IOException;

/**
 * CSV format specific implementation of Sink.
 */
public class CsvSink implements Sink {
    private enum State { Outside, InHeader, InRows };
    private enum Token { Unknown, Type, Value };

    private final Integer _ROWS_BASE_INDENTATION = 2;
    private final Integer _HEADER_BASE_INDENTATION = 1;

    private Writer _writer;
    private String _key = null;
    private Ur.Type _type = null;
    private Integer _column = 0;
    private State _state = State.Outside;
    private Boolean _nextValueFirstInRow = true;
    private Boolean _firstRowInFile = true;
    private Integer _objectIndentationLevel = 0;
    private Token _nextValue = Token.Unknown;

    public CsvSink(Writer writer) {
        _writer = writer;
    }

    @Override
    public void openObject() {
        _objectIndentationLevel++;
    }

    @Override
    public void closeObject() {
        _objectIndentationLevel--;
        if (_state == State.InRows && _objectIndentationLevel == _ROWS_BASE_INDENTATION) {
            _nextValueFirstInRow = true;
        }
        if (_state == State.InHeader && _objectIndentationLevel == _HEADER_BASE_INDENTATION) {
            _nextValueFirstInRow = true;
            _firstRowInFile = false;
        }
    }

    @Override
    public void openArray() {
        if (_key.equals(Ur.KEY_CSV_HEADER)) {
            _nextValueFirstInRow = true;
            _state = State.InHeader;
        }
        else if (_key.equals(Ur.KEY_CSV_HEADER)) {
            _nextValueFirstInRow = true;
            _state = State.InRows;
        }
        else if (_key.equals(Ur.KEY_TYPE)) {
            _nextValue = Token.Type;
        }
        else if (_key.equals(Ur.KEY_VALUE)) {
            _nextValue = Token.Value;
        }
    }

    @Override
    public void closeArray() {}

    @Override
    public void setNextKey(String key) {
        _key = key;
    }

    @Override
    public void writeValue(String value) throws IOException {
        switch (_nextValue) {
            case Type:
                _type = getType(value);
                break;
            case Value:
                writeValueToken(value);
                _nextValueFirstInRow = false;
                _type = null;
                _nextValue = Token.Unknown;
                break;
            default:
        }
    }

    private Ur.Type getType(String value) throws IOException {
        if (value.equals(Ur.VALUE_OBJECT)) {
            return Ur.Type.Object;
        }
        else if (value.equals(Ur.VALUE_ARRAY)) {
            return Ur.Type.Array;
        }
        else if (value.equals(Ur.VALUE_STRING)) {
            return Ur.Type.String;
        }
        else if (value.equals(Ur.VALUE_NUMBER)) {
            return Ur.Type.Number;
        }
        else if (value.equals(Ur.VALUE_BOOLEAN)) {
            return Ur.Type.Boolean;
        }
        else {
            throw new IOException("Invalid Ur datatype: " + value);
        }
    }

    private void writeValueToken(String value) throws IOException {
        writeSeparator();
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

    private void writeSeparator() throws IOException {
        if (!_nextValueFirstInRow) {
            _writer.write(",");
            return;
        }
        if (!_firstRowInFile) {
            _writer.write("\n");
        }
    }

    private void writeBoolean(String value) throws IOException {
        String sanitizedValue = "false";
        if ("1".equals(value)
            || "true".equals(value)
            || "True".equals(value)) {
            sanitizedValue = "true";
        }
        _writer.write(sanitizedValue);
    }

    private void writeNumber(String value) throws IOException {
        String sanitizedValue = value.replace(",", ".").replace(" ", "");
        _writer.write(sanitizedValue);
    }

    private void writeString(String value) throws IOException {
        String sanitizedValue = value
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\"", "\\\"");
        _writer.write("\"");
        _writer.write(sanitizedValue);
        _writer.write("\"");
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