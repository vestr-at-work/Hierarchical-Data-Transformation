package cz.cuni.mff.hdt.operation.filter;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.operation.VariableHelper;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.path.UrPath;
import cz.cuni.mff.hdt.path.VariableUrPath;

/**
 * Class implementing the filter operation of the transformation language.
 * This operation filters elements from the input based on specified predicates.
 */
public class FilterOperation implements Operation {
    public static final String KEY_PATH = "path";
    public static final String KEY_PREDICATE = "predicate";

    private ArrayList<Pair<UrPath, UrPredicate>> nonVariablePaths;
    private ArrayList<Pair<VariableUrPath, UrPredicate>> variablePaths;

    /**
     * Constructs a FilterOperation with the given operation specifications.
     *
     * @param operationSpecs the JSONArray containing operation specifications
     * @throws IOException if there is an error parsing the specifications
     */
    public FilterOperation(JSONArray operationSpecs) throws IOException {
        nonVariablePaths = new ArrayList<>();
        variablePaths = new ArrayList<>();
        for (var spec : operationSpecs) {
            parseSpec(spec);
        }
    }

    /**
     * Executes the filter operation on the given input {@code Ur}.
     *
     * @param inputUr the input {@code Ur} to filter
     * @return the filtered {@code Ur}
     * @throws OperationFailedException if the operation fails
     */
    @Override
    public Ur execute(Ur inputUr) throws OperationFailedException {
        var outputUr = new Ur(new JSONObject(inputUr.getInnerRepresentation().toMap()));
        for (var pair : nonVariablePaths) {
            var path = pair.getLeft();
            var predicate = pair.getRight();
            try {
                var value = outputUr.get(path);
                if (!predicate.evaluate(value)) {
                    outputUr.delete(path);
                }
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
        matchVariablesAndFilterRecursive(inputUr, inputUr, outputUr, 0, allPathsIndices);

        return outputUr;
    }

    private void resetVariables(ArrayList<Integer> matchingPathsIndices, int iteration) {
        for (var index : matchingPathsIndices) {
            variablePaths.get(index).getLeft().tryResetVariable(iteration);
        }
    }

    private void matchVariablesAndFilterRecursive(Ur propertyUr, Ur inputUr, Ur outputUr, int iteration,
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
                    if (!sameAsNonVariable(matchedPath)) {
                        filterMatched(outputUr, index);
                    }
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
                matchVariablesAndFilterRecursive(newPropertyUr, inputUr, outputUr, iteration + 1, matchingPathsIndices);
            }
            catch (IOException e) {
                throw new OperationFailedException("Error occured when matching named variables.");
            }

            resetVariables(matchingPathsIndices, iteration);
        }
    }

    private boolean sameAsNonVariable(UrPath matchedPath) {
        // TODO this is slow. we do it for every match
        for (var pair : nonVariablePaths) {
            var nonVariablePath = pair.getLeft();
            if (matchedPath.equals(nonVariablePath)) {
                return true;
            }
        }
        return false;
    }

    private ArrayList<VariableUrPath> getVariablePaths(ArrayList<Integer> pathsIndices) {
        var outputList = new ArrayList<VariableUrPath>();
        for (var pathIndex : pathsIndices) {
            var path = variablePaths.get(pathIndex).getLeft();
            outputList.add(path);
        }
        return outputList;
    }

    private void filterMatched(Ur outputUr, Integer pathIndex) throws IOException {
        var pair = variablePaths.get(pathIndex);
        var path = pair.getLeft().getUrPath();
        var predicate = pair.getRight();
        var value = outputUr.get(path);
        if (!predicate.evaluate(value)) {
            outputUr.delete(path);
        }
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
        if (!specObject.has(KEY_PATH) || !specObject.has(KEY_PREDICATE)) {
            throw new IOException("Mandatory keys are missing from item in specs");
        }
        var pathUnknown = specObject.get(KEY_PATH);
        var predicateUnknown = specObject.get(KEY_PREDICATE);
        if (!(pathUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_PATH + "' in spec item");
        }
        if (!(predicateUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_PREDICATE + "' in spec item");
        }
        parsePath((String)pathUnknown, UrPredicateFactory.create((String)predicateUnknown));
    }

    private void parsePath(String stringPath, UrPredicate predicate) throws IOException {
        var path = new VariableUrPath(stringPath);

        if (path.hasVariables()) {
            variablePaths.add(Pair.of(path, predicate));
            return;
        }
        
        nonVariablePaths.add(Pair.of(path.getUrPath(), predicate));
    }
}
