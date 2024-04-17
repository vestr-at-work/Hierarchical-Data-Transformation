package cz.cuni.mff.hdt.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private ArrayList<Entity<WriterAdapterEntityType>> currentPosition;

    public SinkWriterAdapter(Sink sink) {
        outputSink = sink;
        currentPosition = new ArrayList<>();
    }

    /*
     * Writes passed value at the given position to sink and updates its state.
     * Positions cannot "go back" because we are streaming data to the sink.
     */
    public void write(List<Entity<WriterAdapterEntityType>> newPosition, String value) throws IOException {
        var sameEntitiesCount = getSameEntitiesCount(newPosition);
        var entitiesToBeClosed = currentPosition.size() - sameEntitiesCount;

        closeOpenEntities(entitiesToBeClosed);
        openEntities(newPosition.stream().skip(sameEntitiesCount).collect(Collectors.toList()));
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

    private int getSameEntitiesCount(List<Entity<WriterAdapterEntityType>> entities) {
        int count = 0;
        for (; count < entities.size() && count < currentPosition.size(); count++) {
            if (entities.get(count).equals(currentPosition.get(count))) {
                continue;
            }
            break;
        }
        return count;
    }

    private void writeValue(String value) throws IOException {
        outputSink.writeValue(value);
    }

    private void openEntities(List<Entity<WriterAdapterEntityType>> entities) throws IOException {
        for (var entity : entities) {
            switch (entity.type()) {
                case Object:
                    outputSink.openObject();
                    break;
                case Array:
                    outputSink.openArray();
                    break;
                case Property:
                    outputSink.setNextKey(entity.name());
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
            switch (lastEntity.type()) {
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

}
