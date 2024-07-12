package cz.cuni.mff.hdt.operation.default_op;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.operation.VariableHelper;
import cz.cuni.mff.hdt.transformation.TypedValue;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.path.UrPath;
import cz.cuni.mff.hdt.path.VariableUrPath;

/**
 * Class implementing the default operation of the transformation language.
 * This operation sets default values at specified paths if they are not already present in the input.
 */
public class DefaultOperation implements Operation {
    public static final String KEY_PATH = "path";
    public static final String KEY_VALUE = "value";

    private ArrayList<Pair<UrPath, TypedValue>> nonVariablePaths;
    private ArrayList<Pair<VariableUrPath, TypedValue>> variablePaths;

    /**
     * Constructs a DefaultOperation with the given operation specifications.
     *
     * @param operationSpecs the JSONArray containing operation specifications
     * @throws IOException if there is an error parsing the specifications
     */
    public DefaultOperation(JSONArray operationSpecs) throws IOException {
        nonVariablePaths = new ArrayList<>();
        variablePaths = new ArrayList<>();
        for (var spec : operationSpecs) {
            parseSpec(spec);
        }
    }

    /**
     * Executes the default operation on the given input {@code Ur}.
     *
     * @param inputUr the input {@code Ur} to apply default values to
     * @return the {@code Ur} with default values applied
     * @throws OperationFailedException if the operation fails
     */
    @Override
    public Ur execute(Ur inputUr) throws OperationFailedException {
        var outputUr = new Ur(new JSONObject(inputUr.getInnerRepresentation().toMap()));
        for (var pair : nonVariablePaths) {
            var path = pair.getLeft();
            var typedValue = Ur.getTypedValueUr(pair.getRight());
            if (outputUr.isPresent(path)) {
                continue;
            }

            try {
                outputUr.set(path, typedValue);
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
        matchVariablesAndDefaultRecursive(inputUr, inputUr, outputUr, 0, allPathsIndices);

        return outputUr;
    }

    private void resetVariables(ArrayList<Integer> matchingPathsIndices, int iteration) {
        for (var index : matchingPathsIndices) {
            variablePaths.get(index).getLeft().tryResetVariable(iteration);
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
                var matchedPath = variablePaths.get(index).getLeft().getUrPath();
                if (matchedPath == null) { // some variables not matched yet
                    continue;
                }
                try {
                    // if (!sameAsNonVariable(matchedPath)) <- check not needed since it just stores default value
                    defaultMatched(outputUr, index);
                }
                catch (IOException e) {
                    throw new OperationFailedException(e.getMessage());
                }

                // remove them from recursion
                variablePaths.get(index).getLeft().tryResetVariable(iteration);
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
            var path = variablePaths.get(pathIndex).getLeft();
            outputList.add(path);
        }
        return outputList;
    }

    private void defaultMatched(Ur outputUr, Integer pathIndex) throws IOException {
        var path = variablePaths.get(pathIndex).getLeft().getUrPath();
        var typedValue = Ur.getTypedValueUr(variablePaths.get(pathIndex).getRight());
        if (outputUr.isPresent(path)) {
            return;
        }
        outputUr.set(path, typedValue);
    }

    private ArrayList<Integer> getMatchingPathIndices(String key, Ur.Type type, ArrayList<Integer> indicesToPaths, int tokenIndex) {
        var outputIndices = new ArrayList<Integer>();
        for (var pathIndex : indicesToPaths) {
            var path = variablePaths.get(pathIndex).getLeft();
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
        if (!specObject.has(KEY_PATH) || !specObject.has(KEY_VALUE)) {
            throw new IOException("Mandatory keys are missing from item in specs");
        }
        var pathObject = specObject.get(KEY_PATH);
        var value = specObject.get(KEY_VALUE);
        var type = Ur.getPrimitiveUrString(value);
        if (!(pathObject instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_PATH + "' in spec item");
        }
        parsePath((String)pathObject, new TypedValue(type, value.toString()));
    }

    private void parsePath(String stringPath, TypedValue value) throws IOException {
        var path = new VariableUrPath(stringPath);

        if (path.hasVariables()) {
            variablePaths.add(Pair.of(path, value));
            return;
        }
        
        nonVariablePaths.add(Pair.of(path.getUrPath(), value));
    }
}
