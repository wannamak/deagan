package org.spcgreenville.deagan.physical;

public class EdgeDetector {
  public native int beginEdgeDetection(
      long gpioContext,
      EdgeDetectCallback edgeDetectCallback);
}
