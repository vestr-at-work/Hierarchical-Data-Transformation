package cz.cuni.mff.hdt.path;

import java.io.IOException;
import java.util.List;

/**
 * Class representing variable UrPath aka. path in the Ur object model with the named variables.
 */
public class VariableUrPath {
    private List<UrPathToken> tokens;
    private boolean hasVariables;

    /**
     * Constructs a VariableUrPath from a string representation.
     * 
     * @param path the string representation of the path
     * @throws IOException if there is an error parsing the path
     */
    public VariableUrPath(String path) {

    }

    /**
     * Returns the length of the VariableUrPath.
     * 
     * @return the number of tokens in the path
     */
    public int length() {
        return tokens.size();
    }

    /**
     * Returns base UrPath with matched variable values or null if some variables not matched. 
     * 
     * @return base UrPath with matched variables
     */
    public UrPath getUrPath() {
        return null;
    }

    /**
     * Returns sub-path of base UrPath with matched variable values and prvided length or null if some variables not matched. 
     * 
     * @param length length of the output sub-path
     * @return base UrPath with matched variables
     */
    public UrPath getUrPath(Integer length) throws IOException {
        return null;
    }

    /**
     * Returns information if path has variables.
     * 
     * @return true if VariableUrPath has only BaseUrPathTokens, false otherwise
     */
    public boolean hasVariables() {
        return hasVariables;
    }

    
    // Class VariableUrPath
    // - has variable tokens support
    // - has export to UrPath when variable values provided
    // - supports subPath
    // - bool hasVariables() method
    // - can copy itself
    // - gets values of variables by name
}
