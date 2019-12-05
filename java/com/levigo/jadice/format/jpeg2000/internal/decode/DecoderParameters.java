package com.levigo.jadice.format.jpeg2000.internal.decode;

import com.levigo.jadice.format.jpeg2000.internal.image.Region;

public class DecoderParameters {

  /**
   * Flag indicating if codestream validation should be performed.
   */
  public boolean validate;

  /**
   * Depicts the region of interest.
   */
  public Region region;

  public DecoderParameters() {
    validate = false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;

    DecoderParameters that = (DecoderParameters) o;

    if (validate != that.validate)
      return false;
    return !(region != null ? !region.equals(that.region) : that.region != null);

  }
  @Override
  public int hashCode() {
    int result = (validate ? 1 : 0);
    result = 31 * result + (region != null ? region.hashCode() : 0);
    return result;
  }
}
