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
    public Optional<Operation> create(String operationName, JSONArray operationSpecs) throws IOException;
}
