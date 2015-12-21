/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to aggregate portfolios
 */
@Scriptable
public class PortfolioCopyMoveTool extends AbstractTool<ToolContext> {

  private static final String ORIGINAL_PORTFOLIO_NAME = "n";
  private static final String NEW_PORTFOLIO_NAME = "m";
  private static final String COPY_OPT = "d";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    new PortfolioCopyMoveTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final PortfolioSearchRequest portfolioSearchRequest = new PortfolioSearchRequest();
    portfolioSearchRequest.setName(getCommandLine().getOptionValue(ORIGINAL_PORTFOLIO_NAME));
    final PortfolioSearchResult portfolioSearchResult = getToolContext().getPortfolioMaster().search(portfolioSearchRequest);

    if (portfolioSearchResult.getDocuments().size() == 0) {
      throw new OpenGammaRuntimeException("No matching portfolio could be found, will not proceed.");
    } else if (portfolioSearchResult.getDocuments().size() > 1) {
      throw new OpenGammaRuntimeException("More than one portfolio matches, will not proceed.");
    } else {
      final PortfolioDocument portfolioDocument = portfolioSearchResult.getFirstDocument();
      final PortfolioSearchRequest portfolioSearchRequest1 = new PortfolioSearchRequest();
      portfolioSearchRequest1.setName(getCommandLine().getOptionValue(NEW_PORTFOLIO_NAME));
      final PortfolioSearchResult portfolioSearchResult1 = getToolContext().getPortfolioMaster().search(portfolioSearchRequest1);
      if (portfolioSearchResult1.getDocuments().size() != 0) {
        throw new OpenGammaRuntimeException("A portfolio with the specified new name already exists, will not proceed.");
      }
      if (getCommandLine().hasOption(COPY_OPT)) {
        // Copy - check if new portfolio name already in use
        portfolioDocument.getPortfolio().setName(getCommandLine().getOptionValue(NEW_PORTFOLIO_NAME));
        getToolContext().getPortfolioMaster().add(portfolioDocument);
      } else {
        // Move
        portfolioDocument.getPortfolio().setName(getCommandLine().getOptionValue(NEW_PORTFOLIO_NAME));
        getToolContext().getPortfolioMaster().update(portfolioDocument);
      }
    }
  }

  @Override
  protected Options createOptions(final boolean contextProvided) {
    final Options options = super.createOptions(contextProvided);

    final Option origNameOption = new Option(
        ORIGINAL_PORTFOLIO_NAME, "origname", true, "The name of the OpenGamma portfolio to copy or rename");
    origNameOption.setRequired(true);
    options.addOption(origNameOption);

    final Option newNameOption = new Option(
        NEW_PORTFOLIO_NAME, "newname", true, "The new name of the OpenGamma portfolio");
    newNameOption.setRequired(true);
    options.addOption(newNameOption);

    final Option copyOption = new Option(
        COPY_OPT, "duplicate", false, "If specified, copy the portfolio instead of moving it (but share referenced positions and securities)");
    copyOption.setRequired(false);
    options.addOption(copyOption);

    return options;
  }
}
