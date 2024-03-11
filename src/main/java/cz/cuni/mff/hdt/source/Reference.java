package cz.cuni.mff.hdt.source;

public interface Reference {
  /**
   * Anounce that we no longer need this reference.
   */
  public void close();
}