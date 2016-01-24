/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.testutils;

import java.util.HashMap;

import com.mcleodmoores.starling.client.component.StarlingToolContext;
import com.opengamma.component.ComponentManager;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.OpenGammaComponentServer;

/**
 *
 */
public class StarlingTestUtils {

  /**
   * Creates a tool context for use in tests.
   * @return  the tool context
   */
  public static StarlingToolContext getToolContext() {
    final OpenGammaComponentServer server = new OpenGammaComponentServer();
    final ComponentManager componentManager = server.createManager("classpath:/inmemory/inmemory.properties", new HashMap<String, String>());
    final ComponentRepository repository = componentManager.getRepository();
    componentManager.init();
    componentManager.start();
    return repository.getInstance(StarlingToolContext.class, "tool");
  }

  /**
   * Creates a tool context for use in tests.
   * @return  the tool context
   */
  public static StarlingToolContext getToolContext(final String propertiesFilePath) {
    final OpenGammaComponentServer server = new OpenGammaComponentServer();
    final ComponentManager componentManager = server.createManager("classpath:" + propertiesFilePath, new HashMap<String, String>());
    final ComponentRepository repository = componentManager.getRepository();
    componentManager.init();
    componentManager.start();
    return repository.getInstance(StarlingToolContext.class, "tool");
  }
}
