package cz.cuni.mff.hdt.transformation.operations.valueshift;

import cz.cuni.mff.hdt.adapter.SinkWriterAdapter;
import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.DocumentSource;
import cz.cuni.mff.hdt.transformation.operations.Operation;
import cz.cuni.mff.hdt.transformation.operations.OperationFailedException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Class implementing the value-shift operation from the transformational language
 */
public class ValueShiftOperation implements Operation {
    private JSONObject operationDefinition;
    private SinkWriterAdapter sinkWriterAdapter;
    private Dictionary<String, OperationVariable> variableDict;

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
        variableDict = new Hashtable<String, OperationVariable>();

        ArrayList<Key> keysInOrder = new ArrayList<>();
        ArrayList<String> namedKeys = new ArrayList<>();
        ArrayList<String> variableKeys = new ArrayList<>();

        // TODO the language should first match source keys by exact names and only then by named variables
        // think about how to implement that easily 
        // maybe we should seperate the key to variable and nonvariable and the nonvariable match first?
        
        // we want it in the same order as on the input 
            // - actually we need to have it in the order of operation definition, because of the sink
        // we want to first match exactly 
        // and then test every named variable on the rest. input has to match atliest one variable

        // go through keys and save them in order in a list
            // save the information about variable


        for (var keysIterator = operationDefinition.keys(); keysIterator.hasNext();) {
            String keyName = keysIterator.next();

            var prefixLength = "@var:".length();
            if (keyName.length() > prefixLength && keyName.substring(0, prefixLength).equals("@var:")) {
                var varName = keyName.substring(prefixLength);
                keysInOrder.add(new Key(varName, KeyType.Variable, variableKeys.size()));
                variableKeys.add(varName);
                variableDict.put(varName, new OperationVariable(varName, null));
                continue;
            }

            keysInOrder.add(new Key(keyName, KeyType.Const, namedKeys.size()));
            namedKeys.add(keyName);
        }

        for (var keysIterator = operationDefinition.keys(); keysIterator.hasNext();) {
            String key = keysIterator.next();
            
            Optional<String> pathInOutput = TryGetPath(operationDefinition.get(key));
            if (pathInOutput.isPresent()) {
                var parsedPath = getParsedPath(pathInOutput.get());
                String value = getValueFromSource(inputSource);
                sinkWriterAdapter.write(parsedPath, value);
            }

            // else walk the source aka get the key/key in position of variable from the source
            // and check that they are the same, if not throw exception
            
            // call recursive method

        }

        // we have gone through all the keys in operation definition
        // if the source is not at the end and we have no variables, throw exception
        // else we iterate through source and try to match the variables

        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    private Optional<String> getMatchingSourceKey() {
        // TODO implement
        throw new UnsupportedOperationException("Unimplemented method 'getMatchingSourceKey'");
    }

    private String getValueFromSource(DocumentSource source) {
        // TODO implement
        throw new UnsupportedOperationException("Unimplemented method 'getValueFromSource'");
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
