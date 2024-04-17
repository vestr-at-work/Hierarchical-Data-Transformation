package cz.cuni.mff.hdt.source;

import cz.cuni.mff.hdt.reference.PropertyReference;

public interface PropertySource {

    /*
     * Returns the value of the property.
     */
    public String value(PropertyReference reference);

    /*
     * Returns the type of the property.
     */
    public String type(PropertyReference reference);
}
