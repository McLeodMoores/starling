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

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.RestUtils;
import com.opengamma.web.analytics.ViewportResults;
import com.opengamma.web.analytics.ViewportResultsJsonCsvWriter;

/**
 * Writes an instance of {@link ViewportResults} to an HTTP response as JSON and CSV.
 */
@Provider
@Produces(value = { MediaType.APPLICATION_JSON, RestUtils.TEXT_CSV })
public class ViewportResultsMessageBodyWriter implements MessageBodyWriter<ViewportResults> {

  private final ViewportResultsJsonCsvWriter _resultWriter;

  public ViewportResultsMessageBodyWriter(final ViewportResultsJsonCsvWriter resultWriter) {
    ArgumentChecker.notNull(resultWriter, "resultWriter");
    _resultWriter = resultWriter;
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return type.equals(ViewportResults.class);
  }

  @Override
  public long getSize(final ViewportResults results,
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(final ViewportResults results,
      final Class<?> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType,
      final MultivaluedMap<String, Object> httpHeaders,
      final OutputStream entityStream) throws IOException, WebApplicationException {
    if (mediaType.getType().equals(MediaType.APPLICATION_JSON_TYPE.getType())
        && mediaType.getSubtype().equalsIgnoreCase(MediaType.APPLICATION_JSON_TYPE.getSubtype())) {
      entityStream.write(_resultWriter.getJson(results).getBytes());
    }
    if (mediaType.getType().equals(RestUtils.TEXT_CSV_TYPE.getType()) && mediaType.getSubtype().equalsIgnoreCase(RestUtils.TEXT_CSV_TYPE.getSubtype())) {
      entityStream.write(_resultWriter.getCsv(results).getBytes());
    }
  }
}
