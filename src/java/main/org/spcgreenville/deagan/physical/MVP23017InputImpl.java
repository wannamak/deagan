package org.spcgreenville.deagan.physical;

import org.spcgreenville.deagan.logical.InputPin;

import java.io.IOException;

public class MVP23017InputImpl implements InputPin {
  private final MCP23017Controller controller;
  private final int pin;

  public MVP23017InputImpl(MCP23017Controller controller, int pin) {
    this.controller = controller;
    this.pin = pin;
  }

  @Override
  public boolean isHigh() {
    return controller.get(pin) == MCP23017Controller.Value.HIGH;
  }

  @Override
  public void initialize() {}
}
