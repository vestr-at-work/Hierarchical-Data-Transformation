package cz.cuni.mff.hdt.operation.copy_by_id;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.operation.VariableHelper;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.path.PropertyToken;
import cz.cuni.mff.hdt.path.UrPath;
import cz.cuni.mff.hdt.path.VariableUrPath;

/*
 * Class implementing the copy-by-id operation of the transformation language
 */
public class CopyByIdOperation implements Operation {
    public static final String KEY_ID_PATH = "id-path";
    public static final String KEY_ENTITY_PARENT_PATH = "entity-parent-path";

    // first is id path, second is entity parent path
    private ArrayList<Pair<UrPath, UrPath>> nonVariablePaths;
    private ArrayList<Pair<VariableUrPath, VariableUrPath>> variablePaths;

    /**
     * Constructs a new CopyByIdOperation with the operation specifications.
     *
     * @param operationSpecs the JSON array containing the operation specifications
     * @throws IOException if there is an error parsing the operation specifications
     */
    public CopyByIdOperation(JSONArray operationSpecs) throws IOException {
        nonVariablePaths = new ArrayList<>();
        variablePaths = new ArrayList<>();
        for (var spec : operationSpecs) {
            parseSpec(spec);
        }
    }

    /**
     * Executes the copy-by-id operation on the provided input {@code Ur} object.
     *
     * @param inputUr the input {@code Ur} object
     * @return the resulting {@code Ur} object after the copy-by-id operation is applied
     * @throws OperationFailedException if the operation fails
     */
    @Override
    public Ur execute(Ur inputUr) throws OperationFailedException {
        var outputUr = new Ur(new JSONObject(inputUr.getInnerRepresentation().toMap()));
        // Part for nonVariables 
        for (var pair : nonVariablePaths) {
            var idPath = pair.getLeft();
            var entityCopyPath = pair.getRight();
            try {
                Ur idUrPrimitive = inputUr.get(idPath);
                String value = getPrimitiveValue(idUrPrimitive);
                // add id to entity copy path
                var idProperty = new PropertyToken(value);
                entityCopyPath.tokens.add(idProperty);

                Ur entityToCopy = inputUr.get(entityCopyPath);
                // we want to replace the whole entity in which the id resided
                if (idPath.length() == 0) {
                    throw new OperationFailedException("Value from '" + entityCopyPath + "' could not be copied. Can not replace parent of root");
                }
                outputUr.set(idPath.getUrPath(idPath.length() - 1), entityToCopy);
                // return to original state
                entityCopyPath.tokens.remove(entityCopyPath.length() - 1);
            }
            catch (IOException e) {
                // TODO inputPath will not be printed as an readable UrPath. Add nice toString()
                throw new OperationFailedException("Value from '" + entityCopyPath + "' could not be copied");
            }
        }

        // Part for variables
        if (variablePaths.isEmpty()) {
            return outputUr;
        }

        var allPathsIndices = new ArrayList<Integer>();
        for (Integer i = 0; i < variablePaths.size(); i++) {allPathsIndices.add(i);};
        matchVariablesAndCopyRecursive(inputUr, inputUr, outputUr, 0, allPathsIndices);
        
        return outputUr;
    }

    private String getPrimitiveValue(Ur idUrPrimitive) throws IOException {
        var inner = idUrPrimitive.getInnerRepresentation();
        if (!inner.has(Ur.KEY_VALUE)) {
            throw new IOException("Ur has no '" + Ur.KEY_VALUE + "' key");
        }
        var valueArrayUnknown = inner.get(Ur.KEY_VALUE);
        if (!(valueArrayUnknown instanceof JSONArray)) {
            throw new IOException("Incorrect type of '" + Ur.KEY_VALUE + "' key.");
        }
        var valueArray = (JSONArray)valueArrayUnknown;
        if (valueArray.isEmpty()) {
            throw new IOException("Empty array in '" + Ur.KEY_VALUE + "' key.");
        }
        var valueUnknown = valueArray.get(0);
        if (!(valueUnknown instanceof String)) {
            throw new IOException("Incorrect type of first element of array in '" + Ur.KEY_VALUE + "' key.");
        }

        return (String)valueUnknown;
    }

