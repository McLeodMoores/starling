/**
 * Copyright (C) 2014 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.mcleodmoores.integration.simulatedexamples;

import com.opengamma.component.OpenGammaComponentServer;
import com.opengamma.util.ShutdownUtils;

/**
 * A component server that uses Finmath integration functions and simulated market data. By default,
 * the server is started in development mode.
 * <p>
 * Before the server can be started, the example HSQL database must have been set up, either from the
 * command line or a launch configuration that uses {@link com.mcleodmoores.integration.simulatedexamples.TestFinmathDatabaseCreator}.
 */
public class TestFinmathSimulatedComponentServer extends OpenGammaComponentServer {

  /**
   * Main method to start an OpenGamma server. If the command line is empty, the development
   * configuration file is used.
   * @param clArgs The arguments.
   */
  public static void main(final String[] clArgs) {
    String[] args;
    if (clArgs.length == 0) {
      args = new String[] {"-v", "classpath:/fullstack/fullstack-dev.properties"};
    } else {
      args = clArgs;
    }
    if (!new TestFinmathSimulatedComponentServer().run(args)) {
      ShutdownUtils.exit(-1);
    }
  }
}
