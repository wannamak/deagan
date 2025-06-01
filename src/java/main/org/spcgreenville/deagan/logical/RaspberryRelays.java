package org.spcgreenville.deagan.logical;

import org.spcgreenville.deagan.physical.GPIORelayImpl;
import org.spcgreenville.deagan.physical.MCP23017Controller;
import org.spcgreenville.deagan.physical.MCP23017RelayImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class RaspberryRelays extends Relays {
  private static final Logger logger = Logger.getLogger(RaspberryRelays.class.getName());

  @Override
  public void initialize() throws IOException {
    MCP23017Controller controller1 =
        new MCP23017Controller(MCP23017Controller.FIRST_MCP_23017_EXPANDER_BOARD_DEVICE_ID);
    controller1.initialize();

    MCP23017Controller controller2 =
        new MCP23017Controller(MCP23017Controller.FIRST_MCP_23017_EXPANDER_BOARD_DEVICE_ID);
    controller2.initialize();

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
        new MCP23017RelayImpl(controller2, 6),
        new MCP23017RelayImpl(controller2, 7),
        new MCP23017RelayImpl(controller2, 8),
        new MCP23017RelayImpl(controller2, 9),
        new MCP23017RelayImpl(controller2, 10),
        new MCP23017RelayImpl(controller2, 11),
        new MCP23017RelayImpl(controller2, 12),
        new MCP23017RelayImpl(controller2, 13),
        new MCP23017RelayImpl(controller2, 14),
        new MCP23017RelayImpl(controller2, 15),
    };
    for (int i = 0; i < relays.length; ++i) {
      logger.info(String.format("Initializing relay %d", i));
      relays[i].initialize();
    }
  }
}
