package cz.cuni.mff.hdt.sink.xml;

import org.junit.jupiter.api.*;
import java.util.*;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class XmlSinkTest {
    @Test
    public void testComplexXmlHeaderAndAttributeAndListItemsPrettyPrint() {
        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<person xml:lang=\"en\">\n  <name>\n    Ailish\n  </name>\n  <list>\n    <item>\n      apple\n    </item>\n    <item>\n      pear\n    </item>\n  </list>\n</person>";

        var writer = new StringWriter();
        var sink = new XmlSink(writer, true);

        try {
            sink.openObject();

            // xml header
            sink.setNextKey("@version");
            sink.openArray();
            sink.writeValue("1.0");
            sink.closeArray();
            sink.setNextKey("@encoding");
            sink.openArray();
            sink.writeValue("UTF-8");
            sink.closeArray();

            // open person object
            sink.setNextKey("person");
            sink.openArray();
            sink.openObject();

            // attributes
            sink.setNextKey("@attributes");
            sink.openArray();
            sink.openObject();
            // xml:lang start
            sink.setNextKey("xml:lang");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("en");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            // xml:lang end
            sink.closeObject();
            sink.closeArray();

            // person type
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // name property
            sink.setNextKey("name");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("Ailish");
            sink.closeArray();
            sink.closeObject();

            // open list object
            sink.setNextKey("list");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // first list item
            sink.setNextKey("@1:item");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("apple");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();

            // second list item
            sink.setNextKey("@2:item");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("pear");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            
            // close list object
            sink.closeObject();
            sink.closeArray();

            // close person object
            sink.closeObject();
            sink.closeArray();

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {
            fail(e);
        }
        var result = writer.toString();

        assertEquals(expectedResult, result);
    }

    @Test
    public void testComplexXmlHeaderAndAttributeAndListItems() {
        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><person xml:lang=\"en\"><name>Ailish</name><list><item>apple</item><item>pear</item></list></person>";

        var writer = new StringWriter();
        var sink = new XmlSink(writer, false);

        try {
            sink.openObject();

            // xml header
            sink.setNextKey("@version");
            sink.openArray();
            sink.writeValue("1.0");
            sink.closeArray();
            sink.setNextKey("@encoding");
            sink.openArray();
            sink.writeValue("UTF-8");
            sink.closeArray();

            // open person object
            sink.setNextKey("person");
            sink.openArray();
            sink.openObject();

            // attributes
            sink.setNextKey("@attributes");
            sink.openArray();
            sink.openObject();
            // xml:lang start
            sink.setNextKey("xml:lang");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("en");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            // xml:lang end
            sink.closeObject();
            sink.closeArray();

            // person type
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // name property
            sink.setNextKey("name");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("Ailish");
            sink.closeArray();
            sink.closeObject();

            // open list object
            sink.setNextKey("list");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // first list item
            sink.setNextKey("@1:item");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("apple");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();

            // second list item
            sink.setNextKey("@2:item");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("pear");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            
            // close list object
            sink.closeObject();
            sink.closeArray();

            // close person object
            sink.closeObject();
            sink.closeArray();

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {
            fail(e);
        }
        var result = writer.toString();

        assertEquals(expectedResult, result);
    }

    @Test
    public void testXmlHeaderSimpleObject() {
        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><person><name>Ailish</name></person>";

        var writer = new StringWriter();
        var sink = new XmlSink(writer, false);

        try {
            sink.openObject();

            // xml header
            sink.setNextKey("@version");
            sink.openArray();
            sink.writeValue("1.0");
            sink.closeArray();
            sink.setNextKey("@encoding");
            sink.openArray();
            sink.writeValue("UTF-8");
            sink.closeArray();

            // open person object
            sink.setNextKey("person");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // name property
            sink.setNextKey("name");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("Ailish");
            sink.closeArray();
            sink.closeObject();

            // close person object
            sink.closeArray();
            sink.closeObject();

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {
            fail(e);
        }
        var result = writer.toString();

        assertEquals(expectedResult, result);
    }

    @Test
    public void testSimpleObject() {
        String expectedResult = "<person><name>Ailish</name></person>";

        var writer = new StringWriter();
        var sink = new XmlSink(writer, false);

        try {
            sink.openObject();

            // open person object
            sink.setNextKey("person");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // name property
            sink.setNextKey("name");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("Ailish");
            sink.closeArray();
            sink.closeObject();

            // close person object
            sink.closeArray();
            sink.closeObject();

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {
            fail(e);
        }
        var result = writer.toString();

        assertEquals(expectedResult, result);
    }

    @Test
    public void testSimpleObjectWithList() {
        String expectedResult = "<person><list><item>apple</item><item>pear</item></list></person>";

        var writer = new StringWriter();
        var sink = new XmlSink(writer, false);

        try {
            sink.openObject();

            // open person object
            sink.setNextKey("person");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // open list object
            sink.setNextKey("list");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // first list item
            sink.setNextKey("@1:item");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("apple");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();

            // second list item
            sink.setNextKey("@2:item");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("pear");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            
            // close list object
            sink.closeObject();
            sink.closeArray();

            // close person object
            sink.closeObject();
            sink.closeArray();

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {
            fail(e);
        }
        var result = writer.toString();

        assertEquals(expectedResult, result);
    }

    @Test
    public void testSimpleObjectWithAttributes() {
        String expectedResult = "<person xml:lang=\"en\"><name>Ailish</name></person>";

        var writer = new StringWriter();
        var sink = new XmlSink(writer, false);

        try {
            sink.openObject();

            // open person object
            sink.setNextKey("person");
            sink.openArray();
            sink.openObject();

            // attributes
            sink.setNextKey("@attributes");
            sink.openArray();
            sink.openObject();
            // xml:lang start
            sink.setNextKey("xml:lang");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("en");
            sink.closeArray();
            sink.closeObject();
            sink.closeArray();
            // xml:lang end
            sink.closeObject();
            sink.closeArray();

            // person type
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("object");
            sink.closeArray();

            // name property
            sink.setNextKey("name");
            sink.openArray();
            sink.openObject();
            sink.setNextKey("@type");
            sink.openArray();
            sink.writeValue("string");
            sink.closeArray();
            sink.setNextKey("@value");
            sink.openArray();
            sink.writeValue("Ailish");
            sink.closeArray();
            sink.closeObject();

            // close person object
            sink.closeObject();
            sink.closeArray();

            sink.closeObject();
            sink.flush();
        }
        catch (Exception e) {
            fail(e);
        }
        var result = writer.toString();

        assertEquals(expectedResult, result);
    }
}