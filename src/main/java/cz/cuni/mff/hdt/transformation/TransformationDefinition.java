package cz.cuni.mff.hdt.transformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.converter.InputConverter;
import cz.cuni.mff.hdt.converter.InputConverterFactory;
import cz.cuni.mff.hdt.converter.OutputConverter;
import cz.cuni.mff.hdt.converter.OutputConverterFactory;
import cz.cuni.mff.hdt.operation.Operation;
import cz.cuni.mff.hdt.operation.OperationFactory;

/**
 * Container of transformation operations in order
 */
public class TransformationDefinition {
    public static final String KEY_OPERATIONS = "operations";
    public static final String KEY_OPERATION = "operation";
    public static final String KEY_SPECIFICATION = "specs";
    public static final String KEY_INPUT_CONVERTER = "input-converter";
    public static final String KEY_OUTPUT_CONVERTER = "output-converter";

    /*
     * Operations to be executed in a transformations in order
     */
    public List<Operation> operations = new ArrayList<>();

    /**
     * Ur input converter of the transformation
     */
    public InputConverter inputConverter;

    /**
     * Ur output converter of the transformation
     */
    public OutputConverter outputConverter;

    /**
     * Constructs a new {@code TransformationDefinition} with the specified list of operations.
     *
     * @param operations the list of operations
     * @param inputConverter inputConverter to be used
     * @param outputConverter outputConverter to be used
     */
    public TransformationDefinition(List<Operation> operations, InputConverter inputConverter, 
            OutputConverter outputConverter) {
                
        this.operations = operations;
        this.inputConverter = inputConverter;
        this.outputConverter = outputConverter;
    }

    /**
     * Creates a {@code TransformationDefinition} from a JSON object using the specified {@code OperationFactory}.
     * Is a factory method.
     *
     * @param transformationDefinitionObject the JSON object containing the transformation definition
     * @param operationFactory the factory to create operations
     * @param inputFactory the factory to create Ur input converter
     * @param outputFactory the factory to create Ur output converter
     * @return the created {@code TransformationDefinition}
     * @throws IllegalArgumentException if the JSON object is invalid or missing required keys
     */
    public static TransformationDefinition getTransformationDefinition(
            JSONObject transformationDefinitionObject, OperationFactory operationFactory,
            InputConverterFactory inputFactory, OutputConverterFactory outputFactory) throws IllegalArgumentException {

        ArrayList<Operation> newOperations = getOperations(transformationDefinitionObject, operationFactory);
        InputConverter inputConverter = getInputConverter(transformationDefinitionObject, inputFactory);
        OutputConverter outputConverter = getOutputConverter(transformationDefinitionObject, outputFactory);
        return new TransformationDefinition(newOperations, inputConverter, outputConverter);
    }

    private static OutputConverter getOutputConverter(JSONObject transformationDefinitionObject,
            OutputConverterFactory outputFactory) {
            
        if (!transformationDefinitionObject.has(KEY_OUTPUT_CONVERTER)) {
            throw new IllegalArgumentException("Transformation definition does not include mandatory '" + KEY_INPUT_CONVERTER + "' key");
        }

        var outputConverterValue = transformationDefinitionObject.get(KEY_OUTPUT_CONVERTER);
        if (!(outputConverterValue instanceof String)) {
            throw new IllegalArgumentException("Key '" + KEY_OUTPUT_CONVERTER + "' not a string");
        }

        var outputConverter = outputFactory.create((String)outputConverterValue);
        if (outputConverter.isEmpty()) {
            throw new IllegalArgumentException("Unsupported output converter '" + (String)outputConverterValue + "' provided in transformation definition");
        }

        return outputConverter.get();
    }

    private static InputConverter getInputConverter(JSONObject transformationDefinitionObject,
            InputConverterFactory inputFactory) {

        if (!transformationDefinitionObject.has(KEY_INPUT_CONVERTER)) {
            throw new IllegalArgumentException("Transformation definition does not include mandatory '" + KEY_INPUT_CONVERTER + "' key");
        }

        var inputConverterValue = transformationDefinitionObject.get(KEY_INPUT_CONVERTER);
        if (!(inputConverterValue instanceof String)) {
            throw new IllegalArgumentException("Key '" + KEY_INPUT_CONVERTER + "' not a string");
        }

        var inputConverter = inputFactory.create((String)inputConverterValue);
        if (inputConverter.isEmpty()) {
            throw new IllegalArgumentException("Unsupported input converter '" + (String)inputConverterValue + "' provided in transformation definition");
        }

        return inputConverter.get();
    }

    private static ArrayList<Operation> getOperations(
            JSONObject transformationDefinitionObject, OperationFactory operationFactory) throws IllegalArgumentException {

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
                throw new IllegalArgumentException("Incorrect '" + KEY_OPERATION + "' value in operation with index: '" + index + "'. Has to be of type String");
            }
            var operationTypeString = (String)operationType;
            var specsObject = (JSONArray)specs;

            Optional<Operation> parsedOperation;
            try {
                parsedOperation = operationFactory.create(operationTypeString, specsObject);
            }
            catch (IOException e) {
                throw new IllegalArgumentException("Incorrect specification in '" + operationTypeString + "' operation. " + e.getMessage());
            }
            
            if (parsedOperation.isEmpty()) {
                throw new IllegalArgumentException("Uknown operation '" + operationTypeString + "'");
            }
            operationList.add(parsedOperation.get());

            index++;
        }

        return operationList;
    }
}