package cz.cuni.mff.hdt.reference.json.memory;

import org.json.JSONObject;

import cz.cuni.mff.hdt.reference.EntityReference;

public class JsonInMemoryEntityReference implements EntityReference {
    public JSONObject object;

    public JsonInMemoryEntityReference(JSONObject object) {
        this.object = object;
    }

    @Override
    public void close() {
        // Do nothing
    }
    
}
