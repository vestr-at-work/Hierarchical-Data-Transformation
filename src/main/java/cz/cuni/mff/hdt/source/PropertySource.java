package cz.cuni.mff.hdt.source;

interface PropertySource {

  /**
   * Return string value of a referecne.
   */
  String value(ValueReference reference);

}