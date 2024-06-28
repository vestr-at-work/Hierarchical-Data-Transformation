package cz.cuni.mff.hdt.path;

/**
 * Token representing an array item in Ur.
 * 
 * If index is null it is for appending new item at the end of the array.
 */
public class ArrayItemToken extends BaseUrPathToken {
    private Integer index;

    /**
     * Constructs an ArrayItemToken with the specified index.
     * 
     * @param index the index of the array item, or null if appending
     */
    public ArrayItemToken(Integer index) {
        this.index = index;
    }

    /**
     * Returns the index of the array item.
     * 
     * @return the index, or null if appending
     */
    public Integer getIndex() {
        return index;
    }
}
