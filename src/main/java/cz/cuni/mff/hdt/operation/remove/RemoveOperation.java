package cz.cuni.mff.hdt.operation.remove;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.ur.UrPath;

public class RemoveOperation implements Operation {
    public static final String KEY_PATH = "path";

    private ArrayList<UrPath> removePaths;

    public RemoveOperation(JSONArray operationSpecs) throws IOException {
        for (var spec : operationSpecs) {
            removePaths.add(parseSpec(spec));
        }
    }

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
