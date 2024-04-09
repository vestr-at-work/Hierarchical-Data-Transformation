package cz.cuni.mff.hdt.source.json.memory;

import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.EntityReference;
import cz.cuni.mff.hdt.reference.Reference;
import cz.cuni.mff.hdt.source.ArraySource;
import cz.cuni.mff.hdt.source.DocumentSource;
import cz.cuni.mff.hdt.source.EntitySource;

public class JsonInMemoryDocumentSource implements DocumentSource {
    // we have to have the root object/array saved

    @Override
    public Reference next() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'next'");
    }

    @Override
    public EntitySource getSourceFromReference(EntityReference entityReferece) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSourceFromReference'");
    }

    @Override
    public ArraySource getSourceFromReference(ArrayReference referece) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSourceFromReference'");
    }
    
}
