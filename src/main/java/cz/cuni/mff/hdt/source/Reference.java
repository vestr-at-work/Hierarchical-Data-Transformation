package cz.cuni.mff.hdt.source;

interface Reference {
  /**
   * Anounce that we no longer need this reference.
   */
  void close();
}