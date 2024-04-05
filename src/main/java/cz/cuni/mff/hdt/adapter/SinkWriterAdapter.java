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

    private enum EntityType { Object, Array, Property, ArrayItem }
    
    /**
     * Record holding information about entity 
     */
    private record Entity(EntityType type, String value) {
        @Override
        public final boolean equals(Object other) {
            if (other instanceof Entity) {
                var bothValuesNull = ((Entity)other).value == null && this.value == null;
                var noValuesNull =  (((Entity)other).value != null && this.value != null);
                return ( bothValuesNull || ((noValuesNull) && ((Entity)other).value.equals(this.value)))
                    && ((Entity)other).type.equals(this.type);
            }
            return false;
        }
    }
    
    public SinkWriterAdapter(Sink sink) {
        outputSink = sink;
        currentPosition = new ArrayList<>();
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
        for (; i < parsedTokens.length && i < currentPosition.size(); i++) {
            if (parsedTokens[i].equals(currentPosition.get(i))) {
                continue;
            }
            break;
        }
        var entitiesToBeClosed = currentPosition.size() - i;

        closeOpenEntities(entitiesToBeClosed);
        openEntities(Arrays.copyOfRange(parsedTokens, i, parsedTokens.length));
        writeValue(value);
    }

    /**
     * Closes all the open arrays and objects
     * @throws IOException
     */
    public void finishWriting() throws IOException {
        closeOpenEntities(currentPosition.size());
        outputSink.flush();
    }

    private void writeValue(String value) throws IOException {
        outputSink.writeValue(value);
    }

    private void openEntities(Entity[] entities) throws IOException {
        for (var entity : entities) {
            switch (entity.type) {
                case Object:
                    outputSink.openObject();
                    break;
                case Array:
                    outputSink.openArray();
                    break;
                case Property:
                    outputSink.setNextKey(entity.value);
                case ArrayItem:
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
            var lastEntity = currentPosition.get(currentPosition.size() - 1);
            switch (lastEntity.type) {
                case Object:
                    outputSink.closeObject();
                    break;
                case Array:
                    outputSink.closeArray();
                    break;
                case Property:
                case ArrayItem:
                    break;
                default:
                    // Unreachable code
                    throw new IllegalStateException("Unreachable");
            }
            currentPosition.remove(currentPosition.size() - 1);
        }
    }

    private Entity[] getParsedTokens(String[] jsonPointerTokens) {
        ArrayList<Entity> entities = new ArrayList<>();
        for (int i = 1; i < jsonPointerTokens.length; i++) {
            var token = jsonPointerTokens[i];
            if (tokenIsArray(token)) {
                entities.add(new Entity(EntityType.Array, null));
                entities.add(new Entity(EntityType.ArrayItem, null));
            } 
            else {
                entities.add(new Entity(EntityType.Object, null));
                entities.add(new Entity(EntityType.Property, getKey(token)));
            }
        }

        return entities.toArray(new Entity[entities.size()]);
    }

    private String getKey(String token) {
        return token.replace("~1", "/")
            .replace("~2", "[")
            .replace("~3", "]")
            .replace("~0", "~");
    }

    private boolean tokenIsArray(String token) {
        return (token.length() >= 2 
            && token.charAt(0) == '[' 
            && token.charAt(token.length() - 1) == ']');
    }

}
