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
import cz.cuni.mff.hdt.path.VariableArrayItemToken;
import cz.cuni.mff.hdt.path.VariablePropertyToken;
import cz.cuni.mff.hdt.path.VariableUrPath;

/**
 * Class implementing the filter operation of the transformation language.
 * This operation filters elements from the input based on specified predicates.
 */
public class FilterOperation implements Operation {
    public static final String TESTED_KEY_PATH = "tested-path";
    public static final String FILTERED_KEY_PATH = "filtered-path";
    public static final String KEY_PREDICATE = "predicate";

    // left is tested path, right is filtered path
    private ArrayList<Pair<UrPath, UrPath>> nonVariablePaths = new ArrayList<>();;
    private ArrayList<Pair<VariableUrPath, VariableUrPath>> variablePaths = new ArrayList<>();;
    // predicates have matching indices with coresponding paths
    private ArrayList<UrPredicate> predicatesForNonVariablePaths = new ArrayList<>();;
    private ArrayList<UrPredicate> predicatesForVariablePaths = new ArrayList<>();;

    /**
     * Constructs a FilterOperation with the given operation specifications.
     *
     * @param operationSpecs the JSONArray containing operation specifications
     * @throws IOException if there is an error parsing the specifications
     */
    public FilterOperation(JSONArray operationSpecs) throws IOException {
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
        for (int i = 0; i < nonVariablePaths.size(); i++) {
            var pair = nonVariablePaths.get(i);
            var testedPath = pair.getLeft();
            var filteredPath = pair.getRight();
            var predicate = predicatesForNonVariablePaths.get(i);
            try {
                var value = inputUr.get(testedPath);
                if (!predicate.evaluate(value)) {
                    outputUr.delete(filteredPath);
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
                    if (!sameAsNonVariable(matchedPath) && outputUr.isPresent(matchedPath)) {
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
        var testedPath = pair.getLeft();
        var filteredPath = pair.getRight();
        var value = outputUr.get(testedPath.getUrPath());

        var predicate = predicatesForVariablePaths.get(pathIndex);
        if (!predicate.evaluate(value)) {
            fillFilteredPathVariables(testedPath, filteredPath);
            var filteredPathNonVariable = filteredPath.getUrPath();
            if (!outputUr.isPresent(filteredPathNonVariable)) {
                return;
            }

            outputUr.delete(filteredPathNonVariable);
        }
    }

    private void fillFilteredPathVariables(VariableUrPath testedPath, VariableUrPath filteredPath) throws IOException {
        var filteredPathVariableMap = filteredPath.getVariableIndices();
        var testedPathVariableMap = testedPath.getVariableIndices();

        for (var filteredVariableName : filteredPathVariableMap.keySet()) {
            var testedVariableIndex = testedPathVariableMap.get(filteredVariableName);
            var filteredVariableIndex = filteredPathVariableMap.get(filteredVariableName);

            var testedToken = testedPath.tokens.get(testedVariableIndex);
            var filteredToken = filteredPath.tokens.get(filteredVariableIndex);

            if (testedToken instanceof VariablePropertyToken) {
                var value = ((VariablePropertyToken)testedToken).getKey();
                if (filteredToken instanceof VariablePropertyToken) {
                    ((VariablePropertyToken)filteredToken).setKey(value);
                }
                else {
                    try {
                        ((VariableArrayItemToken)filteredToken).setIndex(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e) {
                        throw new IOException("Non integer key matched for array item index");
                    }
                }
            }
            else if (testedToken instanceof VariableArrayItemToken) {
                var value = ((VariableArrayItemToken)testedToken).getIndex();
                if (filteredToken instanceof VariableArrayItemToken) {
                    ((VariableArrayItemToken)filteredToken).setIndex(value);
                }
                else {
                    ((VariablePropertyToken)filteredToken).setKey(value.toString());
                }
            }
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
            throw new IOException("Incorrect type of an item in filter operation specs");
        }
        var specObject = (JSONObject)spec;
        if (!specObject.has(TESTED_KEY_PATH) || !specObject.has(KEY_PREDICATE)) {
            throw new IOException("Mandatory keys are missing from item in filter operation specs");
        }
        var testedPathUnknown = specObject.get(TESTED_KEY_PATH);
        var filteredPathUnknown = specObject.get(FILTERED_KEY_PATH);
        var predicateUnknown = specObject.get(KEY_PREDICATE);
        if (!(testedPathUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + TESTED_KEY_PATH + "' in filter operation spec item");
        }
        if (!(filteredPathUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + FILTERED_KEY_PATH + "' in filter operation spec item");
        }
        if (!(predicateUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_PREDICATE + "' in filter operation spec item");
        }
        parsePath((String)filteredPathUnknown, (String)testedPathUnknown, UrPredicateFactory.create((String)predicateUnknown));
    }

    private void parsePath(String filteredPathString, String testedPathString, UrPredicate predicate) throws IOException {
        var testedPath = new VariableUrPath(testedPathString);
        var filteredPath = new VariableUrPath(filteredPathString);

        if (!testedPath.hasVariables() && filteredPath.hasVariables()) {
            throw new IOException("Unexpected variable in '" + FILTERED_KEY_PATH + "' in filter operation specs. No matching variable present in '" + TESTED_KEY_PATH + "'");
        }
        if (!testedPath.hasVariables() && !filteredPath.hasVariables()) {
            nonVariablePaths.add(Pair.of(testedPath.getUrPath(), filteredPath.getUrPath()));
            predicatesForNonVariablePaths.add(predicate);
            return;
        }

        if (!filteredPathVariablesValid(testedPath, filteredPath)) {
            throw new IOException("Unexpected variable in '" + FILTERED_KEY_PATH + "' in filter operation specs. No matching variable present in '" + TESTED_KEY_PATH + "'");
        }

        
        variablePaths.add(Pair.of(testedPath, filteredPath));
        predicatesForVariablePaths.add(predicate);
    }

    private boolean filteredPathVariablesValid(VariableUrPath testedUrPath, VariableUrPath filteredUrPath) {
        var outputPathVariables = filteredUrPath.getVariableIndices();
        var inputPathVariables = testedUrPath.getVariableIndices();

        for (var outputPathVariableName : outputPathVariables.keySet()) {
            if (!inputPathVariables.containsKey(outputPathVariableName)) {
                return false;
            }
        }

        return true;
    }
}
