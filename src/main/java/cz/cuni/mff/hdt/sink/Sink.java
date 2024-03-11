package cz.cuni.mff.hdt.sink;

public interface Sink {
    public void openObject();

    public void closeObject();

    public void openArray();

    public void closeArray();

    public void setNextKey(String key);

    public void writeValue(String value);

    public void flush();
}