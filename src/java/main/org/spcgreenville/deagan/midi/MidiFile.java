package org.spcgreenville.deagan.midi;

import javax.sound.midi.Track;
import java.io.File;
import java.util.List;

public interface MidiFile {
  File getFile();

  /**
   * @deprecated Use getMusicalPhrase
   */
  int getTrackSize();

  /**
   * @deprecated Use getMusicalPhrase
   */
  Track getTrack(int trackIndex);

  public static class MidiNote {
    public final int note;
    public final int durationMs;

    public MidiNote(int note, int durationMs) {
      this.note = note;
      this.durationMs = durationMs;
    }

    @Override
    public String toString() {
      return String.format("[%d,%dms]", note, durationMs);
    }
  }

  List<MidiNote> getMusicalPhrase(org.spcgreenville.deagan.midi.ChimePhrase chimePhrase);
}
