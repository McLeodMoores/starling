/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * Calculates a best estimate of the implied volatility of an option.
 */
public class ImpliedVolatilityCalculator implements NormalizationRule {

  @Override
  public MutableFudgeMsg apply(final MutableFudgeMsg msg, final String securityUniqueId, final FieldHistoryStore fieldHistory) {

    Double impliedVolatility = msg.getDouble(MarketDataRequirementNames.BEST_IMPLIED_VOLATILITY);
    if (impliedVolatility != null) {
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }

    impliedVolatility = msg.getDouble(MarketDataRequirementNames.MID_IMPLIED_VOLATILITY);
    if (impliedVolatility != null) {
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }

    impliedVolatility = msg.getDouble(MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY);
    if (impliedVolatility != null) {
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }

    final Double impliedVolatilityBid = msg.getDouble(MarketDataRequirementNames.BID_IMPLIED_VOLATILITY);
    final Double impliedVolatilityAsk = msg.getDouble(MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY);

    if (impliedVolatilityBid != null && impliedVolatilityAsk != null) {
      impliedVolatility = (impliedVolatilityBid + impliedVolatilityAsk) / 2;
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }

    final FudgeMsg lkv = fieldHistory.getLastKnownValues();
    impliedVolatility = lkv.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY);
    if (impliedVolatility != null) {
      msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, impliedVolatility);
      return msg;
    }

    return msg;
  }

}
