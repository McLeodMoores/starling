/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import com.google.common.collect.Lists;
import com.opengamma.component.tool.AbstractTool;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.scripts.Scriptable;

/**
 * Tool to aggregate portfolios
 */
@Scriptable
public class PortfolioMergeTool extends AbstractTool<ToolContext> {

  private static final String INPUT_PORTFOLIO_NAMES = "n";
  private static final String NEW_PORTFOLIO_NAME = "m";
  private static final String FLATTEN_OPTION_NAME = "f";

  //-------------------------------------------------------------------------
  /**
   * Main method to run the tool.
   *
   * @param args  the standard tool arguments, not null
   */
  public static void main(final String[] args) {  // CSIGNORE
    new PortfolioMergeTool().invokeAndTerminate(args);
  }

  //-------------------------------------------------------------------------
  @Override
  protected void doRun() {
    final String[] portfolioNames = getCommandLine().getOptionValues(INPUT_PORTFOLIO_NAMES);
    final List<ManageablePortfolio> sourcePortfolios = new ArrayList<>();
    for (final String sourcePortfolioName : portfolioNames) {
      System.out.println("Loading portfolio " + sourcePortfolioName);
      sourcePortfolios.add(loadPortfolio(sourcePortfolioName));
    }
    String newName;
    if (getCommandLine().hasOption(NEW_PORTFOLIO_NAME)) {
      newName = getCommandLine().getOptionValue(NEW_PORTFOLIO_NAME);
      System.out.println("Using supplied destination portfolio name:" + newName);
    } else {
      newName = mergePortfolioNames(sourcePortfolios);
      System.out.println("No supplied destination portfolio name, generating name:" + newName);
    }
    System.out.println("Merging...");
    final ManageablePortfolio resultingPortfolio = mergePortfolios(sourcePortfolios, newName, getCommandLine().hasOption(FLATTEN_OPTION_NAME));
    System.out.println("Finished merge, storing...");
    storePortfolio(resultingPortfolio);
    System.out.println("Done");
  }

  private void storePortfolio(final ManageablePortfolio resultingPortfolio) {
    final PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    final String name = resultingPortfolio.getName();
    final PortfolioSearchRequest request = new PortfolioSearchRequest();
    request.setName(name);
    final PortfolioSearchResult result = portfolioMaster.search(request);
    final int numResults = result.getDocuments().size();
    if (numResults > 1) {
      System.err.println("There are multiple matching resulting portfolios, don't know which to update, so aborting.");
      System.exit(1);
    } else if (numResults == 0) {
      final PortfolioDocument document = new PortfolioDocument(resultingPortfolio);
      System.out.println("No existing result portfolio, creating a fresh one");
      portfolioMaster.add(document);
    } else if (numResults == 1) {
      final PortfolioDocument firstDocument = result.getFirstDocument();
      firstDocument.setPortfolio(resultingPortfolio);
      System.out.println("Found existing result portfolio, updating it");
      portfolioMaster.update(firstDocument);
    }
  }

  public static List<ObjectId> flatten(final ManageablePortfolio inputPortfolio) {
    final List<ObjectId> positions = Lists.newArrayList();
    flatten(inputPortfolio.getRootNode(), positions);
    return positions;
  }

  private static void flatten(final ManageablePortfolioNode portfolioNode, final List<ObjectId> flattenedPortfolio) {
    flattenedPortfolio.addAll(portfolioNode.getPositionIds());
    for (final ManageablePortfolioNode subNode : portfolioNode.getChildNodes()) {
      flatten(subNode, flattenedPortfolio);
    }
  }

  private ManageablePortfolio mergePortfolios(final List<ManageablePortfolio> sourcePortfolios, final String newName, final boolean flatten) {
    final ManageablePortfolio mergedPortfolio = new ManageablePortfolio(newName);
    for (final ManageablePortfolio portfolio : sourcePortfolios) {
      final ManageablePortfolioNode mergedRootNode = mergedPortfolio.getRootNode();
      if (flatten) {
        final List<ObjectId> positions = flatten(portfolio);
        for (final ObjectId positionId : positions) {
          mergedRootNode.addPosition(positionId);
        }
      } else {
        mergedRootNode.addChildNode(portfolio.getRootNode());
      }
    }
    return mergedPortfolio;
  }

  private String mergePortfolioNames(final List<ManageablePortfolio> sourcePortfolios) {
    final StringBuilder sb = new StringBuilder();
    sb.append("Merger of (");
    final Iterator<ManageablePortfolio> iter = sourcePortfolios.iterator();
    while (iter.hasNext()) {
      final ManageablePortfolio next = iter.next();
      sb.append(next.getName().trim());
      if (iter.hasNext()) {
        sb.append(",");
      }
    }
    sb.append(")");
    return sb.toString();
  }

  private ManageablePortfolio loadPortfolio(final String name) {
    final PortfolioMaster portfolioMaster = getToolContext().getPortfolioMaster();
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(name);
    searchRequest.setVersionCorrection(VersionCorrection.LATEST);
    final PortfolioSearchResult result = portfolioMaster.search(searchRequest);
    if (result.getDocuments().size() > 1) {
      System.err.println("Found multiple copies of portfolio called " + name + ", quitting");
      System.exit(1);
    }
    if (result.getDocuments().size() == 0) {
      System.err.println("Portfolio called " + name + " could not be found, quitting");
      System.exit(1);
    }
    return result.getFirstPortfolio();
  }

  @Override
  protected Options createOptions(final boolean contextProvided) {
    final Options options = super.createOptions(contextProvided);

    final Option origNameOption = new Option(
        INPUT_PORTFOLIO_NAMES, "origname", true, "The name of the portfolios to merge (preserves originals)");
    origNameOption.setRequired(true);
    origNameOption.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(origNameOption);

    final Option newNameOption = new Option(
        NEW_PORTFOLIO_NAME, "newname", true, "The new name of the portfolio (default to generated name)");
    newNameOption.setRequired(false);
    options.addOption(newNameOption);

    final Option flattenOption = new Option(
        FLATTEN_OPTION_NAME, "flatten", false, "If specified, flatten the source portfolios prior to merging");
    flattenOption.setRequired(false);
    options.addOption(flattenOption);

    return options;
  }
}
