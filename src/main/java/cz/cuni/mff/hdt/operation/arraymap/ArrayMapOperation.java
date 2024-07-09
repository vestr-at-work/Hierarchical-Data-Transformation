package cz.cuni.mff.hdt.operation.arraymap;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.operation.VariableHelper;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.ur.Ur.Type;
import cz.cuni.mff.hdt.path.UrPath;
import cz.cuni.mff.hdt.path.VariableUrPath;

/**
 * Class implementing the array-map operation of the transformation language.
 * This operation sets converts the objects at specified paths to arrays with same content.
 */
public class ArrayMapOperation implements Operation {
    public static final String KEY_PATH = "path";

    private ArrayList<UrPath> nonVariablePaths;
    private ArrayList<VariableUrPath> variablePaths;

    /**
     * Constructs a ArrayMapOperation with the given operation specifications.
     *
     * @param operationSpecs the JSONArray containing operation specifications
     * @throws IOException if there is an error parsing the specifications
     */
    public ArrayMapOperation(JSONArray operationSpecs) throws IOException {
        nonVariablePaths = new ArrayList<>();
        variablePaths = new ArrayList<>();
        for (var spec : operationSpecs) {
            parseSpec(spec);
        }
    }

    /**
     * Executes the array-map operation on the given input {@code Ur}.
     *
     * @param inputUr the input {@code Ur} to apply operation to
     * @return the {@code Ur} with operation applied
     * @throws OperationFailedException if the operation fails
     */
    @Override
    public Ur execute(Ur inputUr) throws OperationFailedException {
        var outputUr = new Ur(new JSONObject(inputUr.getInnerRepresentation().toMap()));
        for (var path : nonVariablePaths) {
            if (!outputUr.isPresent(path)) {
                // TODO add path printing
                throw new OperationFailedException("Incorrect path provided. No such element on a given path.");
            }

            try {
                Ur entityToBeConverted = outputUr.getShared(path);
                if (entityToBeConverted.getRootType() != Type.Object) {
                    throw new OperationFailedException("Incorrect path provided. Not an object on a given path.");
                }
                
                Ur convertedArray = getConvertedArray(entityToBeConverted);
                outputUr.update(path, convertedArray);

            } catch (IOException e) {
                // this should never happen since we have check that the path is valid. just for the compiler
            }
            
        }

        // Part for variables
        if (variablePaths.isEmpty()) {
            return outputUr;
        }

        var allPathsIndices = new ArrayList<Integer>();
        for (Integer i = 0; i < variablePaths.size(); i++) {allPathsIndices.add(i);};
        matchVariablesAndDefaultRecursive(inputUr, inputUr, outputUr, 0, allPathsIndices);

        return outputUr;
    }

    private Ur getConvertedArray(Ur inputUr) {
        var inputJson = inputUr.getInnerRepresentation();
        var outputJson = new JSONObject().put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_ARRAY));

        Integer index = 0;
        for (var key : inputJson.keySet()) {
            if (key.equals(Ur.KEY_TYPE)) {
                continue;
            }

            var property = inputJson.get(key);
            outputJson.put(index.toString(), property);
            index++;
        }

        return new Ur(outputJson);
    }

    private void resetVariables(ArrayList<Integer> matchingPathsIndices, int iteration) {
        for (var index : matchingPathsIndices) {
            variablePaths.get(index).tryResetVariable(iteration);
        }
    }

    private void matchVariablesAndDefaultRecursive(Ur propertyUr, Ur inputUr, Ur outputUr, int iteration,
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
            var matchingPaths = getVariablePaths(matchingPathsIndices);
            VariableHelper.updateVariablePaths(key, type, matchingPaths, iteration);

            var fullyMatchedPaths = new ArrayList<Integer>();
            for (var index : matchingPathsIndices) {
                var matchedPath = variablePaths.get(index).getUrPath();
                if (matchedPath == null) { // some variables not matched yet
                    continue;
                }
                try {
                    arrayMapMatched(outputUr, index);
                }
                catch (IOException e) {
                    throw new OperationFailedException(e.getMessage());
                }

                // remove them from recursion
                variablePaths.get(index).tryResetVariable(iteration);
                fullyMatchedPaths.add(index);
            }
            matchingPathsIndices.removeAll(fullyMatchedPaths);
            
            if (matchingPathsIndices.isEmpty()) {
                continue;
            }

            var someMatchingIndex = matchingPathsIndices.get(0);
            var urPathToProperty = variablePaths.get(someMatchingIndex).getUrPath(iteration + 1);
            try {
                // get Ur of property
                var newPropertyUr = inputUr.getShared(urPathToProperty);
                // call function recursively
                matchVariablesAndDefaultRecursive(newPropertyUr, inputUr, outputUr, iteration + 1, matchingPathsIndices);
            }
            catch (IOException e) {
                throw new OperationFailedException("Error occured when matching named variables.");
            }

            resetVariables(matchingPathsIndices, iteration);
        }
    }

    private ArrayList<VariableUrPath> getVariablePaths(ArrayList<Integer> pathsIndices) {
        var outputList = new ArrayList<VariableUrPath>();
        for (var pathIndex : pathsIndices) {
            var path = variablePaths.get(pathIndex);
            outputList.add(path);
        }
        return outputList;
    }

    private void arrayMapMatched(Ur outputUr, Integer pathIndex) throws IOException {
        var path = variablePaths.get(pathIndex).getUrPath();
        Ur entityToBeConverted = outputUr.getShared(path);
        if (entityToBeConverted.getRootType() != Type.Object) {
            // if not an object do nothing
            return;
        }
        
        Ur convertedArray = getConvertedArray(entityToBeConverted);
        outputUr.update(path, convertedArray);
    }

    private ArrayList<Integer> getMatchingPathIndices(String key, Ur.Type type, ArrayList<Integer> indicesToPaths, int tokenIndex) {
        var outputIndices = new ArrayList<Integer>();
        for (var pathIndex : indicesToPaths) {
            var path = variablePaths.get(pathIndex);
            var pathToken = path.tokens.get(tokenIndex);
            if (VariableHelper.tokenMatchesValue(pathToken, key, type)) {
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
        if (!specObject.has(KEY_PATH)) {
            throw new IOException("Mandatory keys are missing from item in specs");
        }
        var pathObject = specObject.get(KEY_PATH);
        if (!(pathObject instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_PATH + "' in spec item");
        }
        parsePath((String)pathObject);
    }

    private void parsePath(String stringPath) throws IOException {
        var path = new VariableUrPath(stringPath);

        if (path.hasVariables()) {
            variablePaths.add(path);
            return;
        }
        
        nonVariablePaths.add(path.getUrPath());
    }
}
