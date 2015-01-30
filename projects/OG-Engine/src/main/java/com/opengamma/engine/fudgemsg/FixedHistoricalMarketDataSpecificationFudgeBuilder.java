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
import org.threeten.bp.LocalDate;

import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.view.listener.ProcessTerminatedCall;

/**
 * Fudge message builder for {@link FixedHistoricalMarketDataSpecification}
 * NOTE: jim 28-Jan-15 -- This class _should_ be auto fudge encoded because it's a JodaBean, but the encoding for 
 * LatestHistoricalMarketDataSpecification doesn't work so I'm adding this so I don't have to revisit.
 */
@FudgeBuilderFor(FixedHistoricalMarketDataSpecification.class)
public class FixedHistoricalMarketDataSpecificationFudgeBuilder implements FudgeBuilder<FixedHistoricalMarketDataSpecification> {

  private static final String TIME_SERIES_RESOLVER_KEY_FIELD = "timeSeriesResolverKey";
  private static final String SNAPSHOT_DATE_FIELD = "snapshotDate";
  
  @Override
  public MutableFudgeMsg buildMessage(FudgeSerializer serializer, FixedHistoricalMarketDataSpecification object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(0, FixedHistoricalMarketDataSpecification.class.getName());
    if (object.getTimeSeriesResolverKey() != null) {
      msg.add(TIME_SERIES_RESOLVER_KEY_FIELD, object.getTimeSeriesResolverKey());
    }
    msg.add(SNAPSHOT_DATE_FIELD, object.getSnapshotDate());
    return msg;
  }

  @Override
  public FixedHistoricalMarketDataSpecification buildObject(FudgeDeserializer deserializer, FudgeMsg msg) {
    LocalDate snapshotDate = msg.getValue(LocalDate.class, SNAPSHOT_DATE_FIELD);
    if (msg.hasField(TIME_SERIES_RESOLVER_KEY_FIELD)) {
      String timeSeriesResolverKey = msg.getString(TIME_SERIES_RESOLVER_KEY_FIELD);
      return new FixedHistoricalMarketDataSpecification(timeSeriesResolverKey, snapshotDate);
    } else {
      return new FixedHistoricalMarketDataSpecification(snapshotDate);
    }
  }

}
