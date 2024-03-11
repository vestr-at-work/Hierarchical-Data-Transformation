package cz.cuni.mff.hdt.sink.csv;

import cz.cuni.mff.hdt.sink.Sink;

import java.io.Writer;

public class CsvSink implements Sink {
    private Writer _writer;
    private String _key = null;

    @Override
    public void openObject() {

    }

    @Override
    public void closeObject() {

    }

    @Override
    public void openArray() {

    }

    @Override
    public void closeArray() {

    }

    @Override
    public void setNextKey(String key) {
        _key = key;
    }

    @Override
    public void writeValue(String value) {

    }

    @Override
    public void flush() {

    }
}