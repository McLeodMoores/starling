/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionOrTrade;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
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
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMasterUtils;
import com.opengamma.util.ArgumentChecker;

/**
 * Persists a portfolio (and its associated securities, positions and
 * trades) into the database masters.
 */
public class PortfolioWriter {

  /**
   * Logger for the class.
   */
  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioWriter.class);

  /**
   * Indicates if the data should actually be written to the masters
   * or whether this is a dry run.
   */
  private final boolean _write;
  /**
   * Flag to switch on
   */
  private final boolean _updateIfExists;

  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;
  private final SecurityMaster _securityMaster;

  /**
   * Create a new persister, initialized so that it is ready to write portfolios as required. Do not use this constructor any
   * more, use the other one.
   *
   * @param write  true if the data should actually be written to the masters
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param securityMaster  the security master, not null
   * @deprecated  explicitly state whether addOrUpdate behaviour is desired.  Despite this being a bug, others may rely on this
   *             constructor for existing behaviour.
   */
  @Deprecated
  public PortfolioWriter(final boolean write, final PortfolioMaster portfolioMaster,
                         final PositionMaster positionMaster, final SecurityMaster securityMaster) {
    this(write, false, portfolioMaster, positionMaster, securityMaster);
  }

  /**
   * Create a new persister, initialized so that it is ready to write (create or update) portfolios as required.
   *
   * @param write should the data actually be written to the masters
   * @param updateIfExists  whether to update rather than add a new portfolio if one with the same name exists
   * @param portfolioMaster  the portfolio master, not null
   * @param positionMaster  the position master, not null
   * @param securityMaster  the security master, not null
   */
  public PortfolioWriter(final boolean write, final boolean updateIfExists, final PortfolioMaster portfolioMaster,
                         final PositionMaster positionMaster, final SecurityMaster securityMaster) {
    _write = write;
    _updateIfExists = updateIfExists;
    _portfolioMaster = ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    _positionMaster = ArgumentChecker.notNull(positionMaster, "positionMaster");
    _securityMaster = ArgumentChecker.notNull(securityMaster, "securityMaster");
  }

  /**
   * Write the portfolio and supporting securities to the masters.
   *
   * @deprecated write(portfolio) is usually more useful.
   * @param portfolio the portfolio to be written, not null
   * @param securities the securities associated with the portfolio (some
   * of which may already exist in the security master)
   */
  @Deprecated
  public void write(final Portfolio portfolio, final Set<ManageableSecurity> securities) {
    persistSecurities(securities);
    persistPortfolio(portfolio);
  }

  /**
   * Write the portfolio and supporting securities to the masters.
   * Securities are assumed to be attached to the portfolio in memory.
   * @param portfolio the portfolio to be written, not null
   * @return unique id of the portfolio or null if write not enabled.
   */
  public UniqueId write(final Portfolio portfolio) {
    persistSecurities(walkPortfolioForSecurities(portfolio));
    return persistPortfolio(portfolio);
  }

  private Set<ManageableSecurity> walkPortfolioForSecurities(final Portfolio portfolio) {
    return walkPortfolioForSecurities(portfolio.getRootNode(), new HashSet<ManageableSecurity>());
  }

  private Set<ManageableSecurity> walkPortfolioForSecurities(final PortfolioNode node, Set<ManageableSecurity> existingSecurities) {
    for (final Position position : node.getPositions()) {
      existingSecurities = walkPositionOrTrade(position, existingSecurities);
      for (final com.opengamma.core.position.Trade trade : position.getTrades()) {
        existingSecurities = walkPositionOrTrade(trade, existingSecurities);
      }
    }
    for (final PortfolioNode childNode : node.getChildNodes()) {
      existingSecurities = walkPortfolioForSecurities(childNode, existingSecurities);
    }
    return existingSecurities;
  }

  private Set<ManageableSecurity> walkPositionOrTrade(final PositionOrTrade position, final Set<ManageableSecurity> existingSecurities) {
    final Security security = position.getSecurity();
    if (security instanceof ManageableSecurity) {
      final ManageableSecurity manageableSecurity = (ManageableSecurity) security;
      existingSecurities.add(manageableSecurity);
    } else {
      throw new OpenGammaRuntimeException("securities on position/trade " + position + " should be descendents of ManageableSecurity: have " + security);
    }
    return existingSecurities;
  }

  // Note that if we are passed an OTC security for which we've auto-generated an
  // external id, then there is no way for it to be updated in case it contained
  // incorrect data. If updates are required then when passed the security needs
  // to have an external id (which could well be the external trade id)
  private void persistSecurities(final Set<ManageableSecurity> securities) {

    for (final ManageableSecurity security : securities) {

      if (security.getExternalIdBundle().isEmpty()) {
        throw new OpenGammaRuntimeException("Unable to persist security with no external id: " + security);
      }

      if (_write) {
        final ManageableSecurity updated = SecurityMasterUtils.addOrUpdateSecurity(_securityMaster, security);
        if (updated == null) {
          throw new OpenGammaRuntimeException("Error persisting security: " + security);
        }
      }
      s_logger.info("Successfully processed security: {}", security);
    }
  }

  private UniqueId persistPortfolio(final Portfolio portfolio) {
    final ManageablePortfolio currentPortfolio = findCurrentPortfolio(portfolio);
    return insertPortfolio(portfolio, currentPortfolio, currentPortfolio == null ? null : currentPortfolio.getUniqueId());
  }

  private ManageablePortfolio findCurrentPortfolio(final Portfolio portfolio) {

    final PortfolioSearchRequest searchRequest = new PortfolioSearchRequest();
    final String portfolioName = portfolio.getName();
    searchRequest.setName(portfolioName);
    final PortfolioSearchResult result = _portfolioMaster.search(searchRequest);

    final List<ManageablePortfolio> portfolios = result.getPortfolios();

    final int size = portfolios.size();
    if (size > 1) {
      s_logger.warn("More than one portfolio found with name: {} - using first found", portfolioName);
    }

    return size == 0 ? null : portfolios.get(0);
  }

  private UniqueId insertPortfolio(final Portfolio portfolio, final ManageablePortfolio manageablePortfolio, final UniqueId previousId) {
    UniqueId uid;
    final List<ManageablePosition> manageablePositions = new ArrayList<>();
    final ManageablePortfolio managebalePortfolio = persistPositions(portfolio, manageablePositions);

    final String portfolioName = portfolio.getName();
    final PortfolioDocument portfolioDocument = new PortfolioDocument();
    if (!portfolioEqual(portfolio, manageablePortfolio)) {
      if (!_updateIfExists) {
        s_logger.warn("Persisting a flat portfolio structure, use alternate constructor with updateIfExists flag set to true");
        s_logger.warn("This mode is retained purely for backwards compatibility.");
        // legacy mode ignores incoming portfolio structure and creates a flat portfolio.
        portfolioDocument.setPortfolio(createPortfolio(portfolioName, manageablePositions));
      } else {
        portfolioDocument.setPortfolio(managebalePortfolio);
      }
      if (previousId != null) {
        portfolioDocument.setUniqueId(previousId);
      }

      if (_write) {
        if (!_updateIfExists) {
          s_logger.warn("Persisting a new copy of existing portfolio, use alternate constructor with updateIfExists flag set to true");
          s_logger.warn("This mode is retained purely for backwards compatibility.");
          uid = _portfolioMaster.add(portfolioDocument).getUniqueId();
        } else {
          if (previousId != null) {
            uid = _portfolioMaster.update(portfolioDocument).getUniqueId();
          } else {
            uid = _portfolioMaster.add(portfolioDocument).getUniqueId();
          }
        }
      } else {
        uid = null;
      }
    } else {
      uid = previousId;
      s_logger.debug("Portfolio structure didn't change, so not updating portfolio structure");
    }
    s_logger.info("Created portfolio with name: {}", portfolioName);
    return uid;
  }

  private boolean portfolioEqual(final Portfolio portfolio, final ManageablePortfolio existingPortfolio) {
    if (existingPortfolio == null) {
      return false;
    }
    if (!portfolio.getName().equals(existingPortfolio.getName())) {
      return false;
    }
    if (!portfolio.getAttributes().equals(existingPortfolio.getAttributes())) {
      return false;
    }
    return portfolioNodeEqual(portfolio.getRootNode(), existingPortfolio.getRootNode());
  }

  private boolean portfolioNodeEqual(final PortfolioNode portfolioNode, final ManageablePortfolioNode existingPortfolioNode) {
    if (!portfolioNode.getName().equals(existingPortfolioNode.getName())) {
      return false;
    }
    if (portfolioNode.getPositions().size() != existingPortfolioNode.getPositionIds().size()) {
      return false;
    }
    if (portfolioNode.getChildNodes().size() != existingPortfolioNode.getChildNodes().size()) { // fail fast.
      return false;
    }
    final Iterator<Position> posIter = portfolioNode.getPositions().iterator();
    final Iterator<ObjectId> existingIter = existingPortfolioNode.getPositionIds().iterator();
    // loop through positions and see if any don't match object ids.
    while (posIter.hasNext()) {
      final Position pos = posIter.next();
      final ObjectId existingPosId = existingIter.next();
      if (pos.getUniqueId() == null) {
        return false;
      }
      if (!pos.getUniqueId().getObjectId().equals(existingPosId)) {
        return false;
      }
    }
    final Iterator<PortfolioNode> nodeIter = portfolioNode.getChildNodes().iterator();
    final Iterator<ManageablePortfolioNode> existingNodeIter = existingPortfolioNode.getChildNodes().iterator();
    while (nodeIter.hasNext()) {
      final PortfolioNode node = nodeIter.next();
      final ManageablePortfolioNode existingNode = existingNodeIter.next();
      if (!portfolioNodeEqual(node, existingNode)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Build a manageable portfolio structure (but don't persist yet), and persist positions as we go.  We also build up
   * a list of manageable positions persisted to allow the creation of flat portfolios when in legacy mode (_updateIfExists==false)
   * @param portfolio  the simple portfolio containing the structure we want
   * @param legacyList  an empty arraylist to be filled with manageable portfolios
   * @return the constructed manageable portfolio, not persisted yet.
   */
  private ManageablePortfolio persistPositions(final Portfolio portfolio, final List<ManageablePosition> legacyList) {
    final ManageablePortfolio manageablePortfolio = new ManageablePortfolio();
    manageablePortfolio.setName(portfolio.getName());
    manageablePortfolio.setAttributes(portfolio.getAttributes());
    manageablePortfolio.setRootNode(persistPositions(portfolio.getRootNode(), legacyList));
    return manageablePortfolio;
  }

  private ManageablePortfolioNode persistPositions(final PortfolioNode portfolioNode, final List<ManageablePosition> legacyList) {
    final ManageablePortfolioNode manageablePortfolioNode = new ManageablePortfolioNode();
    manageablePortfolioNode.setName(portfolioNode.getName());
    for (final Position position : portfolioNode.getPositions()) {
      if (position.getSecurityLink() == null || position.getSecurityLink().getExternalId().isEmpty()) {
        throw new OpenGammaRuntimeException("Unable to persist position with no security external id: " + position);
      }

      final ManageablePosition manageablePosition = new ManageablePosition(position.getQuantity(),
                                                                     position.getSecurityLink().getExternalId());
      manageablePosition.setAttributes(position.getAttributes());
      manageablePosition.setTrades(convertTrades(position));

      if (_write) {
        if (!_updateIfExists) {
          s_logger.warn("Persisting a new copy of existing position, use alternate constructor with updateIfExists flag set to true");
          s_logger.warn("This mode is retained purely for backwards compatibility.");
          final PositionDocument addedDoc = _positionMaster.add(new PositionDocument(manageablePosition));
          manageablePortfolioNode.addPosition(addedDoc.getObjectId());
          legacyList.add(addedDoc.getPosition());
        } else {
          PositionDocument addedOrUpdatedDoc;
          if (position.getUniqueId() != null) {

            final PositionDocument existingDocument = _positionMaster.get(position.getUniqueId());
            if (existingDocument != null) {
              manageablePosition.setUniqueId(position.getUniqueId());
              final Position simplePosition = existingDocument.getPosition().toPosition();
              if (!position.equals(simplePosition)) {
                addedOrUpdatedDoc = _positionMaster.update(new PositionDocument(manageablePosition));
              } else {
                addedOrUpdatedDoc = existingDocument; // else do nothing, it's the same.
              }
            } else {
              // bad unique id.
              addedOrUpdatedDoc = _positionMaster.add(new PositionDocument(manageablePosition));
            }
          } else {
            // doesn't exist
            addedOrUpdatedDoc = _positionMaster.add(new PositionDocument(manageablePosition));
          }
          manageablePortfolioNode.addPosition(addedOrUpdatedDoc.getObjectId());
          legacyList.add(addedOrUpdatedDoc.getPosition());
        }
      }
      s_logger.info("Added/updated position {}", position);
    }
    if (_updateIfExists) {
      for (final PortfolioNode child : portfolioNode.getChildNodes()) {
        manageablePortfolioNode.addChildNode(persistPositions(child, legacyList));
      }
    } else {
      s_logger.warn("Not recursing to sub-nodes to preserve legacy behaviour, use alternate constructor with updateIfExists flag set to true");
    }
    return manageablePortfolioNode;
  }

  private List<ManageableTrade> convertTrades(final Position position) {

    final List<ManageableTrade> converted = new ArrayList<>();
    for (final Trade trade : position.getTrades()) {
      if (trade.getSecurityLink().getExternalId().isEmpty()) {
        throw new OpenGammaRuntimeException("Unable to persist trade with no security external id: " + trade);
      }
      converted.add(new ManageableTrade(trade));
    }
    return converted;
  }

  private ManageablePortfolio createPortfolio(final String portfolioName, final List<ManageablePosition> positions) {

    final ManageablePortfolioNode rootNode = new ManageablePortfolioNode(portfolioName);

    for (final ManageablePosition position : positions) {
      rootNode.addPosition(position.getUniqueId());
    }

    return new ManageablePortfolio(portfolioName, rootNode);
  }

}
