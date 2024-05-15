package cz.cuni.mff.hdt.converter;

import java.io.IOException;
import java.io.InputStream;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Convertor converting from particular format to Unified representation. 
 */
public interface InputConverter {
    public Ur convert(InputStream input) throws IOException;
}
