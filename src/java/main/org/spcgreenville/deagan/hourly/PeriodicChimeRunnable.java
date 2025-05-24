package org.spcgreenville.deagan.hourly;

import org.spcgreenville.deagan.logical.Notes;
import org.spcgreenville.deagan.midi.ChimePhrase;
import org.spcgreenville.deagan.midi.LowestMidiNotePlayer;
import org.spcgreenville.deagan.midi.MidiFile;
import org.spcgreenville.deagan.midi.MidiFileDatabase;
import org.spcgreenville.deagan.midi.MidiFileIterator;
import org.spcgreenville.deagan.midi.MidiFileSelector;
import org.spcgreenville.deagan.midi.MidiNotePlayer;
import org.spcgreenville.deagan.midi.MidiPlayer;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.time.LocalTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PeriodicChimeRunnable implements Runnable {
  private final Logger logger = Logger.getLogger(PeriodicChimeRunnable.class.getName());
  private MidiFile currentFile;
  private MidiPlayer tunePlayer;
  private MidiPlayer chimePlayer;

  private static final int SILENCE_PRIOR_TO_HOUR_CHIMES_MS = 3000;
  private static final int SILENCE_BETWEEN_HOUR_CHIMES_MS = 1200;

  private final MidiFileDatabase database;
  private final MidiFileSelector midiFileSelector;
  private final Notes notes;
  private final org.spcgreenville.deagan.Proto.Config config;
  private final LocalTime dailyStartTime;
  private final LocalTime dailyEndTime;
  private MidiFileIterator midiFileIterator;

  public PeriodicChimeRunnable(MidiFileDatabase database, Notes notes, org.spcgreenville.deagan.Proto.Config config) {
    this.database = database;
    this.midiFileSelector = new MidiFileSelector(database, config);
    this.notes = notes;
    this.config = config;
    this.dailyStartTime = LocalTime.of(
        config.getDailyStartTime().getHour(),
        config.getDailyStartTime().getMinuteOfHour());
    this.dailyEndTime = LocalTime.of(
        config.getDailyEndTime().getHour(),
        config.getDailyEndTime().getMinuteOfHour());
  }

  @Override
  public void run() {
    logger.finer("PCR triggered at " + LocalTime.now());
    try {
      runInternal();
    } catch (IOException | InvalidMidiDataException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  private void runInternal() throws IOException, InvalidMidiDataException {
    LocalTime time = LocalTime.now();

    if (time.isAfter(dailyEndTime)) {
      logger.finer("Not chiming due to after end time");
      return;
    }

    if (time.isBefore(dailyStartTime)) {
      logger.finer("Not chiming due to before start time");
      return;
    }

    int MINUTE_OF_HOUR_TO_BEGIN_CHIME = 30; // 15

    if (currentFile == null || time.getMinute() == MINUTE_OF_HOUR_TO_BEGIN_CHIME) {
      int transposition;
      {
        currentFile = midiFileSelector.selectDatabaseFile();
        List<Integer> possibleTranspositions = database.getPossibleTranspositions(currentFile.getFile());
        int transpositionIndex = midiFileSelector.getRandomInt(possibleTranspositions.size());
        transposition = possibleTranspositions.get(transpositionIndex);
      }
      if (currentFile == null) {
        logger.info("Not chiming due to no chime files available");
        return;
      }

      logger.info("Tune: " + currentFile);
      logger.info("Transposition: " + transposition);

      tunePlayer = new MidiPlayer(currentFile, new MidiNotePlayer(notes, transposition));
      chimePlayer = new MidiPlayer(currentFile, new LowestMidiNotePlayer(notes, transposition));
    }

    ChimePhrase chimePhrase = getChimePhraseFromMinuteOfHour(time.getMinute());
    if (chimePhrase == null) {
      logger.finer("Not chiming due to unexpected minute of hour");
      return;
    }

    tunePlayer.play(chimePhrase);

    if (config.getEnableHourCountChime() && chimePhrase == ChimePhrase.HOUR) {
      uncheckedThreadSleepMs(SILENCE_PRIOR_TO_HOUR_CHIMES_MS);

      int numRepeats = time.getHour();
      if (numRepeats > 12) {
        numRepeats -= 12;
      }

      //volume.setForte();
      for (int i = 0; i < numRepeats; i++) {
        if (i > 0) {
          uncheckedThreadSleepMs(SILENCE_BETWEEN_HOUR_CHIMES_MS);
        }
        chimePlayer.play(ChimePhrase.CHIME);
      }
    }
  }

  private void uncheckedThreadSleepMs(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ie) {
      logger.log(Level.WARNING, ie.getMessage(), ie);
    }
  }

  private ChimePhrase getChimePhraseFromMinuteOfHour(int minuteOfHour) {
    switch (minuteOfHour) {
      case 0:
        return ChimePhrase.HOUR;
//      case 15:
//        return MidiFile.QUARTER_TRACK;
      case 30:
        return ChimePhrase.HALF;
//      case 45:
//        return MidiFile.THREE_QUARTERS_TRACK;
      default:
        logger.finer("Unrecognized minute of hour " + minuteOfHour);
        return null;
    }
  }
}
