package org.spcgreenville.deagan.physical;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.logging.Logger;

public class MCP23017Controller {

  private final Logger logger = Logger.getLogger(MCP23017Controller.class.getName());
  private final SystemManagementBus bus;
  private final int deviceId;
  private final List<Integer> outputPins;
  private final List<Integer> inputPins;

  private static final int NUM_OUTPUTS = 16;

  // https://ww1.microchip.com/downloads/en/devicedoc/20001952c.pdf
  // The addresses are for IOCON.BANK = 0 / 16-bit mode, see p12.
  private static final int MCP23017_IODIRECTION = 0x00;
  private static final int MCP23017_IPOL = 0x02;
  private static final int MCP23017_GPINTEN = 0x04;
  private static final int MCP23017_DEFVAL = 0x06;
  private static final int MCP23017_INTCON = 0x08;
  private static final int MCP23017_IOCON = 0x0A;
  private static final int MCP23017_GPPU = 0x0C;
  private static final int MCP23017_INTF = 0x0E;
  private static final int MCP23017_INTCAP = 0x10;
  private static final int MCP23017_GPIO = 0x12;
  private static final int MCP23017_OLATCH = 0x14;

  private static final int MCP23017_REGISTER_A_BIT = 0x00;
  private static final int MCP23017_REGISTER_B_BIT = 0x01;

  private static class Pin {
    final int latchRegister;
    final int gpioRegister;
    final int directionRegister;
    final int interruptOnChangePins;
    final int ioConfigurationRegister;
    final int bitmask;

    Pin(int registerBit, int bitmask) {
      this.latchRegister = MCP23017_OLATCH | registerBit;
      this.gpioRegister = MCP23017_GPIO | registerBit;
      this.directionRegister = MCP23017_IODIRECTION | registerBit;
      this.interruptOnChangePins = MCP23017_GPINTEN | registerBit;
      this.ioConfigurationRegister = MCP23017_IOCON | registerBit;
      this.bitmask = bitmask;
    }

    int update(int original, boolean value) {
      if (value) {
        return original | bitmask;
      } else {
        return original & ~bitmask;
      }
    }

    boolean isSet(int registerValue) {
      return (registerValue & bitmask) > 0;
    }
  }

  private static final Pin[] PINS = new Pin[]{
      new Pin(MCP23017_REGISTER_A_BIT, 1),
      new Pin(MCP23017_REGISTER_A_BIT, 2),
      new Pin(MCP23017_REGISTER_A_BIT, 4),
      new Pin(MCP23017_REGISTER_A_BIT, 8),
      new Pin(MCP23017_REGISTER_A_BIT, 16),
      new Pin(MCP23017_REGISTER_A_BIT, 32),
      new Pin(MCP23017_REGISTER_A_BIT, 64),
      new Pin(MCP23017_REGISTER_A_BIT, 128),

      new Pin(MCP23017_REGISTER_B_BIT, 1),
      new Pin(MCP23017_REGISTER_B_BIT, 2),
      new Pin(MCP23017_REGISTER_B_BIT, 4),
      new Pin(MCP23017_REGISTER_B_BIT, 8),
      new Pin(MCP23017_REGISTER_B_BIT, 16),
      new Pin(MCP23017_REGISTER_B_BIT, 32),
      new Pin(MCP23017_REGISTER_B_BIT, 64),
      new Pin(MCP23017_REGISTER_B_BIT, 128),
  };

  public MCP23017Controller(SystemManagementBus bus, int deviceId,
      List<Integer> outputPins, List<Integer> inputPins) {
    this.bus = bus;
    this.deviceId = deviceId;
    this.outputPins = outputPins;
    this.inputPins = inputPins;
  }

  public void initialize() {
    Preconditions.checkState(bus.initialize(deviceId));
    initializeDirectionRegisters();
    initializeInputPins();
  }

  public enum Value {
    HIGH,
    LOW
  }

  public void set(int pin, Value value) {
    Pin register = PINS[pin];
    int bitmap = bus.readByte(deviceId, register.gpioRegister);
    bitmap = register.update(bitmap, value == Value.HIGH);
    bus.writeByte(deviceId, register.gpioRegister, bitmap);
  }

  public Value get(int pin) {
    Pin register = PINS[pin];
    return register.isSet(bus.readByte(deviceId, register.gpioRegister)) ? Value.HIGH : Value.LOW;
  }

  private void initializeRegisters() {
    //for (int register = 0; register < 22; register++) {
    //if (register == 20 || register == 21) {
    // latch registers?
    //  bus.writeByte(deviceId, register, 0xff);
    //} else {
    //bus.writeByte(deviceId, register, 0);
    //}
    //}
  }

  private void initializeDirectionRegisters() {
    for (int pinNumber = 0; pinNumber < PINS.length; pinNumber++) {
      Pin pin = PINS[pinNumber];
      int value = bus.readByte(deviceId, pin.directionRegister);
      boolean direction = outputPins.contains(pinNumber) ? false /* output */ : true;
      Preconditions.checkState(!direction || inputPins.contains(pinNumber));
      value = pin.update(value, direction);
      bus.writeByte(deviceId, pin.directionRegister, value);
    }
  }

  private void initializeInputPins() {
    for (int pinNumber : inputPins) {
      Pin pin = PINS[pinNumber];
      int value = bus.readByte(deviceId, pin.interruptOnChangePins);
      value = pin.update(value, true /* enable interrupt-on-change */);
      bus.writeByte(deviceId, pin.interruptOnChangePins, value);

      int config = 0b01000000;  // Enables MIRROR so either A or B trigger the interrupt
      bus.writeByte(deviceId, pin.ioConfigurationRegister, config);
    }
  }
}
