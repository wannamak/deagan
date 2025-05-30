package org.spcgreenville.deagan.physical;

import org.spcgreenville.deagan.logical.Relay;

import java.io.IOException;

public class MCP23017RelayImpl implements Relay {
  private final org.spcgreenville.deagan.physical.MCP23017Controller controller;
  private final int pin;

  public MCP23017RelayImpl(org.spcgreenville.deagan.physical.MCP23017Controller controller, int pin) {
    this.controller = controller;
    this.pin = pin;
  }

  @Override
  public void close() {
    controller.set(pin, org.spcgreenville.deagan.physical.MCP23017Controller.Value.LOW);
  }

  @Override
  public void open() {
    controller.set(pin, org.spcgreenville.deagan.physical.MCP23017Controller.Value.HIGH);
  }

  @Override
  public boolean isClosed() throws IOException {
    return controller.get(pin) == org.spcgreenville.deagan.physical.MCP23017Controller.Value.LOW;
  }

  @Override
  public void initialize() throws IOException {

  }
}
