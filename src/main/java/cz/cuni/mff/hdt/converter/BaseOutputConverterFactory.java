package cz.cuni.mff.hdt.converter;

import java.util.Optional;

import cz.cuni.mff.hdt.converter.csv.CsvOutputConverter;
import cz.cuni.mff.hdt.converter.json.JsonOutputConverter;
import cz.cuni.mff.hdt.converter.ur_inner.UrInnerOutputConverter;
import cz.cuni.mff.hdt.converter.xml.XmlOutputConverter;

/**
 * Base factory class to create different types of output converters based on their name.
 */
public class BaseOutputConverterFactory implements OutputConverterFactory {
    public static final String JSON_DEFAULT_OUTPUT_CONVERTER = "json-default";
    public static final String XML_DEFAULT_OUTPUT_CONVERTER = "xml-default";
    public static final String CSV_DEFAULT_OUTPUT_CONVERTER = "csv-default";
    public static final String RDF_TTL_DEFAULT_OUTPUT_CONVERTER = "rdf-ttl-default";
    public static final String RDF_TRIG_DEFAULT_OUTPUT_CONVERTER = "rdf-trig-default";
    public static final String UR_INNER_OUTPUT_CONVERTER = "ur-inner";

    /**
     * Create supported output converter or return empty Optional
     */
    @Override
    public Optional<OutputConverter> create(String converterName) {
        switch (converterName) {
            case JSON_DEFAULT_OUTPUT_CONVERTER:
                return Optional.of(new JsonOutputConverter());
            case XML_DEFAULT_OUTPUT_CONVERTER:
                return Optional.of(new XmlOutputConverter());
            case CSV_DEFAULT_OUTPUT_CONVERTER:
                return Optional.of(new CsvOutputConverter());
            case RDF_TTL_DEFAULT_OUTPUT_CONVERTER:
                //return Optional.of(new RdfTtlOutputConverter());
                throw new UnsupportedOperationException("Unimplemented RDF Turtle OutputConverter");
            case RDF_TRIG_DEFAULT_OUTPUT_CONVERTER:
                //return Optional.of(new RdfTrigOutputConverter());
                throw new UnsupportedOperationException("Unimplemented RDF Trig OutputConverter");
            case UR_INNER_OUTPUT_CONVERTER:
                return Optional.of(new UrInnerOutputConverter());
            default:
                return Optional.empty();
        }
    }
}
