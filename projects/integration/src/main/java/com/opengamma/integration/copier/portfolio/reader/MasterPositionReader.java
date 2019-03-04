/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.copier.portfolio.reader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.portfolio.PortfolioSearchRequest;
import com.opengamma.master.portfolio.PortfolioSearchResult;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.ObjectsPair;

/**
 * Portfolio reader.
 */
public class MasterPositionReader implements PositionReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(PositionReader.class);

  private final PortfolioMaster _portfolioMaster;
  private final PositionMaster _positionMaster;
  private final SecuritySource _securitySource;

  private final PortfolioDocument _portfolioDocument;

  private ManageablePortfolioNode _currentNode;
  private final Stack<Iterator<ManageablePortfolioNode>> _nodeIteratorStack;
  private Iterator<ManageablePortfolioNode> _nodeIterator;
  private Iterator<ObjectId> _positionIdIterator;


  public MasterPositionReader(final String portfolioName, final PortfolioMaster portfolioMaster,
      final PositionMaster positionMaster, final SecuritySource securitySource) {

    ArgumentChecker.notEmpty(portfolioName, "portfolioName");
    ArgumentChecker.notNull(portfolioMaster, "portfolioMaster");
    ArgumentChecker.notNull(positionMaster, "positionMaster");
    ArgumentChecker.notNull(securitySource, "securitySource");

    _portfolioMaster = portfolioMaster;
    _positionMaster = positionMaster;
    _securitySource = securitySource;
    _portfolioDocument = openPortfolio(portfolioName);

    if (_portfolioDocument == null) {
      throw new OpenGammaRuntimeException("Portfolio " + portfolioName + " could not be opened");
    }
    _currentNode = _portfolioDocument.getPortfolio().getRootNode();

    final List<ManageablePortfolioNode> rootNodeList = new ArrayList<>();
    rootNodeList.add(_portfolioDocument.getPortfolio().getRootNode());

    _nodeIterator = rootNodeList.iterator();
    _nodeIteratorStack = new Stack<>();
    _positionIdIterator = _nodeIterator.next().getPositionIds().iterator();
  }

  @Override
  public ObjectsPair<ManageablePosition, ManageableSecurity[]> readNext() {

    final ObjectId positionId = getNextPositionId();
    if (positionId == null) {
      return null;
    }
    ManageablePosition position;
    try {
      position = _positionMaster.get(positionId, VersionCorrection.LATEST).getPosition();
    } catch (final Throwable t) {
      return ObjectsPair.of(null, null);
    }

    // Write the related security(ies)
    final ManageableSecurityLink sLink = position.getSecurityLink();
    final Security security = sLink.resolveQuiet(_securitySource);
    if (security != null && security instanceof ManageableSecurity) {

      // Find underlying security
      // TODO support multiple underlyings; unfortunately the system does not
      // provide a standard way
      // to retrieve underlyings
      if (((ManageableSecurity) security).propertyNames().contains("underlyingId")) {
        final ExternalId id = (ExternalId) ((ManageableSecurity) security).property("underlyingId").get();

        Security underlying;
        try {
          underlying = _securitySource.getSingle(id.toBundle());
          if (underlying != null) {
            return ObjectsPair.of(position, new ManageableSecurity[] { (ManageableSecurity) security, (ManageableSecurity) underlying });
          }
          LOGGER.warn("Could not resolve underlying " + id + " for security " + security.getName());
        } catch (final Throwable e) {
          // Underlying not found
          LOGGER.warn("Error trying to resolve underlying " + id + " for security " + security.getName());
        }
      }
      return ObjectsPair.of(position, new ManageableSecurity[] { (ManageableSecurity) security });

    }
    LOGGER.warn("Could not resolve security relating to position " + position.getName());
    return ObjectsPair.of(null, null);
  }

  @Override
  public String[] getCurrentPath() {
    final Stack<ManageablePortfolioNode> stack =
        _portfolioDocument.getPortfolio().getRootNode().findNodeStackByObjectId(_currentNode.getUniqueId());
    stack.remove(0);
    final String[] result = new String[stack.size()];
    int i = stack.size();
    while (!stack.isEmpty()) {
      result[--i] = stack.pop().getName();
    }
    return result;
  }

  @Override
  public void close() {
    // Nothing to close
  }

  @Override
  public String getPortfolioName() {
    return null;
  }

  /**
   * Walks the tree, depth-first, and returns the next position's id. Uses _positionIdIterator,
   * _nodeIterator and _nodeIteratorStack to maintain location state across calls.
   * @return
   */
  private ObjectId getNextPositionId() {

    while (true) {
      // Return the next position in the current portfolio node's list, if any there
      if (_positionIdIterator.hasNext()) {
        return _positionIdIterator.next();

        // Current node's positions exhausted, find another node
      }
      // Go down to current node's child nodes to find more positions
      // (depth-first)
      _nodeIteratorStack.push(_nodeIterator);
      _nodeIterator = _currentNode.getChildNodes().iterator();

      // If there are no more nodes here pop back up until a node is available
      while (!_nodeIterator.hasNext()) {
        if (!_nodeIteratorStack.isEmpty()) {
          _nodeIterator = _nodeIteratorStack.pop();
        } else {
          return null;
        }
      }

      // Go to the next node and start fetching positions there
      _currentNode = _nodeIterator.next();
      _positionIdIterator = _currentNode.getPositionIds().iterator();
    }
  }

  private PortfolioDocument openPortfolio(final String portfolioName) {

    final PortfolioSearchRequest portSearchRequest = new PortfolioSearchRequest();
    portSearchRequest.setName(portfolioName);
    final PortfolioSearchResult portSearchResult = _portfolioMaster.search(portSearchRequest);
    final PortfolioDocument portfolioDoc = portSearchResult.getFirstDocument();

    return portfolioDoc;
  }

}
