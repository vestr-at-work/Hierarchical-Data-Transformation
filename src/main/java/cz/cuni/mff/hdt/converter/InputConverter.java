package cz.cuni.mff.hdt.converter;

import java.io.IOException;
import java.io.InputStream;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Converter converting from particular format to Unified representation. 
 */
public interface InputConverter {

    /**
     * Converts the input stream into a Unified representation (Ur).
     *
     * @param input the input stream to convert
     * @return the Unified representation (Ur) of the input
     * @throws IOException if an I/O error occurs during conversion
     */
    public Ur convert(InputStream input) throws IOException;
}
