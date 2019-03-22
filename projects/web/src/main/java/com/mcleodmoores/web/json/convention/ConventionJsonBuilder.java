/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.AbstractFudgeBuilder;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.web.json.AbstractJSONBuilder;
import com.opengamma.web.json.FudgeMsgJSONWriter;

/**
 * Base class for builders that encode / decode JSON and provide templates for {@link Convention} types. Any attributes that a convention has are converted as
 * if they were their own object (c.f. {@link com.opengamma.id.ExternalIdBundleFudgeBuilder}) rather than as a map. This makes building forms more
 * straightforward.
 *
 * @param <T>
 *          the type of the convention
 */
public abstract class ConventionJsonBuilder<T extends Convention> extends AbstractJSONBuilder<T> {
  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(ConventionJsonBuilder.class);
  /** The field name used in the message */
  private static final String UNDERLYING_CONVENTION_NAME = "underlyingConventionName";
  /** The field name used in the message */
  private static final String ATTR_FIELD_NAME = "attributes";
  /** The field name used in the message */
  private static final String KEY_FIELD_NAME = "Key";
  /** The field name used in the message */
  private static final String VALUE_FIELD_NAME = "Value";
  /** The current Fudge context */
  private static final FudgeContext FUDGE_CONTEXT = OpenGammaFudgeContext.getInstance();
  /** A Fudge serializer */
  private static final FudgeSerializer FUDGE_SERIALIZER = new FudgeSerializer(FUDGE_CONTEXT);
  /** An external id with blank fields */
  static final ExternalId EMPTY_EID = ExternalId.of(" ", " ");

  /**
   * Completes the conversion of the convention. The input JSON string has any attributes stripped out to allow conversion of the message without errors, as the
   * original JSON message had the attribute field in a format that is not compatible with <code>Map<String, String></code>.
   *
   * @param json
   *          the JSON to convert to a convention, not null
   * @param attributes
   *          the attributes (an empty map if there are no attributes set), not null
   * @return the converted convention
   */
  abstract T fromJson(String json, Map<String, String> attributes);

  /**
   * Gets a copy of the convention. This allows the attributes to be removed and encoded separately without changing the original convention.
   *
   * @param convention
   *          the convention, not null
   * @return a copy of this convention e.g. one returned from a <code>clone()</code> method.
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
  public String toJSON(final T convention) {
    final MutableFudgeMsg newMsg = FUDGE_SERIALIZER.newMessage();
    addAttributes(convention, newMsg);
    return convertToJson(newMsg);
  }

  /**
   * Temporary method that adds the name of the underlying convention to the message.
   *
   * @param convention
   *          the convention
   * @param underlyingConventionId
   *          the identifier of the underlying convention
   * @param conventionMaster
   *          a convention master
   * @return the message
   */
  String toJSONWithUnderlyingConvention(final T convention, final ExternalId underlyingConventionId, final ConventionMaster conventionMaster) {
    final MutableFudgeMsg newMsg = FUDGE_SERIALIZER.newMessage();
    addAttributes(convention, newMsg);
    addConventionName(underlyingConventionId, conventionMaster, newMsg);
    return convertToJson(newMsg);
  }

  /**
   * Adds attribute information to the message.
   *
   * @param convention
   *          the convention
   * @param msg
   *          the message
   */
  void addAttributes(final T convention, final MutableFudgeMsg msg) {
    final Map<String, String> attributes = convention.getAttributes();
    final T copy = getCopy(convention);
    final FudgeMsg fudgeMsg = FUDGE_CONTEXT.toFudgeMsg(copy).getMessage();
    final FudgeMsg attributesFudgeMsg = AttributesFudgeBuilder.buildMessage(FUDGE_SERIALIZER, attributes);
    for (final FudgeField field : fudgeMsg.getAllFields()) {
      if (!ATTR_FIELD_NAME.equals(field.getName())) {
        msg.add(field);
      }
    }
    for (final FudgeField field : attributesFudgeMsg.getAllFields()) {
      msg.add(field);
    }
  }

