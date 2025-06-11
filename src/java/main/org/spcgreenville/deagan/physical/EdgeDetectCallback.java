package org.spcgreenville.deagan.physical;

public interface EdgeDetectCallback {
  enum EdgeType {
    RISING_EDGE,
    FALLING_EDGE
  }

  public void onCallback(EdgeType edgeType, int pin);
}

