package cz.cuni.mff.hdt.operation;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONArray;

/*
 * Interface for the factory class creating all the supported operations in transformation.
 * 
 * For new user defined operations new OperationFactory implementation that supports such operation needs to be added.
 */
public interface OperationFactory {
    
    /**
     * Creates an operation based on the given operation name and specifications.
     *
     * @param operationName the name of the operation to create
     * @param operationSpecs the specifications for the operation
     * @return an Optional containing the created operation, or an empty Optional if the operation name is unknown
     * @throws IOException if there is an error creating the operation
     */
    public Optional<Operation> create(String operationName, JSONArray operationSpecs) throws IOException;
}
