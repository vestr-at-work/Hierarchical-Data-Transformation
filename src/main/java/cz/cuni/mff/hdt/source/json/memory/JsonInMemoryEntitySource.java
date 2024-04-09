package cz.cuni.mff.hdt.source.json.memory;

import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.EntityReference;
import cz.cuni.mff.hdt.source.ArraySource;
import cz.cuni.mff.hdt.source.EntitySource;

public class JsonInMemoryEntitySource implements EntitySource {

    @Override
    public ArrayReference property(EntityReference reference, String property) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'property'");
    }

    @Override
    public ArrayReference items(EntityReference reference) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'items'");
    }

    @Override
    public ArraySource getSourceFromReference(ArrayReference referece) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSourceFromReference'");
    }

    @Override
    public EntitySource getSourceFromReference(EntityReference entityReferece) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getSourceFromReference'");
    }
    
}
