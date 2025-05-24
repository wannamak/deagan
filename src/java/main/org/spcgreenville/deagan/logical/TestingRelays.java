package org.spcgreenville.deagan.logical;

import java.io.IOException;

public class TestingRelays extends org.spcgreenville.deagan.logical.Relays {
  private static final int NUM_RELAYS = 24;

  @Override
  public void initialize() throws IOException {
    relays = new org.spcgreenville.deagan.logical.Relay[NUM_RELAYS];
    for (int i = 0; i < NUM_RELAYS; i++) {
      relays[i] = new org.spcgreenville.deagan.logical.Relay() {
        @Override
        public void close() {
        }

        @Override
        public void open() {
        }

        @Override
        public boolean isClosed() throws IOException {
          return false;
        }

        @Override
        public void initialize() throws IOException {
        }
      };
    }
  }
}
