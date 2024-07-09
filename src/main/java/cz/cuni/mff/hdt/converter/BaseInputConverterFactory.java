package cz.cuni.mff.hdt.converter;

import java.util.Optional;

import cz.cuni.mff.hdt.converter.csv.CsvInputConverter;
import cz.cuni.mff.hdt.converter.json.JsonInputConverter;
import cz.cuni.mff.hdt.converter.rdf_trig.RdfTrigInputConverter;
import cz.cuni.mff.hdt.converter.rdf_ttl.RdfTtlInputConverter;
import cz.cuni.mff.hdt.converter.xml.XmlInputConverter;

/**
 * Base factory class to create different types of input converters based on their name.
 */
public class BaseInputConverterFactory implements InputConverterFactory {
    public static final String JSON_DEFAULT_INPUT_CONVERTER = "json-default";
    public static final String XML_DEFAULT_INPUT_CONVERTER = "xml-default";
    public static final String CSV_DEFAULT_INPUT_CONVERTER = "csv-default";
    public static final String RDF_TTL_DEFAULT_INPUT_CONVERTER = "rdf-ttl-default";
    public static final String RDF_TRIG_DEFAULT_INPUT_CONVERTER = "rdf-trig-default";

    /**
     * Create supported input converter or return empty Optional
     */
    @Override
    public Optional<InputConverter> create(String converterName) {
        switch (converterName) {
            case JSON_DEFAULT_INPUT_CONVERTER:
                return Optional.of(new JsonInputConverter());
            case XML_DEFAULT_INPUT_CONVERTER:
                return Optional.of(new XmlInputConverter());
            case CSV_DEFAULT_INPUT_CONVERTER:
                return Optional.of(new CsvInputConverter());
            case RDF_TTL_DEFAULT_INPUT_CONVERTER:
                return Optional.of(new RdfTtlInputConverter());
            case RDF_TRIG_DEFAULT_INPUT_CONVERTER:
                return Optional.of(new RdfTrigInputConverter());
            default:
                return Optional.empty();
        }
    }
}
