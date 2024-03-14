package cz.cuni.mff.hdt.sink.csv;

import org.junit.jupiter.api.*;
import java.util.*;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CsvSinkTest {
    @Test
    public void testOneElementHeaderAndSingleOneElementRow() {
        String expectedResult = "\"name\"\n\"Ailish\"";

        var writer = new StringWriter();
        var sink = new CsvSink(writer);

        try {
            sink.openObject();
            // header
            sink.setNextKey("@header");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("array");
            sink.closeArray();
            sink.setNextKey("0");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("name");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            sink.closeObject();
            // row
            sink.setNextKey("@header");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("array");
            sink.closeArray();
            sink.setNextKey("0");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();
            sink.setNextKey("name");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("Ailish");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {
            fail(e);
        }
        var result = writer.toString();

        assertEquals(expectedResult, result);
    }
}
