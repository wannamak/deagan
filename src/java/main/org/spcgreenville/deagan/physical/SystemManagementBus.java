package org.spcgreenville.deagan.physical;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SystemManagementBus {
  private final Logger logger = Logger.getLogger(SystemManagementBus.class.getName());
  private Map<Integer, Integer> deviceIdToFd = new HashMap<>();

  public synchronized boolean initialize(int deviceId) {
    int fd = initializeFileDescriptor("/dev/i2c-1", deviceId);
    if (fd < 0) {
      logger.warning("Unable to initialize bus for device " + deviceId);
      return false;
    } else {
      Preconditions.checkState(deviceIdToFd.put(deviceId, fd) == null);
      return true;
    }
  }

  public synchronized int readByte(int deviceId, int register) {
    Preconditions.checkState(deviceIdToFd.containsKey(deviceId));
    int fd = deviceIdToFd.get(deviceId);
    if (fd < 0) {
      logger.warning("Uninitialized read of register " + register);
      return 0;
    }
    return readByteNative(fd, register);
  }

  public synchronized void writeByte(int deviceId, int register, int value) {
    Preconditions.checkState(deviceIdToFd.containsKey(deviceId));
    int fd = deviceIdToFd.get(deviceId);
    if (fd < 0) {
      logger.warning("Uninitialize write of register " + register);
      return;
    }
    if (writeByteNative(fd, register, value) < 0) {
      logger.warning("Error writing register " + register);
    }
  }

  private native int readByteNative(int fd, int register);

  private native int writeByteNative(int fd, int register, int value);

  private native int initializeFileDescriptor(String devicePath, int deviceId);
}
