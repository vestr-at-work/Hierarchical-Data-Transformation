package cz.cuni.mff.hdt.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONPointer;

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
    private ArrayList<Entity> currentPosition;

    private enum EntityType { Object, Array }
    
    /**
     * Record holding information about entity 
     */
    private record Entity(EntityType type, String value) {
        @Override
        public final boolean equals(Object other) {
            if (other instanceof Entity) {
                return ((Entity)other).value.equals(this.value) 
                    && ((Entity)other).type.equals(this.type);
            }
            return false;
        }
    }
    
    public SinkWriterAdapter(Sink sink) {
        outputSink = sink;
    }

    /*
     * Writes passed value at the given position to sink and updates its state.
     * Positions cannot "go back" because we are streaming data to the sink.
     */
    public void write(JSONPointer position, String value) throws IOException {
        var jsonPointerString = position.toString();
        String[] jsonPointerTokens = jsonPointerString.split("/");
        Entity[] parsedTokens = getParsedTokens(jsonPointerTokens);

        int i = 0;
        while (currentPosition.get(i).equals(parsedTokens[i])) {
            i++;
        }
        var entitiesToBeClosed = currentPosition.size() - i;

        closeOpenEntities(entitiesToBeClosed);
        openEntities(Arrays.copyOfRange(parsedTokens, i + 1, parsedTokens.length));
        writeValue(value);
    }

    private void writeValue(String value) throws IOException {
        outputSink.writeValue(value);
    }

    private void openEntities(Entity[] entities) throws IOException {
        for (var entity : entities) {
            switch (entity.type) {
                case Object:
                    outputSink.openObject();
                    outputSink.setNextKey(entity.value);
                    break;
                case Array:
                    outputSink.openArray();
                    break;
                default:
                    // Unreachable code
                    throw new IllegalStateException("Unreachable");
            }
            currentPosition.add(entity);
        }
    }

    private void closeOpenEntities(int entitiesToBeClosed) throws IOException {
        for (int i = 0; i < entitiesToBeClosed; i++) {
            var lastEntity = currentPosition.get(currentPosition.size());
            switch (lastEntity.type) {
                case Object:
                    outputSink.closeObject();
                    break;
                case Array:
                    outputSink.closeArray();
                    break;
                default:
                    // Unreachable code
                    throw new IllegalStateException("Unreachable");
            }
            currentPosition.remove(lastEntity);
        }
    }

    private Entity[] getParsedTokens(String[] jsonPointerTokens) {
        ArrayList<Entity> entities = new ArrayList<>();
        // TODO first token is empty
        for (var token : jsonPointerTokens) {

        }

        throw new UnsupportedOperationException("");
    }

}
