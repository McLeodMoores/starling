/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.simulatedexamples.populator;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;

/**
 *
 */
public class TestExposureFunctionsLoader extends AbstractTool<ToolContext> {

  public static void main(final String[] args) {
    new TestExposureFunctionsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    TestExposureFunctionsPopulator.populateConfigMaster(configMaster);
  }

}
