package org.spcgreenville.deagan.physical;

import org.spcgreenville.deagan.logical.InputPin;

import java.nio.file.Path;
import java.util.logging.Logger;

public class GPIOInputImpl extends GPIOController implements InputPin {
  private static final Logger logger = Logger.getLogger(GPIOInputImpl.class.getName());

  public GPIOInputImpl(Path devicePath, int logicalPin) {
    super(devicePath, logicalPin, Direction.IN);
  }

  @Override
  public boolean isHigh() {
    return get().equals(Value.ACTIVE);
  }
}
