/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.Counterparty;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.money.Currency;

/**
 * Converts Trades to Json
 */
public final class TradeJsonConverter {

  /**
   * Restricted constructor.
   */
  private TradeJsonConverter() {
  }

  public static Set<ManageableTrade> fromJson(final String json) {
    final Set<ManageableTrade> trades = Sets.newHashSet();
    try {
      final JSONObject jsonObject = new JSONObject(json);
      if (jsonObject.has("trades")) {
        final JSONArray jsonArray = jsonObject.getJSONArray("trades");
        for (int i = 0; i < jsonArray.length(); i++) {
          final JSONObject tradeJson = jsonArray.getJSONObject(i);
          final ManageableTrade trade = new ManageableTrade();

          if (tradeJson.has("premium")) {
            trade.setPremium(tradeJson.getDouble("premium"));
          }
          if (tradeJson.has("counterParty")) {
            trade.setCounterpartyExternalId(ExternalId.of(Counterparty.DEFAULT_SCHEME, tradeJson.getString("counterParty")));
          }
          if (tradeJson.has("premiumCurrency")) {
            trade.setPremiumCurrency(Currency.of(tradeJson.getString("premiumCurrency")));
          }
          if (tradeJson.has("premiumDate")) {
            final LocalDate premiumDate = LocalDate.parse(tradeJson.getString("premiumDate"));
            trade.setPremiumDate(premiumDate);
            if (tradeJson.has("premiumTime")) {
              final LocalTime premiumTime = LocalTime.parse(tradeJson.getString("premiumTime"));
              final ZoneOffset premiumOffset = getOffset(tradeJson, "premiumOffset");
              final ZonedDateTime zonedDateTime = premiumDate.atTime(premiumTime).atZone(premiumOffset);
              trade.setPremiumTime(zonedDateTime.toOffsetDateTime().toOffsetTime());
            }
          }
          if (tradeJson.has("quantity")) {
            trade.setQuantity(new BigDecimal(tradeJson.getString("quantity")));
          }
          if (tradeJson.has("tradeDate")) {
            final LocalDate tradeDate = LocalDate.parse(tradeJson.getString("tradeDate"));
            trade.setTradeDate(tradeDate);
            if (tradeJson.has("tradeTime")) {
              final LocalTime tradeTime = LocalTime.parse(tradeJson.getString("tradeTime"));
              final ZoneOffset tradeOffset = getOffset(tradeJson, "tradeOffset");
              final ZonedDateTime zonedDateTime = tradeDate.atTime(tradeTime).atZone(tradeOffset);
              trade.setTradeTime(zonedDateTime.toOffsetDateTime().toOffsetTime());
            }
          }
          addTradeAttributes(trade, tradeJson);
          trades.add(trade);
        }
      } else {
        throw new OpenGammaRuntimeException("missing trades field in trades json document");
      }
    } catch (final JSONException ex) {
      throw new OpenGammaRuntimeException("Error parsing Json document for Trades", ex);
    }
    return trades;
  }

  private static void addTradeAttributes(final ManageableTrade trade, final JSONObject tradeJson) throws JSONException {
    if (tradeJson.has("attributes")) {
      final JSONObject attributes = tradeJson.getJSONObject("attributes");
      if (attributes.has("dealAttributes")) {
        final JSONObject dealAttributes = attributes.getJSONObject("dealAttributes");
        addAttributes(trade, dealAttributes);
      }
      if (attributes.has("userAttributes")) {
        final JSONObject userAttributes = attributes.getJSONObject("userAttributes");
        addAttributes(trade, userAttributes);
      }
    }
  }

  private static void addAttributes(final ManageableTrade trade, final JSONObject attributes) throws JSONException {
    @SuppressWarnings("rawtypes")
    final
    Iterator keys = attributes.keys();
    while (keys.hasNext()) {
      final String attrKey = (String) keys.next();
      final String attrValue = attributes.getString(attrKey);
      trade.addAttribute(attrKey, attrValue);
    }
  }

  private static ZoneOffset getOffset(final JSONObject tradeJson, final String fieldName) throws JSONException {
    ZoneOffset premiumOffset = ZoneOffset.UTC;
    if (tradeJson.has(fieldName)) {
      final String offsetId = StringUtils.trimToNull(tradeJson.getString(fieldName));
      if (offsetId != null) {
        premiumOffset = ZoneOffset.of(offsetId);
      }
    }
    return premiumOffset;
  }
}
