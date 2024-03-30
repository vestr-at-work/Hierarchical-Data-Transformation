package cz.cuni.mff.hdt.transformation;

import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.DocumentSource;

/**
 * Container for context info about transformation
 */
public record TransformationContext(
    DocumentSource inputSource,
    Sink outputSink,
    TransformationDefinition transformationFile
) {}
