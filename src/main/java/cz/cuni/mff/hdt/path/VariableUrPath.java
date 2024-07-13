package cz.cuni.mff.hdt.path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class representing variable UrPath aka. path in the Ur object model with the named variables.
 */
public class VariableUrPath {
    public List<UrPathToken> tokens;
    protected static final String VARIABLE = "@var";
    protected static final String VARIABLE_NAME_DELIMETER = ":";

    private boolean hasVariables;
    private HashMap<String, Integer> variableIndices;

    /**
     * Constructs a VariableUrPath from a string token representation.
     * 
     * @param pathStringTokens list of the path string token
     * @throws IOException if there is an error parsing the path
     */
    public VariableUrPath(String[] pathStringTokens) throws IOException {
        hasVariables = false;
        variableIndices = new HashMap<>();
        tokens = getParsedTokens(pathStringTokens);
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
        return getUrPath(tokens.size());
    }

    /**
     * Returns sub-path of base UrPath with matched variable values and provided length or null if some variables not matched. 
     * 
     * @param length length of the output sub-path
     * @return base UrPath with matched variables
     */
    public UrPath getUrPath(Integer length) {
        var outputTokens = new ArrayList<BaseUrPathToken>();
        for (int i = 0; i < tokens.size() && i < length; i++) {
            var token = tokens.get(i);
            if (token instanceof BaseUrPathToken) {
                outputTokens.add((BaseUrPathToken)token);
                continue;
            }

            if (token instanceof VariableArrayItemToken) {
                var index = ((VariableArrayItemToken)token).getIndex();
                if (index == null) {
                    return null;
                }
                outputTokens.add(new ArrayItemToken(index));
            }
            else if (token instanceof VariablePropertyToken) {
                var key = ((VariablePropertyToken)token).getKey();
                if (key == null) {
                    return null;
                }
                outputTokens.add(new PropertyToken(key));
            }
        }
        
        return new UrPath(outputTokens);
    }

    /**
     * Returns information if path has variables.
     * 
     * @return true if VariableUrPath has only BaseUrPathTokens, false otherwise
     */
    public boolean hasVariables() {
        return hasVariables;
    }

    /**
     * Returns hashmap with the variable names as keys and indices of variable tokens as values.
     */
    public HashMap<String, Integer> getVariableIndices() {
        return variableIndices;
    }

    /**
     * Set property variable on index. If not a property variable on given index do nothing.
     * 
     * @param index position of the variable starting from 0
     * @param value value to set variable to
     * @throws IndexOutOfBoundsException thrown then index out of bounds
     */
    public void trySetPropertyVariable(int index, String value) throws IndexOutOfBoundsException {
        if (index >= tokens.size()) {
            throw new IndexOutOfBoundsException("Invalid index provided");
        }
        var token = tokens.get(index);
        if (token instanceof VariablePropertyToken) {
            ((VariablePropertyToken)token).setKey(value);
        }
    }

    /**
     * Set array item variable on index. If not an array item variable on given index do nothing.
     * 
     * @param index position of the variable starting from 0
     * @param value value to set variable to
     * @throws IndexOutOfBoundsException thrown then index out of bounds
     */
    public void trySetArrayItemVariable(int index, Integer value) throws IndexOutOfBoundsException {
        if (index >= tokens.size()) {
            throw new IndexOutOfBoundsException("Invalid index provided");
        }
        var token = tokens.get(index);
        if (token instanceof VariableArrayItemToken) {
            ((VariableArrayItemToken)token).setIndex(value);
        }
    }

    /**
     * Reset variable on index. If not a variable on index do nothing.
     * 
     * @param index position of the variable starting from 0
     * @throws IndexOutOfBoundsException thrown then index out of bounds
     */
    public void tryResetVariable(int index) throws IndexOutOfBoundsException {
        if (index >= tokens.size()) {
            throw new IndexOutOfBoundsException("Invalid index provided");
        }
        var token = tokens.get(index);
        if (token instanceof VariableArrayItemToken) {
            ((VariableArrayItemToken)token).setIndex(null);
        }
        else if (token instanceof VariablePropertyToken) {
            ((VariablePropertyToken)token).setKey(null);
        }
    }

    protected List<UrPathToken> getParsedTokens(String[] pathStringTokens) throws IOException {
        ArrayList<UrPathToken> tokens = new ArrayList<>();
        for (int i = 0; i < pathStringTokens.length; i++) {
            var token = pathStringTokens[i];
            if (tokenIsArray(token)) {
                if (tokenIsVariable(token)) {
                    hasVariables = true;
                    var name = getVariableName(token);
                    tokens.add(new VariableArrayItemToken(name, null));
                    variableIndices.put(name, tokens.size() - 1);
                    continue;
                }
                
                try {
                    tokens.add(new ArrayItemToken(getArrayIndexKey(token)));
                }
                catch (NumberFormatException e) {
                    throw new IOException("Incorrect index format in provided UrPath string.");
                }
            } 
            else { // is object property
                if (tokenIsVariable(token)) {
                    hasVariables = true;
                    var name = getVariableName(token);
                    tokens.add(new VariablePropertyToken(name, null));
                    variableIndices.put(name, tokens.size() - 1);
                    continue;
                }

                tokens.add(new PropertyToken(getKey(token)));
            }
        }
        return tokens;
    }

    protected String getKey(String token) {
        return token.replace("~1", "[")
            .replace("~2", "]")
            .replace("~0", "~");
    }

    protected Integer getArrayIndexKey(String token) {
        if (token.length() == 2) {
            return null;
        }
        return Integer.parseInt(token.substring(1, token.length() - 1));
    }

    protected boolean tokenIsArray(String token) {
        return (token.length() >= 2 
            && token.charAt(0) == '[' 
            && token.charAt(token.length() - 1) == ']');
    }

    protected boolean tokenIsVariable(String token) {
        if (token.length() >= VARIABLE.length() + 1
            && token.substring(0, VARIABLE.length() + 1).equals("[" + VARIABLE)) {
            
            return true;
        } 
        if (token.length() >= VARIABLE.length()
            && token.substring(0, VARIABLE.length()).equals(VARIABLE)) {
                
            return true;
        }
        return false;
    }

    protected String getVariableName(String token) {
        var parts = token.split(VARIABLE_NAME_DELIMETER);
        if (parts.length < 2) {
            return null;
        }
        return parts[1];
    }
}
