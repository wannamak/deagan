package org.spcgreenville.deagan.physical;

public interface EdgeDetectCallback {
  enum EdgeType {
    RISING_EDGE,
    FALLING_EDGE
  }

  void onCallback(EdgeType edgeType, int pin);
}

