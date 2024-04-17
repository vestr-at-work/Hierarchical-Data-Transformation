package cz.cuni.mff.hdt.adapter;
    
/**
 * Record holding information about entity 
 */
public record Entity<EntityType>(EntityType type, String name) {
    @Override
    public final boolean equals(Object other) {
        if (other instanceof Entity) {
            var bothValuesNull = ((Entity<?>)other).name == null && this.name == null;
            var noValuesNull =  (((Entity<?>)other).name != null && this.name != null);
            return ( bothValuesNull || ((noValuesNull) && ((Entity<?>)other).name.equals(this.name)))
                && ((Entity<?>)other).type.equals(this.type);
        }
        return false;
    }
}
