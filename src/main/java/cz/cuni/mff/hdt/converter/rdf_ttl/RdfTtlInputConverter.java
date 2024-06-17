package cz.cuni.mff.hdt.converter.rdf_ttl;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.converter.InputConverter;
import cz.cuni.mff.hdt.ur.Ur;

public class RdfTtlInputConverter implements InputConverter {

    @Override
    public Ur convert(InputStream input) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, input, RDFLanguages.TURTLE);

        JSONObject result = new JSONObject();
        var knownResources = new HashMap<String, JSONObject>();
        var resourcesToCheck = new HashMap<String, Entry<JSONObject, String>>();
        
        // Iterate through subjects in the model
        ResIterator subjects = model.listSubjects();
        while (subjects.hasNext()) {
            Resource subject = subjects.next();
            // is blank node
            if (subject.getURI() == null) {
                continue;
            }
            JSONObject subjectJson = new JSONObject();
            subjectJson.put(Ur.KEY_RDF_ID, new JSONArray().put(subject.getURI()));
            knownResources.put(subject.getURI(), subjectJson);

            StmtIterator properties = subject.listProperties();
            convertProperties(properties, subjectJson, knownResources, resourcesToCheck);
            
            result.put(subject.getURI(), new JSONArray().put(subjectJson));
        }

        checkAndReplaceResources(knownResources, resourcesToCheck);

        return new Ur(result);
    }

    private static void checkAndReplaceResources(
        HashMap<String, JSONObject> knownResources, 
        HashMap<String, Entry<JSONObject, String>> resourcesToCheck) {
        
        for (var resourceToCheck : resourcesToCheck.entrySet()) {
            if (knownResources.containsKey(resourceToCheck.getKey())) {
                var pair = resourceToCheck.getValue();
                var parent = pair.getKey();
                var propertyName = pair.getValue();
                var resourceId = resourceToCheck.getKey();
                
                // replace the resource with its data
                parent.put(propertyName, knownResources.get(resourceId));
            }
        }
    }

    private static JSONObject convertBlankNode(
        Resource blankNode, 
        HashMap<String, JSONObject> knownResources, 
        HashMap<String, Entry<JSONObject, String>> resourcesToCheck) {

        var blankNodeJson = new JSONObject();
        StmtIterator properties = blankNode.listProperties();
        convertProperties(properties, blankNodeJson, knownResources, resourcesToCheck);
        return blankNodeJson;
    }

    private static void convertProperties(StmtIterator properties, 
        JSONObject outputJson, 
        HashMap<String, JSONObject> knownResources, 
        HashMap<String, Entry<JSONObject, String>> resourcesToCheck) {

        while (properties.hasNext()) {
            Statement stmt = properties.next();
            Property property = stmt.getPredicate();
            RDFNode object = stmt.getObject();
            
            String propertyUri = property.getURI();
            
            if (!outputJson.has(propertyUri)) {
                outputJson.put(propertyUri, new JSONArray());
            }
            JSONArray propertyArray = outputJson.getJSONArray(propertyUri);

            if (object.isLiteral()) {
                JSONObject literalObject = new JSONObject();
                Literal literal = object.asLiteral();
                literalObject.put(Ur.KEY_VALUE, new JSONArray().put(literal.getLexicalForm()));

                if (literal.getDatatypeURI() != null && !literal.getLanguage().isEmpty()) {
                    literalObject.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_LANG_STRING_URI));
                    literalObject.put(Ur.KEY_RDF_LANGUAGE, new JSONArray().put(literal.getLanguage()));
                } else if (literal.getDatatypeURI() != null) {
                    literalObject.put(Ur.KEY_TYPE, new JSONArray().put(literal.getDatatypeURI()));
                } else {
                    literalObject.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_STRING_URI));
                }

                propertyArray.put(literalObject);
            } else if (object.isResource()) {
                JSONObject resourceObject = new JSONObject();
                Resource resource = object.asResource();

                if (resource.isAnon()) {
                    resourceObject = convertBlankNode(resource, knownResources, resourcesToCheck);
                } else if (knownResources.containsKey(resource.getURI())) {
                    resourceObject = knownResources.get(resource.getURI());
                } else {
                    resourceObject.put(Ur.KEY_RDF_ID, new JSONArray().put(resource.getURI()));
                    resourcesToCheck.put(resource.getURI(), new SimpleEntry<>(outputJson, property.getURI()));
                }

                propertyArray.put(resourceObject);
            }
        }
    }
}
