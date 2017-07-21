package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;

public class ExamplesExposureFunctionLoader extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   * @param args The standard tool arguments, not null
   */
  public static void main(final String[] args) {
    new ExamplesExposureFunctionLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    ExamplesExposureFunctionConfigPopulator.populateConfigMaster(configMaster);
  }
}
