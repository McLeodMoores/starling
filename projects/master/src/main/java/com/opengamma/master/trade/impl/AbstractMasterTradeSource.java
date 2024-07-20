/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.change.DummyChangeManager;
import com.opengamma.core.change.PassthroughChangeManager;
import com.opengamma.core.trade.Trade;
import com.opengamma.core.trade.TradeSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.ManageablePortfolio;
import com.opengamma.master.portfolio.ManageablePortfolioNode;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * An abstract TradeSource built on top of an underlying master with possibly a TradeMaster and or TradeSource to
 * resolve the Trades in the portfolio.
 *
 */
@PublicSPI
public abstract class AbstractMasterTradeSource implements TradeSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractMasterTradeSource.class);

  private final TradeMaster _tradeMaster;

  public AbstractMasterTradeSource(final TradeMaster tradeMaster) {
    ArgumentChecker.notNull(tradeMaster, "tradeMaster");
    _tradeMaster = tradeMaster;
  }

  protected abstract ChangeProvider[] changeProviders();

  @Override
  public ChangeManager changeManager() {
    final ChangeProvider[] changeProviders = changeProviders();
    if (changeProviders != null) {
      return new PassthroughChangeManager(changeProviders());
    }
    return DummyChangeManager.INSTANCE;
  }

  /**
   * Gets the tradeMaster.
   * @return the tradeMaster
   */
  public TradeMaster getTradeMaster() {
    return _tradeMaster;
  }

}
