package cz.cuni.mff.hdt.reference.json.memory;

import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.Reference;

public class JsonInMemoryArrayReference implements ArrayReference {
    public Reference innerReference;

    public JsonInMemoryArrayReference(Reference reference) {
        innerReference = reference;
    }

    @Override
    public void close() {
        // Do nothing
    }
    
}
