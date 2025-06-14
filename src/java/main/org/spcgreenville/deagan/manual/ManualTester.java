package org.spcgreenville.deagan.manual;

import org.spcgreenville.deagan.ConfigReader;
import org.spcgreenville.deagan.Proto;
import org.spcgreenville.deagan.logical.Inputs;
import org.spcgreenville.deagan.logical.Outputs;
import org.spcgreenville.deagan.logical.Relay;
import org.spcgreenville.deagan.logical.Relays;
import org.spcgreenville.deagan.physical.HardwareConfig;

import java.io.IOException;

public class ManualTester {
  public static int STRIKE_DELAY_MS = 2500;

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println("args path_to_config");
      System.exit(-1);
    }
    new ManualTester(args[0]).run();
  }

  private class ChangeRinger extends Thread {
    private Relays relays;
    private boolean stopRequested;
    private boolean isStopped;

    public ChangeRinger(Relays relays) {
      this.relays = relays;
    }

    public void requestStop() {
      this.stopRequested = true;
    }

    public boolean isStopped() {
      return isStopped;
    }

    @Override
    public void run() {
      while (!stopRequested) {
        for (int relayIndex = 0; relayIndex < relays.length(); relayIndex++) {
          if (stopRequested) {
            break;
          }
          relays.get(relayIndex).close();
          if (stopRequested) {
            relays.get(relayIndex).open();
            break;
          }
          try {
            Thread.sleep(STRIKE_DELAY_MS);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          relays.get(relayIndex).open();
          if (stopRequested) {
            break;
          }
          try {
            Thread.sleep(STRIKE_DELAY_MS);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      }
      isStopped = true;
    }
  }

  private Proto.Config config;
  private ChangeRinger changeRinger;

  public ManualTester(String pathToConfig) throws IOException {
    this.config = new ConfigReader().readConfig(pathToConfig);
  }

  public void run() throws IOException {
    System.loadLibrary("deagan");
    
    HardwareConfig hardwareConfig = new HardwareConfig(config);
    Relays relays = new Outputs(hardwareConfig);
    Inputs inputs = new Inputs(config, hardwareConfig);
    relays.initialize();
    inputs.initialize();

    String lastCommand = "";
    while (true) {
      try {
        System.out.printf("0-%d to toggle relays; s0-s%d for 1/2 second strike; " +
                "h to read on/off switch; c for on/off change ring; x to exit\n",
            relays.length() - 1, relays.length() - 1);
        String line = System.console().readLine().trim();
        if (line.isEmpty()) {
          line = lastCommand;
        } else {
          lastCommand = line;
        }
        if (line.equals("c")) {
          if (changeRinger != null && !changeRinger.isStopped()) {
            changeRinger.requestStop();
            continue;
          }
          changeRinger = new ChangeRinger(relays);
          changeRinger.start();
          continue;
        }
        if (line.equals("x")) {
          return;
        }
        boolean strike = false;
        if (line.startsWith("s")) {
          strike = true;
          line = line.substring(1);
        }
        int index = Integer.parseInt(line);
        Relay[] allRelays = relays.getRelays();
        if (index < 0 || index >= allRelays.length) {
          System.out.println("No relay by that number");
          continue;
        }
        Relay targetRelay = allRelays[index];

        if (strike) {
          System.out.println("striking relay " + index);
          targetRelay.close();
          try {
            Thread.sleep(500);
          } catch (InterruptedException ie) {
          }
          targetRelay.open();
        } else {
          if (targetRelay.isClosed()) {
            System.out.println("opening relay " + index);
            targetRelay.open();
          } else {
            System.out.println("closing relay " + index);
            targetRelay.close();
          }
        }

        for (int i = 0; i < relays.length(); i++) {
          Relay relay = relays.get(i);
          System.out.printf("  rel%d=%s%s%s", i, i == index ? "**" : "",
              relay.isClosed() ? "on " : "off", i == index ? "**" : "");
          if ((i % 8) == 0) {
            System.out.println();
          }
        }
        System.out.println();

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
