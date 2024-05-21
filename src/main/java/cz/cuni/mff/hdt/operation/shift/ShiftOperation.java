package cz.cuni.mff.hdt.operation.shift;

import org.json.JSONArray;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;

/*
 * Class implementing the shift operation of the transformation language
 */
public class ShiftOperation implements Operation {
    private JSONArray operationDefinition;

    public ShiftOperation(JSONArray operationDefinition) {
        this.operationDefinition = operationDefinition;
    }

    @Override
    public Ur execute(Ur input) throws OperationFailedException {
        return input;
    }
}
