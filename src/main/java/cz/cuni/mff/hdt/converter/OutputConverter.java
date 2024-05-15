package cz.cuni.mff.hdt.converter;

import java.io.IOException;
import java.io.OutputStream;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Convertor converting from Unified representation to particular format. 
 */
public interface OutputConverter {
    public OutputStream convert(Ur data) throws IOException;
}
