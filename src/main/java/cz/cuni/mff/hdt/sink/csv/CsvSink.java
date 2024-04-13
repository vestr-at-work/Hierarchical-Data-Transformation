package cz.cuni.mff.hdt.sink.csv;

import cz.cuni.mff.hdt.sink.*;
import cz.cuni.mff.hdt.ur.Ur;

import java.io.Writer;
import java.io.IOException;

/**
 * CSV format specific implementation of Sink.
 */
public class CsvSink extends UrSink {
    private enum State { Unknown, InHeader, InRows };
    private enum Token { Unknown, Type, Value };

    private final Integer _ROWS_BASE_INDENTATION = 2;
    private final Integer _HEADER_BASE_INDENTATION = 1;

    private String key = null;
    private Ur.Type type = null;
    private State state = State.Unknown;
    private Boolean nextValueFirstInRow = true;
    private Boolean firstRowInFile = true;
    private Integer objectIndentationLevel = 0;
    private Token nextValue = Token.Unknown;

    public CsvSink(Writer writer) {
        this.writer = writer;
    }

    @Override
    public void openObject() throws IOException {
        objectIndentationLevel++;
    }

    @Override
    public void closeObject() throws IOException {
        objectIndentationLevel--;
        if (state == State.InRows && objectIndentationLevel == _ROWS_BASE_INDENTATION) {
            nextValueFirstInRow = true;
        }
        if (state == State.InHeader && objectIndentationLevel == _HEADER_BASE_INDENTATION) {
            nextValueFirstInRow = true;
            firstRowInFile = false;
        }
    }

    @Override
    public void openArray() throws IOException {
        if (key.equals(Ur.KEY_CSV_HEADER)) {
            nextValueFirstInRow = true;
            state = State.InHeader;
        }
        else if (key.equals(Ur.KEY_CSV_HEADER)) {
            nextValueFirstInRow = true;
            state = State.InRows;
        }
        else if (key.equals(Ur.KEY_TYPE)) {
            nextValue = Token.Type;
        }
        else if (key.equals(Ur.KEY_VALUE)) {
            nextValue = Token.Value;
        }
    }

    @Override
    public void closeArray() throws IOException {}

    @Override
    public void setNextKey(String key) throws IOException {
        this.key = key;
    }

    @Override
    public void writeValue(String value) throws IOException {
        switch (nextValue) {
            case Type:
                type = getType(value);
                break;
            case Value:
                writeValueToken(value);
                nextValueFirstInRow = false;
                type = null;
                nextValue = Token.Unknown;
                break;
            default:
        }
    }

    private void writeValueToken(String value) throws IOException {
        writeSeparator();
        switch(type) {
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
        if (!nextValueFirstInRow) {
            writer.write(",");
            return;
        }
        if (!firstRowInFile) {
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