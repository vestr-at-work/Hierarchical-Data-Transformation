package cz.cuni.mff.hdt.operation.default_op;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.transformation.TypedValue;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.ur.UrPath;

/**
 * Class implementing the default operation of the transformation language.
 * This operation sets default values at specified paths if they are not already present in the input.
 */
public class DefaultOperation implements Operation {
    public static final String KEY_PATH = "path";
    public static final String KEY_VALUE = "value";

    private ArrayList<Pair<UrPath, TypedValue>> defaults;

    /**
     * Constructs a DefaultOperation with the given operation specifications.
     *
     * @param operationSpecs the JSONArray containing operation specifications
     * @throws IOException if there is an error parsing the specifications
     */
    public DefaultOperation(JSONArray operationSpecs) throws IOException {
        defaults = new ArrayList<>();
        for (var spec : operationSpecs) {
            defaults.add(parseSpec(spec));
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
        for (var pair : defaults) {
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

        return outputUr;
    }

    private Pair<UrPath, TypedValue> parseSpec(Object spec) throws IOException {
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
        return Pair.of(new UrPath((String)pathObject), new TypedValue(type, value.toString()));
    }
}
