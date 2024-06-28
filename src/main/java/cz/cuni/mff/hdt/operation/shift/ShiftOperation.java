package cz.cuni.mff.hdt.operation.shift;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.path.UrPath;

/*
 * Class implementing the shift operation of the transformation language
 */
public class ShiftOperation implements Operation {
    public static final String KEY_INPUT_PATH = "input-path";
    public static final String KEY_OUTPUT_PATH = "output-path";

    // first is input path, second is array of output paths
    private ArrayList<Pair<UrPath, ArrayList<UrPath>>> noVariablePaths;

    // paths with no variables
    // paths with variables (probably will be more complex structure)

    /**
     * Constructs a new ShiftOperation with the operation specifications.
     *
     * @param operationSpecs the JSON array containing the operation specifications
     * @throws IOException if there is an error parsing the operation specifications
     */
    public ShiftOperation(JSONArray operationSpecs) throws IOException {
        noVariablePaths = new ArrayList<>();
        for (var spec : operationSpecs) {
            parseSpec(spec);
        }
    }

    /**
     * Executes the shift operation on the provided input {@code Ur} object.
     *
     * @param inputUr the input {@code Ur} object
     * @return the resulting {@code Ur} object after the shift operation is applied
     * @throws OperationFailedException if the shift operation fails
     */
    @Override
    public Ur execute(Ur inputUr) throws OperationFailedException {
        var outputUr = new Ur(new JSONObject());
        // this stays 
        for (var pair : noVariablePaths) {
            var inputPath = pair.getLeft();
            var outputPaths = pair.getRight();
            try {
                Ur toShift = inputUr.get(inputPath);
                for (var outputPath : outputPaths) {
                    outputUr.set(outputPath, toShift);
                }
            }
            catch (IOException e) {
                throw new OperationFailedException("Value from '" + inputPath + "' could not be shifted");
            }
        }

        // after no variable paths delt with there will be the code for variables


        return outputUr;
    }

    private void parseSpec(Object spec) throws IOException {
        if (!(spec instanceof JSONObject)) {
            throw new IOException("Incorrect type of an item in specs");
        }
        var specObject = (JSONObject)spec;
        if (!specObject.has(KEY_INPUT_PATH) || !specObject.has(KEY_OUTPUT_PATH)) {
            throw new IOException("Mandatory keys are missing from item in specs");
        }

        var inputPathUnknown = specObject.get(KEY_INPUT_PATH);
        var outputPathsUnknown = specObject.get(KEY_OUTPUT_PATH);

        if (!(inputPathUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_INPUT_PATH + "' in spec item");
        }
        if (!(outputPathsUnknown instanceof String) && !(outputPathsUnknown instanceof JSONArray)) {
            throw new IOException("Incorrect type of key '" + KEY_OUTPUT_PATH + "' in spec item");
        }

        if (outputPathsUnknown instanceof String) {
            parsePaths((String)inputPathUnknown, (String)outputPathsUnknown);
        }
        else {
            var outputPathsArray = (JSONArray)outputPathsUnknown;
            parsePaths((String)inputPathUnknown, outputPathsArray);
        }
    }

    private void parsePaths(String inputPath, String outputPath) throws IOException {
        var inputPathTokens = getPathTokens(inputPath);
        var outputPathTokens = getPathTokens(outputPath);
        // No varibles in inputPath but some variables in outputPath -> error 
        if (inputPathTokens.getRight() == null && outputPathTokens.getRight() != null) {
            throw new IOException("Unexpected variable in '" + KEY_OUTPUT_PATH + "'. No matching variable present in '" + KEY_INPUT_PATH + "'");
        }
        // No variables
        else if (inputPathTokens.getRight() == null && outputPathTokens.getRight() == null) {
            var inputUrPath = new UrPath(inputPathTokens.getLeft().toArray(new String[inputPathTokens.getLeft().size()]));
            var outputUrPath = new UrPath(outputPathTokens.getLeft().toArray(new String[outputPathTokens.getLeft().size()]));
            var outputPathArray = new ArrayList<UrPath>();
            outputPathArray.add(outputUrPath);

            noVariablePaths.add(Pair.of(inputUrPath, outputPathArray));
            return;
        }

        // Has variables
        
        // parse varibles on indexes 
        // and somehow store it

        // Class VariableUrPath
        // - has variable tokens support
        // - has export to UrPath when variable values provided
        // - supports subPath
        // - bool hasVariables() method
        // - can copy itself
        // - gets values of variables by name

    }

    private void parsePaths(String inputPath, JSONArray outputPaths) throws IOException {
        for (var outputPath : outputPaths) {
            if (!(outputPath instanceof String)) {
                throw new IOException("Incorrect type of output path item");
            }
            //outputPaths.add(new UrPath((String)outputPath));
        }
    }

    private Pair<ArrayList<String>, ArrayList<Integer>> getPathTokens(String inputPath) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getPathTokens'");
    }
}
