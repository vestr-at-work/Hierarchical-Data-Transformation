package cz.cuni.mff.hdt.transformation.operations.valueshift;

import org.json.JSONObject;

public class OperationVariable {
    public String Name;
    public String Value;
    public JSONObject PositionInOperationDefinition;

    public OperationVariable(String name, String value, JSONObject position) {
        Name = name;
        Value = value;
        PositionInOperationDefinition = position;
    }
}
