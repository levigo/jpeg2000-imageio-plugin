package com.levigo.jadice.format.jpeg2000.internal.tier1;

import java.util.Arrays;

import com.levigo.jadice.format.jpeg2000.internal.image.SubbandType;

public final class States {

  //  public static final int UNIFORM = 18;
  //  public static final int RUNLENGTH = 17;
  //
  //  public static final int NOT_FIRST_REFINE = 16;
  //  public static final int FIRST_REFINE = 15;
  //  public static final int FIRST_REFINE_ZERO = 14;
  //
  //  private static final int[][] CONTRIBUTION = new int[3][3];
  //
  //  public static final int SIGN_13 = 13;
  //  public static final int SIGN_12 = 12;
  //  public static final int SIGN_11 = 11;
  //  public static final int SIGN_10 = 10;
  //  public static final int SIGN_9 = 9;
  //
  //  /**
  //   * <b>Table format</b>: <code>SIGN_LOOKUP[contribution H][contribution V]</code> <br>
  //   * <p>
  //   * <table>
  //   * <th>index</th>
  //   * <th>value</th>
  //   * <tr>
  //   * <td>0</td>
  //   * <td>0</td>
  //   * </tr>
  //   * <tr>
  //   * <td>1</td>
  //   * <td>1</td>
  //   * </tr>
  //   * <tr>
  //   * <td>2</td>
  //   * <td>-1</td>
  //   * </tr>
  //   * </table>
  //   * </p>
  //   * The contribution values are retrievable via {@link #getContribution(int, int)}.
  //   */
  //  private static final int[][] SIGN_LOOKUP = new int[3][3];
  //  private static final int[][] SIGN_XOR_BIT = new int[3][3];
  //
  //  public static final int SIGNIFICANT_8 = 8;
  //  public static final int SIGNIFICANT_3 = 3;
  //  public static final int SIGNIFICANT_1 = 1;
  //  public static final int ZERO_CONTEXT = 0;


  /** First of 9 labels, kappa_sig. */
  public static final byte KAPPA_SIG_BASE = 0;

  /** First and only label, kappa_run. */
  public static final byte KAPPA_RUN = 9;

  /** First of 5 labels, kappa_sign. */
  public static final byte KAPPA_SIGN_BASE = 10;

  /** First of 3 labels, kappa_run. */
  public static final byte KAPPA_MAG_BASE = 15;

  /** First and only label, kappa_uniform. */
  public static final byte KAPPA_UNI = 18;

  /** Total number of probability states. */
  public static final byte KAPPA_NUM_STATES = 18;

  /* NOTE:
   * The following 9 flag bits are used to construct significance coding context labels (denoted kappa_sig in the book).
   * The bit positions of these flags for the first, second, third and fourth sample in each stripe column are obtained
   * by adding 0, 3, 6 or 9 to the following constants. The 18 least significant bit positions of the 32-bit context
   * word are thus occupied by these significance flags.
   */

  /** Indicates significance of top-left neighbour. */
  public static final byte SIGMA_TL_POS = 0;

  /** Indicates significance of top-centre neighbour. */
  public static final byte SIGMA_TC_POS = 1;

  /** Indicates significance of top-right neighbour. */
  public static final byte SIGMA_TR_POS = 2;

  /** Indicates significance of centre-left neighbour. */
  public static final byte SIGMA_CL_POS = 3;

  /** Indicates significance of current sample. */
  public static final byte SIGMA_CC_POS = 4;

  /** Indicates significance of centre-right neighbour. */
  public static final byte SIGMA_CR_POS = 5;

  /** Indicates significance of bottom-left neighbour. */
  public static final byte SIGMA_BL_POS = 6;

  /** Indicates significance of bottom-centre neighbour. */
  public static final byte SIGMA_BC_POS = 7;

  /** Indicates significance of bottom-right neighbour. */
  public static final byte SIGMA_BR_POS = 8;

  public static final int SIGMA_TL_BIT = 1 << SIGMA_TL_POS;
  public static final int SIGMA_TC_BIT = 1 << SIGMA_TC_POS;
  public static final int SIGMA_TR_BIT = 1 << SIGMA_TR_POS;
  public static final int SIGMA_CL_BIT = 1 << SIGMA_CL_POS;
  public static final int SIGMA_CC_BIT = 1 << SIGMA_CC_POS;
  public static final int SIGMA_CR_BIT = 1 << SIGMA_CR_POS;
  public static final int SIGMA_BL_BIT = 1 << SIGMA_BL_POS;
  public static final int SIGMA_BC_BIT = 1 << SIGMA_BC_POS;
  public static final int SIGMA_BR_BIT = 1 << SIGMA_BR_POS;

