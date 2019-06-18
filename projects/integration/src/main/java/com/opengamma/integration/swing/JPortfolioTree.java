/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.swing;

import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalIdDisplayComparator;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Tree Cell Renderer for portfolio tree.
 */
public class JPortfolioTree extends JTree {

  private static final long serialVersionUID = 1L;
  private final ExternalIdDisplayComparator _idBundleComparator;

  public JPortfolioTree(final DefaultTreeModel defaultTreeModel, final ConfigSource configSource) {
    super(defaultTreeModel);
    _idBundleComparator = new ExternalIdDisplayComparator();  //.getComparator(configSource, ExternalIdDisplayComparatorUtils.DEFAULT_CONFIG_NAME);
  }

  @Override
  public String convertValueToText(final Object value, final boolean selected,
      final boolean expanded, final boolean leaf, final int row, final boolean hasFocus) {
    if (value != null) {
      if (value instanceof PortfolioNode) {
        final PortfolioNode portfolioNode = (PortfolioNode) value;
        return portfolioNode.getName();
      } else if (value instanceof Position) {
        final Position position = (Position) value;
        final ExternalIdBundle bundle = position.getSecurityLink().getExternalId();
        if (!bundle.isEmpty()) {
          final SortedSet<ExternalId> sorted = new TreeSet<>(_idBundleComparator);
          sorted.addAll(bundle.getExternalIds());
          return sorted.iterator().next() + " (" + position.getQuantity() + ")";
        }
        return position.getSecurity().getName() + " (" + position.getQuantity() + ")";
      } else if (value instanceof Trade) {
        final Trade trade = (Trade) value;
        return trade.getQuantity() + " on " + trade.getTradeDate();
      } else if (value instanceof Security) {
        final Security security = (Security) value;
        return security.getName();
      }
    }
    return "";
  }
}
