package org.spcgreenville.deagan.physical;

import org.spcgreenville.deagan.Proto;

import java.util.List;

public class HardwareConfig {
  public final SystemManagementBus systemManagementBus;
  public final MCP23017Controller controller1;
  public final MCP23017Controller controller2;

  public HardwareConfig(Proto.Config config) {
    systemManagementBus = new SystemManagementBus();

    controller1 =
        new MCP23017Controller(systemManagementBus,
            config.getFirstMpc23017I2CAddress(),
            List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15),
            List.of());
    controller1.initialize();

    controller2 =
        new MCP23017Controller(systemManagementBus,
            config.getSecondMpc23017I2CAddress(),
            List.of(0, 1, 2, 3, 4, 5),
            List.of(6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
    controller2.initialize();
  }
}
