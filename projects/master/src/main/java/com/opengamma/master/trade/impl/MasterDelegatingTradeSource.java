/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade.impl;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.opengamma.core.change.ChangeProvider;
import com.opengamma.core.trade.Trade;
import com.opengamma.core.trade.impl.DelegatingTradeSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.trade.TradeMaster;
import com.opengamma.master.trade.TradeSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * A <code>TradeSource</code> implemented using an underlying
 * {@code TradeMaster} and {@code DelegatingTradeSource}.
 * <p>
 * The {@link com.opengamma.core.Trade.TradeSource} interface provides
 * portfolio and Trade to the engine via a narrow API. This class provides
 * the source on top of a standard {@link PortfolioMaster} and
 * {@link DelegatingTradeSource}.
 */
@PublicSPI
public class MasterDelegatingTradeSource extends AbstractMasterTradeSource {

  private static final Logger LOGGER = LoggerFactory.getLogger(MasterDelegatingTradeSource.class);

  private final DelegatingTradeSource _delegatingTradeSource;

  public MasterDelegatingTradeSource(final TradeMaster tradeMaster, final DelegatingTradeSource TradeSource) {
    super(tradeMaster);
    ArgumentChecker.notNull(TradeSource, "TradeSource");

    _delegatingTradeSource = TradeSource;
  }

  @Override
  protected Collection<Trade> trades(final TradeSearchRequest TradeSearch) {
    LOGGER.debug("findTrades.TradeSearchRequest {}", TradeSearch);
    final List<Trade> Trades = Lists.newArrayList();
    for (final ObjectId TradeObjectId : TradeSearch.getTradeObjectIds()) {
      final Trade Trade = getDelegatingTradeSource().getTrade(TradeObjectId, VersionCorrection.LATEST);
      if (Trade != null) {
        Trades.add(Trade);
      } else {
        LOGGER.warn("Trade {} cannot be found in DelegatingTradeSource {}", TradeObjectId, getDelegatingTradeSource());
      }
    }
    return Trades;
  }

  @Override
  public Trade getTrade(final ObjectId objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return getDelegatingTradeSource().getTrade(objectId, versionCorrection);
  }

  @Override
  public Trade getTrade(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return getDelegatingTradeSource().getTrade(uniqueId);
  }

  /**
   * Gets the delegatingTradeSource.
   * @return the delegatingTradeSource
   */
  public DelegatingTradeSource getDelegatingTradeSource() {
    return _delegatingTradeSource;
  }

  @Override
  protected ChangeProvider[] changeProviders() {
    return new ChangeProvider[] {getTradeMaster()};
  }

  //-------------------------------------------------------------------------
  @Override
  public String toString() {
    return getClass().getSimpleName() + "[" + getTradeMaster() + "," + getDelegatingTradeSource() + "]";
  }

}
