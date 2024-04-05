package cz.cuni.mff.hdt.program;

import cz.cuni.mff.hdt.adapter.SinkWriterAdapter;
import cz.cuni.mff.hdt.sink.json.JsonSink;
import cz.cuni.mff.hdt.sink.xml.*;

import java.io.IOError;
import java.io.IOException;
import java.io.StringWriter;

import org.json.JSONPointer;

public class Program {
    public static void main(String[] args) throws IOException {

        var writer = new StringWriter();
        var sink = new XmlSink(writer, true);
        var adapter = new SinkWriterAdapter(sink);

        try {
            //adapter.write(new JSONPointer("/@type/[]"), "object");
            adapter.write(new JSONPointer("/person/[]/@type/[]"), "object");
            adapter.write(new JSONPointer("/person/[]/name/[]/@type/[]"), "string");
            adapter.write(new JSONPointer("/person/[]/name/[]/@value/[]"), "Ailish");
            adapter.write(new JSONPointer("/person/[]/age/[]/@type/[]"), "number");
            adapter.write(new JSONPointer("/person/[]/age/[]/@value/[]"), "112");
            adapter.finishWriting();
        }
        catch (Exception e) {
            throw e;
        }
        var result = writer.toString();
        System.out.print(result);
    }
}