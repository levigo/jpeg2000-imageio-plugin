package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.io.SectorInputStream;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;

public class PPT extends AbstractMarkerSegment implements Comparable<PPT> {

  public int Lppt;
  public int Zppt;
  public SectorInputStream Ippt;

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException {
    Lppt = source.readUnsignedShort();
    Zppt = source.readUnsignedByte();
    Ippt = new SectorInputStream(source, source.getStreamPosition(), Lppt - 3);
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Lppt);
    sink.writeByte(Zppt);
    int read = 0;
    final byte buffer[] = new byte[65535];
    while ((read = Ippt.read(buffer)) > 0) {
      sink.write(buffer, 0, read);
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {

  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Lppt", Lppt, "TODO"));
    markerInfo.add(new PropertiesParameterInfo("Zppt", Zppt, "TODO"));
    markerInfo.add(new PropertiesParameterInfo("Ippt (length)", Ippt.length(), "TODO"));
  }

  @Override
  public Marker getMarker() {
    return Marker.PPT;
  }

  @Override
  public int compareTo(PPT ppt) {
    return this.Zppt - ppt.Zppt;
  }
}
