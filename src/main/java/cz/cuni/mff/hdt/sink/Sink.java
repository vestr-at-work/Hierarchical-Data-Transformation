package cz.cuni.mff.hdt.sink;

import java.io.IOException;

public interface Sink {
    public void openObject() throws IOException;

    public void closeObject() throws IOException;

    public void openArray() throws IOException;

    public void closeArray() throws IOException;

    public void setNextKey(String key) throws IOException;

    public void writeValue(String value) throws IOException;

    public void flush();
}