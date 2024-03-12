package cz.cuni.mff.hdt.sink;

import java.io.IOException;

public interface Sink {
    public void openObject();

    public void closeObject();

    public void openArray();

    public void closeArray();

    public void setNextKey(String key);

    public void writeValue(String value) throws IOException;

    public void flush();
}