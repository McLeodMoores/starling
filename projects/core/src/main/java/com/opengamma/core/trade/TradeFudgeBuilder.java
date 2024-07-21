/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.trade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.fudgemsg.mapping.GenericFudgeBuilderFor;
import org.fudgemsg.wire.types.FudgeWireType;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.trade.Trade;
import com.opengamma.core.trade.impl.SimpleTrade;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;

/**
 * Fudge message builder for {@code Trade}.
 */
@GenericFudgeBuilderFor(com.opengamma.core.trade.Trade.class)
public class TradeFudgeBuilder implements FudgeBuilder<com.opengamma.core.trade.Trade> {

  /** Field name. */
  public static final String UNIQUE_ID_FIELD_NAME = "uniqueId";
  /** Field name. */
  public static final String ATTRIBUTES_FIELD_NAME = "attributes";
  /** Field name. */
  protected static final String TRADE_ID_FIELD_NAME = "tradeId";


  protected static MutableFudgeMsg buildMessageImpl(final FudgeSerializer serializer, final com.opengamma.core.trade.Trade trade) {
    final MutableFudgeMsg message = serializer.newMessage();
    if (trade.getUniqueId() != null) {
      serializer.addToMessage(message, UNIQUE_ID_FIELD_NAME, null, trade.getUniqueId());
    }
    final MutableFudgeMsg tradeIdBundleMsg = serializer.newMessage();
    if ( ! trade.getExternalTradeIds().isEmpty()) {
      for (ExternalId externalId : trade.getExternalTradeIds()) {
        tradeIdBundleMsg.add(externalId.getScheme().getName(), null, externalId.getValue());
      }
      serializer.addToMessage(message, TRADE_ID_FIELD_NAME, null, tradeIdBundleMsg);
    }

    if (haveAttributes(trade)) {
      final MutableFudgeMsg attributesMsg = serializer.newMessage();
      for (Entry<String, String> entry : trade.getAttributes().entrySet()) {
        attributesMsg.add(entry.getKey(), entry.getValue());
      }
      serializer.addToMessage(message, ATTRIBUTES_FIELD_NAME, null, attributesMsg);
    }
    return message;
  }

  private static boolean haveAttributes(final com.opengamma.core.trade.Trade trade) {
    return trade.getAttributes() != null && !trade.getAttributes().isEmpty();
  }

  @Override
  public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final com.opengamma.core.trade.Trade trade) {
    final MutableFudgeMsg message = buildMessageImpl(serializer, trade);
    message.add(null, FudgeSerializer.TYPES_HEADER_ORDINAL, FudgeWireType.STRING, com.opengamma.core.trade.Trade.class.getName());
    return message;
  }

  protected static SimpleTrade buildObjectImpl(final FudgeDeserializer deserializer, final FudgeMsg message) {

    SimpleTrade trade = new SimpleTrade();

    if (message.hasField(UNIQUE_ID_FIELD_NAME)) {
      FudgeField uniqueIdField = message.getByName(UNIQUE_ID_FIELD_NAME);
      if (uniqueIdField != null) {
        trade.setUniqueId(deserializer.fieldValueToObject(UniqueId.class, uniqueIdField));
      }      
    }
    if (message.hasField(TRADE_ID_FIELD_NAME)) {
      FudgeMsg tradeIdBundleMsg = message.getMessage(TRADE_ID_FIELD_NAME);
      Collection<ExternalId> ids = new ArrayList<>(tradeIdBundleMsg.getNumFields());
      for (FudgeField fudgeField : tradeIdBundleMsg) {
        String scheme = fudgeField.getName();
        Object value = fudgeField.getValue();
        if (scheme != null && value != null) {
          ExternalId id = ExternalId.of(scheme, (String) value);
          ids.add(id);
        }
      }
      trade.setExternalTradeIds(ExternalIdBundle.of(ids));
    }
    if (message.hasField(ATTRIBUTES_FIELD_NAME)) {
      FudgeMsg attributesMsg = message.getMessage(ATTRIBUTES_FIELD_NAME);
      for (FudgeField fudgeField : attributesMsg) {
        String key = fudgeField.getName();
        Object value = fudgeField.getValue();
        if (key != null && value != null) {
          trade.addAttribute(key, (String) value);
        }
      }
    }
    return trade;
  }

  @Override
  public Trade buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
    return buildObjectImpl(deserializer, message);
  }

}
