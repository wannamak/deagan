package org.spcgreenville.deagan.manual;

import com.google.common.base.Preconditions;
import org.spcgreenville.deagan.Proto;
import org.spcgreenville.deagan.logical.Notes;
import org.spcgreenville.deagan.logical.Power;
import org.spcgreenville.deagan.logical.RaspberryRelays;
import org.spcgreenville.deagan.logical.Relays;
import org.spcgreenville.deagan.midi.ChimePhrase;
import org.spcgreenville.deagan.midi.MidiFile;
import org.spcgreenville.deagan.midi.MidiFileDatabase;
import org.spcgreenville.deagan.midi.MidiFileSelector;
import org.spcgreenville.deagan.midi.MidiNotePlayer;
import org.spcgreenville.deagan.midi.MidiPlayer;
import org.spcgreenville.deagan.physical.GPIOChipInfoProvider;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

import static org.spcgreenville.deagan.physical.GPIOChipInfoProvider.DEFAULT_RASPBERRY_PI_DEVICE_LABEL;

// ./scripts/run.sh chimebox.manual.ManualMidiPlayer 0 0 0
public class ManualMidiPlayer {
  private final Logger logger = Logger.getLogger(ManualMidiPlayer.class.getName());

  private final Power power;
  private final Notes notes;
  private final MidiFileDatabase database;
  private final MidiFileSelector fileSelector;

  public static void main(String[] args) throws Exception {
    if (args.length != 3) {
      System.err.println("args file_index chime_phrase transposition");
      System.exit(-1);
    }
    System.loadLibrary("deagan");
    new ManualMidiPlayer().run(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
  }

  public ManualMidiPlayer() throws IOException {
    this.database = new MidiFileDatabase();
    GPIOChipInfoProvider gpioManager = new GPIOChipInfoProvider();
    Path gpioDevicePath = gpioManager.getDevicePathForLabel(DEFAULT_RASPBERRY_PI_DEVICE_LABEL);
    Preconditions.checkNotNull(
        gpioDevicePath, "No device for label " + DEFAULT_RASPBERRY_PI_DEVICE_LABEL);
    Relays relays = new RaspberryRelays(); // new TestingRelays();
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
