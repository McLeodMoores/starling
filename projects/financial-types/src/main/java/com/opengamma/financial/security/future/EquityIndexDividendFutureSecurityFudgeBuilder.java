/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.future;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;

/**
 * A Fudge builder for {@code EquityIndexDividendFutureSecurity}.
 */
@FudgeBuilderFor(EquityIndexDividendFutureSecurity.class)
public class EquityIndexDividendFutureSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<EquityIndexDividendFutureSecurity> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final EquityIndexDividendFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    EquityIndexDividendFutureSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final EquityIndexDividendFutureSecurity object, final MutableFudgeMsg msg) {
    EquityFutureSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
  }

  @Override
  public EquityIndexDividendFutureSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final EquityIndexDividendFutureSecurity object = new EquityIndexDividendFutureSecurity();
    EquityIndexDividendFutureSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final EquityIndexDividendFutureSecurity object) {
    EquityFutureSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
  }

}
