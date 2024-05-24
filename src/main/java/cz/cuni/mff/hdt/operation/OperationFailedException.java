package cz.cuni.mff.hdt.operation;

/*
 * Custom exception to be thrown when transformation operation fails
 */
public class OperationFailedException extends RuntimeException {

    /**
     * Constructs a new OperationFailedException with the specified detail message.
     *
     * @param message the detail message
     */
    public OperationFailedException(String message) {
        super(message);
    }
}