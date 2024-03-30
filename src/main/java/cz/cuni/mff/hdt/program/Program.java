package cz.cuni.mff.hdt.program;

import cz.cuni.mff.hdt.sink.xml.*;
import java.io.StringWriter;

public class Program {
    public static void main(String[] args) {

        var writer = new StringWriter();
        var sink = new XmlSink(writer);

        try {
            sink.openObject();

            sink.setNextKey("@version");
            sink.openArray();
            sink.writeValue("1.0");
            sink.closeArray();
            sink.setNextKey("@encoding");
            sink.openArray();
            sink.writeValue("UTF-8");
            sink.closeArray();

            // open person
            sink.setNextKey("person");
            sink.openArray();
            sink.openObject();

            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // name
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

            // close person
            sink.closeObject();
            sink.closeArray();

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {
            System.out.println(e);
        }
        var result = writer.toString();
        System.out.print(result);
    }
}