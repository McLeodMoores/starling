/**
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Instant;

import com.opengamma.DataNotFoundException;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.integration.tool.portfolio.PortfolioWriter;
import com.opengamma.master.DocumentVisibility;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * Class to build load and save portfolios and provide lists, test for existence etc.
 */
public final class PortfolioManager {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(PortfolioManager.class);
  /** A thread pool to use when resolving the portfolio */
  private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
  /** The portfolio master */
  private final PortfolioMaster _portfolioMaster;
  /** The position master */
  private final PositionMaster _positionMaster;
  /** The position source */
  private final PositionSource _positionSource;
  /** The security master */
  private final SecurityMaster _securityMaster;
  /** The security source */
  private final SecuritySource _securitySource;

  /**
   * Create a PortfolioManager object with the provided tool context.
   * This tool context must contain a portfolio master, position master, position source, security master and security source.
   * @param toolContext  the tool context
   */
  public PortfolioManager(final ToolContext toolContext) {
    ArgumentChecker.notNull(toolContext, "toolContext");
    _portfolioMaster = ArgumentChecker.notNull(toolContext.getPortfolioMaster(), "toolContext.portfolioMaster");
    _positionMaster = ArgumentChecker.notNull(toolContext.getPositionMaster(), "toolContext.positionMaster");
    _positionSource = ArgumentChecker.notNull(toolContext.getPositionSource(), "toolContext.positionSource");
    _securityMaster = ArgumentChecker.notNull(toolContext.getSecurityMaster(), "toolContext.securityMaster");
    _securitySource = ArgumentChecker.notNull(toolContext.getSecuritySource(), "toolContext.securitySource");
  }

  /**
   * Create a PortfolioManager object with the required data masters
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param positionSource  the position source, not null
   * @param securityMaster  the security master, not null
   * @param securitySource  the security source, not null
   */
  public PortfolioManager(final PortfolioMaster portfolioMaster, final PositionMaster positionMaster, final PositionSource positionSource,
      final SecurityMaster securityMaster, final SecuritySource securitySource) {
    _portfolioMaster = ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    _positionMaster = ArgumentChecker.notNull(positionMaster, "positionMaster");
    _positionSource = ArgumentChecker.notNull(positionSource, "positionSource");
    _securityMaster = ArgumentChecker.notNull(securityMaster, "securityMaster");
    _securitySource = ArgumentChecker.notNull(securitySource, "securitySource");
  }

  /**
   * Test to see if a portfolio with the provided key exists.
   * @param portfolioKey  a key for the portfolio, not null
   * @return true, if a portfolio with this name exists
   */
  public boolean portfolioExists(final PortfolioKey portfolioKey) {
    ArgumentChecker.notNull(portfolioKey, "portfolioKey");
    if (portfolioKey.hasUniqueId()) {
      try {
        final PortfolioDocument portfolioDocument = _portfolioMaster.get(portfolioKey.getUniqueId());
        if (portfolioKey.getName().equals(portfolioDocument.getPortfolio().getName())) {
          return true; // sanity check
        }
        LOGGER.error("Portfolio name {} in PortfolioKey is inconsistent with embedded uniqueId {} so indicating not present",
            portfolioKey.getName(), portfolioKey.getUniqueId());
        return false;
      } catch (final DataNotFoundException dnfe) {
        LOGGER.error("Problem getting portfolio for key: {}", dnfe.getMessage());
        return false;
      } catch (final AuthorizationException ae) {
        LOGGER.error("Not authorized to get portfolio with key {}", ae.getMessage());
        return false;
      }
    }
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setName(portfolioKey.getName());
    searchRequest.setIncludePositions(false);
    final PortfolioSearchResult searchResult = _portfolioMaster.search(searchRequest);
    return !searchResult.getDocuments().isEmpty();
  }

  /**
   * Load the latest version of the named portfolio.
   * @param portfolioKey  the key for the portfolio, not null
   * @return the loaded portfolio with all links to positions, trade and securities resolved
   */
  public Portfolio loadPortfolio(final PortfolioKey portfolioKey) {
    return loadPortfolio(portfolioKey, Instant.now());
  }

