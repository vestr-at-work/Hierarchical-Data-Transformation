package cz.cuni.mff.hdt.transformation.operations.valueshift;

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
