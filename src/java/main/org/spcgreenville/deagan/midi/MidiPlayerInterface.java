package org.spcgreenville.deagan.midi;

public interface MidiPlayerInterface {
  void sleep(long durationMillis);

  void noteOn(int midiNote);

  void noteOff(int midiNote);
}
