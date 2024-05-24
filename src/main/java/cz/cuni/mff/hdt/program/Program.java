package cz.cuni.mff.hdt.program;

import cz.cuni.mff.hdt.converter.json.JsonInputConverter;
import cz.cuni.mff.hdt.converter.json.JsonOutputConverter;
import cz.cuni.mff.hdt.operation.BasicOperationFactory;
import cz.cuni.mff.hdt.transformation.Transformation;
import cz.cuni.mff.hdt.transformation.TransformationDefinition;
import cz.cuni.mff.hdt.ur.Ur;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONObject;

/**
 * The main class to run the transformation program.
 */
public class Program {
    /**
     * The main method to run the program.
     *
     * @param args the command line arguments, where the first argument is the path to the input file,
     *             and the second argument is the path to the transformation definition file
     * @throws IOException if there is an error reading the input files
     */
    public static void main(String[] args) throws IOException {

        // String pathToInputFile = args[0];

        // Ur inputUr = new JsonInputConverter().convert(new ByteArrayInputStream(Files.readAllBytes(Paths.get(pathToInputFile))));
        // System.out.println(inputUr.getInnerRepresentation().toString(2));
        // //inputUr.set(new UrPath("/person/called"), inputUr.get(new UrPath("/person/name")));
        // inputUr.delete(new UrPath("/person/name/@value"));
        // System.out.println("-----");
        // System.out.println(inputUr.getInnerRepresentation().toString(2));
        // //var output = new JsonOutputConverter().convert(inputUr);
        // //System.out.println(output);





        
        String pathToInputFile = args[0];
        String pathToTransformationDefinition = args[1];

        var transformationDefinition = TransformationDefinition.getTransformationDefinition(
            new JSONObject(readFileAsString(pathToTransformationDefinition)), 
            new BasicOperationFactory()
        );
        
        var transformation = new Transformation(transformationDefinition);
        Ur inputUr = new JsonInputConverter().convert(new ByteArrayInputStream(Files.readAllBytes(Paths.get(pathToInputFile))));

        System.out.println(inputUr.getInnerRepresentation().toString(2));

        Ur outputUr = transformation.transform(inputUr);

        System.out.println("----");
        System.out.println(outputUr.getInnerRepresentation().toString(2));

        var output = new JsonOutputConverter().convert(outputUr);
        
        System.out.println(output);
    }

    /**
     * Reads the content of a file and returns it as a string.
     *
     * @param fileName the path to the file
     * @return the content of the file as a string
     * @throws IOException if there is an error reading the file
     */
    public static String readFileAsString(String fileName)
        throws IOException
    {
        String data = "";
        data = new String(
            Files.readAllBytes(Paths.get(fileName)));
        return data;
    }
}