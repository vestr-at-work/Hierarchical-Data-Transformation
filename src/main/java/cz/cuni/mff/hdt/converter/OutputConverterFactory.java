package cz.cuni.mff.hdt.converter;

import java.util.Optional;

/*
 * Interface for the factory class creating all the supported output converters in the transformation.
 * 
 * For new user defined output converters new OutputConverterFactory implementation that supports such output converters needs to be added.
 */
public interface OutputConverterFactory {
    
    /**
     * Creates an output converter based on the given operation name and specifications.
     *
     * @param operationName the name of the output converter to create
     * @return an Optional containing the created output converter, or an empty Optional if the name is unknown
     */
    public Optional<OutputConverter> create(String converterName);
}
