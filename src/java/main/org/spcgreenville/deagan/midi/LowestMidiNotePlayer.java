package org.spcgreenville.deagan.midi;

import org.spcgreenville.deagan.logical.Notes;

public class LowestMidiNotePlayer extends MidiNotePlayer {
  public LowestMidiNotePlayer(Notes notes, int transposition) {
    super(notes, transposition);
  }

  @Override
  public int getChimeNote(int midiNote) {
    int chimeNote = super.getChimeNote(midiNote);
    if (chimeNote > 12) {
      chimeNote -= 12;
    }
    return chimeNote;
  }
}
