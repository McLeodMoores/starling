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
import org.threeten.bp.Instant;

import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;

/**
 * Fudge message builder for {@link ViewDefinitionCompilationFailedCall}
 */
@FudgeBuilderFor(ViewDefinitionCompilationFailedCall.class)
public class ViewDefinitionCompilationFailedCallFudgeBuilder implements FudgeBuilder<ViewDefinitionCompilationFailedCall> {

  private static final String VALUATION_TIME_FIELD = "valuationTime";
  private static final String EXCEPTION_FIELD = "exception";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final ViewDefinitionCompilationFailedCall object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    serializer.addToMessage(msg, VALUATION_TIME_FIELD, null, object.getValuationTime());
    serializer.addToMessage(msg, EXCEPTION_FIELD, null, object.getException());
    return msg;
  }

  @Override
  public ViewDefinitionCompilationFailedCall buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final Instant valuationTime = deserializer.fieldValueToObject(Instant.class, msg.getByName(VALUATION_TIME_FIELD));
    final Exception exception = deserializer.fieldValueToObject(Exception.class, msg.getByName(EXCEPTION_FIELD));
    return new ViewDefinitionCompilationFailedCall(valuationTime, exception);
  }

}
