package cz.cuni.mff.hdt.transformation;

import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.OperationFailedException;
import cz.cuni.mff.hdt.ur.Ur;

/**
 * Core class for transformation of the hierarchical data 
 */
public class Transformation {
    private TransformationDefinition definition;

    /**
     * Constructs a new {@code Transformation} with the specified {@code TransformationDefinition}.
     *
     * @param definition the transformation definition
     */
    public Transformation(TransformationDefinition definition) {
        this.definition = definition;
    }

    /**
     * Main function for transforming the data.
     *
     * @param input the input hierarchical data to be transformed
     * @return the transformed hierarchical data
     * @throws OperationFailedException if an operation in the transformation fails
     */
    public Ur transform(Ur input) throws OperationFailedException {
        Ur operationInput = input;
        Ur operationOutput = new Ur(new JSONObject());
        for (int i = 0; i < definition.operations.size(); i++) {
            if (i > 0) {
                operationInput = operationOutput;
            }
            var operation = definition.operations.get(i);
            operationOutput = operation.execute(operationInput);
        }

        return operationOutput;
    }
}