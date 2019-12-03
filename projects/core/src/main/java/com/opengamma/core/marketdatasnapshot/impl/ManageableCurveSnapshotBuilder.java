/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot.impl;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.threeten.bp.Instant;

/**
 * Fudge message builder for {@link ManageableCurveSnapshot}.
 *
 * <pre>
 *   message {
 *     ManageableUnstructuredMarketDataSnapshot values;
       Instant time;
 *   }
 * </pre>
 */
@FudgeBuilderFor(ManageableCurveSnapshot.class)
public final class ManageableCurveSnapshotBuilder implements FudgeBuilder<ManageableCurveSnapshot> {
  private static final String VALUATION_TIME_MSG = "valuationTime";
  private static final String VALUES_MSG = "values";
  /**
   * Returns an instance.
   */
  public static final FudgeBuilder<ManageableCurveSnapshot> INSTANCE = new ManageableCurveSnapshotBuilder();

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ManageableCurveSnapshot object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    //FudgeSerializer.addClassHeader(msg, ManageableCurveSnapshot.class);
    serializer.addToMessage(msg, VALUES_MSG, null, object.getValues());
    serializer.addToMessage(msg, VALUATION_TIME_MSG, null, object.getValuationTime());
    return msg;
  }

  @Override
  public ManageableCurveSnapshot buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    ManageableUnstructuredMarketDataSnapshot values = null;
    FudgeField field = msg.getByName(VALUES_MSG);
    if (field != null) {
      values = deserializer.fieldValueToObject(ManageableUnstructuredMarketDataSnapshot.class, field);

    }
    Instant valuationTime = null;
    field = msg.getByName(VALUATION_TIME_MSG);
    if (field != null) {
      valuationTime = deserializer.fieldValueToObject(Instant.class, field);
    }
    ManageableCurveSnapshot result = null;
    if (valuationTime != null && values != null) {
      result = ManageableCurveSnapshot.of(valuationTime, values);
    }
    return result;
  }
}