  /**
   * Adds the convention name to the message.
   *
   * @param underlyingConventionId
   *          the convention id
   * @param conventionMaster
   *          a convention master
   * @param msg
   *          the message
   */
  void addConventionName(final ExternalId underlyingConventionId, final ConventionMaster conventionMaster, final MutableFudgeMsg msg) {
    final ConventionSearchRequest request = new ConventionSearchRequest();
    request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.ANY, underlyingConventionId));
    try {
      final ConventionSearchResult result = conventionMaster.search(request);
      if (result.getConventions().size() == 1) {
        final String underlyingConventionName = result.getSingleConvention().getName();
        msg.add(UNDERLYING_CONVENTION_NAME, underlyingConventionName);
      }
    } catch (final DataNotFoundException e) {
      // log but ignore this for now
      LOGGER.warn("Could not get any convention for request {}", request);
    }
  }

  /**
   * Converts a Fudge message to JSON.
   *
   * @param msg
   *          the message
   * @return the message as JSON
   */
  String convertToJson(final MutableFudgeMsg msg) {
    final StringWriter sw = new StringWriter();
    try (FudgeMsgJSONWriter fudgeJSONWriter = new FudgeMsgJSONWriter(FUDGE_CONTEXT, sw)) {
      fudgeJSONWriter.writeMessage(msg);
      return sw.toString();
    }
  }

  /**
   * Temporary method that uses the underlying convention name to get the identifier of the underlying convention.
   *
   * @param json
   *          the JSON returned from the GUI
   * @param underlyingConventionFieldName
   *          the name of the field containing the convention name
   * @param conventionType
   *          the type of the underlying convention
   * @param conventionMaster
   *          a convention master
   * @return the JSON with an identifier rather than the convention name that can be parsed by Fudge
   */
  static String replaceUnderlyingConventionName(final String json, final String underlyingConventionFieldName, final ConventionType conventionType,
      final ConventionMaster conventionMaster) {
    // TODO temporary fix - only the name of the underlying index convention is returned from the GUI
    final String toParse = json;
    try {
      final StringReader sr = new StringReader(toParse);
      final JSONObject jsonObject = new JSONObject(new JSONTokener(sr));
      final JSONObject data = jsonObject.getJSONObject("data");
      if (data != null) {
        try {
          // can the underlying field be parsed as an external id
          ExternalId.parse(data.getString(underlyingConventionFieldName));
        } catch (final IllegalArgumentException e) {
          // if not, use the convention name to get the id from the master
          final String conventionName;
          if (data.has(UNDERLYING_CONVENTION_NAME)) {
            conventionName = data.getString(underlyingConventionFieldName);
          } else if (data.has(underlyingConventionFieldName)) {
            conventionName = data.getString(underlyingConventionFieldName);
          } else {
            conventionName = null;
          }
          data.remove(underlyingConventionFieldName);
          final ConventionSearchRequest request = new ConventionSearchRequest();
          request.setName(conventionName);
          request.setConventionType(conventionType);
          final ConventionSearchResult result = conventionMaster.search(request);
          if (result.getConventions().size() == 1) {
            final ExternalId conventionId = result.getSingleConvention().getExternalIdBundle().iterator().next();
            data.put(underlyingConventionFieldName, conventionId.toString());
          }
          jsonObject.remove("data");
          jsonObject.put("data", data);
        }
      }
      return jsonObject.toString();
    } catch (final JSONException e) {
      throw new IllegalArgumentException(e);
    }
  }

  /**
   * Gets the Fudge serializer.
   *
   * @return the Fudge serializer
   */
  FudgeSerializer getFudgeSerializer() {
    return FUDGE_SERIALIZER;
  }

  /**
   * Partial implementation of a Fudge builder for the attribute map. Only the message builder is implemented.
   */
  static final class AttributesFudgeBuilder extends AbstractFudgeBuilder {

    /**
     * Builds the message.
     *
     * @param serializer
     *          the serializer, not null
     * @param object
     *          the attributes, not null
     * @return a message
     */
    public static MutableFudgeMsg buildMessage(final FudgeSerializer serializer, final Map<String, String> object) {
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
