package cz.cuni.mff.hdt.path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing UrPath aka. path in the Ur object model.
 */
public class UrPath {
    public List<BaseUrPathToken> tokens;

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
     * Constructs a UrPath from an array of string tokens.
     * 
     * @param pathTokens the string tokens representing the path
     * @throws IOException if there is an error parsing the path
     */
    public UrPath(String[] pathTokens) throws IOException {
        var pathStringTokens = pathTokens;
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
    public UrPath(List<BaseUrPathToken> tokens) {
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

    protected List<BaseUrPathToken> getParsedTokens(String[] pathStringTokens) throws IOException {
        ArrayList<BaseUrPathToken> tokens = new ArrayList<>();
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
