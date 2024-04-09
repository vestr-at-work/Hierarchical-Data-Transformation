package cz.cuni.mff.hdt.reference.json.memory;

import cz.cuni.mff.hdt.reference.ValueReference;

public class JsonInMemoryValueReference implements ValueReference {

    public String Value;

    public JsonInMemoryValueReference(String value) {
        Value = value;
    }

    @Override
    public void close() {
        // Do nothing
    }
}
