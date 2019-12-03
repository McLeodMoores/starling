/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.viewer.status;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.integration.viewer.status.impl.BloombergReferencePortfolioMaker;
import com.opengamma.integration.viewer.status.impl.ViewStatusCalculationWorker;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.scripts.Scriptable;

/**
 * The view status tool.
 */
@Scriptable
public class ViewStatusTool extends AbstractTool<ToolContext> {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(ViewStatusTool.class);

  // -------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args
   *          the standard tool arguments, not null
   */
  public static void main(final String[] args) { // CSIGNORE
    new ViewStatusTool().invokeAndTerminate(args);
  }

  // -------------------------------------------------------------------------
  @Override
  protected void doRun() throws Exception {
    final ViewStatusOption option = ViewStatusOption.getViewStatusReporterOption(getCommandLine(), getToolContext());

    final String portfolioName = option.getPortfolioName();
    UniqueId portfolioId = null;
    if (portfolioName == null) {
      portfolioId = createReferencePortfolio();
    } else {
      portfolioId = findPortfolioId(portfolioName);
    }
    if (portfolioId == null) {
      throw new OpenGammaRuntimeException("Couldn't find portfolio " + portfolioName);
    }
    generateViewStatusReport(portfolioId, option);
  }

  private void generateViewStatusReport(final UniqueId portfolioId, final ViewStatusOption option) {

    final ViewStatusCalculationWorker calculationWorker = new ViewStatusCalculationWorker(getToolContext(), portfolioId, option);
    final ViewStatusResultAggregator resultAggregator = calculationWorker.run();

    final ViewStatusResultProducer resultProducer = new ViewStatusResultProducer();
    final String statusResult = resultProducer.statusResult(resultAggregator, option.getFormat(), option.getAggregateType());
    try {
      final File outputFile = option.getOutputFile();
      LOGGER.debug("Writing status report into : {}", outputFile.getPath());
      FileUtils.writeStringToFile(outputFile, statusResult);
    } catch (final IOException ex) {
      throw new OpenGammaRuntimeException("Error writing view-status report to " + option.getOutputFile().toString(), ex);
    }

  }

  private UniqueId findPortfolioId(final String portfolioName) {
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioName);
    final PortfolioSearchResult searchResult = getToolContext().getPortfolioMaster().search(searchRequest);
    UniqueId portfolioId = null;
    if (searchResult.getFirstPortfolio() != null) {
      portfolioId = searchResult.getFirstPortfolio().getUniqueId().toLatest();
    }
    return portfolioId;
  }

  private UniqueId createReferencePortfolio() {
    final ToolContext toolContext = getToolContext();
    final BloombergReferencePortfolioMaker portfolioMaker = new BloombergReferencePortfolioMaker(toolContext.getPortfolioMaster(),
        toolContext.getPositionMaster(), toolContext.getSecurityMaster());
    portfolioMaker.run();
    return findPortfolioId(BloombergReferencePortfolioMaker.PORTFOLIO_NAME);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Options createOptions(final boolean contextProvided) {
    final Options toolOptions = super.createOptions(contextProvided);

    final Options viewStatusOptions = ViewStatusOption.createOptions();
    for (final Option option : (Collection<Option>) viewStatusOptions.getOptions()) {
      LOGGER.debug("adding {} to tool options", option);
      toolOptions.addOption(option);
    }
    return toolOptions;
  }

}