  /**
   * The following bit mask may be used to determine whether the first sample in a stripe column has any significant
   * neighbours. Shifting the mask by 3, 6 and 9 yields neighbourhood masks for the second, third and fourth samples
   * in the stripe column.
   */
  // @formatter:off
  public static final int NBRHD_MASK = 
      SIGMA_TL_BIT | SIGMA_TC_BIT | SIGMA_TR_BIT |
      SIGMA_CL_BIT |                SIGMA_CR_BIT |
      SIGMA_BL_BIT | SIGMA_BC_BIT | SIGMA_BR_BIT;
  // @formatter:on

  /* NOTE:
   * The following three flag bits are used to identify the sign of any significant sample in a stripe column, and
   * also to control coding pass membership in an efficient manner, including the exclusion of samples which are
   * outside the code-block boundaries, i.e. "out-of-bounds" (OOB). The bit positions identified below correspond to
   * the flags for the first sample in each stripe.  Add 3, 6 and 9 to these to obtain the flag bit positions for
   * subsequent stripe rows. The fact that consecutive stripe rows have all flag bits (including significance) spaced
   * apart by 3 bit positions, greatly facilitates efficient manipulation.  For an explanation of coding pass membership
   * assessment and  OOB identification using these flags, refer to Table 17.1 in the book by Taubman and Marcellin.
   */

  /** 0 if insignificant or positive; 1 for negative. */
  public static final byte CHI_POS = 21;

  /** 1 if sample is to be coded in mag-ref pass. */
  public static final byte MU_POS = 19;

  /** 1 if sample was coded in sig-prop pass. */
  public static final byte PI_POS = 20;

  public static final int CHI_BIT = 1 << CHI_POS;
  public static final int MU_BIT = 1 << MU_POS;
  public static final int PI_BIT = 1 << PI_POS;

  /** Set this bit to prevent inclusion in any pass. */
  public static final int OOB_MARKER = CHI_BIT;

  /** Needs both bits off. */
  public static final int SIG_PROP_MEMBER_MASK = SIGMA_CC_BIT | CHI_BIT;

  /** Need all 0. */
  public static final int CLEANUP_MEMBER_MASK = SIGMA_CC_BIT | PI_BIT | CHI_BIT;

  /* NOTE:
   * The following are the only bits free in the 32 bit word.  They are used to store the sign of the sample
   * immediately above (previous) the first sample in the stripe column and immediately below (next) the last sample
   * in the stripe column.  This greatly simplifies the construction of sign coding contexts when necessary. Note
   * that the NEXT_CHI bit follows the same progression, in groups of 3, as all of the other flags, so that it is
   * separated by 3 bit positions from the sign of the last sample in the stripe column.  It is, unfortunately, not
   * possible to preserve this relationship for the PREV_CHI bit, which slightly complicates sign coding for the
   * first sample in each quad.
   */

  public static final byte PREV_CHI_POS = 18;
  public static final byte NEXT_CHI_POS = 31;

  public static final int PREV_CHI_BIT = 1 << PREV_CHI_POS;
  public static final int NEXT_CHI_BIT = 1 << NEXT_CHI_POS;

  /* NOTE:
   * The following identify the 8 flag bits in the index supplied to the sign coding lookup table. They are derived
   * from the sign bits in three `quad_flags' words using simple logical operations.
   */

  public static final byte UP_NBR_SIG_POS = 0;
  public static final byte UP_NBR_CHI_POS = 1;
  public static final byte LEFT_NBR_SIG_POS = 2;
  public static final byte LEFT_NBR_CHI_POS = 3;
  public static final byte RIGHT_NBR_SIG_POS = 4;
  public static final byte RIGHT_NBR_CHI_POS = 5;
  public static final byte DOWN_NBR_SIG_POS = 6;
  public static final byte DOWN_NBR_CHI_POS = 7;

  /** Number of extra context-words between stripes. */
  public static final byte EXTRA_CONTEXT_WORDS = 3;

  public static final byte[][] SIG_LUT = new byte[4][512];
  public static final byte[] SIGN_LUT = new byte[256];

  static {
    fillSignTables();
    fillSignificanceTables();
  }

