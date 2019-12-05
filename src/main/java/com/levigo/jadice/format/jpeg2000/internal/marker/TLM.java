/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.levigo.jadice.format.jpeg2000.internal.marker;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.internal.codestream.Codestream;
import com.levigo.jadice.format.jpeg2000.internal.param.DirectParameterInfo;
import com.levigo.jadice.format.jpeg2000.internal.param.Parameters;
import com.levigo.jadice.format.jpeg2000.internal.param.PropertiesParameterInfo;
import com.levigo.jadice.format.jpeg2000.msg.CodestreamMessages;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Defined in <i>ITU-T.800, A.7.1</i>.
 * <p>
 * <b>Function:</b> Describes the length of every tile-part in the codestream. Each tile-part's length is measured from
 * the first byte of the {@link SOT} marker segment to the end of the bit-stream data of that tile-part. The value of
 * each individual tile-part length in the TLM marker segment is the same as the value in the corresponding {@link
 * SOT#Psot} in the SOT marker segment.
 * <p>
 * <b>Usage:</b> Main header. Optional use in the main header only. There may be multiple {@link TLM} marker segments
 * in the main header.
 * <p>
 * <b>Length:</b> Variable depending on the number of tile-parts in the codestream.
 */
public class TLM extends AbstractMarkerSegment {

  /**
   * Length of marker segment in bytes (not including the marker). The value of this parameter is determined by
   * equation <i>ITU-T.800, (A-7)</i>.
   */
  public int Ltlm;

  /**
   * Index of this marker segment relative to all other {@link TLM} marker segments present in the <b>current</b>
   * header. The sequence of <code>(Ttlm<sup>i</sup>, Ptlm<sup>i</sup>)</code> pairs from this marker segment is
   * concatenated, in order of increasing {@link #Ztlm}, with the sequences of pairs from other marker segments. The
   * jth entry in the resulting list contains the tile index and tile-part length pair for the jth tile-part appearing
   * in the codestream.
   */
  public int Ztlm;

  /** Size of the {@link #Ttlm} and {@link #Ptlm} parameters. */
  public int Stlm;

  /**
   * Tile index of the ith tile-part. Either none or one value for every tile-part. The number of tile-parts in each
   * tile can be derived from this marker segment (or the concatenated list of all such markers) or from a non-zero
   * {@link SOT#TNsot} parameter, if present.
   */
  public List<Integer> Ttlm;

  /**
   * Length, in bytes, from the beginning of the {@link SOT} marker of the ith tile-part to the end of the bit stream
   * data for that tile-part. One value for every tile-part.
   */
  public List<Integer> Ptlm;

  @Override
  public Marker getMarker() {
    return Marker.TLM;
  }

  @Override
  public MarkerKey getMarkerKey() {
    return new MarkerKey(getMarker(), Ztlm);
  }

  @Override
  protected void read(ImageInputStream source, Codestream codestream) throws IOException, JPEG2000Exception {
    Ltlm = source.readUnsignedShort();
    Ztlm = source.readUnsignedByte();
    Stlm = source.readUnsignedByte();

    final int numTtlmBits = numTtlmBits(Parameters.extract(Stlm, MASK_ST));
    if(numTtlmBits > 0) {
      Ttlm = new LinkedList<>();
    }
    
    final int numPtlmBits = numPtlmBits(Parameters.extract(Stlm, MASK_SP));
    Ptlm = new LinkedList<>();
    
    int remainingBits = (Ltlm - 4) << 3;
    while (remainingBits > 0) {
      if (numTtlmBits > 0) {
        Ttlm.add((int) (source.readBits(numTtlmBits) & 0xFFFF));
        remainingBits -= numTtlmBits;
      }
      Ptlm.add((int) source.readBits(numPtlmBits));
      remainingBits -= numPtlmBits;
    }
  }
  
  private int numTtlmBits(final int st) throws JPEG2000Exception {
    switch (st){
      case VALUE_T_SIZE_8:
        return 8;
      case VALUE_T_SIZE_16:
        return 16;
      case VALUE_T_SIZE_0:
        return 0;
    }

    throw new JPEG2000Exception(CodestreamMessages.ILLEGAL_PARAMETER_VALUE, "Stlm ST", st);
  }

  private int numPtlmBits(int sp) throws JPEG2000Exception {
    switch(sp){
      case VALUE_P_SIZE_16:
        return 16;
      case VALUE_P_SIZE_32:
        return 32;
    }

    throw new JPEG2000Exception(CodestreamMessages.ILLEGAL_PARAMETER_VALUE, "Stlm SP", sp);
  }

  @Override
  protected void write(ImageOutputStream sink, Codestream codestream) throws IOException {
    sink.writeShort(Ltlm);
    sink.writeByte(Ztlm);
    sink.writeByte(Stlm);

    for (int i = 0; i < Ptlm.size(); i++) {
      if (Ttlm != null) {
        if (Parameters.isValue(Stlm, MASK_ST, VALUE_T_SIZE_16)) {
          sink.writeShort(Ttlm.get(i));
        } else if (Parameters.isValue(Stlm, MASK_ST, VALUE_T_SIZE_8)) {
          sink.writeByte(Ttlm.get(i));
        }
      }

      if (Parameters.isSet(Stlm, MASK_SP)) {
        sink.writeShort(Ptlm.get(i));
      } else {
        sink.writeByte(Ptlm.get(i));
      }
    }
  }

  @Override
  protected void validate(Codestream codestream) throws JPEG2000Exception {

  }

  @Override
  protected void fillMarkerInfo(MarkerInfo markerInfo) {
    markerInfo.add(new PropertiesParameterInfo("Ltlm", Ltlm, "TLM.L"));
    markerInfo.add(new PropertiesParameterInfo("Ztlm", Ztlm, "TLM.Z"));
    markerInfo.add(new PropertiesParameterInfo("Stlm", Stlm, "TLM.S"));

    if (Ttlm != null) {
      markerInfo.add(new PropertiesParameterInfo("Ttlm", "TLM.T"));
      for (int i = 0; i < Ttlm.size(); i++) {
        markerInfo.add(new DirectParameterInfo("Ttlm[" + i + "]", Ttlm.get(i)));
      }
    }

    markerInfo.add(new PropertiesParameterInfo("Ptlm", "TLM.P"));
    for (int i = 0; i < Ptlm.size(); i++) {
      markerInfo.add(new DirectParameterInfo("Ptlm[" + i + "]", Ptlm.get(i)));
    }
  }

  private static final int MASK_ST = 0x30;
  private static final int VALUE_T_SIZE_0 = 0x00;
  private static final int VALUE_T_SIZE_8 = 0x10;
  private static final int VALUE_T_SIZE_16 = 0x20;

  private static final int MASK_SP = 0x40;
  private static final int VALUE_P_SIZE_16 = 0x00;
  private static final int VALUE_P_SIZE_32 = 0x40;
}
