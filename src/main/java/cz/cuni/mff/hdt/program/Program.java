package cz.cuni.mff.hdt.program;

import cz.cuni.mff.hdt.sink.xml.*;
import java.io.StringWriter;

public class Program {
    public static void main(String[] args) {
        //System.out.println("Hello world!");

        var writer = new StringWriter();
        var sink = new XmlSink(writer, true);

        try {
            sink.openObject();
            // header
            sink.setNextKey("person");
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

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {

        }
        var result = writer.toString();
        System.out.print(result);
    }
}