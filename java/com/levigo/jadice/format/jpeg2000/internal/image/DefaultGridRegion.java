package com.levigo.jadice.format.jpeg2000.internal.image;

public class DefaultGridRegion implements GridRegion {

  private Region absoluteRegion;

  public DefaultGridRegion(Region absoluteRegion) {
    this.absoluteRegion = absoluteRegion;
  }

  @Override
  public Region absolute() {
    return absoluteRegion;
  }

  @Override
  public Region relativeTo(GridRegion base) {
    return Regions.dispose(absoluteRegion, base.absolute().pos);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("{absolute=").append(absoluteRegion).append("}");
    return sb.toString();
  }
}
