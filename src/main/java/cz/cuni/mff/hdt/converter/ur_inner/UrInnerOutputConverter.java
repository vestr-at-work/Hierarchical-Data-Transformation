package cz.cuni.mff.hdt.converter.ur_inner;

import java.io.IOException;

import cz.cuni.mff.hdt.converter.OutputConverter;
import cz.cuni.mff.hdt.ur.Ur;

/**
 * Converter implementation for converting Unified representation (Ur) to Ur JSON inner representation.
 * Mainly used for debugging purposes.
 */
public class UrInnerOutputConverter implements OutputConverter {

    /**
     * Converts the Unified representation (Ur) data to Ur inner representation in JSON format.
     *
     * @param data the Unified representation (Ur) data to be converted
     * @return a string representing the data in Ur inner representation
     * @throws IOException if an I/O error occurs during conversion
     */
    @Override
    public String convert(Ur data) throws IOException {
        return data.getInnerRepresentation().toString(2);
    }
}
