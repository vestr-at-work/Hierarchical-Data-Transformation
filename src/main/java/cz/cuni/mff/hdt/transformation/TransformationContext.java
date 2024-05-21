package cz.cuni.mff.hdt.transformation;

import cz.cuni.mff.hdt.converter.InputConverter;
import cz.cuni.mff.hdt.converter.OutputConverter;

/**
 * Container for context info about transformation
 */
public record TransformationContext(
    InputConverter inputConverter,
    OutputConverter outputConverter,
    TransformationDefinition transformationFile
) {}
