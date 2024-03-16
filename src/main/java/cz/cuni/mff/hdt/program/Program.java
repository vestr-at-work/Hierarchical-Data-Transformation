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

            sink.setNextKey("@version");
            sink.openArray();
            sink.writeValue("1.0");
            sink.closeArray();
            sink.setNextKey("@encoding");
            sink.openArray();
            sink.writeValue("UTF-8");
            sink.closeArray();

            sink.setNextKey("person");
            sink.openArray();
            sink.openObject();

            sink.setNextKey("@attributes");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("xml:lang");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("en");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();

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

            sink.setNextKey("list");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            sink.setNextKey("@1:item");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("apple");
            sink.closeArray();
            sink.closeObject();

            sink.setNextKey("@2:item");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("pear");
            sink.closeArray();
            sink.closeObject();

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
            System.out.println(e);
        }
        var result = writer.toString();
        System.out.print(result);
    }
}