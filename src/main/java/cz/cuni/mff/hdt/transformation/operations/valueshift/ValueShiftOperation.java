package cz.cuni.mff.hdt.transformation.operations.valueshift;

import cz.cuni.mff.hdt.adapter.SinkWriterAdapter;
import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.ArrayReference;
import cz.cuni.mff.hdt.source.ArraySource;
import cz.cuni.mff.hdt.source.DocumentSource;
import cz.cuni.mff.hdt.source.EntityReference;
import cz.cuni.mff.hdt.source.EntitySource;
import cz.cuni.mff.hdt.source.Reference;
import cz.cuni.mff.hdt.source.ValueReference;
import cz.cuni.mff.hdt.transformation.operations.Operation;
import cz.cuni.mff.hdt.transformation.operations.OperationFailedException;

import java.io.IOException;
import java.util.Optional;

import javax.swing.text.html.Option;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class implementing the value-shift operation from the transformational language
 */
public class ValueShiftOperation implements Operation {
    private JSONObject operationDefinition;

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
        var sinkWriterAdapter = new SinkWriterAdapter(outputSink);
        Reference root = inputSource.next();

        if (root == null) {
            throw new OperationFailedException("No root in the source");
        }

        transformRoot(inputSource, root, sinkWriterAdapter);
    }

    private void transformRoot(DocumentSource inputSource, Reference root,
            SinkWriterAdapter sinkWriterAdapter) throws OperationFailedException {
        
        if (root instanceof EntityReference) {
            EntityReference entityRoot = (EntityReference)root;
            EntitySource source = inputSource.getSourceFromReference(entityRoot);

            transformEntity(source, entityRoot, sinkWriterAdapter, operationDefinition);
            return;
        }
        if (root instanceof ArrayReference) {
            ArrayReference arrayRoot = (ArrayReference)root;
            ArraySource source = inputSource.getSourceFromReference(arrayRoot);

            transformArray(source, arrayRoot, sinkWriterAdapter);
            return;
        }
        
        // Anything else shouldn't be in root
        throw new OperationFailedException("Wrong reference type in the root");
    }

    private void transformArray(ArraySource source, ArrayReference arrayRoot,
            SinkWriterAdapter sinkWriterAdapter) throws OperationFailedException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transformArrayReference'");
    }

    private void transformEntity(EntitySource source, EntityReference entityRoot,
            SinkWriterAdapter sinkWriterAdapter, JSONObject operationObject) throws OperationFailedException {
        
        for (var keysIterator = operationObject.keys(); keysIterator.hasNext();) {
            String key = keysIterator.next();

            ArrayReference item = source.property(entityRoot, key);
            if (item == null) {
                throw new OperationFailedException("Use of unexistant key \'" + key + "\' in operation");
            }
            var arraySource = source.getSourceFromReference(item);
            var reference = arraySource.next(item);

            if (reference instanceof ValueReference) {
                Optional<String> pathInOutput = tryGetPath(operationObject.get(key));
                if (pathInOutput.isEmpty()) {
                    throw new OperationFailedException("Operation and source mismatch");
                }
                var valueReference = (ValueReference)reference;
                var valueSource = arraySource.getSourceFromReference(valueReference);
                String value = valueSource.value(valueReference);
                
                var parsedPath = getParsedPath(pathInOutput.get());
                sinkWriterAdapter.write(parsedPath, value);
            }
            else if (reference instanceof EntityReference) {
                EntityReference entityReference = (EntityReference)reference;
                var entitySource = source.getSourceFromReference(entityReference);
                
                var newOperationObject = tryGetObject(operationObject.get(key));
                if (newOperationObject.isEmpty()) {
                    throw new OperationFailedException("Operation and source mismatch");
                }

                transformEntity(entitySource, entityReference, sinkWriterAdapter,  newOperationObject.get());
            }
            else {
                // There should not be any array (or anything else) here
                throw new OperationFailedException("Incorrect input from source");
            }
        }

        // TODO if there are more entities in the source than in the operation object there should be some kind of error
    }

    private String getParsedPath(String pathFromInput) {
        // TODO implement this function
        // aka check for named variables 
        // and replace them with values of that variables  

        return pathFromInput;
    }

    /*
     * Checks if parameter is wrapped JSONArray with one JSONObject 
     * with one "@path" key and returns its value if true. 
     */
    private Optional<String> tryGetPath(Object valueOfKey) {
        Optional<JSONObject> possibleObjectInArray = tryGetObject(valueOfKey);
        if (possibleObjectInArray.isEmpty()) {
            return Optional.empty();
        }
        JSONObject objectInArray = possibleObjectInArray.get();
        var keysInObjectInArray = objectInArray.keySet();
        if (keysInObjectInArray.size() != 1) {
            return Optional.empty();
        }
        var pathControlKey = keysInObjectInArray.iterator().next();
        if (!pathControlKey.equals("@path")) {
            return Optional.empty();
        }
        String pathValue;
        try {
            pathValue = objectInArray.getString(pathControlKey);
        }
        catch (JSONException e) {
            return Optional.empty();
        }
        
        return Optional.of(pathValue);
    }

    private Optional<JSONObject> tryGetObject(Object value) {
        if (!(value instanceof JSONArray)) {
            return Optional.empty();
        }
        var array = (JSONArray)value;
        if (array.length() != 1) {
            return Optional.empty();
        }
        if (!(array.get(0) instanceof JSONObject)) {
            return Optional.empty();
        }

        return Optional.of((JSONObject)array.get(0));
    }
}
