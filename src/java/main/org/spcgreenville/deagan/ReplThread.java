package org.spcgreenville.deagan;

import org.spcgreenville.deagan.logical.Notes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReplThread extends Thread {
  private final Logger logger = Logger.getLogger(ReplThread.class.getName());
  private final Notes notes;
  private final Proto.Config config;

  public ReplThread(Notes notes, Proto.Config config) {
    this.notes = notes;
    this.config = config;
    setName("ReplThread");
    setDaemon(true);
  }

  private int sequence = 0;

  private Proto.Response getResponse(Proto.Request request) {
    Proto.Response.Builder responseBuilder = Proto.Response.newBuilder().setStatus(Proto.Status.STATUS_SUCCESS);
    if (request.getChime()) {
      responseBuilder.setStatusMessage("Chiming");
    } else if (request.hasLatchRelay()) {
      int relay = request.getLatchRelay();
      responseBuilder.setStatusMessage("Latching relay " + relay);
    } else if (request.getTestConnection()) {
      responseBuilder.setStatusMessage("Test successful " + sequence++);
    } else {
      responseBuilder.setStatus(Proto.Status.)
    }
    return responseBuilder.build();
  }

  public void run() {
    SocketAddress socketAddress =
        new InetSocketAddress(config.getControlHostname(), config.getControlPort());
    while (true) {
      try (Socket socket = new Socket()) {
        socket.setSoTimeout(0);
        socket.connect(socketAddress);
        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        while (true) {
          int length = inputStream.readInt();
          byte[] b = new byte[length];
          if (inputStream.read(b, 0, length) == -1) {
            break;
          }
          Proto.Request request = Proto.Request.parseFrom(b);
          Proto.Response response = getResponse(request);
          b = response.toByteArray();
          outputStream.writeInt(b.length);
          outputStream.write(b);
        }
      } catch (IOException ioe) {
        logger.log(Level.WARNING, "Exception", ioe);
      }
    }
  }
}
