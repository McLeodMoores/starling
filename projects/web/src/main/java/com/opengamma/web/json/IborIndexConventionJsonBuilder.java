/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.json;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeBuilder;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.threeten.bp.LocalTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.convention.IborIndexConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;

/**
 * Custom JSON builder to convert an {@link IborIndexConvention} to JSON and back again.
 */
public final class IborIndexConventionJsonBuilder extends AbstractJSONBuilder<IborIndexConvention> {
  /**
   * Static instance.
   */
  public static final IborIndexConventionJsonBuilder INSTANCE = new IborIndexConventionJsonBuilder();
  private static final String ORIGINAL_FIELD_NAME = "attributes";
  private static final String ATTR_FIELD_NAME = "ATTR";
  private static final String KEY_FIELD_NAME = "Key";
  private static final String VALUE_FIELD_NAME = "Value";
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  @Override
  public IborIndexConvention fromJSON(final String json) {
    final IborIndexConvention convention = fromJSON(IborIndexConvention.class, ArgumentChecker.notNull(json, "json"));
    final StringReader sr = new StringReader(json);
    try {
      final JSONObject jsonObject = new JSONObject(new JSONTokener(sr));
      final JSONObject data = (JSONObject) jsonObject.get("data");
      if (data != null) {
        final JSONArray attributeJson = data.getJSONArray(ATTR_FIELD_NAME);
        if (attributeJson != null) {
          for (int i = 0; i < attributeJson.length(); i++) {
            final JSONObject entry = attributeJson.getJSONObject(i);
            convention.addAttribute(entry.getString(KEY_FIELD_NAME), entry.getString(VALUE_FIELD_NAME));
          }
        }
      }
    } catch (final JSONException ex) {
      throw new FudgeRuntimeException("Error " + ex.getMessage() + " from JSON stream", ex);
    }
    return convention;
  }

  @Override
  public String toJSON(final IborIndexConvention object) {
    final Map<String, String> attributes = object.getAttributes();
    final IborIndexConvention copy = object.clone();
    final FudgeSerializer serializer = new FudgeSerializer(s_fudgeContext);
    final FudgeMsg fudgeMsg = s_fudgeContext.toFudgeMsg(copy).getMessage();
    final FudgeMsg attributesFudgeMsg = AttributesFudgeBuilder.BUILDER.buildMessage(serializer, attributes);
    final MutableFudgeMsg newMsg = serializer.newMessage();
    for (final FudgeField field : fudgeMsg.getAllFields()) {
      if (!ORIGINAL_FIELD_NAME.equals(field.getName())) {
        newMsg.add(field);
      }
    }
    for (final FudgeField field : attributesFudgeMsg.getAllFields()) {
      newMsg.add(field);
    }
    final StringWriter sw = new StringWriter();
    try (FudgeMsgJSONWriter fudgeJSONWriter = new FudgeMsgJSONWriter(s_fudgeContext, sw)) {
      fudgeJSONWriter.writeMessage(newMsg);
      return sw.toString();
    }
  }

  @Override
  public String getTemplate() {
    return IborIndexConventionJsonBuilder.INSTANCE.toJSON(getDummyIborIndexConvention());
  }

  private static IborIndexConvention getDummyIborIndexConvention() {
    return new IborIndexConvention("XXXX", ExternalIdBundle.EMPTY, DayCounts.ACT_360, BusinessDayConventions.MODIFIED_FOLLOWING,
        2, false, Currency.USD, LocalTime.of(11, 0), "", ExternalSchemes.financialRegionId("US"), ExternalSchemes.financialRegionId("US"),
        "");
  }

  private IborIndexConventionJsonBuilder() {
  }

  private static class AttributesFudgeBuilder extends AbstractFudgeBuilder implements FudgeBuilder<Map<String, String>> {
    public static final FudgeBuilder<Map<String, String>> BUILDER = new AttributesFudgeBuilder();

    @Override
    public MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Map<String, String> object) {
      final MutableFudgeMsg msg = serializer.newMessage();
      for (final Map.Entry<String, String> entry : object.entrySet()) {
        final MutableFudgeMsg entryMsg = serializer.newMessage();
        addToMessage(entryMsg, KEY_FIELD_NAME, entry.getKey());
        addToMessage(entryMsg, VALUE_FIELD_NAME, entry.getValue());
        addToMessage(msg, ATTR_FIELD_NAME, entryMsg);
      }
      return msg;
    }

    @Override
    public Map<String, String> buildObject(final FudgeDeserializer deserializer, final FudgeMsg message) {
      throw new UnsupportedOperationException();
    }

    private AttributesFudgeBuilder() {
    }
  }
}
