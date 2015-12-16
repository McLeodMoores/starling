/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.quandl.server;

import com.opengamma.component.OpenGammaComponentServer;
import com.opengamma.util.ShutdownUtils;

/**
 *
 */
public class QuandlComponentServer extends OpenGammaComponentServer {

  public static void main(String[] args) {
    if (args.length == 0) {
      args = new String[] {"-v", "classpath:/fullstack/fullstack-dev.properties"};
    }
    if (!new QuandlComponentServer().run(args)) {
      ShutdownUtils.exit(-1);
    }
  }
}
