package cz.cuni.mff.hdt.path;

/**
 * Token representing a property with key in Ur
 */
public class PropertyToken extends BaseUrPathToken {
    private String key;

    /**
     * Constructs a PropertyToken with the specified key.
     * 
     * @param key the key of the property
     */
    public PropertyToken(String key) {
        this.key = key;
    }

    /**
     * Returns the key of the property.
     * 
     * @return the key
     */
    public String getKey() {
        return key;
    }

    @Override
    public boolean equals(Object other) {
        // self check
        if (this == other) {
            return true;
        }
        // null check
        if (other == null) {
            return false;
        }
        // type check and cast
        if (getClass() != other.getClass()) {
            return false;
        }
        PropertyToken otherToken = (PropertyToken) other;
        // field comparison
        return key.equals(otherToken.key);
    }
}
