/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.model.testutils;

import java.math.BigDecimal;
import java.util.Collections;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.InMemorySecuritySource;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.impl.InMemoryPositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class FunctionTestUtils {
  private static final ExternalId DUMMY_REGION = ExternalId.of("TestRegion", "DUMMY");
  private static final Counterparty DUMMY_COUNTERPARTY = new SimpleCounterparty(ExternalId.of("TestCtpty", "DUMMY"));
  private static final OffsetTime DEFAULT_TRADE_TIME = OffsetTime.of(LocalTime.of(11, 0), ZoneOffset.UTC);

  public static Security createAndStoreFXForwardSecurity(final InMemorySecuritySource securitySource, final Currency payCurrency,
      final double payAmount, final Currency receiveCurrency, final double receiveAmount, final ZonedDateTime forwardDate) {
    final FXForwardSecurity security = new FXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, DUMMY_REGION);
    securitySource.addSecurity(security);
    return security;
  }

  public static ComputationTarget createSecurityTarget(final Security security) {
    return new ComputationTarget(ComputationTargetType.SECURITY, security);
  }

  public static Trade createAndStoreSingleTradeFromSecurity(final InMemoryPositionMaster positionMaster, final Security security, final LocalDate tradeDate) {
    final ManageableTrade trade = new ManageableTrade();
    final ManageableSecurityLink securityLink = new ManageableSecurityLink();
    securityLink.setTarget(security);
    trade.setSecurityLink(securityLink);
    trade.setQuantity(BigDecimal.ONE);
    trade.setCounterpartyExternalId(DUMMY_COUNTERPARTY.getExternalId());
    trade.setTradeDate(tradeDate);
    trade.setTradeTime(DEFAULT_TRADE_TIME);
    final ManageablePosition position = new ManageablePosition();
    position.setTrades(Collections.singletonList(trade));
    positionMaster.add(new PositionDocument(position));
    return trade;
  }

  public static ComputationTarget createTradeTarget(final Trade trade) {
    return new ComputationTarget(ComputationTargetType.TRADE, trade.getUniqueId());
  }
}
