package cz.cuni.mff.hdt.convertor;

import java.io.IOException;
import java.io.InputStream;

import cz.cuni.mff.hdt.ur.Ur;

/**
 * Convertor converting from particular format to Unified representation. 
 */
public interface InputConvertor {
    public Ur convert(InputStream input) throws IOException;
}
