package cz.cuni.mff.hdt.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cz.cuni.mff.hdt.sink.Sink;
import cz.cuni.mff.hdt.transformation.TypedValue;
import cz.cuni.mff.hdt.ur.Ur;

/**
 * Adapter for easily writing out valid Ur document.
 * 
 * It adds Ur array constructs where and Ur '@type' info where it's needed.
 */
public class UrAwareSinkWriterAdapter {

    private enum UrEntityType { Object, Array, Property };

    private SinkWriterAdapter sinkWriter;
    private ArrayList<Entity<WriterAdapterEntityType>> sinkWriterCurrentPosition;
    private ArrayList<Entity<UrEntityType>> currentPosition;

    public UrAwareSinkWriterAdapter(Sink outputSink) {
        sinkWriter = new SinkWriterAdapter(outputSink);
        sinkWriterCurrentPosition = new ArrayList<>();
        currentPosition = new ArrayList<>();
    }

    public void write(String path, TypedValue value) throws IOException {
        var urEntities = getParsedTokens(path.split("/"));
        var sameEntitiesCount = getSameEntitiesCount(urEntities);
        var entitiesToBeClosedCount = currentPosition.size() - sameEntitiesCount;

        closeOpenEntities(entitiesToBeClosedCount);
        var containsRoot = currentPosition.isEmpty() ? true : false;
        openNewEntities(urEntities.stream().skip(sameEntitiesCount).collect(Collectors.toList()), containsRoot);
        writeProperty(value);
    }

    public void finishWriting() throws IOException {
        sinkWriter.finishWriting();
    }

    private void writeProperty(TypedValue value) throws IOException {
        addUrEntityOpening();
        // Write type
        sinkWriterCurrentPosition.add(
            new Entity<>(WriterAdapterEntityType.Property, Ur.KEY_TYPE));
        addPrivitiveOpening();
        sinkWriter.write(sinkWriterCurrentPosition, value.type());

        removeLastElements(sinkWriterCurrentPosition, 3);

        // Write value
        sinkWriterCurrentPosition.add(
            new Entity<>(WriterAdapterEntityType.Property, Ur.KEY_TYPE));
        addPrivitiveOpening();
        sinkWriter.write(sinkWriterCurrentPosition, value.value());

        removeLastElements(sinkWriterCurrentPosition, 6);
    }

    private void openNewEntities(List<Entity<UrEntityType>> newEntities, boolean isRoot) throws IOException {
        for (var entity : newEntities) {
            currentPosition.add(entity);
            switch (entity.type()) {
                case Object:
                    if (isRoot) {
                        addUrRootOpening();
                        isRoot = false;
                    }
                    else {
                        addUrEntityOpening();
                    }
                    writeType(Ur.VALUE_OBJECT);
                    break;
                case Array:
                    if (isRoot) {
                        addUrRootOpening();
                        isRoot = false;
                    }
                    else {
                        addUrEntityOpening();
                    }
                    writeType(Ur.VALUE_ARRAY);
                    break;
                case Property:
                    sinkWriterCurrentPosition.add(
                        new Entity<>(WriterAdapterEntityType.Property, entity.name()));
                    break;
                default:
                    break;
            }
        }
    }

    private void addUrRootOpening() {
        sinkWriterCurrentPosition.add(
            new Entity<WriterAdapterEntityType>(WriterAdapterEntityType.Object, null));
    }

    private void writeType(String typeValue) throws IOException {
        sinkWriterCurrentPosition.add(
            new Entity<WriterAdapterEntityType>(WriterAdapterEntityType.Property, Ur.KEY_TYPE));
        addPrivitiveOpening();

        sinkWriter.write(sinkWriterCurrentPosition, typeValue);

        removeLastElements(sinkWriterCurrentPosition, 3);
    }

    private void addPrivitiveOpening() {
        sinkWriterCurrentPosition.add(
            new Entity<WriterAdapterEntityType>(WriterAdapterEntityType.Array, null));
        sinkWriterCurrentPosition.add(
            new Entity<WriterAdapterEntityType>(WriterAdapterEntityType.ArrayItem, null));
    }

    private void addUrEntityOpening() {
        sinkWriterCurrentPosition.add(
            new Entity<WriterAdapterEntityType>(WriterAdapterEntityType.Array, null));
        sinkWriterCurrentPosition.add(
            new Entity<WriterAdapterEntityType>(WriterAdapterEntityType.ArrayItem, null));
        sinkWriterCurrentPosition.add(
            new Entity<WriterAdapterEntityType>(WriterAdapterEntityType.Object, null));
    }

    private void closeOpenEntities(int entitiesToBeClosedCount) {
        for (int i = 0; i < entitiesToBeClosedCount; i++) {
            var lastEntity = currentPosition.get(currentPosition.size() - 1);
            switch (lastEntity.type()) {
                case Object:
                case Array:
                    currentPosition.remove(currentPosition.size() - 1);
                    var elementsToRemoveCount = currentPosition.isEmpty() ? 1 : 3; 
                    removeLastElements(sinkWriterCurrentPosition, elementsToRemoveCount);
                    break;
                case Property:
                    currentPosition.remove(currentPosition.size() - 1);
                    removeLastElements(sinkWriterCurrentPosition, 1);
                    break;
                default:
                    // Unreachable code
                    throw new IllegalStateException("Unreachable");
            }
            currentPosition.remove(currentPosition.size() - 1);
        }
    }

    private void removeLastElements(List<?> list, int count) {
        while (count >= 0 && !list.isEmpty()) {
            list.remove(list.size() - 1);
            count--;
        }
    }

    private int getSameEntitiesCount(List<Entity<UrEntityType>> entities) {
        int count = 0;
        for (; count < entities.size() && count < currentPosition.size(); count++) {
            if (entities.get(count).equals(currentPosition.get(count))) {
                continue;
            }
            break;
        }
        return count;
    }

    private List<Entity<UrEntityType>> getParsedTokens(String[] pointerTokens) {
        ArrayList<Entity<UrEntityType>> entities = new ArrayList<>();
        // TODO what about root object??
        for (int i = 1; i < pointerTokens.length; i++) {
            var token = pointerTokens[i];
            if (tokenIsArray(token)) {
                entities.add(new Entity<>(UrEntityType.Array, null));
                entities.add(new Entity<>(UrEntityType.Property, getArrayIndexKey(token)));
            } 
            else { // is object property
                entities.add(new Entity<>(UrEntityType.Object, null));
                entities.add(new Entity<>(UrEntityType.Property, getKey(token)));
            }
        }

        return entities;
    }

    private String getKey(String token) {
        return token.replace("~1", "/")
            .replace("~2", "[")
            .replace("~3", "]")
            .replace("~0", "~");
    }

    private String getArrayIndexKey(String token) {
        return getKey(token.substring(1, token.length() - 1));
    }

    private boolean tokenIsArray(String token) {
        return (token.length() >= 2 
            && token.charAt(0) == '[' 
            && token.charAt(token.length() - 1) == ']');
    }
}
