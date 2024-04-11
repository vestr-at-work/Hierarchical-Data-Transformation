package cz.cuni.mff.hdt.transformation;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.transformation.operations.Operation;
import cz.cuni.mff.hdt.transformation.operations.valueshift.ValueShiftOperation;

/**
 * Container of transformation operations in order
 */
public class TransformationDefinition {
    /*
     * Operations to be executed in a transformations in order
     */
    public List<Operation> operations = new ArrayList<>();

    public TransformationDefinition(List<Operation> operations) {
        this.operations = operations;
    }

    public static TransformationDefinition getTransformationDefinitionFromObject(JSONObject transformationDefinitionObject) {
        ArrayList<Operation> newOperations = getOperations(transformationDefinitionObject);
        return new TransformationDefinition(newOperations);
    }

    private static ArrayList<Operation> getOperations(JSONObject transformationDefinitionObject) {
        var operations = transformationDefinitionObject.get("@operations");
        if (!(operations instanceof JSONArray)) {
            throw new IllegalArgumentException("Transformation definition does not include an array of operations");
        }
        var operationList = new ArrayList<Operation>();
        var operationArray = (JSONArray)operations;
        for (var operation : operationArray) {
            if (!(operation instanceof JSONObject)) {
                throw new IllegalArgumentException("Operation in transformation definition is not an object");
            }
            var operationObject = (JSONObject)operation;
            if (!operationObject.has("@operation")) {
                throw new IllegalArgumentException("Operation in transformation definition does not have mandatory '@operation' key");
            }
            if (!operationObject.has("@specs")) {
                throw new IllegalArgumentException("Operation in transformation definition does not have mandatory '@specs' key");
            }
            var specs = operationObject.get("@specs");
            if (!(specs instanceof JSONObject)) {
                throw new IllegalArgumentException("Operation in transformation definition does not have  valid '@specs' property");
            }
            var specsObject = (JSONObject)specs;
            var operationType = operationObject.get("@operation");

            if (operationType instanceof String && ((String)operationType).equals("value-shift")) {
                operationList.add(new ValueShiftOperation(specsObject));
            }
            // TODO add more operations
        }

        return operationList;
    }
}