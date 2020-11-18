package org.neo4j.hop.model;

import org.apache.hop.metadata.api.HopMetadataProperty;

public class GraphPresentation {

  @HopMetadataProperty
  protected int x;

  @HopMetadataProperty
  protected int y;

  public GraphPresentation() {
  }

  public GraphPresentation( int x, int y ) {
    this();
    this.x = x;
    this.y = y;
  }

  public GraphPresentation clone() {
    return new GraphPresentation( x, y );
  }

  /**
   * Gets x
   *
   * @return value of x
   */
  public int getX() {
    return x;
  }

  /**
   * @param x The x to set
   */
  public void setX( int x ) {
    this.x = x;
  }

  /**
   * Gets y
   *
   * @return value of y
   */
  public int getY() {
    return y;
  }

  /**
   * @param y The y to set
   */
  public void setY( int y ) {
    this.y = y;
  }
}
