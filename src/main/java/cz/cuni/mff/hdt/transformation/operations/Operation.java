package cz.cuni.mff.hdt.transformation.operations;

import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.DocumentSource;

/*
 * Common interface for all transformation operations
 * 
 * For new user defined operations this interface needs to be implemented
 */
public interface Operation {
    public void execute(DocumentSource inputSource, Sink outputSink) throws OperationFailedException;
}
