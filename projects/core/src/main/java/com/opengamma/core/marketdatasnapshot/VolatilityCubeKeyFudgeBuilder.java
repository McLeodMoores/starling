/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.core.marketdatasnapshot;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeBuilderFor;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Encodes / decodes a {@link YieldCurveKey}.
 * <pre>
 *   message {
 *     String definitionName
 *     String specificationName
 *     String quoteType
 *     String quoteUnits
 *   }
 * </pre>
 */
@FudgeBuilderFor(VolatilityCubeKey.class)
public final class VolatilityCubeKeyFudgeBuilder implements FudgeBuilder<VolatilityCubeKey> {
  /**
   * An instance.
   */
  public static final FudgeBuilder<VolatilityCubeKey> INSTANCE = new VolatilityCubeKeyFudgeBuilder();
  private static final String QUOTE_UNITS_MSG = "quoteUnits";
  private static final String QUOTE_TYPE_MSG = "quoteType";
  private static final String SPECIFICATION_NAME_MSG = "specificationName";
  private static final String DEFINITION_NAME_MSG = "definitionName";

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final VolatilityCubeKey object) {
    final MutableFudgeMsg msg = serializer.newMessage();
    msg.add(DEFINITION_NAME_MSG, object.getDefinitionName());
    msg.add(SPECIFICATION_NAME_MSG, object.getSpecificationName());
    msg.add(QUOTE_TYPE_MSG, object.getQuoteType());
    msg.add(QUOTE_UNITS_MSG, object.getQuoteUnits());
    return msg;
  }

  @Override
  public VolatilityCubeKey buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return VolatilityCubeKey.of(message.getString(DEFINITION_NAME_MSG), message.getString(SPECIFICATION_NAME_MSG),
        message.getString(QUOTE_TYPE_MSG), message.getString(QUOTE_UNITS_MSG));
  }

}
