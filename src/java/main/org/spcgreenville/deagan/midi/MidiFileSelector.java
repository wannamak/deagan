package org.spcgreenville.deagan.midi;

import com.google.common.collect.ImmutableList;
import org.spcgreenville.deagan.Proto;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import static org.spcgreenville.deagan.midi.MidiFileDatabase.BEECH_SPRING;
import static org.spcgreenville.deagan.midi.MidiFileDatabase.SOISSONS;
import static org.spcgreenville.deagan.midi.MidiFileDatabase.ST_MICHAELS;
import static org.spcgreenville.deagan.midi.MidiFileDatabase.WESTMINSTER;
import static org.spcgreenville.deagan.midi.MidiFileDatabase.WHITTINGTON;

public class MidiFileSelector {
  private final Logger logger = Logger.getLogger(MidiFileSelector.class.getName());

  private final Random random = new Random();
  private final MidiReader reader = new MidiReader();
  private final MidiFileDatabase database;
  private final Proto.Config config;

  private final List<Integer> ordinaryIndexes = ImmutableList.of(
      WESTMINSTER,
      WHITTINGTON,
      SOISSONS,
      ST_MICHAELS,
      BEECH_SPRING
  );

  public MidiFileSelector(MidiFileDatabase database, Proto.Config config) {
    this.database = database;
    this.config = config;
  }

  public int getRandomInt(int maxPlusOne) {
    return random.nextInt(maxPlusOne);
  }

  public MidiFile selectDatabaseFile() throws IOException, InvalidMidiDataException {
    int index = random.nextInt(ordinaryIndexes.size());
    File chimeFile = database.getFile(ordinaryIndexes.get(index));
    logger.finer("Selected " + chimeFile.getAbsolutePath());
    return reader.readDatabaseFile(chimeFile);
  }

  public MidiFile selectDatabaseFile(int fileIndex) throws Exception {
    File chimeFile = database.getFile(fileIndex);
    if (!chimeFile.exists()) {
      logger.warning("File not found: " + chimeFile.getAbsolutePath());
      return null;
    }
    return reader.readDatabaseFile(chimeFile);
  }
}
