/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.component;

import com.opengamma.component.OpenGammaComponentServer;
import com.opengamma.util.ShutdownUtils;

/**
 *
 */
public class ExampleSimulatedComponentServer extends OpenGammaComponentServer {

  public static void main(final String[] clArgs) {
    String[] args;
    if (clArgs.length == 0) {
      args = new String[] { "-v", "classpath:/fullstack/fullstack-mmexamples-dev.properties" };
    } else {
      args = clArgs;
    }
    OpenGammaComponentServer.setStartingMessage("======== STARTING STARLING ========");
    OpenGammaComponentServer.setStartupCompleteMessage("======== STARLING STARTED in ");
    OpenGammaComponentServer.setStartupFailedMessage("======== STARLING STARTUP FAILED ========");
    if (!new ExampleSimulatedComponentServer().run(args)) {
      ShutdownUtils.exit(-1);
    }
  }
}
