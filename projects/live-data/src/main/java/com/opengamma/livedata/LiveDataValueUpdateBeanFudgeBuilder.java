/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Fudge message builder for {@code LiveDataValueUpdate}.
 */
@FudgeBuilderFor(LiveDataValueUpdateBean.class)
public class LiveDataValueUpdateBeanFudgeBuilder implements FudgeBuilder<LiveDataValueUpdateBean> {

  /** Field name. */
  public static final String SEQUENCE_NUMBER_FIELD_NAME = "sequenceNumber";
  /** Field name. */
  public static final String SPECIFICATION_FIELD_NAME = "specification";
  /** Field name. */
  public static final String FIELDS_FIELD_NAME = "fields";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final LiveDataValueUpdateBean object) {
    return LiveDataValueUpdateBeanFudgeBuilder.toFudgeMsg(serializer, object);
  }

  /**
   * Serializes the bean and adds it to a new message.
   *
   * @param serializer
   *          the serializer, not null
   * @param object
   *          the bean, not null
   * @return the message
   */
  public static MutableFudgeMsg toFudgeMsg(final FudgeSerializer serializer, final LiveDataValueUpdateBean object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    LiveDataValueUpdateBeanFudgeBuilder.toFudgeMsg(serializer, object, msg);
    return msg;
  }

  /**
   * Serializes the bean and adds fields to the message.
   *
   * @param serializer
   *          the serializer, not null
   * @param object
   *          the bean, not null
   * @param msg
   *          the message
   */
  public static void toFudgeMsg(final FudgeSerializer serializer, final LiveDataValueUpdateBean object, final MutableFudgeMsg msg) {
    msg.add(SEQUENCE_NUMBER_FIELD_NAME, object.getSequenceNumber());
    if (object.getSpecification() != null) {
      msg.add(SPECIFICATION_FIELD_NAME, LiveDataSpecificationFudgeBuilder.toFudgeMsg(serializer, object.getSpecification()));
    }
    if (object.getFields() != null) {
      msg.add(FIELDS_FIELD_NAME, object.getFields());
    }
  }

  @Override
  public LiveDataValueUpdateBean buildObject(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    return LiveDataValueUpdateBeanFudgeBuilder.fromFudgeMsg(deserializer, msg);
  }

  /**
   * Deserializes a Fudge message representing a bean.
   *
   * @param deserializer
   *          the deserializer, not null
   * @param msg
   *          the message, not null
   * @return the bean or null if any of the fields have no value
   */
  public static LiveDataValueUpdateBean fromFudgeMsg(final FudgeDeserializer deserializer, final FudgeMsg msg) {
    final Long sequenceNumber = msg.getLong(SEQUENCE_NUMBER_FIELD_NAME);
    final FudgeMsg specificationFields = msg.getMessage(SPECIFICATION_FIELD_NAME);
    final FudgeMsg fields = msg.getMessage(FIELDS_FIELD_NAME);
    if (sequenceNumber == null) {
      return null;
    }
    if (specificationFields == null) {
      return null;
    }
    if (fields == null) {
      return null;
    }
    final LiveDataSpecification spec = LiveDataSpecificationFudgeBuilder.fromFudgeMsg(deserializer, specificationFields);
    return new LiveDataValueUpdateBean(sequenceNumber, spec, fields);
  }

}
