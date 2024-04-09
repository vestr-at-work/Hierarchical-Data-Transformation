package cz.cuni.mff.hdt.adapter;

import java.io.IOException;

import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.EntityReference;
import cz.cuni.mff.hdt.reference.Reference;
import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.source.ArraySource;
import cz.cuni.mff.hdt.source.DocumentSource;
import cz.cuni.mff.hdt.source.EntitySource;

public class SinkSourceAdapter implements Sink, DocumentSource {

    @Override
    public Reference next() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'next'");
    }

    @Override
    public void openObject() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'openObject'");
    }

    @Override
    public void closeObject() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeObject'");
    }

    @Override
    public void openArray() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'openArray'");
    }

    @Override
    public void closeArray() throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'closeArray'");
    }

    @Override
    public void setNextKey(String key) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setNextKey'");
    }

    @Override
    public void writeValue(String value) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'writeValue'");
    }

    @Override
    public void flush() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'flush'");
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