  private static void fillSignificanceTables() {
    for (int idx = 0; idx < 512; idx++) {
      // Start with the context map for the HL (horizontally high-pass) band
      int band = SubbandType.HL.id;
      int v1 = ((idx >> SIGMA_TC_POS) & 1) + ((idx >> SIGMA_BC_POS) & 1);
      int v2 = ((idx >> SIGMA_CL_POS) & 1) + ((idx >> SIGMA_CR_POS) & 1);
      int v3 =
          ((idx >> SIGMA_TL_POS) & 1) + ((idx >> SIGMA_TR_POS) & 1) + ((idx >> SIGMA_BL_POS) & 1) + (
              (idx >> SIGMA_BR_POS) & 1);

      int kappa;
      if (v1 == 2)
        kappa = 8;
      else if (v1 == 1) {
        if (v2 != 0)
          kappa = 7;
        else if (v3 != 0)
          kappa = 6;
        else
          kappa = 5;
      } else {
        if (v2 != 0)
          kappa = 2 + v2;
        else
          kappa = 0 + ((v3 > 2) ? 2 : v3);
      }
      SIG_LUT[band][idx] = (byte) kappa;

      //  Now build the context map for the LH (vertically high-pass) band
      band = SubbandType.LH.id;
      v1 = ((idx >> SIGMA_CL_POS) & 1) + ((idx >> SIGMA_CR_POS) & 1);
      v2 = ((idx >> SIGMA_TC_POS) & 1) + ((idx >> SIGMA_BC_POS) & 1);
      v3 =
          ((idx >> SIGMA_TL_POS) & 1) + ((idx >> SIGMA_TR_POS) & 1) + ((idx >> SIGMA_BL_POS) & 1) + (
              (idx >> SIGMA_BR_POS) & 1);
      if (v1 == 2)
        kappa = 8;
      else if (v1 == 1) {
        if (v2 != 0)
          kappa = 7;
        else if (v3 != 0)
          kappa = 6;
        else
          kappa = 5;
      } else {
        if (v2 != 0)
          kappa = 2 + v2;
        else
          kappa = 0 + ((v3 > 2) ? 2 : v3);
      }
      SIG_LUT[band][idx] = (byte) kappa;

      // Finally, build the context map for the HH band
      band = SubbandType.HH.id;
      v1 =
          ((idx >> SIGMA_TL_POS) & 1) + ((idx >> SIGMA_TR_POS) & 1) + ((idx >> SIGMA_BL_POS) & 1) + (
              (idx >> SIGMA_BR_POS) & 1);
      v2 =
          ((idx >> SIGMA_CL_POS) & 1) + ((idx >> SIGMA_CR_POS) & 1) + ((idx >> SIGMA_TC_POS) & 1) + (
              (idx >> SIGMA_BC_POS) & 1);

      if (v1 >= 3) {
        kappa = 8;
      } else if (v1 == 2) {
        if (v2 >= 1) {
          kappa = 7;
        } else {
          kappa = 6;
        }
      } else if (v1 == 1)
        kappa = 3 + ((v2 > 2) ? 2 : v2);
      else
        kappa = 0 + ((v2 > 2) ? 2 : v2);
      SIG_LUT[band][idx] = (byte) kappa;
    }

    final byte[] sigLUT_LH = SIG_LUT[SubbandType.LH.id];
    SIG_LUT[SubbandType.LL.id] = Arrays.copyOf(sigLUT_LH, sigLUT_LH.length);
  }

  private static void fillSignTables() {
    for (int idx = 0; idx < 256; idx++) {
      int vpos = 0;
      int vneg = 0;
      int hpos = 0;
      int hneg = 0;
      int kappa;

      if (((idx >> UP_NBR_SIG_POS) & 1) != 0) {
        kappa = (idx >> UP_NBR_CHI_POS) & 1;
        vneg |= kappa;
        vpos |= 1 - kappa;
      }

      if (((idx >> DOWN_NBR_SIG_POS) & 1) != 0) {
        kappa = (idx >> DOWN_NBR_CHI_POS) & 1;
        vneg |= kappa;
        vpos |= 1 - kappa;
      }

      if (((idx >> LEFT_NBR_SIG_POS) & 1) != 0) {
        kappa = (idx >> LEFT_NBR_CHI_POS) & 1;
        hneg |= kappa;
        hpos |= 1 - kappa;
      }

      if (((idx >> RIGHT_NBR_SIG_POS) & 1) != 0) {
        kappa = (idx >> RIGHT_NBR_CHI_POS) & 1;
        hneg |= kappa;
        hpos |= 1 - kappa;
      }

      int v1 = hpos - hneg;
      int v2 = vpos - vneg;
      int predict = 0;
      if (v1 < 0) {
        predict = 1;
        v1 = -v1;
        v2 = -v2;
      }
      if (v1 == 0) {
        if (v2 < 0) {
          predict = 1;
          v2 = -v2;
        }
        kappa = v2;
      } else
        kappa = 3 + v2;

      SIGN_LUT[idx] = (byte) ((kappa << 1) | predict);
    }
  }

  public static byte[] getSignificanceLUT(SubbandType type) {
    return SIG_LUT[type.id];
  }
}
