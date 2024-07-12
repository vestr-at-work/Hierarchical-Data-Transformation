package cz.cuni.mff.hdt.operation;

import java.io.IOException;
import java.util.List;

import cz.cuni.mff.hdt.path.ArrayItemToken;
import cz.cuni.mff.hdt.path.PropertyToken;
import cz.cuni.mff.hdt.path.UrPathToken;
import cz.cuni.mff.hdt.path.VariableArrayItemToken;
import cz.cuni.mff.hdt.path.VariablePropertyToken;
import cz.cuni.mff.hdt.path.VariableUrPath;
import cz.cuni.mff.hdt.ur.Ur;

/**
 * Helper class holding methods for working with UrPath Variables and VariableUrPaths
 */
public class VariableHelper {
    /**
     * Updates variable value based on the value provided on every path in paths on tokenIndex
     * 
     * @param value value to update the variable to
     * @param type type of the variable token
     * @param paths list of the variable paths to be updated
     * @param tokenIndex index of the token in variable paths
     */
    public static void updateVariablePaths(String value, Ur.Type type, List<VariableUrPath> paths, int tokenIndex) {
        if (type == Ur.Type.Array) {
            try {
                var arrayIndex = Integer.parseInt(value);
                for (var path : paths) {
                    path.trySetArrayItemVariable(tokenIndex, arrayIndex);
                }
            }
            catch (NumberFormatException e) {
                throw new OperationFailedException("Incorrect Ur Array representaion. Indices not integers.");
            }
            return;
        }

        for (var path : paths) {
            path.trySetPropertyVariable(tokenIndex, value);
        }
    }

    public static boolean tokenMatchesValue(UrPathToken token, String value, Ur.Type type) {
        if (type == Ur.Type.Object && token instanceof PropertyToken) {
            if (((PropertyToken)token).getKey().equals(value)) {
                return true;
            }
        }
        else if (type == Ur.Type.Object && token instanceof VariablePropertyToken) {
            return true;
        }
        else if (type == Ur.Type.Array && token instanceof ArrayItemToken) {
            if (((ArrayItemToken)token).getIndex().toString().equals(value)) {
                return true;
            }
        }
        else if (type == Ur.Type.Array && token instanceof VariableArrayItemToken) {
            return true;
        }
        return false;
    }

    public static void fillOutputPathVariables(VariableUrPath inputPath, VariableUrPath outputPath) throws IOException {
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
}
