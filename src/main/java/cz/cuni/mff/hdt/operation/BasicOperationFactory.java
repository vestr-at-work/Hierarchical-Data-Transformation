package cz.cuni.mff.hdt.operation;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONArray;

import cz.cuni.mff.hdt.operation.arraymap.ArrayMapOperation;
import cz.cuni.mff.hdt.operation.default_op.DefaultOperation;
import cz.cuni.mff.hdt.operation.filter.FilterOperation;
import cz.cuni.mff.hdt.operation.remove.RemoveOperation;
import cz.cuni.mff.hdt.operation.shift.ShiftOperation;

/**
 * Factory class to create different types of operations based on their name.
 */
public class BasicOperationFactory implements OperationFactory {
    public static final String OPERATION_DEFAULT = "default";
    public static final String OPERATION_FILTER = "filter";
    public static final String OPERATION_REMOVE = "remove";
    public static final String OPERATION_SHIFT = "shift";
    public static final String OPERATION_ARRAYMAP = "array-map";

    /**
     * Creates an operation based on the given operation name and specifications.
     *
     * @param operationName the name of the operation to create
     * @param operationSpecs the specifications for the operation
     * @return an Optional containing the created operation, or an empty Optional if the operation name is unknown
     * @throws IOException if there is an error creating the operation
     */
    @Override
    public Optional<Operation> create(String operationName, JSONArray operationSpecs) throws IOException {
        switch (operationName) {
            case OPERATION_DEFAULT:
                return Optional.of(new DefaultOperation(operationSpecs));
            case OPERATION_FILTER:
                return Optional.of(new FilterOperation(operationSpecs));
            case OPERATION_SHIFT:
                return Optional.of(new ShiftOperation(operationSpecs));
            case OPERATION_REMOVE:
                return Optional.of(new RemoveOperation(operationSpecs));
            case OPERATION_ARRAYMAP:
                return Optional.of(new ArrayMapOperation(operationSpecs));
            default:
                return Optional.empty();
        }
    }
}
