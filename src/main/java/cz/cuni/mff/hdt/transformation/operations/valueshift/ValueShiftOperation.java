package cz.cuni.mff.hdt.transformation.operations.valueshift;

import cz.cuni.mff.hdt.adapter.SinkWriterAdapter;
import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.DocumentSource;
import cz.cuni.mff.hdt.transformation.operations.Operation;
import cz.cuni.mff.hdt.transformation.operations.OperationFailedException;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;
import java.util.Stack;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * Class implementing the value-shift operation from the transformational language
 */
public class ValueShiftOperation implements Operation {
    private JSONObject operationDefinition;
    private SinkWriterAdapter sinkWriterAdapter;
    private Stack<Dictionary<String, OperationVariable>> variableStack;

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
        variableStack = new Stack<>();
        variableStack.add(new Hashtable<String, OperationVariable>());
        

        // TODO the language should first match source keys by exact names and only then by named variables
        // think about how to implement that easily 
        // maybe we should seperate the key to variable and nonvariable and the nonvariable match first?

        for (var keysIterator = operationDefinition.keys(); keysIterator.hasNext();) {
            String key = keysIterator.next();
            // if key named variable save it and somehow save this object with for repeat match
            if (key.length() > 6 && key.substring(0, 5).equals("@var:")) {
                var varName = key.substring(5);
                Optional<String> matchingSourceKey = getMatchingSourceKey();
                if (matchingSourceKey.isEmpty()) {
                    // did not match the variable 
                    // TODO think about what to do
                }
                // on variable hit save the name, current JSONobject (so we know where to go back)
                // and the value from the source
                variableStack.peek().put(varName, new OperationVariable(varName, matchingSourceKey.get(), operationDefinition));
                
                // we need some special flag for if the recrusion is from variable match or not
                    // if from variable match then on operationDef and source missmatch just abort dont throw exception
            }
                
            
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
