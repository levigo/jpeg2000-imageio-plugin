package com.levigo.jadice.format.jpeg2000.internal.tier2;

import java.util.Comparator;

import com.levigo.jadice.document.internal.specref.Refer;
import com.levigo.jadice.document.internal.specref.Spec;
import com.levigo.jadice.format.jpeg2000.internal.codestream.PacketHeader;

@Refer(to = Spec.J2K_CORE, page = 59, section = "B.12", called = "Progression Order")
public enum ProgressionOrder implements Comparator<PacketHeader> {
  LRCP {
    @Override
    public int compare(PacketHeader p1, PacketHeader p2) {
      if (p1.layer < p2.layer) {
        return -1;
      } else if (p1.layer > p2.layer) {
        return 1;
      } else if (p1.res < p2.res) {
        return -1;
      } else if (p1.res > p2.res) {
        return 1;
      } else if (p1.comp < p2.comp) {
        return -1;
      } else if (p1.comp > p2.comp) {
        return 1;
      } else if (p1.precinct < p2.precinct) {
        return -1;
      } else if (p1.precinct > p2.precinct) {
        return 1;
      } else if (p1 == p2) {
        return 0; // This case handles the first packet that is added to the set.
      }

      throw new IllegalStateException("Two packets should never be the same!");
    }
  },

  RLCP {
    @Override
    public int compare(PacketHeader p1, PacketHeader p2) {
      if (p1.res < p2.res) {
        return -1;
      } else if (p1.res > p2.res) {
        return 1;
      } else if (p1.layer < p2.layer) {
        return -1;
      } else if (p1.layer > p2.layer) {
        return 1;
      } else if (p1.comp < p2.comp) {
        return -1;
      } else if (p1.comp > p2.comp) {
        return 1;
      } else if (p1.precinct < p2.precinct) {
        return -1;
      } else if (p1.precinct > p2.precinct) {
        return 1;
      } else if (p1 == p2) {
        return 0; // This case handles the first packet that is added to the set.
      }

      throw new IllegalStateException("Two packets should never be the same!");
    }
  },
  
  RPCL {
    @Override
    public int compare(PacketHeader p1, PacketHeader p2) {
      if (p1.res < p2.res) {
        return -1;
      } else if (p1.res > p2.res) {
        return 1;
      } else if (p1.precinct < p2.precinct) {
        return -1;
      } else if (p1.precinct > p2.precinct) {
        return 1;
      } else if (p1.comp < p2.comp) {
        return -1;
      } else if (p1.comp > p2.comp) {
        return 1;
      } else if (p1.layer < p2.layer) {
        return -1;
      } else if (p1.layer > p2.layer) {
        return 1;
      } else if (p1 == p2) {
        return 0; // This case handles the first packet that is added to the set.
      }

      throw new IllegalStateException("Two packets should never be the same!");
    }
  },
  
  PCRL {
    @Override
    public int compare(PacketHeader p1, PacketHeader p2) {
      if (p1.precinct < p2.precinct) {
        return -1;
      } else if (p1.precinct > p2.precinct) {
        return 1;
      } else if (p1.comp < p2.comp) {
        return -1;
      } else if (p1.comp > p2.comp) {
        return 1;
      } else if (p1.res < p2.res) {
        return -1;
      } else if (p1.res > p2.res) {
        return 1;
      } else if (p1.layer < p2.layer) {
        return -1;
      } else if (p1.layer > p2.layer) {
        return 1;
      } else if (p1 == p2) {
        return 0; // This case handles the first packet that is added to the set.
      }

      throw new IllegalStateException("Two packets should never be the same!");
    }
  },
  
  CPRL {
    @Override
    public int compare(PacketHeader p1, PacketHeader p2) {
      if (p1.comp < p2.comp) {
        return -1;
      } else if (p1.comp > p2.comp) {
        return 1;
      } else if (p1.precinct < p2.precinct) {
        return -1;
      } else if (p1.precinct > p2.precinct) {
        return 1;
      } else if (p1.res < p2.res) {
        return -1;
      } else if (p1.res > p2.res) {
        return 1;
      } else if (p1.layer < p2.layer) {
        return -1;
      } else if (p1.layer > p2.layer) {
        return 1;
      } else if (p1 == p2) {
        return 0; // This case handles the first packet that is added to the set.
      }

      throw new IllegalStateException("Two packets should never be the same!");
    }
  }
}
