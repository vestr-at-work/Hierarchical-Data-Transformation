package cz.cuni.mff.hdt.transformation.operations;

/*
 * Custom exception to be thrown when transformation operation fails
 */
public class OperationFailedException extends RuntimeException {

    public OperationFailedException(String message) {
        super(message);
    }
}