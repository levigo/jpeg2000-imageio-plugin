package com.levigo.jadice.format.jpeg2000.internal.image;

import java.awt.Point;

public class Pair {

  public static Pair p(final int x, final int y) {
    return new Pair(x, y);
  }

  public int x;
  public int y;

  /**
   * Default constructor without any initialization.
   */
  public Pair() {
    // nothing to do
  }

  public Pair(final int x, final int y) {
    this.x = x;
    this.y = y;
  }

  public Pair minus(Pair subtrahend) {
    return new Pair(x - subtrahend.x, y - subtrahend.y);
  }

  public void subtract(Pair subtract) {
    x -= subtract.x;
    y -= subtract.y;
  }

  public Pair plus(Pair add) {
    return new Pair(x + add.x, y + add.y);
  }

  public void add(Pair addend) {
    x += addend.x;
    y += addend.y;
  }

  public Pair inverse() {
    return new Pair(-x, -y);
  }

  public void invert() {
    x = -x;
    y = -y;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    final Pair pair = (Pair) o;

    if (x != pair.x) {
      return false;
    }
    if (y != pair.y) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString() {
    return "(" + x + ", " + y + ")";
  }

  @Override
  protected Pair clone() {
    return new Pair(x, y);
  }

  public Point toPoint() {
    return new Point(x, y);
  }
}
