package org.spcgreenville.deagan.logical;

import com.google.common.base.Preconditions;
import org.spcgreenville.deagan.Proto;
import org.spcgreenville.deagan.physical.EdgeDetectCallback;
import org.spcgreenville.deagan.physical.EdgeDetector;
import org.spcgreenville.deagan.physical.GPIOController;
import org.spcgreenville.deagan.physical.GPIOInputImpl;
import org.spcgreenville.deagan.physical.HardwareConfig;
import org.spcgreenville.deagan.physical.MVP23017InputImpl;

import java.nio.file.Path;

public class Inputs implements EdgeDetectCallback {
  private final Proto.Config config;
  private final HardwareConfig hardwareConfig;
  private GPIOController controllers[];
  private InputPin inputPins[];

  public Inputs(Proto.Config config, HardwareConfig hardwareConfig) {
    this.config = config;
    this.hardwareConfig = hardwareConfig;
  }

  public void initialize() {
    inputPins = new InputPin[] {
      new MVP23017InputImpl(hardwareConfig.controller2, 6),
      new MVP23017InputImpl(hardwareConfig.controller2, 7),
      new MVP23017InputImpl(hardwareConfig.controller2, 8),
      new MVP23017InputImpl(hardwareConfig.controller2, 9),
      new MVP23017InputImpl(hardwareConfig.controller2, 10),
      new MVP23017InputImpl(hardwareConfig.controller2, 11),
      new MVP23017InputImpl(hardwareConfig.controller2, 12),
      new MVP23017InputImpl(hardwareConfig.controller2, 13),
      new MVP23017InputImpl(hardwareConfig.controller2, 14),
      new MVP23017InputImpl(hardwareConfig.controller2, 15),
      new GPIOInputImpl(Path.of(config.getGpioLabel()), config.getGpioInterruptPin())
    };

    for (InputPin inputPin : inputPins) {
      inputPin.initialize();
    }

    // starts a new thread to invoke onCallback.
    int result = new EdgeDetector().beginEdgeDetection(this);
    Preconditions.checkState(result == 0, result);
  }

  @Override
  public void onCallback(EdgeType edgeType, int pin) {
    System.out.println("Edge detect " + edgeType + " on pin " + pin);
    for (int i = 0; i < inputPins.length; i++) {
      InputPin inputPin = inputPins[i];
      if (inputPin.isHigh()) {
        System.out.println("  Array index " + i + " is high");
      }
    }
  }
}
