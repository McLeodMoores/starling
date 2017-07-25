/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.config;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.config.ConfigMaster;

/**
 *
 */
public class ExamplesFxImpliedCurveConfigsLoader extends AbstractTool<ToolContext> {

  public static void main(final String[] args) {
    new ExamplesFxImpliedCurveConfigsLoader().invokeAndTerminate(args);
  }

  @Override
  protected void doRun() throws Exception {
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    ExamplesFxImpliedCurveConfigsPopulator.populateConfigMaster(configMaster);
  }
}