  /**
   * Load a version of the named portfolio at a time in the past.
   * @param portfolioKey  the key for the portfolio, not null
   * @param versionAsOf  the instant at which to look up the portfolio/positions/trades/securities, not null
   * @return the loaded portfolio with all links to positions, trade and securities resolved
   */
  public Portfolio loadPortfolio(final PortfolioKey portfolioKey, final Instant versionAsOf) {
    ArgumentChecker.notNull(portfolioKey, "portfolioKey");
    ArgumentChecker.notNull(versionAsOf, "versionAsOf");
    final VersionCorrection vc = VersionCorrection.ofVersionAsOf(versionAsOf);
    ManageablePortfolio manageablePortfolio;
    if (portfolioKey.hasUniqueId()) {
      final PortfolioDocument document = _portfolioMaster.get(portfolioKey.getUniqueId().getObjectId(), vc);
      manageablePortfolio = document.getValue();
    } else {
      final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
      searchRequest.setName(portfolioKey.getName());
      searchRequest.setVersionCorrection(vc);
      searchRequest.setIncludePositions(false);
      final PortfolioSearchResult searchResult = _portfolioMaster.search(searchRequest);
      if (searchResult.getFirstPortfolio() != null) {
        manageablePortfolio = searchResult.getFirstPortfolio();
      } else {
        throw new OpenGammaRuntimeException("Could not find portfolio with key " + portfolioKey + " or there were multiple portfolios with this name");
      }
    }
    // This bit is odd.  For historical reasons, ManageablePortfolio doesn't implement Portfolio.  The code to convert it to Portfolio is in the source.
    Portfolio portfolio = _positionSource.getPortfolio(manageablePortfolio.getUniqueId(), vc); // TODO why do we have to pass the vc here?!?
    // Resolve all the securities, positions and trades.
    portfolio = PortfolioCompiler.resolvePortfolio(portfolio, EXECUTOR_SERVICE, _securitySource, VersionCorrection.ofVersionAsOf(versionAsOf));
    return portfolio;
  }

  /**
   * Gets the portfolio keys for all visible portfolios in the master.
   * @return a list of PortfolioKeys representing each of the portfolios loaded, not null
   * Order is preserved
   */
  public Set<PortfolioKey> getPortfolioList() {
    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    searchRequest.setIncludePositions(false);
    final PortfolioSearchResult result = _portfolioMaster.search(searchRequest);
    final Set<PortfolioKey> results = new LinkedHashSet<>();
    for (final PortfolioDocument portfolioDoc : result.getDocuments()) {
      if (portfolioDoc.getVisibility() == DocumentVisibility.VISIBLE) {
        results.add(PortfolioKey.of(portfolioDoc.getPortfolio().getName(), portfolioDoc.getUniqueId()));
      }
    }
    return results;
  }

  /**
   * Save or update a portfolio under the name set in the portfolio object.
   * The name of the portfolio must not be the empty string.
   * @param portfolio  the portfolio to save, not null
   * @return a key for the portfolio
   */
  public PortfolioKey savePortfolio(final Portfolio portfolio) {
    ArgumentChecker.notNull(portfolio, "portfolio");
    ArgumentChecker.notBlank(portfolio.getName(), "portfolio.name");
    final PortfolioWriter writer = new PortfolioWriter(true, true, _portfolioMaster, _positionMaster, _securityMaster);
    final UniqueId id = writer.write(portfolio);
    return PortfolioKey.of(portfolio.getName(), id);
  }

  /**
   * Delete a portfolio.  This may be undoable by examining history in the underlying master.
   * The PortfolioKey name cannot contain wildcard '*' characters to prevent accidental mass deletion.
   * @param portfolioKey  the key for the portfolio to delete, not null
   */
  public void deletePortfolio(final PortfolioKey portfolioKey) {
    ArgumentChecker.notNull(portfolioKey, "portfolioKey");
    if (portfolioKey.getName().contains("*")) {
      throw new OpenGammaRuntimeException("Too dangerous to allow wildcard delete, iterate over list");
    }
    if (portfolioKey.hasUniqueId()) {
      _portfolioMaster.remove(portfolioKey.getUniqueId());
    } else {
      final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
      searchRequest.setIncludePositions(false);
      searchRequest.setName(portfolioKey.getName());
      final PortfolioSearchResult result = _portfolioMaster.search(searchRequest);
      if (result.getFirstPortfolio() != null) {
        _portfolioMaster.remove(result.getFirstPortfolio().getUniqueId());
      } else {
        throw new OpenGammaRuntimeException("No portfolio called " + portfolioKey.getName() + " could be found");
      }
    }
  }

  /**
   * Enum representing the extent of deletion.
   */
  public enum DeleteScope {
    /**
     * Delete the portfolio structure.
     */
    PORTFOLIO,
    /**
     * Delete the positions and trades.
     */
    POSITION,
    /**
     * Delete the securities referred to by positions and trades.
     */
    SECURITY
  };

