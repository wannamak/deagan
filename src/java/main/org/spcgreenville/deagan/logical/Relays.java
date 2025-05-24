package org.spcgreenville.deagan.logical;

import java.io.IOException;
import java.util.Arrays;

public abstract class Relays {
  protected org.spcgreenville.deagan.logical.Relay[] relays;

  public abstract void initialize() throws IOException;

  public org.spcgreenville.deagan.logical.Relay[] getRelays() {
    return relays;
  }

  public int length() {
    return relays.length;
  }

  public org.spcgreenville.deagan.logical.Relay get(int index) {
    return relays[index];
  }

  @Override
  public String toString() {
    return "Relays";
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(relays);
  }

  @Override
  public boolean equals(Object that) {
    if (!(that instanceof Relays)) {
      return false;
    }
    return Arrays.equals(((Relays) that).relays, relays);
  }
}
