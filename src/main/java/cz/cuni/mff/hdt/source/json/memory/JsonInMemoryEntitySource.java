package cz.cuni.mff.hdt.source.json.memory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.cuni.mff.hdt.reference.ArrayReference;
import cz.cuni.mff.hdt.reference.EntityReference;
import cz.cuni.mff.hdt.reference.json.memory.JsonInMemoryArrayReference;
import cz.cuni.mff.hdt.reference.json.memory.JsonInMemoryEntityReference;
import cz.cuni.mff.hdt.reference.json.memory.JsonInMemoryValueReference;
import cz.cuni.mff.hdt.source.ArraySource;
import cz.cuni.mff.hdt.source.EntitySource;
import cz.cuni.mff.hdt.ur.Ur;

public class JsonInMemoryEntitySource implements EntitySource {

    protected JsonInMemoryDocumentSource documentSource;

    public JsonInMemoryEntitySource(JsonInMemoryDocumentSource documentSource) {
        this.documentSource = documentSource;
    }

    @Override
    public ArrayReference property(EntityReference reference, String key) {
        if (!(reference instanceof JsonInMemoryEntityReference)) {
            throw new IllegalArgumentException("Passed reference is not compatible with JsonInMemoryEntitySource");
        }

        var jsonEntityReference = (JsonInMemoryEntityReference)reference;
        Object property;
        try {
            property = jsonEntityReference.object.get(key);
        }
        catch (JSONException e) {
            return null;
        }

        if (property instanceof JSONObject) {
            var object = (JSONObject)property;
            object.put(Ur.KEY_TYPE, Ur.VALUE_OBJECT);
            return new JsonInMemoryArrayReference(new JsonInMemoryEntityReference(object));
        }
        else if (property instanceof JSONArray) {
            var array = (JSONArray)property;
            var entity = documentSource.makeUrObjectFromArray(array);

            return new JsonInMemoryArrayReference(entity);
        }
        else if (property instanceof String) {
            var value = (String)property;
            if (key.equals(Ur.KEY_TYPE) || key.equals(Ur.KEY_VALUE)) {
                return new JsonInMemoryArrayReference(new JsonInMemoryValueReference(value));
            }

            var object = new JSONObject();
            object.put(Ur.KEY_TYPE, Ur.VALUE_STRING);
            object.put(Ur.KEY_VALUE, value);

            return new JsonInMemoryArrayReference(new JsonInMemoryEntityReference(object));
        }
        else {
            throw new InternalError("Unsupported type in the reference");
        }

    }

    @Override
    public ArrayReference items(EntityReference reference) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'items'");
    }

    @Override
    public ArraySource getSourceFromReference(ArrayReference referece) {
        return documentSource.getArraySource();
    }

    @Override
    public EntitySource getSourceFromReference(EntityReference entityReferece) {
        return this;
    }
    
}
