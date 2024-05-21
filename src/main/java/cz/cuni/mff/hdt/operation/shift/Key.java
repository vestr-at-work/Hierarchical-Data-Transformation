package cz.cuni.mff.hdt.operation.shift;

public class Key {
    public String Value;
    public KeyType Type;
    public Integer PositionInTypedList;

    public Key(String value, KeyType type, Integer position) {
        Value = value;
        Type = type;
        PositionInTypedList = position;
    }
}
