/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.json;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONArray;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opengamma.web.analytics.ErrorInfo;

/**
 * Writes a list of {@link ErrorInfo} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ErrorInfoMessageBodyWriter implements MessageBodyWriter<List<ErrorInfo>> {

  private static final String ERROR_MESSAGE = "errorMessage";
  private static final String ID = "id";

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    if (!(genericType instanceof ParameterizedType)) {
      return false;
    }
    final ParameterizedType parameterizedType = (ParameterizedType) genericType;
    return parameterizedType.getRawType().equals(List.class) &&
        parameterizedType.getActualTypeArguments().length == 1 &&
        parameterizedType.getActualTypeArguments()[0].equals(ErrorInfo.class);
  }

  @Override
  public long getSize(final List<ErrorInfo> errorInfo,
                      final Class<?> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType) {
    return -1; // unknown
  }

  @Override
  public void writeTo(final List<ErrorInfo> errorInfos,
                      final Class<?> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType,
                      final MultivaluedMap<String, Object> httpHeaders,
                      final OutputStream entityStream) throws IOException, WebApplicationException {
    final List<Map<String, Object>> errors = Lists.newArrayList();
    for (final ErrorInfo errorInfo : errorInfos) {
      errors.add(ImmutableMap.<String, Object>of(ERROR_MESSAGE, errorInfo.getMessage(), ID, errorInfo.getId()));
    }
    entityStream.write(new JSONArray(errors).toString().getBytes());
  }
}
