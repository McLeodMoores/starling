/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;

/**
 * Fudge message builder for {@link LatestHistoricalMarketDataSpecification}
 * NOTE: jim 28-Jan-15 -- This class _should_ be auto fudge encoded because it's a JodaBean, but the encoding 
 * doesn't work (it gets encoded as a HistoricalMarketDataSpecification, it's sub-class, so i think it's a JodaBean/Fudge 
 * issue)
 */
@FudgeBuilderFor(LatestHistoricalMarketDataSpecification.class)
public class LatestHistoricalMarketDataSpecificationFudgeBuilder implements FudgeBuilder<LatestHistoricalMarketDataSpecification> {

  private static final String TIME_SERIES_RESOLVER_KEY_FIELD = "timeSeriesResolverKey";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, LatestHistoricalMarketDataSpecification object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(0, LatestHistoricalMarketDataSpecification.class.getName());
    if (object.getTimeSeriesResolverKey() != null) {
      msg.add(TIME_SERIES_RESOLVER_KEY_FIELD, object.getTimeSeriesResolverKey());
    }
    return msg;
  }

  @Override
  public LatestHistoricalMarketDataSpecification buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    if (msg.hasField(TIME_SERIES_RESOLVER_KEY_FIELD)) {
      String timeSeriesResolverKey = msg.getString(TIME_SERIES_RESOLVER_KEY_FIELD);
      return new LatestHistoricalMarketDataSpecification(timeSeriesResolverKey);
    } else {
      return new LatestHistoricalMarketDataSpecification();
    }
  }

}