    private void resetVariables(ArrayList<Integer> matchingPathsIndices, int iteration) {
        for (var index : matchingPathsIndices) {
            variablePaths.get(index).getLeft().tryResetVariable(iteration);
        }
    }

    private void matchVariablesAndCopyRecursive(Ur propertyUr, Ur inputUr, Ur outputUr, int iteration,
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
                    if (!sameAsNonVariable(matchedPath) && inputUr.isPresent(matchedPath)) {
                        copyMatched(inputUr, outputUr, index);
                    }
                }
                catch (IOException e) {
                    // TODO inputPath will not be printed as an readable UrPath. Add nice toString()
                    throw new OperationFailedException("Value from '" + matchedPath + "' could not be shifted");
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
                matchVariablesAndCopyRecursive(newPropertyUr, inputUr, outputUr, iteration + 1, matchingPathsIndices);
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

    private void copyMatched(Ur inputUr, Ur outputUr, Integer pathIndex) throws IOException {
        var pathPair = variablePaths.get(pathIndex);
        var idPath = pathPair.getLeft();
        var entityCopyPath = pathPair.getRight();
        var noVariableIdPath = idPath.getUrPath();

        Ur idUrPrimitive = inputUr.get(noVariableIdPath);
        String value = getPrimitiveValue(idUrPrimitive);

        VariableHelper.fillOutputPathVariables(idPath, entityCopyPath);
        var noVariableEntityCopyPath = entityCopyPath.getUrPath();
        var idProperty = new PropertyToken(value);
        noVariableEntityCopyPath.tokens.add(idProperty);

        if (!inputUr.isPresent(noVariableEntityCopyPath)) {
            return;
        }

        Ur entityToCopy = inputUr.get(noVariableEntityCopyPath);
        if (noVariableIdPath.length() == 0) {
            throw new IOException("Can not replace parent of root");
        }
        noVariableIdPath.tokens.remove(noVariableIdPath.length() - 1);
        outputUr.set(noVariableIdPath, entityToCopy);
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
        if (!specObject.has(KEY_ID_PATH) || !specObject.has(KEY_ENTITY_PARENT_PATH)) {
            throw new IOException("Mandatory keys are missing from item in specs");
        }

        var idPathUnknown = specObject.get(KEY_ID_PATH);
        var entityParentPathUnknown = specObject.get(KEY_ENTITY_PARENT_PATH);

        if (!(idPathUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_ID_PATH + "' in spec item");
        }
        if (!(entityParentPathUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_ENTITY_PARENT_PATH + "' in spec item");
        }

        parsePaths((String)idPathUnknown, (String)entityParentPathUnknown);
    }

    private void parsePaths(String idPath, String entityParentPath) throws IOException {
        var idUrPath = new VariableUrPath(idPath);
        var entityParentUrPath = new VariableUrPath(entityParentPath);
        
        if (!idUrPath.hasVariables() && entityParentUrPath.hasVariables()) {
            throw new IOException("Unexpected variable in '" + KEY_ENTITY_PARENT_PATH + "'. No matching variable present in '" + KEY_ID_PATH + "'");
        }
        // No variables
        else if (!idUrPath.hasVariables() && !entityParentUrPath.hasVariables()) {
            nonVariablePaths.add(Pair.of(idUrPath.getUrPath(), entityParentUrPath.getUrPath()));
            return;
        }

        if (!outputVariablesValid(idUrPath, entityParentUrPath)) {
            throw new IOException("Unexpected variable in '" + KEY_ENTITY_PARENT_PATH + "': " + entityParentPath + ". No matching variable present in '" + KEY_ID_PATH + "': " + idPath);
        }

        variablePaths.add(Pair.of(idUrPath, entityParentUrPath));
    }

    private boolean outputVariablesValid(VariableUrPath inputUrPath, VariableUrPath outputUrPath) {
        var outputPathVariables = outputUrPath.getVariableIndices();
        var inputPathVariables = inputUrPath.getVariableIndices();

        for (var outputPathVariableName : outputPathVariables.keySet()) {
            if (!inputPathVariables.containsKey(outputPathVariableName)) {
                return false;
            }
        }

        return true;
    }
}
