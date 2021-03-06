package com.levigo.jadice.format.jpeg2000.internal.image;

import com.levigo.jadice.format.jpeg2000.JPEG2000Exception;
import com.levigo.jadice.format.jpeg2000.msg.ValidationMessages;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.List;

@RunWith(Parameterized.class)
public class RegionsSubsamplingTest {

  private final Region region;
  private final Pair subsampling;
  private final Object expected;

  @Parameterized.Parameters
  public static List<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {new Region(100, 100), new Pair(1, 1), new Region(100, 100)},
        {new Region(100, 100), new Pair(2, 3), new Region(0, 0, 50, 33)},
        {new Region(10, 10), new Pair(4, 5), new Region(0, 0, 2, 2)},
        {new Region(10, 10, 10, 10), new Pair(2, 2), new Region(5, 5, 5, 5)},
        {new Region(10, 10, 10, 10), new Pair(-1, -1),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
        {new Region(10, 10, 10, 10), new Pair(1, -1),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
        {new Region(10, 10, 10, 10), new Pair(-1, 1),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
        {new Region(10, 10, 10, 10), new Pair(0, 1),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
        {new Region(10, 10, 10, 10), new Pair(1, 0),
            new JPEG2000Exception(ValidationMessages.ILLEGAL_SUBSAMPLING_FACTORS)
        },
    });
  }

  public RegionsSubsamplingTest(Region region, Pair subsampling, Object expected) {
    this.region = region;
    this.subsampling = subsampling;
    this.expected = expected;
  }

  @Test
  public void runTest() throws JPEG2000Exception {
    if (expected instanceof Region) {
      Region actual = Regions.createSubsampled(region, subsampling);
      Assert.assertEquals(expected, actual);
    } else if (expected instanceof JPEG2000Exception) {
      JPEG2000Exception expectedException = null;
      try {
        Regions.createSubsampled(region, subsampling);
      } catch (JPEG2000Exception e) {
        expectedException = e;
      }

      Assert.assertNotNull(expectedException);
    }
  }
}
