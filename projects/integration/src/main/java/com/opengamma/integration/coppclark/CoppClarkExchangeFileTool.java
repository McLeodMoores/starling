/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.coppclark;

import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.scripts.Scriptable;

/**
 * Loads the exchange data from the Copp-Clark data source.
 * <p>
 * This will merge the input with the data already in the database.
 */
@Scriptable
public class CoppClarkExchangeFileTool extends AbstractTool<ToolContext> {

  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    new CoppClarkExchangeFileTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final ExchangeMaster master = getToolContext().getExchangeMaster();
    CoppClarkExchangeFileReader.createPopulated(master);
  }

}
