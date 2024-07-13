package cz.cuni.mff.hdt.converter.rdf_trig;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.jena.graph.Graph;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.cuni.mff.hdt.converter.InputConverter;
import cz.cuni.mff.hdt.ur.Ur;

public class RdfTrigInputConverter implements InputConverter {

    @Override
    public Ur convert(InputStream input) throws IOException {
        Dataset dataset = DatasetFactory.create();
        RDFDataMgr.read(dataset, input, RDFLanguages.TRIG);
        JSONObject result = new JSONObject().put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_OBJECT));
        var knownResources = new HashMap<String, JSONObject>();
        var resourcesToCheck = new HashMap<String, Entry<JSONObject, String>>();
        
        // Iterate through named graphs in the dataset
        dataset.listNames().forEachRemaining(graphName -> {
            Graph graph = dataset.getNamedModel(graphName).getGraph();
            processGraph(graph, knownResources, resourcesToCheck, result);
        });

        // Process the default graph (if any)
        Graph defaultGraph = dataset.getDefaultModel().getGraph();
        processGraph(defaultGraph, knownResources, resourcesToCheck, result);

        //checkAndReplaceResources(knownResources, resourcesToCheck);

        return new Ur(result);
    }

    private static void processGraph(Graph graph, HashMap<String, JSONObject> knownResources, HashMap<String, Entry<JSONObject, String>> resourcesToCheck, JSONObject result) {
        Model model = ModelFactory.createModelForGraph(graph);
        ResIterator subjects = model.listSubjects();
        
        while (subjects.hasNext()) {
            Resource subject = subjects.next();
            // is blank node
            if (subject.getURI() == null) {
                continue;
            }
            JSONObject subjectJson = new JSONObject();
            var idLiteral = getIdLiteral(subject.getURI());
            subjectJson.put(Ur.KEY_RDF_ID, new JSONArray().put(idLiteral));
            subjectJson.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_OBJECT));
            knownResources.put(subject.getURI(), subjectJson);

            StmtIterator properties = subject.listProperties();
            convertProperties(properties, subjectJson, knownResources, resourcesToCheck);
            
            result.put(subject.getURI(), new JSONArray().put(subjectJson));
        }
    }

    private static JSONObject getIdLiteral(String uri) {
        var idLiteral = new JSONObject();
        idLiteral.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_STRING_URI));
        idLiteral.put(Ur.KEY_VALUE, new JSONArray().put(uri));
        return idLiteral;
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
                parent.put(propertyName, new JSONArray().put(knownResources.get(resourceId)));
            }
        }
    }

    private static void convertBlankNode(
        Resource blankNode,
        JSONObject blankNodeJson,
        HashMap<String, JSONObject> knownResources, 
        HashMap<String, Entry<JSONObject, String>> resourcesToCheck) {

        StmtIterator properties = blankNode.listProperties();
        convertProperties(properties, blankNodeJson, knownResources, resourcesToCheck);
    }

    private static void convertProperties(
        StmtIterator properties, 
        JSONObject parentJson, 
        HashMap<String, JSONObject> knownResources, 
        HashMap<String, Entry<JSONObject, String>> resourcesToCheck) {
        
        var propertyCounts = new HashMap<String, Integer>();
        while (properties.hasNext()) {
            Statement stmt = properties.next();
            Property property = stmt.getPredicate();
            RDFNode object = stmt.getObject();
            
            String propertyUri = property.getURI();

            var outputObject = new JSONObject().put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_OBJECT));
            if (object.isLiteral()) {
                Literal literal = object.asLiteral();
                outputObject.put(Ur.KEY_VALUE, new JSONArray().put(literal.getLexicalForm()));

                if (literal.getDatatypeURI() != null && !literal.getLanguage().isEmpty()) {
                    outputObject.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_LANG_STRING_URI));
                    var langPrimitive = new JSONObject()
                        .put(Ur.KEY_VALUE, new JSONArray().put(literal.getLanguage()))
                        .put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_STRING));
                    outputObject.put(Ur.KEY_RDF_LANGUAGE, new JSONArray().put(langPrimitive));
                } 
                else if (literal.getDatatypeURI() != null) {
                    outputObject.put(Ur.KEY_TYPE, new JSONArray().put(literal.getDatatypeURI()));
                } 
                else {
                    outputObject.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_STRING_URI));
                }
            } 
            else if (object.isResource()) {
                Resource resource = object.asResource();

                if (resource.isAnon()) {
                    convertBlankNode(resource, outputObject, knownResources, resourcesToCheck);
                } 
                else if (false && knownResources.containsKey(resource.getURI())) {
                    outputObject = knownResources.get(resource.getURI());
                } 
                else {
                    var idLiteral = getIdLiteral(resource.getURI());
                    outputObject.put(Ur.KEY_RDF_ID, new JSONArray().put(idLiteral));
                    resourcesToCheck.put(resource.getURI(), new SimpleEntry<>(parentJson, propertyUri));
                }
            }
            

            if (propertyCounts.containsKey(propertyUri)) { // RDF TTL array
                var count = propertyCounts.get(propertyUri);
                var presentValue = parentJson.getJSONArray(propertyUri).getJSONObject(0);
                if (count == 1) {
                    var propertyObject = new JSONObject();
                    propertyObject.put(Ur.KEY_TYPE, new JSONArray().put(Ur.VALUE_ARRAY));
                    propertyObject.put("0", new JSONArray().put(presentValue));
                    propertyObject.put(count.toString(), new JSONArray().put(outputObject));
                    parentJson.put(propertyUri, new JSONArray().put(propertyObject));
                }
                else {
                    presentValue.put(count.toString(), new JSONArray().put(outputObject));
                }
                
                propertyCounts.put(propertyUri, count + 1);
            } 
            else {
                propertyCounts.put(propertyUri, 1);
                JSONArray propertyArray = new JSONArray();
                propertyArray.put(outputObject);
                parentJson.put(propertyUri, propertyArray);                
            }
        }
    }
}
