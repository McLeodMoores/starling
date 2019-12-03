/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.replay;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 *
 *
 * @author yomi
 */
public final class BloombergTicksCollectorLauncher {

  /* package */static final String CONFIG_XML_CLASSPATH = "/com/opengamma/bbg/replay/bloomberg-ticks-collector-context.xml";

  private final ConfigurableApplicationContext _context;

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(BloombergTicksCollectorLauncher.class);

  /**
   *
   */
  private BloombergTicksCollectorLauncher() {
    final ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(CONFIG_XML_CLASSPATH);
    _context = context;
    Runtime.getRuntime().addShutdownHook(new ShutDownThread());
  }

  public void run() {
    _context.start();
  }

  public void exit() {
    _context.stop();
  }

  /**
   * Starts the Bloomberg Ticks Collector.
   *
   * @param args Not needed
   */
  public static void main(final String[] args) { // CSIGNORE

    int duration = 0;
    final CommandLineParser parser = new PosixParser();
    final Options options = new Options();
    options.addOption("d", "duration", true, "minutes to run");
    try {
      final CommandLine cmd = parser.parse(options, args);
      if (cmd.hasOption("duration")) {
        duration = Integer.parseInt(cmd.getOptionValue("duration"));
      }
    } catch (final ParseException exp) {
      LOGGER.error("Option parsing failed: {}", exp.getMessage());
      return;
    }

    final BloombergTicksCollectorLauncher launcher = new BloombergTicksCollectorLauncher();
    launcher.run();
    if (duration > 0) {
      try {
        Thread.sleep(duration * 60 * 1000);
      } catch (final InterruptedException e) {
      }
      launcher.exit();
    }
  }

  private class ShutDownThread extends Thread {
    @Override
    public void run() {
      exit();
    }
  }

}
