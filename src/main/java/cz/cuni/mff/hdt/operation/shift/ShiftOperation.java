package cz.cuni.mff.hdt.operation.shift;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;
import cz.cuni.mff.hdt.ur.UrPath;

/*
 * Class implementing the shift operation of the transformation language
 */
public class ShiftOperation implements Operation {
    public static final String KEY_INPUT_PATH = "input-path";
    public static final String KEY_OUTPUT_PATH = "output-path";

    // first is input path, second is array of output paths
    private ArrayList<Pair<UrPath, ArrayList<UrPath>>> paths;

    public ShiftOperation(JSONArray operationSpecs) throws IOException {
        for (var spec : operationSpecs) {
            paths.add(parseSpec(spec));
        }
    }

    @Override
    public Ur execute(Ur inputUr) throws OperationFailedException {
        var outputUr = new Ur(new JSONObject("{}"));
        for (var pair : paths) {
            var inputPath = pair.getLeft();
            var outputPaths = pair.getRight();
            try {
                Ur toShift = inputUr.get(inputPath);
                for (var outputPath : outputPaths) {
                    outputUr.set(outputPath, toShift);
                }
            }
            catch (IOException e) {
                throw new OperationFailedException("Value from '" + inputPath + "' could not be shifted");
            }
        }

        return outputUr;
    }

    private Pair<UrPath, ArrayList<UrPath>> parseSpec(Object spec) throws IOException {
        if (!(spec instanceof JSONObject)) {
            throw new IOException("Incorrect type of an item in specs");
        }
        var specObject = (JSONObject)spec;
        if (!specObject.has(KEY_INPUT_PATH) || !specObject.has(KEY_OUTPUT_PATH)) {
            throw new IOException("Mandatory keys are missing from item in specs");
        }
        var inputPathUnknown = specObject.get(KEY_INPUT_PATH);
        var outputPathsUnknown = specObject.get(KEY_OUTPUT_PATH);
        if (!(inputPathUnknown instanceof String)) {
            throw new IOException("Incorrect type of key '" + KEY_INPUT_PATH + "' in spec item");
        }
        if (!(outputPathsUnknown instanceof String) && !(outputPathsUnknown instanceof JSONArray)) {
            throw new IOException("Incorrect type of key '" + KEY_OUTPUT_PATH + "' in spec item");
        }
        ArrayList<UrPath> outputPaths = new ArrayList<>();
        if (outputPathsUnknown instanceof String) {
            outputPaths.add(new UrPath((String)outputPathsUnknown));
        }
        else {
            var outputPathsArray = (JSONArray)outputPathsUnknown;
            for (var outputPath : outputPathsArray) {
                if (!(outputPath instanceof String)) {
                    throw new IOException("Incorrect type of output path item");
                }
                outputPaths.add(new UrPath((String)outputPath));
            }
        }

        return Pair.of(new UrPath((String)inputPathUnknown), outputPaths);
    }
}
