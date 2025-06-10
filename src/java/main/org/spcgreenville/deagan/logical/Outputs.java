package org.spcgreenville.deagan.logical;

import org.spcgreenville.deagan.Proto;
import org.spcgreenville.deagan.physical.HardwareConfig;
import org.spcgreenville.deagan.physical.MCP23017Controller;
import org.spcgreenville.deagan.physical.MCP23017RelayImpl;
import org.spcgreenville.deagan.physical.SystemManagementBus;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class Outputs extends Relays {
  private static final Logger logger = Logger.getLogger(Outputs.class.getName());

  private final HardwareConfig hardwareConfig;

  public Outputs(HardwareConfig hardwareConfig) {
    this.hardwareConfig = hardwareConfig;
  }

  @Override
  public void initialize() throws IOException {
    MCP23017Controller controller1 = hardwareConfig.controller1;
    MCP23017Controller controller2 = hardwareConfig.controller2;

    /*
     * We need 20 outputs and 10 inputs.
     * The first controller will be used for 16 outputs.
     * The second controller will be used for 6 outputs (2 extra) and 10 inputs.
     */
    relays = new Relay[]{
        new MCP23017RelayImpl(controller1, 0),
        new MCP23017RelayImpl(controller1, 1),
        new MCP23017RelayImpl(controller1, 2),
        new MCP23017RelayImpl(controller1, 3),
        new MCP23017RelayImpl(controller1, 4),
        new MCP23017RelayImpl(controller1, 5),
        new MCP23017RelayImpl(controller1, 6),
        new MCP23017RelayImpl(controller1, 7),
        new MCP23017RelayImpl(controller1, 8),
        new MCP23017RelayImpl(controller1, 9),
        new MCP23017RelayImpl(controller1, 10),
        new MCP23017RelayImpl(controller1, 11),
        new MCP23017RelayImpl(controller1, 12),
        new MCP23017RelayImpl(controller1, 13),
        new MCP23017RelayImpl(controller1, 14),
        new MCP23017RelayImpl(controller1, 15),
        new MCP23017RelayImpl(controller2, 0),
        new MCP23017RelayImpl(controller2, 1),
        new MCP23017RelayImpl(controller2, 2),
        new MCP23017RelayImpl(controller2, 3),
        new MCP23017RelayImpl(controller2, 4),
        new MCP23017RelayImpl(controller2, 5),
    };
    for (int i = 0; i < relays.length; ++i) {
      logger.info(String.format("Initializing relay %d", i));
      relays[i].initialize();
    }
  }
}
