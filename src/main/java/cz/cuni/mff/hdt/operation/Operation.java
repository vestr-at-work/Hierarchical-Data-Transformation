package cz.cuni.mff.hdt.operation;

import cz.cuni.mff.hdt.ur.Ur;

/*
 * Common interface for all transformation operations
 * 
 * For new user defined operations this interface needs to be implemented
 */
public interface Operation {
    public Ur execute(Ur inputUr) throws OperationFailedException;
}
