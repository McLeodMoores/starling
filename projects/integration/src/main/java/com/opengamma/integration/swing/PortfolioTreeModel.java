/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.compilation.PortfolioCompiler;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.tuple.Pair;

/**
 * Swing TreeModel for browsing a resolved portfolio
 */
public class PortfolioTreeModel implements TreeModel {

  private static final int THREAD_POOL_SIZE = 20;
  private final PositionSource _positionSource;
  private final SecuritySource _securitySource;

  private UniqueId _portfolioId;
  private volatile Portfolio _portfolio;

  private final List<TreeModelListener> _listeners = new ArrayList<TreeModelListener>();

  public PortfolioTreeModel(final UniqueId portfolioId, final ToolContext toolContext) {
    _portfolioId = portfolioId;
    _positionSource = toolContext.getPositionSource();
    _securitySource = toolContext.getSecuritySource();

    final SwingWorker<Portfolio, Pair<UniqueId, SecuritySource>> worker = new SwingWorker<Portfolio, Pair<UniqueId, SecuritySource>>() {
      @Override
      protected Portfolio doInBackground() throws Exception {
        final Portfolio portfolio = _positionSource.getPortfolio(portfolioId, VersionCorrection.LATEST);
        final Portfolio resolvedPortfolio = PortfolioCompiler.resolvePortfolio(portfolio, Executors.newFixedThreadPool(THREAD_POOL_SIZE), _securitySource);
        return resolvedPortfolio;
      }

      @Override
      protected void done() {
        try {
          _portfolio = get();
          if (_portfolio != null) {
            for (final TreeModelListener listener : _listeners) {
              listener.treeStructureChanged(new TreeModelEvent(PortfolioTreeModel.this, new TreePath(_portfolio.getRootNode())));
            }
          }
        } catch (final InterruptedException ex) {
          JOptionPane.showMessageDialog(null, ex.getMessage(), "Error getting portfolio", JOptionPane.ERROR_MESSAGE);
        } catch (final ExecutionException ex) {
          JOptionPane.showMessageDialog(null, ex.getMessage(), "Error getting portfolio", JOptionPane.ERROR_MESSAGE);
        }
      }
    };
    worker.execute();
  }

  @Override
  public Object getRoot() {
    if (_portfolio == null) {
      return null;
    } else {
      return _portfolio.getRootNode();
    }
  }

  @Override
  public Object getChild(final Object parent, final int index) {
    if (parent instanceof PortfolioNode) {
      final PortfolioNode portfolioNode = (PortfolioNode) parent;
      final List<PortfolioNode> childNodes = portfolioNode.getChildNodes();
      if (index < childNodes.size()) {
        return childNodes.get(index);
      } else {
        // we get a position
        final List<Position> positions = portfolioNode.getPositions();
        final int positionIndex = index - childNodes.size();
        if (positionIndex < positions.size()) {
          return positions.get(positionIndex);
        } else {
          return null;
        }
      }
    } else if (parent instanceof Position) {
      final Position position = (Position) parent;
      final Collection<Trade> trades = position.getTrades();
      if (trades.size() > 0) { // position has trades, so return them as children
        if (index < trades.size()) {
          return Lists.newArrayList(trades).get(index);
        } else {
          return null;
        }
      } else { // if this position has no trades, return the security as a sub-node
        if (index == 0) {
          return position.getSecurity();
        }
      }
    } else if (parent instanceof Trade) {
      final Trade trade = (Trade) parent;
      if (trade.getSecurity() != null && index == 0) {
        return trade.getSecurity();
      }
    } // shouldn't encounter securities here as they are always leaf nodes.
    return null;
  }

  @Override
  public int getChildCount(final Object parent) {
    if (parent instanceof PortfolioNode) {
      final PortfolioNode portfolioNode = (PortfolioNode) parent;
      final List<PortfolioNode> childNodes = portfolioNode.getChildNodes();
      return portfolioNode.getChildNodes().size() + portfolioNode.getPositions().size();
    } else if (parent instanceof Position) {
      final Position position = (Position) parent;
      return position.getTrades().size() == 0 ? 1 : position.getTrades().size();
    } else if (parent instanceof Trade) {
      final Trade trade = (Trade) parent;
      if (trade.getSecurity() != null) {
        return 1;
      } else {
        return 0;
      }
    } else if (parent instanceof Security) {
      return 0;
    }
    return 0;
  }

  @Override
  public boolean isLeaf(final Object node) {
    if (node instanceof PortfolioNode) {
      final PortfolioNode portfolioNode = (PortfolioNode) node;
      return portfolioNode.getChildNodes().size() == 0 && portfolioNode.getPositions().size() == 0;
    } else if (node instanceof Position) {
      final Position position = (Position) node;
      return position.getTrades().size() == 0 && position.getSecurity() == null;
    } else if (node instanceof Trade) {
      final Trade trade = (Trade) node;
      return trade.getSecurity() == null;
    } else if (node instanceof Security) {
      return true;
    } else {
      return true;
    }
  }

  @Override
  public void valueForPathChanged(final TreePath path, final Object newValue) {
    throw new UnsupportedOperationException("Tree editing is not supported by this model");
  }

  @Override
  public int getIndexOfChild(final Object parent, final Object child) {
    if (parent instanceof PortfolioNode) {
      final PortfolioNode parentNode = (PortfolioNode) parent;
      if (child instanceof PortfolioNode) {
        final PortfolioNode childNode = (PortfolioNode) child;
        return parentNode.getChildNodes().indexOf(childNode);
      } else if (child instanceof Position) {
        final Position childPosition = (Position) child;
        final int positionIndex = parentNode.getPositions().indexOf(childPosition);
        if (positionIndex == -1) {
          return -1;
        } else {
          return parentNode.getChildNodes().size() + parentNode.getPositions().indexOf(childPosition);
        }
      } else {
        return -1;
      }
    } else if (parent instanceof Position) {
      final Position parentPosition = (Position) parent;
      if (child instanceof Trade) {
        final Trade childTrade = (Trade) child;
        return Lists.newArrayList(parentPosition.getTrades()).indexOf(childTrade); // indexOf returns -1 as expected by this interface if not present
      } else if (child instanceof Security) {
        return 0;
      }
    } else if (parent instanceof Trade) {
      final Trade parentTrade = (Trade) parent;
      if (child instanceof Security) {
        return 0;
      } else {
        return -1;
      }
    } else if (parent instanceof Security) {
      return -1;
    }
    return -1;
  }

  @Override
  public void addTreeModelListener(final TreeModelListener l) {
    _listeners.add(l);
  }

  @Override
  public void removeTreeModelListener(final TreeModelListener l) {
    _listeners.remove(l);
  }

}
