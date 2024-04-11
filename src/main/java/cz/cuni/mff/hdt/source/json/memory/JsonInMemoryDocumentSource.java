package cz.cuni.mff.hdt.source.json.memory;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.EntityReference;
import cz.cuni.mff.hdt.reference.Reference;
import cz.cuni.mff.hdt.reference.json.memory.JsonInMemoryEntityReference;
import cz.cuni.mff.hdt.source.ArraySource;
import cz.cuni.mff.hdt.source.DocumentSource;
import cz.cuni.mff.hdt.source.EntitySource;
import cz.cuni.mff.hdt.ur.Ur;

public class JsonInMemoryDocumentSource implements DocumentSource {
    // we have to have the root object/array saved
    protected boolean rootIsArray = false;
    protected JSONArray rootArray = null;
    protected JSONObject rootObject = null;

    protected boolean rootRead = false;

    protected JsonInMemoryEntitySource entitySource = new JsonInMemoryEntitySource(this);
    protected JsonInMemoryArraySource arraySource = new JsonInMemoryArraySource(this);
    protected JsonInMemoryValueSource valueSource = new JsonInMemoryValueSource();

    public JsonInMemoryArraySource getArraySource() {
        return arraySource;
    }

    public JsonInMemoryEntitySource getEntitySource() {
        return entitySource;
    }

    public JsonInMemoryValueSource getValueSource() {
        return valueSource;
    }

    public JsonInMemoryDocumentSource(String jsonString) {
        if (jsonString.length() > 1 && jsonString.charAt(0) == '[') {
            rootArray = new JSONArray(jsonString);
            rootIsArray = true;
            return;
        }
        rootObject = new JSONObject(jsonString);
    }

    @Override
    public Reference next() {
        if (rootRead) {
            return null;
        }
        EntityReference entity = getRootEntityReference();
        rootRead = true;
        return entity;
    }

    @Override
    public EntitySource getSourceFromReference(EntityReference entityReferece) {
        return entitySource;
    }

    @Override
    public ArraySource getSourceFromReference(ArrayReference referece) {
        return arraySource;
    }

    protected EntityReference getRootEntityReference() {
        if (rootIsArray) {
            return makeUrObjectFromArray(rootArray);
        }
        rootObject.append(Ur.KEY_TYPE, Ur.VALUE_OBJECT);
        return new JsonInMemoryEntityReference(rootObject);
    }

    // TODO this should probably be somewhere else
    public EntityReference makeUrObjectFromArray(JSONArray array) {
        JSONObject urArrayObject = new JSONObject();
        urArrayObject.put(Ur.KEY_TYPE, Ur.VALUE_ARRAY);

        Integer index = 0;
        for (var item : array) {
            urArrayObject.put(index.toString(), item);
            index++;
        }

        return new JsonInMemoryEntityReference(urArrayObject);
    }
    
}
