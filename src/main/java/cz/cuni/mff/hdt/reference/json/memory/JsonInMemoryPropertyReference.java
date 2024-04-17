package cz.cuni.mff.hdt.reference.json.memory;

import cz.cuni.mff.hdt.reference.PropertyReference;

public class JsonInMemoryPropertyReference implements PropertyReference {
    public String value;
    public String type;

    public JsonInMemoryPropertyReference(String value, String type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public void close() {
        // Do nothing
    }
}
