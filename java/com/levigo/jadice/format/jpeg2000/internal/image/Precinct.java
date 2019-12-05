package com.levigo.jadice.format.jpeg2000.internal.image;

/**
 * FIXME 
 * 
 * Mitschrieb aus Kurz-Übergabe:
 * - Precinct beginnt immer bei 0/0 des reference grid
 * - Ein Packet steuert alle Daten aller Codeblöcke eines Precicts bei
 */
public class Precinct implements HasGridRegion {

  public int idx;
  public int x;
  public int y;
  
  public Resolution resolution;

  protected GridRegion gridRegion; 
  
  protected Precinct() {
    
  }
  
  public Precinct(int idx, int x, int y, Resolution resolution) {
    this.idx = idx;
    this.x = x;
    this.y = y;
    this.resolution = resolution;

    gridRegion = createGridRegion();
  }

  private GridRegion createGridRegion() {
    final Region precinctPartition = resolution.precinctPartition;
    final Region precinctRegion = Regions.replicate(precinctPartition, x, y, resolution.region().absolute());
    return new DefaultGridRegion(precinctRegion);
  }

  @Override
  public GridRegion region() {
    return gridRegion;
  }
}
