package cz.cuni.mff.hdt.program;

import cz.cuni.mff.hdt.sink.json.JsonSink;
import cz.cuni.mff.hdt.source.json.memory.JsonInMemoryDocumentSource;
import cz.cuni.mff.hdt.transformation.Transformation;
import cz.cuni.mff.hdt.transformation.TransformationContext;
import cz.cuni.mff.hdt.transformation.TransformationDefinition;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

public class Program {
    public static void main(String[] args) throws IOException {

        // var writer = new StringWriter();
        // var sink = new XmlSink(writer, true);
        // var adapter = new SinkWriterAdapter(sink);

        // try {
        //     //adapter.write(new JSONPointer("/@type/[]"), "object");
        //     adapter.write(new JSONPointer("/person/[]/@type/[]"), "object");
        //     adapter.write(new JSONPointer("/person/[]/name/[]/@type/[]"), "string");
        //     adapter.write(new JSONPointer("/person/[]/name/[]/@value/[]"), "Ailish");
        //     adapter.write(new JSONPointer("/person/[]/age/[]/@type/[]"), "number");
        //     adapter.write(new JSONPointer("/person/[]/age/[]/@value/[]"), "112");
        //     adapter.finishWriting();
        // }
        // catch (Exception e) {
        //     throw e;
        // }
        // var result = writer.toString();
        // System.out.print(result);

        
        String pathToInputFile = args[0];
        String pathToTransformationDefinition = args[1];

        var source = new JsonInMemoryDocumentSource(readFileAsString(pathToInputFile));
        var transformationDefinitionObject = new JSONObject(readFileAsString(pathToTransformationDefinition));
        var definition = TransformationDefinition.getTransformationDefinitionFromObject(transformationDefinitionObject);
        var writer = new StringWriter();
        var sink = new JsonSink(writer, true);

        TransformationContext context = new TransformationContext(source, sink, definition);
        var transformation = new Transformation(context);
        
        transformation.transform();

        var result = writer.toString();
        System.out.println(result);
    }

    public static String readFileAsString(String fileName)
        throws IOException
    {
        String data = "";
        data = new String(
            Files.readAllBytes(Paths.get(fileName)));
        return data;
    }
}