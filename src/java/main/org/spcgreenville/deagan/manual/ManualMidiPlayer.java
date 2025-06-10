package org.spcgreenville.deagan.manual;

import com.google.common.base.Preconditions;
import org.spcgreenville.deagan.ConfigReader;
import org.spcgreenville.deagan.Proto;
import org.spcgreenville.deagan.logical.Notes;
import org.spcgreenville.deagan.logical.Power;
import org.spcgreenville.deagan.logical.Outputs;
import org.spcgreenville.deagan.logical.Relays;
import org.spcgreenville.deagan.midi.ChimePhrase;
import org.spcgreenville.deagan.midi.MidiFile;
import org.spcgreenville.deagan.midi.MidiFileDatabase;
import org.spcgreenville.deagan.midi.MidiFileSelector;
import org.spcgreenville.deagan.midi.MidiNotePlayer;
import org.spcgreenville.deagan.midi.MidiPlayer;
import org.spcgreenville.deagan.physical.GPIOChipInfoProvider;
import org.spcgreenville.deagan.physical.HardwareConfig;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

// ./scripts/run.sh deagan.manual.ManualMidiPlayer config.txt 0 0 0
public class ManualMidiPlayer {
  private final Logger logger = Logger.getLogger(ManualMidiPlayer.class.getName());

  private final Proto.Config config;
  private final Power power;
  private final Notes notes;
  private final MidiFileDatabase database;
  private final MidiFileSelector fileSelector;

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.err.println("args path_to_config file_index chime_phrase transposition");
      System.exit(-1);
    }
    System.loadLibrary("deagan");
    new ManualMidiPlayer(args[0]).run(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]));
  }

  public ManualMidiPlayer(String pathToConfig) throws IOException {
    this.config = new ConfigReader().readConfig(pathToConfig);
    this.database = new MidiFileDatabase();
    GPIOChipInfoProvider gpioManager = new GPIOChipInfoProvider();
    Path gpioDevicePath = gpioManager.getDevicePathForLabel(config.getGpioLabel());
    Preconditions.checkNotNull(
        gpioDevicePath, "No device for label " + config.getGpioLabel());

    HardwareConfig hardwareConfig = new HardwareConfig(config);
    Relays relays = new Outputs(hardwareConfig); // new TestingRelays();
    relays.initialize();
    this.power = new Power(relays);
    this.notes = new Notes(relays);
    this.fileSelector = new MidiFileSelector(database, Proto.Config.getDefaultInstance());
  }

  public void run(int fileIndex, int chimePhraseIndex, int transposition) throws Exception {
    MidiFile midiFile = fileSelector.selectDatabaseFile(fileIndex);
    List<Integer> possibleTranspositions = database.getPossibleTranspositions(midiFile.getFile());
    logger.info("Possible transpositions: " + possibleTranspositions);
    MidiPlayer tunePlayer = new MidiPlayer(midiFile,
        new MidiNotePlayer(notes, transposition));
    logger.info(String.format(
        "Playing file index %d phrase %d transposition %d\n", fileIndex, chimePhraseIndex, transposition));
    power.on();
    tunePlayer.play(ChimePhrase.values()[chimePhraseIndex]);
    power.off();
    logger.info("Play complete.");
  }
}
