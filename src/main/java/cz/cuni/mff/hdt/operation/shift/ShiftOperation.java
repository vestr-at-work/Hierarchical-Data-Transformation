package cz.cuni.mff.hdt.operation.shift;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.path.ArrayItemToken;
import cz.cuni.mff.hdt.path.PropertyToken;
import cz.cuni.mff.hdt.path.UrPath;
import cz.cuni.mff.hdt.path.VariableArrayItemToken;
import cz.cuni.mff.hdt.path.VariablePropertyToken;
import cz.cuni.mff.hdt.path.VariableUrPath;

/*
 * Class implementing the shift operation of the transformation language
 */
public class ShiftOperation implements Operation {
    public static final String KEY_INPUT_PATH = "input-path";
    public static final String KEY_OUTPUT_PATH = "output-path";

    // first is input path, second is array of output paths
    private ArrayList<Pair<UrPath, ArrayList<UrPath>>> noVariablePaths;
    private ArrayList<Pair<VariableUrPath, ArrayList<VariableUrPath>>> variablePaths;

    /**
     * Constructs a new ShiftOperation with the operation specifications.
     *
     * @param operationSpecs the JSON array containing the operation specifications
     * @throws IOException if there is an error parsing the operation specifications
     */
    public ShiftOperation(JSONArray operationSpecs) throws IOException {
        noVariablePaths = new ArrayList<>();
        variablePaths = new ArrayList<>();
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
        // Part for no variables 
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
                // TODO inputPath will not be printed as an readable UrPath. Add nice toString()
                throw new OperationFailedException("Value from '" + inputPath + "' could not be shifted");
            }
        }

        // Part for variables
        if (variablePaths.isEmpty()) {
            return outputUr;
        }

        var allPathsIndices = new ArrayList<Integer>();
        for (Integer i = 0; i < variablePaths.size(); i++) {allPathsIndices.add(i);};
        matchAndShiftVariablesRecursive(inputUr, inputUr, outputUr, 0, allPathsIndices);
        
        return outputUr;
    }

    private void resetVariables(ArrayList<Integer> matchingPathsIndices, int iteration) {
        for (var index : matchingPathsIndices) {
            resetVariableValue(index, iteration);
        }
    }

    private void resetVariableValue(Integer index, int iteration) {
        try {
            variablePaths.get(index).getLeft().tryResetVariable(iteration);
        }
        catch (IndexOutOfBoundsException e) {
            throw new IllegalStateException("This should not happen if everything works correctly");
        }
    }

    private void matchAndShiftVariablesRecursive(Ur propertyUr, Ur inputUr, Ur outputUr, int iteration,
            ArrayList<Integer> pathsIndices) throws OperationFailedException {

        var keys = propertyUr.getKeys();
        if (keys == null) {
            return;
        }
        var type = propertyUr.getRootType();
        if ((type != Ur.Type.Object) && (type != Ur.Type.Array)) {
            return;
        }
        for (var key : keys) {
            ArrayList<Integer> matchingPathsIndices = getMatchingPathIndices(key, type, pathsIndices, iteration);
            updateVariablePaths(key, type, matchingPathsIndices, iteration);

            var fullyMatchedPaths = new ArrayList<Integer>();
            for (var index : matchingPathsIndices) {
                var matchedPath = variablePaths.get(index).getLeft().getUrPath();
                
                if (matchedPath == null) { // some variables not matched yet
                    continue;
                }
                
                try {
                    shiftMatchedVariablePath(inputUr, outputUr, index);
                }
                catch (IOException e) {
                    // TODO inputPath will not be printed as an readable UrPath. Add nice toString()
                    throw new OperationFailedException("Value from '" + matchedPath + "' could not be shifted");
                }

                // remove them from recursion
                resetVariableValue(index, iteration);
                fullyMatchedPaths.add(index);
            }
            matchingPathsIndices.removeAll(fullyMatchedPaths);
            
            if (matchingPathsIndices.isEmpty()) {
                continue;
            }

            var someMatchingIndex = matchingPathsIndices.get(0);
            var urPathToProperty = variablePaths.get(someMatchingIndex).getLeft().getUrPath(iteration + 1);
            try {
                // get Ur of property
                var newPropertyUr = inputUr.getShared(urPathToProperty);
                // call function recursively
                matchAndShiftVariablesRecursive(newPropertyUr, inputUr, outputUr, iteration + 1, matchingPathsIndices);
            }
            catch (IOException e) {
                throw new OperationFailedException("Error occured when matching named variables.");
            }

            resetVariables(matchingPathsIndices, iteration);
        }
    }

    private void shiftMatchedVariablePath(Ur inputUr, Ur outputUr, Integer pathIndex) throws IOException {
        var pathPair = variablePaths.get(pathIndex);
        var inputPath = pathPair.getLeft();
        var outputPaths = pathPair.getRight();
        var noVariableInputPath = inputPath.getUrPath();

        Ur toShift = inputUr.get(noVariableInputPath);
        for (var outputPath : outputPaths) {
            fillOutputPathVariables(inputPath, outputPath);
            outputUr.set(outputPath.getUrPath(), toShift);
        }
    }

    private void fillOutputPathVariables(VariableUrPath inputPath, VariableUrPath outputPath) throws IOException {
        var outputPathVariableMap = outputPath.getVariableIndices();
        var inputPathVariableMap = inputPath.getVariableIndices();

        for (var outputVariableName : outputPathVariableMap.keySet()) {
            var inputVariableIndex = inputPathVariableMap.get(outputVariableName);
            var outputVariableIndex = outputPathVariableMap.get(outputVariableName);

            var inputToken = inputPath.tokens.get(inputVariableIndex);
            var outputToken = outputPath.tokens.get(outputVariableIndex);

            if (inputToken instanceof VariablePropertyToken) {
                var value = ((VariablePropertyToken)inputToken).getKey();
                if (outputToken instanceof VariablePropertyToken) {
                    ((VariablePropertyToken)outputToken).setKey(value);
                }
                else {
                    try {
                        ((VariableArrayItemToken)outputToken).setIndex(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e) {
                        throw new IOException("Non integer key matched for array item index");
                    }
                }
            }
            else if (inputToken instanceof VariableArrayItemToken) {
                var value = ((VariableArrayItemToken)inputToken).getIndex();
                if (outputToken instanceof VariableArrayItemToken) {
                    ((VariableArrayItemToken)outputToken).setIndex(value);
                }
                else {
                    ((VariablePropertyToken)outputToken).setKey(value.toString());
                }
            }
        }
    }

    private void updateVariablePaths(String key, Ur.Type type, ArrayList<Integer> matchingPathsIndices, int tokenIndex) {
        if (type == Ur.Type.Array) {
            try {
                var arrayIndex = Integer.parseInt(key);
                for (var pathIndex : matchingPathsIndices) {
                    var path = variablePaths.get(pathIndex).getLeft();
                    path.trySetArrayItemVariable(tokenIndex, arrayIndex);
                }
            }
            catch (NumberFormatException e) {
                throw new OperationFailedException("Incorrect Ur Array representaion. Indices not integers.");
            }
            return;
        }

        for (var pathIndex : matchingPathsIndices) {
            var path = variablePaths.get(pathIndex).getLeft();
            path.trySetPropertyVariable(tokenIndex, key);
        }
    }

    private ArrayList<Integer> getMatchingPathIndices(String key, Ur.Type type, ArrayList<Integer> indicesToPaths, int tokenIndex) {
        var outputIndices = new ArrayList<Integer>();

        for (var pathIndex : indicesToPaths) {
            var path = variablePaths.get(pathIndex).getLeft();
            var pathToken = path.tokens.get(tokenIndex);
            if (type == Ur.Type.Object && pathToken instanceof PropertyToken) {
                if (((PropertyToken)pathToken).getKey().equals(key)) {
                    outputIndices.add(pathIndex);
                }
            }
            else if (type == Ur.Type.Object && pathToken instanceof VariablePropertyToken) {
                outputIndices.add(pathIndex);
            }
            else if (type == Ur.Type.Array && pathToken instanceof ArrayItemToken) {
                if (((ArrayItemToken)pathToken).getIndex().toString().equals(key)) {
                    outputIndices.add(pathIndex);
                }
            }
            else if (type == Ur.Type.Array && pathToken instanceof VariableArrayItemToken) {
                outputIndices.add(pathIndex);
            }
        }

        return outputIndices;
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
        var inputUrPath = new VariableUrPath(inputPath);
        var outputUrPath = new VariableUrPath(outputPath);
        
        if (!inputUrPath.hasVariables() && outputUrPath.hasVariables()) {
            throw new IOException("Unexpected variable in '" + KEY_OUTPUT_PATH + "'. No matching variable present in '" + KEY_INPUT_PATH + "'");
        }
        // No variables
        else if (!inputUrPath.hasVariables() && !outputUrPath.hasVariables()) {
            var outputPathArray = new ArrayList<UrPath>();
            outputPathArray.add(outputUrPath.getUrPath());
            noVariablePaths.add(Pair.of(inputUrPath.getUrPath(), outputPathArray));
            return;
        }

        if (!outputVariablesValid(inputUrPath, outputUrPath)) {
            throw new IOException("Unexpected variable in '" + KEY_OUTPUT_PATH + "': " + outputPath + ". No matching variable present in '" + KEY_INPUT_PATH + "': " + inputPath);
        }

        var outputPathArray = new ArrayList<VariableUrPath>();
        outputPathArray.add(outputUrPath);
        variablePaths.add(Pair.of(inputUrPath, outputPathArray));
    }

    private boolean outputVariablesValid(VariableUrPath inputUrPath, VariableUrPath outputUrPath) {
        // TODO Auto-generated method stub !!!!!!!!
        // throw new UnsupportedOperationException("Unimplemented method 'outputVariablesValid'");



        return true;
    }

    private void parsePaths(String inputPath, JSONArray outputPaths) throws IOException {
        var inputUrPath = new VariableUrPath(inputPath);

        if (!inputUrPath.hasVariables()) {
            var outputPathArray = new ArrayList<UrPath>();
            for (var outputPath : outputPaths) {
                if (!(outputPath instanceof String)) {
                    throw new IOException("Incorrect type of output path item");
                }
                var outputUrPath = new VariableUrPath((String)outputPath);
                if (outputUrPath.hasVariables()) {
                    throw new IOException("Unexpected variable in '" + KEY_OUTPUT_PATH + "'. No matching variable present in '" + KEY_INPUT_PATH + "'");
                }
                outputPathArray.add(outputUrPath.getUrPath());
            }
            noVariablePaths.add(Pair.of(inputUrPath.getUrPath(), outputPathArray));
            return;
        }

        // Else variables present
        var outputPathArray = new ArrayList<VariableUrPath>();
        for (var outputPath : outputPaths) {
            if (!(outputPath instanceof String)) {
                throw new IOException("Incorrect type of output path item");
            }
            var outputUrPath = new VariableUrPath((String)outputPath);
            if (!outputVariablesValid(inputUrPath, outputUrPath)) {
                throw new IOException("Unexpected variable in '" + KEY_OUTPUT_PATH + "': " + outputPath + ". No matching variable present in '" + KEY_INPUT_PATH + "': " + inputPath);
            }
            outputPathArray.add(outputUrPath);
        }
        variablePaths.add(Pair.of(inputUrPath, outputPathArray));
    }
}
