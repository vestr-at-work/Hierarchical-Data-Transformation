package cz.cuni.mff.hdt.ur;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing UrPath aka. path in the Ur object model.
 */
public class UrPath {
    /**
     * Token in UrPath
     */
    public abstract class Token {}

    /**
     * Token representing a property with key in Ur
     */
    public class PropertyToken extends Token {
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
    }
    /**
     * Token representing an array item in Ur.
     * 
     * If index is null it is for appending new item at the end of the array.
     */
    public class ArrayItemToken extends Token {
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

    public List<Token> tokens;

    /**
     * Constructs a UrPath from a string representation.
     * 
     * @param path the string representation of the path
     * @throws IOException if there is an error parsing the path
     */
    public UrPath(String path) throws IOException {
        var pathStringTokens = path.split("/");
        if (pathStringTokens.length == 2 && pathStringTokens[1].equals("")) {
            tokens = new ArrayList<>();
            return;
        }
        tokens = getParsedTokens(pathStringTokens);
    }

    /**
     * Constructs a UrPath from a list of tokens.
     * 
     * @param tokens the list of tokens
     */
    public UrPath(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Returns the length of the UrPath.
     * 
     * @return the number of tokens in the path
     */
    public int length() {
        return tokens.size();
    }

    protected List<Token> getParsedTokens(String[] pathStringTokens) throws IOException {
        ArrayList<Token> tokens = new ArrayList<>();
        for (int i = 1; i < pathStringTokens.length; i++) {
            var token = pathStringTokens[i];
            if (tokenIsArray(token)) {
                try {
                    tokens.add(new ArrayItemToken(getArrayIndexKey(token)));
                }
                catch (NumberFormatException e) {
                    throw new IOException("Incorrect index format in provided UrPath string.");
                }
            } 
            else { // is object property
                tokens.add(new PropertyToken(getKey(token)));
            }
        }
        return tokens;
    }

    protected String getKey(String token) {
        return token.replace("~1", "/")
            .replace("~2", "[")
            .replace("~3", "]")
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
}
