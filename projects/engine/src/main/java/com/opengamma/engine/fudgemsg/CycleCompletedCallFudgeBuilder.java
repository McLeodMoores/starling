/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.listener.CycleCompletedCall;

/**
 * Fudge message builder for {@link CycleCompletedCall}.
 */
@FudgeBuilderFor(CycleCompletedCall.class)
public class CycleCompletedCallFudgeBuilder implements FudgeBuilder<CycleCompletedCall> {

  private static final String FULL_RESULT_FIELD = "fullResult";
  private static final String DELTA_RESULT_FIELD = "deltaResult";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final CycleCompletedCall object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(0, CycleCompletedCall.class.getName());
    final ViewComputationResultModel fullResult = object.getFullResult();
    final ViewDeltaResultModel deltaResult = object.getDeltaResult();
    serializer.addToMessage(msg, FULL_RESULT_FIELD, null, fullResult);
    serializer.addToMessage(msg, DELTA_RESULT_FIELD, null, deltaResult);
    return msg;
  }

  @Override
  public CycleCompletedCall buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final FudgeField fullResultField = msg.getByName(FULL_RESULT_FIELD);
    final ViewComputationResultModel fullResult = fullResultField != null ? deserializer.fieldValueToObject(ViewComputationResultModel.class, fullResultField)
        : null;
    final FudgeField deltaResultField = msg.getByName(DELTA_RESULT_FIELD);
    final ViewDeltaResultModel deltaResult = deltaResultField != null ? deserializer.fieldValueToObject(ViewDeltaResultModel.class, deltaResultField) : null;
    return new CycleCompletedCall(fullResult, deltaResult);
  }

}