  /**
   * Delete a portfolio to a given 'depth'. Normal portfolio deletion leaves positions, trade and securities untouched.
   * This may be a good thing if they are shared with other portfolios, but not otherwise.
   * <p>
   * The PortfolioKey name cannot contain wildcard '*' characters to prevent accidental mass deletion.
   * @param portfolioKey  the key for the portfolio to process, not null
   * @param whatToDelete  an enumset containing values to indicate what to delete, not null
   */
  public void delete(final PortfolioKey portfolioKey, final EnumSet<DeleteScope> whatToDelete) {
    ArgumentChecker.notNull(portfolioKey, "portfolioKey");
    ArgumentChecker.notNull(whatToDelete, "whatToDelete");
    if (portfolioKey.getName().contains("*")) {
      throw new OpenGammaRuntimeException("Too dangerous to allow wildcard delete, iterate over list");
    }
    if (portfolioKey.hasUniqueId()) {
      ManageablePortfolio portfolio;
      try {
        final PortfolioDocument document = _portfolioMaster.get(portfolioKey.getUniqueId());
        portfolio = document.getPortfolio();
      } catch (final DataNotFoundException e) {
        throw new OpenGammaRuntimeException("No portfolio for key " + portfolioKey + " could be found");
      }
      delete(portfolio.getRootNode(), whatToDelete, VersionCorrection.LATEST);
      _portfolioMaster.remove(portfolio.getUniqueId());
    } else {
      final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
      searchRequest.setIncludePositions(false);
      searchRequest.setName(portfolioKey.getName());
      final PortfolioSearchResult result = _portfolioMaster.search(searchRequest);
      if (result.getFirstPortfolio() != null) {
        final ManageablePortfolio portfolio = result.getFirstPortfolio();
        delete(portfolio.getRootNode(), whatToDelete, VersionCorrection.LATEST);
        _portfolioMaster.remove(result.getFirstPortfolio().getUniqueId());
      } else {
        throw new OpenGammaRuntimeException("No portfolio called " + portfolioKey.getName() + " could be found");
      }
    }
  }

  /**
   * Deletes a portfolio to a given depth - portfolio only, portfolio, positions and trades or portfolio, positions and trades,
   * and securities.
   * @param node  the portfolio node
   * @param whatToDelete  the scope of the deletions
   * @param vc  the version correction of the objects to be deleted
   */
  private void delete(final ManageablePortfolioNode node, final EnumSet<DeleteScope> whatToDelete, final VersionCorrection vc) {
    final Set<UniqueId> deletedSecurities = new HashSet<>();
    if (whatToDelete.contains(DeleteScope.POSITION) || whatToDelete.contains(DeleteScope.SECURITY)) {
      for (final ObjectId positionId : node.getPositionIds()) {
        if (whatToDelete.contains(DeleteScope.SECURITY)) {
          final PositionDocument positionDocument = _positionMaster.get(positionId, vc);
          if (positionDocument != null) {
            final ManageablePosition position = positionDocument.getValue();
            for (final ManageableTrade trade : position.getTrades()) {
              try {
                final Security sec = trade.getSecurityLink().resolve(_securitySource);
                if (sec != null) {
                  if (!deletedSecurities.contains(sec.getUniqueId())) {
                    try {
                      _securityMaster.remove(sec.getUniqueId());
                    } catch (final DataNotFoundException dnfe) {
                      // not a problem
                    }
                    deletedSecurities.add(sec.getUniqueId());
                  }
                } else {
                  LOGGER.warn("Trade {} contained invalid security link {} so can't delete security", trade);
                }
              } catch (final DataNotFoundException e) {
                LOGGER.warn("Trade {} contained invalid security link {} so can't delete security", trade);
              }
            }
            try {
              //TODO this always fails - unnecessary code?
              final Security security = position.getSecurityLink().resolve(_securitySource);
              if (security != null) {
                if (!deletedSecurities.contains(security.getUniqueId())) {
                  try {
                    _securityMaster.remove(security.getUniqueId());
                  } catch (final DataNotFoundException dnfe) {
                    // not a problem
                  }
                  deletedSecurities.add(security.getUniqueId());
                }
              } else {
                LOGGER.warn("Position {} contained invalid security link {} so can't delete security", position);
              }
            } catch (final DataNotFoundException e) {
              LOGGER.warn("Position {} contained invalid security link {} so can't delete security", position);
            }
          }
        }
        if (whatToDelete.contains(DeleteScope.POSITION)) {
          try {
            _positionMaster.remove(positionId);
          } catch (final DataNotFoundException dnfe) {
            // not a problem.
          }
        }
      }
      for (final ManageablePortfolioNode child : node.getChildNodes()) {
        delete(child, whatToDelete, vc);
      }
    }
  }
}
