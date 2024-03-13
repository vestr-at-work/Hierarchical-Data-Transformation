package cz.cuni.mff.hdt.sink.csv;

import cz.cuni.mff.hdt.sink.*;
import cz.cuni.mff.hdt.ur.Ur;

import java.io.Writer;
import java.io.IOException;

/**
 * CSV format specific implementation of Sink.
 */
public class CsvSink extends UrSink {
    private enum State { Outside, InHeader, InRows };
    private enum Token { Unknown, Type, Value };

    private final Integer _ROWS_BASE_INDENTATION = 2;
    private final Integer _HEADER_BASE_INDENTATION = 1;

    private String _key = null;
    private Ur.Type _type = null;
    private State _state = State.Outside;
    private Boolean _nextValueFirstInRow = true;
    private Boolean _firstRowInFile = true;
    private Integer _objectIndentationLevel = 0;
    private Token _nextValue = Token.Unknown;

    public CsvSink(Writer writer) {
        _writer = writer;
    }

    @Override
    public void openObject() throws IOException {
        _objectIndentationLevel++;
    }

    @Override
    public void closeObject() throws IOException {
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
    public void openArray() throws IOException {
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
    public void closeArray() throws IOException {}

    @Override
    public void setNextKey(String key) throws IOException {
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

    @Override
    public void flush() {
        try {
            _writer.flush();
        } catch (IOException ex) {
            // TODO throw custom exception
        }
    }
}