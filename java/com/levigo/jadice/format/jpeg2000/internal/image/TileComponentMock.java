package com.levigo.jadice.format.jpeg2000.internal.image;


public class TileComponentMock extends TileComponent {
  public void setGridRegion(DefaultGridRegion gridElement) {
    this.gridRegion = gridElement;
  }

  public void setResolutions(Resolution[] resolutions) {
    this.resolutions = resolutions;
  }

  public void setNumResolutions(int numResolutions) {
    this.dwtLevels = numResolutions - 1;
  }
}
