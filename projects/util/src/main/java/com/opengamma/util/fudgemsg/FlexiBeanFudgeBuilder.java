/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.types.IndicatorType;
import org.fudgemsg.wire.types.FudgeWireType;
import org.joda.beans.impl.flexi.FlexiBean;

/**
 * Builder to convert FlexiBean to and from Fudge.
 */
@FudgeBuilderFor(FlexiBean.class)
public final class FlexiBeanFudgeBuilder implements FudgeBuilder<FlexiBean> {

  /**
   * Singleton instance.
   */
  public static final FlexiBeanFudgeBuilder INSTANCE = new FlexiBeanFudgeBuilder();

  /**
   * Constructor. Must have default no-arg for support as part of the FudgeBuilderFor contract.
   */
  public FlexiBeanFudgeBuilder() {
  }

  //-------------------------------------------------------------------------
  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final FlexiBean bean) {
    final MutableFudgeMsg msg = serializer.newMessage();
    final Map<String, Object> data = bean.toMap();
    for (final Entry<String, Object> entry : data.entrySet()) {
      final Object value = entry.getValue();
      if (value == null) {
        msg.add(entry.getKey(), null, FudgeWireType.INDICATOR, IndicatorType.INSTANCE);
      } else {
        serializer.addToMessageWithClassHeaders(msg, entry.getKey(), null, value);
      }
    }
    return msg;
  }

  @Override
  public FlexiBean buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final FlexiBean bean = new FlexiBean();
    final List<FudgeField> fields = msg.getAllFields();
    for (final FudgeField field : fields) {
      if (field.getName() == null) {
        // Ignore fields without a text label
        continue;
      }
      Object value = deserializer.fieldValueToObject(field);
      value = value instanceof IndicatorType ? null : value;
      bean.set(field.getName(), value);
    }
    return bean;
  }

}
