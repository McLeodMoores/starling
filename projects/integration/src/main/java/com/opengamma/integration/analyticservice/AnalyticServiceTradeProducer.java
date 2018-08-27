/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.analyticservice;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.Trade;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class AnalyticServiceTradeProducer implements TradeProducer, FudgeMessageReceiver {

  private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticServiceTradeProducer.class);

  private final Set<TradeListener> _tradeListeners = new CopyOnWriteArraySet<>();

  @Override
  public void messageReceived(final FudgeContext fudgeContext, final FudgeMsgEnvelope msgEnvelope) {

    final FudgeMsg msg = msgEnvelope.getMessage();
    LOGGER.debug("Message received {}", msg);
    final Trade trade = fudgeContext.fromFudgeMsg(Trade.class, msg);
    notifyListeners(trade);
  }

  @Override
  public void addTradeListener(final TradeListener tradeListener) {
    ArgumentChecker.notNull(tradeListener, "trade listener");
    _tradeListeners.add(tradeListener);
  }

  @Override
  public void removeTradeListener(final TradeListener tradeListener) {
    ArgumentChecker.notNull(tradeListener, "trade listener");
    _tradeListeners.remove(tradeListener);
  }

  private void notifyListeners(final Trade trade) {
    for (final TradeListener listener : _tradeListeners) {
      listener.tradeReceived(trade);
    }
  }

}
