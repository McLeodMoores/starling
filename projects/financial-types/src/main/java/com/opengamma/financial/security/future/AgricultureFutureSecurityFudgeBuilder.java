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
 * A Fudge builder for {@code AgricultureFutureSecurity}.
 */
@FudgeBuilderFor(AgricultureFutureSecurity.class)
public class AgricultureFutureSecurityFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<AgricultureFutureSecurity> {

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final AgricultureFutureSecurity object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    AgricultureFutureSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  public static void toFudgeMsg(final FudgeSerializer serializer, final AgricultureFutureSecurity object, final MutableFudgeMsg msg) {
    CommodityFutureSecurityFudgeBuilder.toFudgeMsg(serializer, object, msg);
  }

  @Override
  public AgricultureFutureSecurity buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final AgricultureFutureSecurity object = new AgricultureFutureSecurity();
    AgricultureFutureSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
    return object;
  }

  public static void fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg, final AgricultureFutureSecurity object) {
    CommodityFutureSecurityFudgeBuilder.fromFudgeMsg(deserializer, msg, object);
  }

}
