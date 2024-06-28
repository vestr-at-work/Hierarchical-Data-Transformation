package cz.cuni.mff.hdt.path;

/**
 * Token representing a named variable of array item in Ur.
 * 
 * If index is null the variable is not matched.
 */
public class VariableArrayItemToken extends UrPathToken {
    private Integer index;
    private String variableName;

    /**
     * Constructs an VariableArrayItemToken with the specified name and index.
     * 
     * @param name the name of the variable
     * @param index the index of the array item
     */
    public VariableArrayItemToken(String name, Integer index) {
        this.index = index;
        variableName = name;
    }

    /**
     * Returns the index of the array item.
     * 
     * @return the index, or null if not set
     */
    public Integer getIndex() {
        return index;
    }

    /**
     * Sets the index of the array item.
     * 
     * @param index the index
     */
    public void setIndex(Integer index) {
        this.index = index;
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
