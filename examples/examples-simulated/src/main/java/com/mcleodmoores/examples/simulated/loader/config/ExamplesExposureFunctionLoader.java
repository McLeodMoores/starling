/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Populates the config master with exposure functions.
 */
@Scriptable
public class ExamplesExposureFunctionLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * 
   * @param args
   *          The standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new ExamplesExposureFunctionLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    ExamplesExposureFunctionConfigsPopulator.populateConfigMaster(configMaster);
  }
}
