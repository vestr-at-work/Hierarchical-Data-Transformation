package cz.cuni.mff.hdt.transformation.operations;

/*
 * Custom exception to be thrown when transformation operation fails
 */
public class OperationFailedException extends Exception {

    public OperationFailedException(String message) {
        super(message);
    }
}