package org.spcgreenville.deagan.hourly;

import org.spcgreenville.deagan.Proto;
import org.spcgreenville.deagan.logical.Notes;
import org.spcgreenville.deagan.midi.MidiFileDatabase;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ChimeSchedulerThread extends Thread {
  private final Logger logger = Logger.getLogger(ChimeSchedulerThread.class.getName());

  private final ScheduledExecutorService scheduler =
      Executors.newScheduledThreadPool(1);

  private final MidiFileDatabase database;
  private final Notes notes;
  private final Proto.Config config;

  public ChimeSchedulerThread(MidiFileDatabase database, Notes notes, Proto.Config config) {
    this.database = database;
    this.notes = notes;
    this.config = config;
  }

  @Override
  public void run() {
    org.spcgreenville.deagan.hourly.PeriodicChimeRunnable runnable = new org.spcgreenville.deagan.hourly.PeriodicChimeRunnable(database, notes, config);
    LocalDateTime today = LocalDateTime.now();
    long initialDelayMillis = getMillisUntilNextChime(today);
    ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
        runnable,
        initialDelayMillis,
        TimeUnit.MINUTES.toMillis(15),
        TimeUnit.MILLISECONDS);
    logger.finer("Initial delay millis = " + initialDelayMillis);
    while (true) {
      try {
        Thread.sleep(Long.MAX_VALUE);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  private long getMillisUntilNextChime(LocalDateTime today) {
    if (today.getMinute() == 0 && today.getSecond() == 0) {
      return 0;
    }
    LocalDateTime then;
    if (today.getMinute() < 15) {
      then = today.withMinute(15).withSecond(0).withNano(0);
    } else if (today.getMinute() < 30) {
      then = today.withMinute(30).withSecond(0).withNano(0);
    } else if (today.getMinute() < 45) {
      then = today.withMinute(45).withSecond(0).withNano(0);
    } else {
      then = today.plusHours(1).withMinute(0).withSecond(0).withNano(0);
    }
    return today.until(then, ChronoUnit.MILLIS);
  }
}
