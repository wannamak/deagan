package org.spcgreenville.deagan.logical;

import org.spcgreenville.deagan.physical.GPIORelayImpl;
import org.spcgreenville.deagan.physical.MCP23017Controller;
import org.spcgreenville.deagan.physical.MCP23017RelayImpl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class RaspberryRelays extends Relays {
  private static final Logger logger = Logger.getLogger(RaspberryRelays.class.getName());

  private final Path gpioDevicePath;

  public RaspberryRelays(Path gpioDevicePath) {
    this.gpioDevicePath = gpioDevicePath;
  }

  @Override
  public void initialize() throws IOException {
    org.spcgreenville.deagan.physical.MCP23017Controller controller = new MCP23017Controller();
    controller.initialize();

    relays = new org.spcgreenville.deagan.logical.Relay[]{
        new GPIORelayImpl(gpioDevicePath, 12),  // chimes power supply
        new GPIORelayImpl(gpioDevicePath, 16),  // low AC power to chimes
        new GPIORelayImpl(gpioDevicePath, 20),  // high AC power to chimes
        new GPIORelayImpl(gpioDevicePath, 21),  // note 1
        new GPIORelayImpl(gpioDevicePath, 23),
        new GPIORelayImpl(gpioDevicePath, 24),
        new GPIORelayImpl(gpioDevicePath, 25),
        new GPIORelayImpl(gpioDevicePath, 26),
        new MCP23017RelayImpl(controller, 7),
        new MCP23017RelayImpl(controller, 6),
        new MCP23017RelayImpl(controller, 5),
        new MCP23017RelayImpl(controller, 4),
        new MCP23017RelayImpl(controller, 3),  // note 10
        new MCP23017RelayImpl(controller, 2),
        new MCP23017RelayImpl(controller, 1),
        new MCP23017RelayImpl(controller, 0),
        new MCP23017RelayImpl(controller, 15),
        new MCP23017RelayImpl(controller, 14),
        new MCP23017RelayImpl(controller, 13),
        new MCP23017RelayImpl(controller, 12),
        new MCP23017RelayImpl(controller, 11),
        new MCP23017RelayImpl(controller, 10),
        new MCP23017RelayImpl(controller, 9),  // note 20
        new MCP23017RelayImpl(controller, 8),  // note 21
    };
    for (int i = 0; i < relays.length; ++i) {
      logger.info(String.format("Initializing relay %d", i));
      relays[i].initialize();
    }
  }
}
