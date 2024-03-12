package cz.cuni.mff.hdt.program;

import cz.cuni.mff.hdt.sink.csv.CsvSink;
import java.io.PrintWriter;

public class Program {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        var sink = new CsvSink(new PrintWriter(System.out));

        try {
            sink.openObject();
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
            sink.flush();
        }
        catch (Exception e) {
            System.out.println(e);
        }
    }
}