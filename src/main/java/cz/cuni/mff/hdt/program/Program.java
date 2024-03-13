package cz.cuni.mff.hdt.program;

import cz.cuni.mff.hdt.sink.json.*;
import java.io.StringWriter;

public class Program {
    public static void main(String[] args) {
        //System.out.println("Hello world!");

        var writer = new StringWriter();
        var sink = new JsonSink(writer, true);

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
            sink.closeObject();
            sink.closeArray();

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {

        }
        var result = writer.toString();
        System.out.print(result);
    }
}