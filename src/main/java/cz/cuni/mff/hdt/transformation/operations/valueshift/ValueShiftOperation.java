package cz.cuni.mff.hdt.transformation.operations.valueshift;

import cz.cuni.mff.hdt.adapter.UrAwareSinkWriterAdapter;
import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.EntityReference;
import cz.cuni.mff.hdt.reference.PropertyReference;
import cz.cuni.mff.hdt.reference.Reference;
import cz.cuni.mff.hdt.reference.ValueReference;
import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.ArraySource;
import cz.cuni.mff.hdt.source.DocumentSource;
import cz.cuni.mff.hdt.source.EntitySource;
import cz.cuni.mff.hdt.transformation.TypedValue;
import cz.cuni.mff.hdt.transformation.operations.Operation;
import cz.cuni.mff.hdt.transformation.operations.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONPointer;

/**
 * Class implementing the value-shift operation from the transformational language
 */
public class ValueShiftOperation implements Operation {
    private JSONObject operationDefinition;

    public ValueShiftOperation(JSONObject operationDefinition) {
        this.operationDefinition = operationDefinition;
    }

    @Override
    public void execute(DocumentSource inputSource, Sink outputSink) throws OperationFailedException {
        var urSinkWriterAdapter = new UrAwareSinkWriterAdapter(outputSink);
        Reference root = inputSource.next();

        if (root == null) {
            throw new OperationFailedException("No root in the source");
        }

        transformRoot(inputSource, root, urSinkWriterAdapter);
        
        try {
            urSinkWriterAdapter.finishWriting();
        }
        catch (IOException e) {
            throw new OperationFailedException("Error occured when writing to sink");
        }
    }

    private void transformRoot(DocumentSource inputSource, Reference root,
        UrAwareSinkWriterAdapter sinkWriterAdapter) throws OperationFailedException {
        
        if (root instanceof EntityReference) {
            EntityReference entityRoot = (EntityReference)root;
            EntitySource source = inputSource.getSourceFromReference(entityRoot);

            transformEntity(source, entityRoot, sinkWriterAdapter, operationDefinition);
            return;
        }
        // TODO maybe this is not needed since we can just support only object in root in Ur 
        if (root instanceof ArrayReference) {
            ArrayReference arrayRoot = (ArrayReference)root;
            ArraySource source = inputSource.getSourceFromReference(arrayRoot);

            transformArray(source, arrayRoot, sinkWriterAdapter);
            return;
        }
        
        // Anything else shouldn't be in the root
        throw new OperationFailedException("Wrong reference type in the root");
    }

    private void transformArray(ArraySource source, ArrayReference arrayRoot,
        UrAwareSinkWriterAdapter sinkWriterAdapter) throws OperationFailedException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'transformArrayReference'");
    }

    private void transformEntity(EntitySource source, EntityReference entityRoot,
        UrAwareSinkWriterAdapter sinkWriterAdapter, JSONObject operationObject) throws OperationFailedException {
        
        for (var keysIterator = operationObject.keys(); keysIterator.hasNext();) {
            String key = keysIterator.next();

            ArrayReference item = source.property(entityRoot, key);
            if (item == null) {
                throw new OperationFailedException("Use of unexistant key \'" + key + "\' in operation");
            }
            var arraySource = source.getSourceFromReference(item);
            var reference = arraySource.next(item);
            // TODO check if it is really a last reference

            if (reference instanceof PropertyReference) {
                String pathInOutput = tryGetPath(operationObject.get(key)).orElseThrow(() -> {
                    throw new OperationFailedException("Operation and source mismatch");
                });
                var propertyReference = (PropertyReference)reference;
                var propertySource = arraySource.getSourceFromReference(propertyReference);
                String value = propertySource.value(propertyReference);
                String type = propertySource.type(propertyReference);
                
                var validatedPath = validatePath(pathInOutput);
                try {
                    sinkWriterAdapter.write(validatedPath, new TypedValue(type, value));
                }
                catch (IOException e) {
                    throw new OperationFailedException("Error occured when writing to sink");
                }
            }
            else if (reference instanceof ValueReference) {
                String pathInOutput = tryGetPath(operationObject.get(key)).orElseThrow(() -> {
                    throw new OperationFailedException("Operation and source mismatch");
                });
                var valueReference = (ValueReference)reference;
                var valueSource = arraySource.getSourceFromReference(valueReference);
                String value = valueSource.value(valueReference);
                
                var validatedPath = validatePath(pathInOutput);
                try {
                    sinkWriterAdapter.write(validatedPath, new TypedValue(Ur.VALUE_STRING, value));
                }
                catch (IOException e) {
                    throw new OperationFailedException("Error occured when writing to sink");
                }
            }
            else if (reference instanceof EntityReference) {
                EntityReference entityReference = (EntityReference)reference;
                var entitySource = source.getSourceFromReference(entityReference);
                
                var newOperationObject = tryGetObject(operationObject.get(key)).orElseThrow(() -> {
                    throw new OperationFailedException("Operation and source mismatch");
                });

                transformEntity(entitySource, entityReference, sinkWriterAdapter,  newOperationObject);
            }
            else {
                // There should not be any array (or anything else) here
                throw new OperationFailedException("Incorrect input from source");
            }
        }

        // TODO if there are more entities in the source than in the operation object there should be some kind of error
    }

    private String validatePath(String pathFromInput) throws OperationFailedException { 
        try {
            return new JSONPointer(pathFromInput).toString();
        }
        catch (IllegalArgumentException e) {
            throw new OperationFailedException("Incorrect path provided. Path: '" + pathFromInput + "'");
        }
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
        var possibleValueArray = objectInArray.get(pathControlKey);
        if (!(possibleValueArray instanceof JSONArray)) {
            return Optional.empty();
        } 
        var valueArray = (JSONArray)possibleValueArray;
        if (valueArray.length() != 1) {
            return Optional.empty();
        }
        if (!(valueArray.get(0) instanceof String)) {
            return Optional.empty();
        }
        
        return Optional.of((String)valueArray.get(0));
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
