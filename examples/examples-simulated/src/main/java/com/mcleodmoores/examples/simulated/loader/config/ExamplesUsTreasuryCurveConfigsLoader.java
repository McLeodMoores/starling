/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Populates the config master with government curve bond configurations.
 */
@Scriptable
public class ExamplesUsTreasuryCurveConfigsLoader extends AbstractTool<ToolContext> {

  /**
   * @param args
   *          the arguments
   */
  public static void main(final String[] args) {
    new ExamplesUsTreasuryCurveConfigsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    ExamplesUsTreasuryCurveConfigsPopulator.populateConfigMaster(configMaster);
  }
}
