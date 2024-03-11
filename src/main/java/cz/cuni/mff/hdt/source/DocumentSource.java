package cz.cuni.mff.hdt.source;

public interface DocumentSource {

  /**
   * Return next EntityReference or null when there is nothing more to iterate.
   */
  public Reference next();

}