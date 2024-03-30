package cz.cuni.mff.hdt.transformation.operations.valueshift;

import cz.cuni.mff.hdt.adapter.SinkWriterAdapter;
import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.DocumentSource;
import cz.cuni.mff.hdt.transformation.operations.Operation;
import cz.cuni.mff.hdt.transformation.operations.OperationFailedException;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ValueShiftOperation implements Operation {
    private JSONObject operationDefinition;
    private SinkWriterAdapter sinkWriterAdapter;

    public ValueShiftOperation(String operationDefinitionString) throws IOException {
        try {
            operationDefinition = new JSONObject(operationDefinitionString);
        }
        catch(JSONException e) {
            throw new IOException("Incorrect operation definition provided");
        }
    }

    @Override
    public void execute(DocumentSource inputSource, Sink outputSink) throws OperationFailedException {
        sinkWriterAdapter = new SinkWriterAdapter(outputSink);
        
        for (var keysIterator = operationDefinition.keys(); keysIterator.hasNext();) {
            String key = keysIterator.next();
            // if key named variable save it and somehow save this object with for repeat match
            
            // if key == "@path" get the value from source and write value to SinkWriterAdapter 
            Optional<String> path = TryGetPath(operationDefinition.get(key));
            if (path.isPresent()) {
                sinkWriterAdapter.write(path.get(), key);
            }

            // else walk the source aka get the key/key in position of variable from the source
            // and check that they are the same
            
            // call recursive method
        }
        // walk the inputSource as the operation definition says
            // when operation definition encounters @path write value to sink
        
        // TODO support named variables

        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    /*
     * Checks if parameter is wrapped JSONArray with one JSONObject 
     * with one "@path" key and returns its value if true. 
     */
    private Optional<String> TryGetPath(Object valueOfKey) {
        if (!(valueOfKey instanceof JSONArray)) {
            return Optional.empty();
        }
        var keyArray = (JSONArray)valueOfKey;
        if (keyArray.length() != 1) {
            return Optional.empty();
        }
        if (!(keyArray.get(0) instanceof JSONObject)) {
            return Optional.empty();
        }
        var keyObjectInArray = (JSONObject)keyArray.get(0);
        var keysInObjectInArray = keyObjectInArray.keySet();
        if (keysInObjectInArray.size() != 1) {
            return Optional.empty();
        }
        var possiblePathControlKey = keysInObjectInArray.iterator().next();
        if (!possiblePathControlKey.equals("@path")) {
            return Optional.empty();
        }
        String pathValue;
        try {
            pathValue = keyObjectInArray.getString(possiblePathControlKey);
        }
        catch (JSONException e) {
            return Optional.empty();
        }
        
        return Optional.of(pathValue);
    }
}
