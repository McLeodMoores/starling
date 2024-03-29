/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.StartupUtils;

/**
 * A monitor that allows the Component-based server to be shutdown remotely.
 */
public final class OpenGammaComponentServerMonitor extends Thread {

  private static final Logger LOGGER = LoggerFactory.getLogger(ComponentManager.class);
  private static final String SECRET_PROPERTY = "commandmonitor.secret";
  private static final String PORT_PROPERTY = "commandmonitor.port";
  private static final int DEFAULT_PORT = 8079;
  private static final int COMMAND_LENGTH = 256;

  static {
    StartupUtils.init();
  }

  private ComponentRepository _repo;
  private String _secret;
  private DatagramSocket _socket;

  /**
   * Creates an instance.
   *
   * @param repo  the repository, not null
   * @param secret  the secret phrase, not null
   * @param port  the port number
   */
  private OpenGammaComponentServerMonitor(final ComponentRepository repo, final String secret, final int port) {
    _repo = repo;
    _secret = secret;

    setDaemon(true);
    setName("OpenGammaComponentServerMonitor");
    try {
      _socket = new DatagramSocket(port, InetAddress.getByName("127.0.0.1"));
    } catch (final Exception e) {
      LOGGER.warn("Failed to create listening socket, monitor will not be available");
      return;
    }
  }

  /**
   * Listens to the socket and stops the server when commanded.
   */
  @Override
  public void run() {
    while (true) {
      final byte[] buffer = new byte[COMMAND_LENGTH];
      final DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
      try {
        _socket.receive(packet);
      } catch (final IOException ex) {
        LOGGER.warn("Error while receiving command packet");
        continue;
      }
      final String received = new String(packet.getData(), 0, packet.getLength());

      if (!received.matches("secret:" + _secret + "\\s+command:\\w+\\s*")) {
        LOGGER.debug("Malformed command or wrong secret");
        continue;
      }

      final String command = received.replaceAll("secret:" + _secret + "\\s+command:(\\w+)\\s*", "$1");
      LOGGER.debug("Received command \"{}\"", command);

      if (command.equals("stop")) {
        handleStop();
      } else if (command.equals("exit")) {
        handleExit();
      } else {
        LOGGER.debug("Unknown command \"{}\"", command);
      }
    }
  }

  private boolean isReady() {
    return _socket != null;
  }

  private void handleStop() {
    if (_repo.isRunning()) {
      _repo.stop();
    }
  }

  private void handleExit() {
    handleStop();
    System.exit(0);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the monitor based on a {@link ComponentRepository}.
   *
   * @param repo  the repository, not null
   */
  public static void create(final ComponentRepository repo) {
    final String secret = System.getProperty(SECRET_PROPERTY);
    if (secret != null) {
      final String port = System.getProperty(PORT_PROPERTY, Integer.toString(DEFAULT_PORT));
      create(repo, secret, Integer.parseInt(port));
    }
  }

  /**
   * Creates the monitor based on a {@link ComponentRepository}.
   *
   * @param repo  the repository, not null
   * @param secret  the secret phrase, not null
   * @param port  the port number
   */
  public static void create(final ComponentRepository repo, final String secret, final int port) {
    final OpenGammaComponentServerMonitor monitor = new OpenGammaComponentServerMonitor(repo, secret, port);
    if (monitor.isReady()) {
      monitor.start();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Main method acting as a client of the monitor.
   * <p>
   * A single argument may be passed, either "stop" or "exit".
   * The default is "exit" (which is a hard stop).
   *
   * @param args  the arguments
   * @throws Exception if an error occurs
   */
  public static void main(final String[] args) throws Exception {  // CSIGNORE
    final InetAddress address = InetAddress.getByName("127.0.0.1");
    final String secret = System.getProperty(SECRET_PROPERTY);
    if (secret == null) {
      System.out.println("Secret must be specified on the command line using " + SECRET_PROPERTY);
      System.exit(1);
    }
    final String port = System.getProperty(PORT_PROPERTY, Integer.toString(DEFAULT_PORT));
    final String command = args.length == 1 ? args[0] : "exit";

    final String toSend = "secret:" + secret + " command:" + command + "\n";
    final byte[] sBuffer = toSend.getBytes();

    System.out.println("Sending \"" + command + "\" to server.");
    DatagramSocket socket = null;
    try {
      socket = new DatagramSocket();
      final DatagramPacket packet = new DatagramPacket(sBuffer, sBuffer.length, address, Integer.parseInt(port));
      socket.send(packet);
    } catch (final Exception e) {
      if (socket != null) {
        ReflectionUtils.invokeNoArgsNoException(socket, "close");
      }
      System.out.println("Send failed...");
      System.exit(1);
    }
    System.exit(0);
  }
}
