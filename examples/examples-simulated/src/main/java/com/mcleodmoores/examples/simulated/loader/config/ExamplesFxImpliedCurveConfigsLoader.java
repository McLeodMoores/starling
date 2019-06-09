/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Populates the config master with FX-implied discounting curves.
 */
@Scriptable
public class ExamplesFxImpliedCurveConfigsLoader extends AbstractTool<ToolContext> {

  /**
   * @param args
   *          the arguments
   */
  public static void main(final String[] args) {
    new ExamplesFxImpliedCurveConfigsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    ExamplesFxImpliedCurveConfigsPopulator.populateConfigMaster(configMaster);
  }
}
