package cz.cuni.mff.hdt.operation.valueshift;

import org.json.JSONArray;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;

/*
 * Class implementing the value-shift operation from the transformational language
 */
public class ValueShiftOperation implements Operation {
    private JSONArray operationDefinition;

    public ValueShiftOperation(JSONArray operationDefinition) {
        this.operationDefinition = operationDefinition;
    }

    @Override
    public Ur execute(Ur input) throws OperationFailedException {
        return input;
    }
}
