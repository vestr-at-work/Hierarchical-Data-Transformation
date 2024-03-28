package cz.cuni.mff.hdt.transformation;

import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.DocumentSource;

public interface Operation {
    public void execute(DocumentSource inputSource, Sink outputSink) throws OperationFailedException;
}
