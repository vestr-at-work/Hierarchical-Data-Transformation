package cz.cuni.mff.hdt.operation;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Common interface for all transformation operations.
 * 
 * <p>For new user-defined operations, this interface needs to be implemented.</p>
 */
public interface Operation {

    /**
     * Executes the transformation operation on the provided {@code Ur} object.
     *
     * @param inputUr the input {@code Ur} object on which the operation is to be performed
     * @return the resulting {@code Ur} object after the operation is applied
     * @throws OperationFailedException if the operation fails
     */
    public Ur execute(Ur inputUr) throws OperationFailedException;
}
