package cz.cuni.mff.hdt.operation.remove;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.operation.VariableHelper;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.path.ArrayItemToken;
import cz.cuni.mff.hdt.path.PropertyToken;
import cz.cuni.mff.hdt.path.UrPath;
import cz.cuni.mff.hdt.path.VariableArrayItemToken;
import cz.cuni.mff.hdt.path.VariablePropertyToken;
import cz.cuni.mff.hdt.path.VariableUrPath;

/**
 * Class implementing the remove operation of the transformation language.
 */
public class RemoveOperation implements Operation {
    public static final String KEY_PATH = "path";

    private ArrayList<UrPath> nonVariablePaths;
    private ArrayList<VariableUrPath> variablePaths;

    /**
     * Constructs a new RemoveOperation with the specified operation specifications.
     *
     * @param operationSpecs the JSON array containing the operation specifications
     * @throws IOException if there is an error parsing the operation specifications
     */
    public RemoveOperation(JSONArray operationSpecs) throws IOException {
        nonVariablePaths = new ArrayList<>();
        variablePaths = new ArrayList<>();
        for (var spec : operationSpecs) {
            parseSpec(spec);
        }
    }

    /**
     * Executes the remove operation on the provided input {@code Ur} object.
     *
     * @param inputUr the input {@code Ur} object
     * @return the resulting {@code Ur} object after the remove operation is applied
     * @throws OperationFailedException if the remove operation fails
     */
    @Override
    public Ur execute(Ur inputUr) throws OperationFailedException {
        var outputUr = new Ur(new JSONObject(inputUr.getInnerRepresentation().toMap()));
        // Part for non variables
        for (var path : nonVariablePaths) {
            try {
                outputUr.delete(path);
            }
            catch (IOException e) {
                throw new OperationFailedException(e.getMessage());
            }
        }

        // Part for variables
        if (variablePaths.isEmpty()) {
            return outputUr;
        }

        var allPathsIndices = new ArrayList<Integer>();
        for (Integer i = 0; i < variablePaths.size(); i++) {allPathsIndices.add(i);};
        matchVariablesAndRemoveRecursive(inputUr, inputUr, outputUr, 0, allPathsIndices);

        return outputUr;
    }

    private void resetVariables(ArrayList<Integer> matchingPathsIndices, int iteration) {
        for (var index : matchingPathsIndices) {
            variablePaths.get(index).tryResetVariable(iteration);
        }
    }

    private void matchVariablesAndRemoveRecursive(Ur propertyUr, Ur inputUr, Ur outputUr, int iteration,
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
                    removeMatched(outputUr, index);
                }
                catch (IOException e) {
                    // TODO inputPath will not be printed as an readable UrPath. Add nice toString()
                    throw new OperationFailedException("Value from '" + matchedPath + "' could not be removed");
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
                matchVariablesAndRemoveRecursive(newPropertyUr, inputUr, outputUr, iteration + 1, matchingPathsIndices);
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

    private void removeMatched(Ur outputUr, Integer pathIndex) throws IOException {
        var path = variablePaths.get(pathIndex).getUrPath();
        outputUr.delete(path);
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
            throw new IOException("Mandatory key is missing from item in specs");
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
