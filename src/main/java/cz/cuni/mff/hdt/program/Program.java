package cz.cuni.mff.hdt.program;

import cz.cuni.mff.hdt.converter.json.JsonInputConverter;
import cz.cuni.mff.hdt.converter.json.JsonOutputConverter;
import cz.cuni.mff.hdt.sink.json.JsonSink;
import cz.cuni.mff.hdt.source.json.memory.JsonInMemoryDocumentSource;
import cz.cuni.mff.hdt.transformation.Transformation;
import cz.cuni.mff.hdt.transformation.TransformationContext;
import cz.cuni.mff.hdt.transformation.TransformationDefinition;
import cz.cuni.mff.hdt.ur.Ur;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

public class Program {
    public static void main(String[] args) throws IOException {

        String pathToInputFile = args[0];

        Ur inputUr = new JsonInputConverter().convert(new ByteArrayInputStream(Files.readAllBytes(Paths.get(pathToInputFile))));
        System.out.println(inputUr.getInnerRepresentation().toString(2));

        System.out.println("-----");
        var output = new JsonOutputConverter().convert(inputUr);
        System.out.println(output);





        
        // String pathToInputFile = args[0];
        // String pathToTransformationDefinition = args[1];

        // var source = new JsonInMemoryDocumentSource(readFileAsString(pathToInputFile));
        // var transformationDefinitionObject = new JSONObject(readFileAsString(pathToTransformationDefinition));
        // var definition = TransformationDefinition.getTransformationDefinitionFromObject(transformationDefinitionObject);
        // var writer = new StringWriter();
        // var sink = new JsonSink(writer, true);

        // TransformationContext context = new TransformationContext(source, sink, definition);
        // var transformation = new Transformation(context);
        
        // transformation.transform();

        // var result = writer.toString();
        // System.out.println(result);
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