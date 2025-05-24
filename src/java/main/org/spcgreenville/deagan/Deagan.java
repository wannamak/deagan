package org.spcgreenville.deagan;

import com.google.common.base.Preconditions;
import com.google.protobuf.TextFormat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Logger;

public class Deagan {
  private final Logger logger = Logger.getLogger(Deagan.class.getName());
  private final org.spcgreenville.deagan.logical.Notes notes;
  private final org.spcgreenville.deagan.midi.MidiFileDatabase database;
  private final org.spcgreenville.deagan.Proto.Config config;


  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Specify path to config.txt");
      System.exit(-1);
    }
    System.loadLibrary("deagan");
    new Deagan(args[0]).run();
  }

  public Deagan(String pathToConfig) throws IOException {
    org.spcgreenville.deagan.Proto.Config.Builder configBuilder = org.spcgreenville.deagan.Proto.Config.newBuilder();
    logger.info("Reading config from " + pathToConfig);
    try (BufferedReader br = new BufferedReader(new FileReader(pathToConfig))) {
      TextFormat.merge(br, configBuilder);
    }
    this.config = configBuilder.build();

    org.spcgreenville.deagan.physical.GPIOChipInfoProvider gpioManager = new org.spcgreenville.deagan.physical.GPIOChipInfoProvider();
    Path gpioDevicePath = gpioManager.getDevicePathForLabel(config.getGpioLabel());
    Preconditions.checkNotNull(
        gpioDevicePath, "No device for label " + config.getGpioLabel());

    org.spcgreenville.deagan.logical.Relays relays = new org.spcgreenville.deagan.logical.RaspberryRelays(gpioDevicePath);
    relays.initialize();
    this.notes = new org.spcgreenville.deagan.logical.Notes(relays);
    this.database = new org.spcgreenville.deagan.midi.MidiFileDatabase();
  }

  public void run() throws Exception {
    org.spcgreenville.deagan.hourly.ChimeSchedulerThread scheduler = new org.spcgreenville.deagan.hourly.ChimeSchedulerThread(
        database, notes, config);
    scheduler.start();
    scheduler.join();  // wait forever
  }
}