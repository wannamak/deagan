package org.spcgreenville.deagan;

import org.spcgreenville.deagan.hourly.ChimeSchedulerThread;
import org.spcgreenville.deagan.logical.Notes;
import org.spcgreenville.deagan.logical.RaspberryRelays;
import org.spcgreenville.deagan.logical.Relays;
import org.spcgreenville.deagan.midi.MidiFileDatabase;

import java.io.IOException;
import java.util.logging.Logger;

public class Deagan {
  private final Logger logger = Logger.getLogger(Deagan.class.getName());
  private final Notes notes;
  private final MidiFileDatabase database;
  private final Proto.Config config;


  public static void main(String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Specify path to config.txt");
      System.exit(-1);
    }
    System.loadLibrary("deagan");
    new Deagan(args[0]).run();
  }

  public Deagan(String pathToConfig) throws IOException {
    this.config = new ConfigReader().readConfig(pathToConfig);
    Relays relays = new RaspberryRelays(config);
    relays.initialize();
    this.notes = new Notes(relays);
    this.database = new MidiFileDatabase();
  }

  public void run() throws Exception {
    ChimeSchedulerThread scheduler = new ChimeSchedulerThread(database, notes, config);
    scheduler.start();

    ReplThread repl = new ReplThread(notes, config);
    repl.start();

    scheduler.join();  // wait forever
  }
}