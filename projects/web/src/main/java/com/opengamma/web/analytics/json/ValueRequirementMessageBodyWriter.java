/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.web.analytics.ValueRequirementTargetForCell;
import com.opengamma.web.json.ValueRequirementJSONBuilder;

/**
 * Writes an instance of {@link ValueRequirement} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ValueRequirementMessageBodyWriter implements MessageBodyWriter<ValueRequirementTargetForCell> {


  @Override
  public boolean isWriteable(final Class<?> aClass, final Type type, final Annotation[] annotations, final MediaType mediaType) {
    return type.equals(ValueRequirementTargetForCell.class);
  }

  @Override
  public long getSize(final ValueRequirementTargetForCell stringValueRequirementPair,
                      final Class<?> aClass,
                      final Type type,
                      final Annotation[] annotations,
                      final MediaType mediaType) {
    // TODO this means unknown size. is it worth encoding it twice to find out the size?
    return -1;
  }

  @Override
  public void writeTo(final ValueRequirementTargetForCell valueReq,
                      final Class<?> aClass,
                      final Type type,
                      final Annotation[] annotations,
                      final MediaType mediaType,
                      final MultivaluedMap<String, Object> stringObjectMultivaluedMap,
                      final OutputStream outputStream) throws IOException, WebApplicationException {
    final ValueRequirementJSONBuilder jsonBuilder = new ValueRequirementJSONBuilder();
    final String valueSpecStr = jsonBuilder.toJSON(valueReq.getValueRequirement());

    JSONObject valueReqJson;
    try {
      // need to convert it to a JSON object instead of a string otherwise it will be inserted into the outer object
      // as an escaped string instead of a child object
      valueReqJson = new JSONObject(valueSpecStr);
    } catch (final JSONException e) {
      throw new OpenGammaRuntimeException("Failed to convert ValueRequirement to JSON", e);
    }
    final ImmutableMap<String, Object> jsonMap = ImmutableMap.of("columnSet", valueReq.getColumnSet(),
                                                           "valueRequirement", valueReqJson);
    outputStream.write(new JSONObject(jsonMap).toString().getBytes());
  }

}
