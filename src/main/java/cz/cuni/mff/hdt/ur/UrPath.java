package cz.cuni.mff.hdt.ur;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UrPath {
    public abstract class Token {}
    public class PropertyToken extends Token {
        protected String key;

        public PropertyToken(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
    public class ArrayItemToken extends Token {
        protected int index;

        public ArrayItemToken(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public List<Token> tokens;

    public UrPath(String path) throws IOException {
        tokens = getParsedTokens(path.split("/"));
    }

    private List<Token> getParsedTokens(String[] pathStringTokens) throws IOException {
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

    private String getKey(String token) {
        return token.replace("~1", "/")
            .replace("~2", "[")
            .replace("~3", "]")
            .replace("~0", "~");
    }

    private int getArrayIndexKey(String token) {
        return Integer.parseInt(token.substring(1, token.length() - 1));
    }

    private boolean tokenIsArray(String token) {
        return (token.length() >= 2 
            && token.charAt(0) == '[' 
            && token.charAt(token.length() - 1) == ']');
    }
}
