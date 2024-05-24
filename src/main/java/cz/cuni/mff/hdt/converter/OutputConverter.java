package cz.cuni.mff.hdt.converter;

import java.io.IOException;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Converter converting from Unified representation to particular format. 
 */
public interface OutputConverter {

    /**
     * Converts the Unified representation (Ur) into a string representation of a particular format.
     *
     * @param data the Unified representation (Ur) to convert
     * @return a string representation of the data in the particular format
     * @throws IOException if an I/O error occurs during conversion
     */
    public String convert(Ur data) throws IOException;
}
