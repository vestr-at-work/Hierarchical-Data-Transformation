package cz.cuni.mff.hdt.sink;

import cz.cuni.mff.hdt.ur.Ur;

import java.io.Writer;
import java.io.IOException;

public abstract class UrSink implements Sink {
    protected Writer _writer;

    public abstract void openObject();

    public abstract void closeObject();

    public abstract void openArray();

    public abstract void closeArray();

    public abstract void setNextKey(String key);

    public abstract void writeValue(String value) throws IOException;

    public abstract void flush();

    protected Ur.Type getType(String value) throws IOException {
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

    protected void writeBoolean(String value) throws IOException {
        String sanitizedValue = "false";
        if ("1".equals(value)
            || "true".equals(value)
            || "True".equals(value)) {
            sanitizedValue = "true";
        }
        _writer.write(sanitizedValue);
    }

    protected void writeNumber(String value) throws IOException {
        String sanitizedValue = value.replace(",", ".").replace(" ", "");
        _writer.write(sanitizedValue);
    }

    protected void writeString(String value) throws IOException {
        String sanitizedValue = value
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\"", "\\\"");
        _writer.write("\"");
        _writer.write(sanitizedValue);
        _writer.write("\"");
    }
}