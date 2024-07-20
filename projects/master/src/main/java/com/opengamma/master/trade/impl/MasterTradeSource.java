/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.trade.Trade;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.trade.ManageableTrade;
import com.opengamma.master.trade.TradeDocument;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.master.trade.TradeSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A {@code TradeSource} implemented using an underlying
 * {@code TradeMaster} and {@code PortfolioMaster}.
 * <p>
 * The {@link com.opengamma.core.trade.TradeSource} interface provides
 * portfolio and Trade to the engine via a narrow API. This class provides
 * the source on top of a standard {@link PortfolioMaster} and
 * {@link TradeMaster}.
 */
@PublicSPI
public class MasterTradeSource extends AbstractMasterTradeSource {
  // TODO: This still needs work re versioning, as it crosses the boundary between two masters

  /**
   * The Trade master.
   */
  private final TradeMaster _TradeMaster;

  /**
   * Creates an instance with underlying masters which does not override versions.
   *
   * @param TradeMaster  the Trade master, not null
   */
  public MasterTradeSource(final TradeMaster TradeMaster) {
    super(tradeMaster);
    ArgumentChecker.notNull(TradeMaster, "TradeMaster");
    _TradeMaster = TradeMaster;
  }

  /**
   * Gets the underlying Trade master.
   *
   * @return the Trade master, not null
   */
  public TradeMaster getTradeMaster() {
    return _TradeMaster;
  }

  @Override
  public Trade getTrade(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    final ManageableTrade Trade = getTradeMaster().get(objectId, versionCorrection).getTrade();
    if (Trade == null) {
      throw new DataNotFoundException("Unable to find Trade: " + objectId + " at " + versionCorrection);
    }
    return Trade.toTrade();
  }

  @Override
  public Trade getTrade(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final ManageableTrade manTrade = getTradeMaster().getTrade(uniqueId);
    if (manTrade == null) {
      throw new DataNotFoundException("Unable to find trade: " + uniqueId);
    }
    return manTrade;
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getPortfolioMaster() + "," + getTradeMaster() + "]";
  }

  @Override
  protected ChangeProvider[] changeProviders() {
    return new ChangeProvider[] {getPortfolioMaster(), getTradeMaster()};
  }

  @Override
  protected Collection<Trade> Trades(final TradeSearchRequest TradeSearch) {
    final List<Trade> result = Lists.newArrayList();
    final TradeSearchResult Trades = getTradeMaster().search(TradeSearch);
    for (final TradeDocument Trade : Trades.getDocuments()) {
      result.add(Trade.getTrade().toTrade());
    }
    return result;
  }

}
