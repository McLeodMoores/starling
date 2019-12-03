/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core;

import java.util.Map;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.collect.Maps;

/**
 * Fudge builder for {@link Mappings}.
 */
@FudgeBuilderFor(Mappings.class)
public class MappingsFudgeBuilder implements FudgeBuilder<Mappings> {

  private static final String ITEM = "item";
  private static final String VALUE = "value";
  private static final String MAPPING = "mapping";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Mappings mappings) {
    final MutableFudgeMsg msg = serializer.newMessage();
    for (final Map.Entry<String, String> entry : mappings.getMappings().entrySet()) {
      final MutableFudgeMsg itemMsg = serializer.newMessage();
      itemMsg.add(VALUE, entry.getKey());
      itemMsg.add(MAPPING, entry.getValue());
      serializer.addToMessage(msg, ITEM, null, itemMsg);
    }
    return msg;
  }

  @Override
  public Mappings buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final Map<String, String> mappings = Maps.newHashMap();
    for (final FudgeField itemField : msg.getAllByName(ITEM)) {
      final FudgeMsg itemMsg = (FudgeMsg) itemField.getValue();
      final String value = deserializer.fieldValueToObject(String.class, itemMsg.getByName(VALUE));
      final String mapping = deserializer.fieldValueToObject(String.class, itemMsg.getByName(MAPPING));
      mappings.put(value, mapping);
    }
    return new Mappings(mappings);
  }
}
