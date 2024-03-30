package cz.cuni.mff.hdt.adapter;

import cz.cuni.mff.hdt.sink.Sink;

/*
 * Adapter for easy writing to Sinks.
 * 
 * User of the SinkWriterAdapter calls write method 
 * and the class holds Sink and state of writing to Sink 
 * to be able to correctly and effectively write to Sink.
 */
public class SinkWriterAdapter {
    private Sink outputSink;
    
    public SinkWriterAdapter(Sink sink) {
        outputSink = sink;
    }

    /*
     * Writes passed value at the given position to sink and updates its state.
     * Positions cannot "go back" because we are streaming data to the sink.
     */
    public void write(String position, String value) {
        throw new UnsupportedOperationException("Unimplemented method 'write'");
    }

}
