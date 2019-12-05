package com.levigo.jadice.format.jpeg2000.internal.marker;

public class MarkerKey {

  public static final int UNDEFINED_ID = -1;

  public int markerCode;
  public int id;

  public MarkerKey(Marker marker) {
    this(marker, UNDEFINED_ID);
  }

  public MarkerKey(Marker marker, int id) {
    markerCode = marker.code;
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    MarkerKey markerKey = (MarkerKey) o;

    if (id != markerKey.id) {
      return false;
    }
    if (markerCode != markerKey.markerCode) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = markerCode;
    result = 31 * result + id;
    return result;
  }

  @Override
  public String toString() {
    return "MarkerKey{" + "markerCode=" + markerCode + ", id=" + id + '}';
  }
}
