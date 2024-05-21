package cz.cuni.mff.hdt.transformation;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFactory;

/**
 * Container of transformation operations in order
 */
public class TransformationDefinition {
    public static final String KEY_OPERATIONS = "operations";
    public static final String KEY_OPERATION = "operation";
    public static final String KEY_SPECIFICATION = "specs";

    /*
     * Operations to be executed in a transformations in order
     */
    public List<Operation> operations = new ArrayList<>();

    public TransformationDefinition(List<Operation> operations) {
        this.operations = operations;
    }

    public static TransformationDefinition getTransformationDefinition(
        JSONObject transformationDefinitionObject, OperationFactory operationFactory) {

        ArrayList<Operation> newOperations = getOperations(transformationDefinitionObject, operationFactory);
        return new TransformationDefinition(newOperations);
    }

    private static ArrayList<Operation> getOperations(
        JSONObject transformationDefinitionObject, OperationFactory operationFactory) {

        if (!transformationDefinitionObject.has(KEY_OPERATIONS)) {
            throw new IllegalArgumentException("Transformation definition does not include mandatory '" + KEY_OPERATIONS + "' key");
        }
        var operations = transformationDefinitionObject.get(KEY_OPERATIONS);
        if (!(operations instanceof JSONArray)) {
            throw new IllegalArgumentException("Transformation definition does not include an array of operations");
        }
        var operationList = new ArrayList<Operation>();
        var operationArray = (JSONArray)operations;
        Integer index = 0;
        for (var operation : operationArray) {
            if (!(operation instanceof JSONObject)) {
                throw new IllegalArgumentException("Not all operations in '" + KEY_OPERATIONS +"' array are an object");
            }
            var operationObject = (JSONObject)operation;
            if (!operationObject.has(KEY_OPERATION)) {
                throw new IllegalArgumentException("Operation in transformation definition does not have mandatory '" + KEY_OPERATION + "' key");
            }
            if (!operationObject.has(KEY_SPECIFICATION)) {
                throw new IllegalArgumentException("Operation in transformation definition does not have mandatory '" + KEY_SPECIFICATION + "' key");
            }
            var specs = operationObject.get(KEY_SPECIFICATION);
            if (!(specs instanceof JSONArray)) {
                throw new IllegalArgumentException("Operation in transformation definition does not have valid '" + KEY_SPECIFICATION + "' property");
            }
            var operationType = operationObject.get(KEY_OPERATION);

            if (!(operationType instanceof String)) {
                throw new IllegalArgumentException("Incorrect '" + KEY_OPERATION + "' value in operation with index: '" + index + "'. Has to be of type String.");
            }
            var operationTypeString = (String)operationType;
            var specsObject = (JSONArray)specs;
            var parsedOperation = operationFactory.get(operationTypeString, specsObject);
            if (parsedOperation.isEmpty()) {
                throw new IllegalArgumentException("Uknown operation '" + operationTypeString + "'");
            }
            operationList.add(parsedOperation.get());

            index++;
        }

        return operationList;
    }
}