package cz.cuni.mff.hdt.sink.json;

import org.junit.jupiter.api.*;
import java.util.*;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class JsonSinkTest {
    @Test
    public void testSimpleStringProperty() {
        String expectedResult = "{\"name\":\"Ailish\"}";

        var writer = new StringWriter();
        var sink = new JsonSink(writer);

        try {
            sink.openObject();
            // object
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
            sink.flush();
        }
        catch (Exception e) {
            fail(e);
        }
        var result = writer.toString();

        assertEquals(expectedResult, result);
    }

    @Test
    public void testComplexArrayPropertyWithObjects() {
        String expectedResult = "{\"name\":\"Ailish\",\"details\":[{\"age\":18},{\"hasLighter\":true}]}";

        var writer = new StringWriter();
        var sink = new JsonSink(writer);

        try {
            sink.openObject();
            // object
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
            // array
            sink.setNextKey("details");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("array");
            sink.closeArray();
            // 1st object
            sink.setNextKey("0");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();
            sink.setNextKey("age");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("number");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("18");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            // 2nd object in array
            sink.setNextKey("1");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();
            sink.setNextKey("hasLighter");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("boolean");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("true");
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