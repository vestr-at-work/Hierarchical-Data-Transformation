package cz.cuni.mff.hdt.path;

/**
 * Token representing a named variable of property in Ur.
 * 
 * If key is null the variable is not matched.
 */
public class VariablePropertyToken extends UrPathToken {
    private String key;
    private String variableName;

    /**
     * Constructs an VariablePropertyToken with the specified name and key.
     * 
     * @param index the index of the array item, or null if appending
     */
    public VariablePropertyToken(String name, String key) {
        this.key = key;
        variableName = name;
    }

    /**
     * Returns the key of the property.
     * 
     * @return the key, or null if not set
     */
    public String getKey() {
        return key;
    }

    /**
     * Sets the key of the property.
     * 
     * @param key the key
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * Returns the name of the variable.
     * 
     * @return the name of the variable
     */
    public String getName() {
        return variableName;
    }
}
