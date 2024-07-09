package cz.cuni.mff.hdt.converter;

import java.util.Optional;

/*
 * Interface for the factory class creating all the supported input converters in the transformation.
 * 
 * For new user defined input converters new InputConverterFactory implementation that supports such input converters needs to be added.
 */
public interface InputConverterFactory {
    
    /**
     * Creates an input converter based on the given operation name and specifications.
     *
     * @param operationName the name of the input converter to create
     * @return an Optional containing the created input converter, or an empty Optional if the name is unknown
     */
    public Optional<InputConverter> create(String converterName);
}
