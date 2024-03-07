package cz.cuni.mff.hdt.source;

interface DocumentSource {

  /**
   * Return next EntityReference or null when there is nothing more to iterate.
   */
  Reference next();

}