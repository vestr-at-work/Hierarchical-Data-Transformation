package cz.cuni.mff.hdt.program;

import cz.cuni.mff.hdt.sink.json.*;
import java.io.StringWriter;

public class Program {
    public static void main(String[] args) {
        //System.out.println("Hello world!");

        var writer = new StringWriter();
        var sink = new JsonSink(writer, false);

        try {

        }
        catch (Exception e) {

        }
        var result = writer.toString();
        System.out.print(result);
    }
}