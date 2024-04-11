package cz.cuni.mff.hdt.source.json.memory;

import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.Reference;
import cz.cuni.mff.hdt.reference.ValueReference;
import cz.cuni.mff.hdt.reference.json.memory.JsonInMemoryArrayReference;
import cz.cuni.mff.hdt.source.ArraySource;
import cz.cuni.mff.hdt.source.ValueSource;

public class JsonInMemoryArraySource implements ArraySource {

    protected JsonInMemoryDocumentSource documentSource;

    public JsonInMemoryArraySource(JsonInMemoryDocumentSource documentSource) {
        this.documentSource = documentSource;
    }

    @Override
    public Reference next(ArrayReference reference) {
        if (!(reference instanceof JsonInMemoryArrayReference)) {
            throw new IllegalArgumentException("Passed reference is not compatible with JsonInMemoryArraySource");
        }

        var jsonArrayReference = (JsonInMemoryArrayReference)reference; 
        return jsonArrayReference.innerReference;
    }

    @Override
    public ArrayReference clone(ArrayReference reference) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'clone'");
    }

    @Override
    public ValueSource getSourceFromReference(ValueReference referece) {
        return documentSource.getValueSource();
    }
    
}
