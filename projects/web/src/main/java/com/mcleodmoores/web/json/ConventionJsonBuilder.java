/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeRuntimeException;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.opengamma.core.convention.Convention;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.web.json.AbstractJSONBuilder;
import com.opengamma.web.json.FudgeMsgJSONWriter;

/**
 * Base class for builders that encode / decode JSON and provide templates for {@link Convention} types.
 * Any attributes that a convention has are converted as if they were their own object (c.f.
 * {@link com.opengamma.id.ExternalIdBundleFudgeBuilder}) rather than as a map. This makes building
 * forms more straightforward.
 *
 * @param <T>  the type of the convention
 */
public abstract class ConventionJsonBuilder<T extends Convention> extends AbstractJSONBuilder<T> {
  /** The field name used in the message */
  private static final String ATTR_FIELD_NAME = "attributes";
  /** The field name used in the message */
  private static final String KEY_FIELD_NAME = "Key";
  /** The field name used in the message */
  private static final String VALUE_FIELD_NAME = "Value";
  /** The current Fudge context */
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();

  /**
   * Completes the conversion of the convention. The input JSON string has any attributes
   * stripped out to allow conversion of the message without errors, as the original JSON
   * message had the attribute field in a format that is not compatible with <code>Map<String, String></code>.
   *
   * @param json  the JSON to convert to a convention, not null
   * @param attributes  the attributes (an empty map if there are no attributes set), not null
   * @return  the converted convention
   */
  abstract T fromJson(String json, Map<String, String> attributes);

  /**
   * Gets a copy of the convention. This allows the attributes to be removed and encoded separately
   * without changing the original convention.
   *
   * @param convention  the convention, not null
   * @return  a copy of this convention e.g. one returned from a <code>clone()</code> method.
   */
  abstract T getCopy(T convention);

  @Override
  public T fromJSON(final String json) {
    ArgumentChecker.notNull(json, "json");
    final StringReader sr = new StringReader(json);
    final Map<String, String> attributes = new HashMap<>();
    try {
      final JSONObject jsonObject = new JSONObject(new JSONTokener(sr));
      final JSONObject data = (JSONObject) jsonObject.get("data");
      if (data != null) {
        if (data.has(ATTR_FIELD_NAME)) {
          final JSONArray attributeJson = data.getJSONArray(ATTR_FIELD_NAME);
          // collect attributes into map
          if (attributeJson != null) {
            for (int i = 0; i < attributeJson.length(); i++) {
              final JSONObject entry = attributeJson.getJSONObject(i);
              attributes.put(entry.getString(KEY_FIELD_NAME), entry.getString(VALUE_FIELD_NAME));
            }
          }
          // remove changed form to deserialize without changed attributes message
          data.remove(ATTR_FIELD_NAME);
        }
      }
      // convert the specific convention and return
      return fromJson(jsonObject.toString(), attributes);
    } catch (final JSONException ex) {
      throw new FudgeRuntimeException("Error " + ex.getMessage() + " from JSON stream", ex);
    }
  }

  @Override
  public String toJSON(final T object) {
    final Map<String, String> attributes = object.getAttributes();
    final T copy = getCopy(object);
    final FudgeSerializer serializer = new FudgeSerializer(FUDGE_CONTEXT);
    final FudgeMsg fudgeMsg = FUDGE_CONTEXT.toFudgeMsg(copy).getMessage();
    final FudgeMsg attributesFudgeMsg = AttributesFudgeBuilder.BUILDER.buildMessage(serializer, attributes);
    final MutableFudgeMsg newMsg = serializer.newMessage();
    for (final FudgeField field : fudgeMsg.getAllFields()) {
      if (!ATTR_FIELD_NAME.equals(field.getName())) {
        newMsg.add(field);
      }
    }
    for (final FudgeField field : attributesFudgeMsg.getAllFields()) {
      newMsg.add(field);
    }
    final StringWriter sw = new StringWriter();
    try (FudgeMsgJSONWriter fudgeJSONWriter = new FudgeMsgJSONWriter(FUDGE_CONTEXT, sw)) {
      fudgeJSONWriter.writeMessage(newMsg);
      return sw.toString();
    }
  }

  /**
   * Partial implementation of a Fudge builder for the attribute map. Only the message builder is implemented.
   */
  private static final class AttributesFudgeBuilder extends AbstractFudgeBuilder {
    public static final AttributesFudgeBuilder BUILDER = new AttributesFudgeBuilder();

    /**
     * Builds the message.
     * @param serializer  the serializer, not null
     * @param object  the attributes, not null
     * @return  a message
     */
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

    private AttributesFudgeBuilder() {
    }
  }

}
