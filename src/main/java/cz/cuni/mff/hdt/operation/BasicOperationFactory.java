package cz.cuni.mff.hdt.operation;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONArray;

import cz.cuni.mff.hdt.operation.default_op.DefaultOperation;
import cz.cuni.mff.hdt.operation.filter.FilterOperation;
import cz.cuni.mff.hdt.operation.remove.RemoveOperation;
import cz.cuni.mff.hdt.operation.shift.ShiftOperation;

public class BasicOperationFactory implements OperationFactory {
    public static final String OPERATION_DEFAULT = "default";
    public static final String OPERATION_FILTER = "filter";
    public static final String OPERATION_REMOVE = "remove";
    public static final String OPERATION_SHIFT = "shift";

    @Override
    public Optional<Operation> get(String operationName, JSONArray operationSpecs) throws IOException {
        if (operationName.equals(OPERATION_DEFAULT)) {
            return Optional.of(new DefaultOperation(operationSpecs));
        }
        else if (operationName.equals(OPERATION_FILTER)) {
            return Optional.of(new FilterOperation(operationSpecs));
        }
        else if (operationName.equals(OPERATION_REMOVE)) {
            return Optional.of(new RemoveOperation(operationSpecs));
        }
        else if (operationName.equals(OPERATION_SHIFT)) {
            return Optional.of(new ShiftOperation(operationSpecs));
        }
        else {
            return Optional.empty();
        }
    }
}
