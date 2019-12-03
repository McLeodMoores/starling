/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Populates the config master with corporate bond curve configurations and adds the referenced bonds to the security master.
 */
@Scriptable
public class ExamplesCorporateBondCurveConfigsLoader extends AbstractTool<ToolContext> {

  /**
   * @param args
   *          the arguments
   */
  public static void main(final String[] args) {
    new ExamplesCorporateBondCurveConfigsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    final SecurityMaster securityMaster = getToolContext().getSecurityMaster();
    ExamplesCorporateBondCurveConfigsPopulator.populateMasters(configMaster, securityMaster);
  }
}
