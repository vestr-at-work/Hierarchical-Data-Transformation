package cz.cuni.mff.hdt.operation.remove;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.path.UrPath;

/**
 * Class implementing the remove operation of the transformation language.
 */
public class RemoveOperation implements Operation {
    public static final String KEY_PATH = "path";

    private ArrayList<UrPath> removePaths;

    /**
     * Constructs a new RemoveOperation with the specified operation specifications.
     *
     * @param operationSpecs the JSON array containing the operation specifications
     * @throws IOException if there is an error parsing the operation specifications
     */
    public RemoveOperation(JSONArray operationSpecs) throws IOException {
        removePaths = new ArrayList<>();
        for (var spec : operationSpecs) {
            removePaths.add(parseSpec(spec));
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
        for (var path : removePaths) {
            try {
                outputUr.delete(path);
            }
            catch (IOException e) {
                throw new OperationFailedException(e.getMessage());
            }
        }
        return outputUr;
    }

    private UrPath parseSpec(Object spec) throws IOException {
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
        return new UrPath((String)pathObject);
    }
}
