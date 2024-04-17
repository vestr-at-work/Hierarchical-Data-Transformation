package cz.cuni.mff.hdt.source.json.memory;

import cz.cuni.mff.hdt.reference.PropertyReference;
import cz.cuni.mff.hdt.reference.json.memory.JsonInMemoryPropertyReference;
import cz.cuni.mff.hdt.source.PropertySource;

public class JsonInMemoryPropertySource implements PropertySource {

    @Override
    public String value(PropertyReference reference) {
        if (!(reference instanceof JsonInMemoryPropertyReference)) {
            throw new IllegalArgumentException("Passed reference is not compatible with JsonInMemoryPropertySource");
        }
        return ((JsonInMemoryPropertyReference)reference).value;
    }

    @Override
    public String type(PropertyReference reference) {
        if (!(reference instanceof JsonInMemoryPropertyReference)) {
            throw new IllegalArgumentException("Passed reference is not compatible with JsonInMemoryPropertySource");
        }
        return ((JsonInMemoryPropertyReference)reference).type;
    }
    
}
