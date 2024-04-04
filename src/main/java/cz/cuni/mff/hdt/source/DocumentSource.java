package cz.cuni.mff.hdt.source;

public interface DocumentSource {

  /**
   * Return next EntityReference or null when there is nothing more to iterate.
   */
  public Reference next();

  /**
   * Get EntitySource from EntityReference.
   * 
   * @param entityReferece EntityReference from which we want to make the source.
   * @return EntitySource from the given EntityReference.
   */
  public EntitySource getSourceFromReference(EntityReference entityReferece);

  /**
   * Get ArraySource from ArrayReference.
   * 
   * @param referece ArrayReference from which we want to make the source.
   * @return ArraySource from the given ArrayReference.
   */
  public ArraySource getSourceFromReference(ArrayReference referece);

}