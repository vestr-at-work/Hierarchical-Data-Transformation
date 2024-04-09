package cz.cuni.mff.hdt.source.json.memory;

import cz.cuni.mff.hdt.reference.ValueReference;
import cz.cuni.mff.hdt.reference.json.memory.JsonInMemoryValueReference;
import cz.cuni.mff.hdt.source.ValueSource;

public class JsonInMemoryValueSource implements ValueSource {

    @Override
    public String value(ValueReference reference) {
        if (!(reference instanceof JsonInMemoryValueReference)) {
            throw new IllegalArgumentException("Passed reference is not compatible with JsonInMemoryValueSource");
        }
        return ((JsonInMemoryValueReference)reference).Value;
    }

}
