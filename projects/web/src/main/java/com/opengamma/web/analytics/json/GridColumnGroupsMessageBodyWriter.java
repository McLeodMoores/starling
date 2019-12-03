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
import com.opengamma.web.analytics.GridColumnGroups;
import com.opengamma.web.analytics.GridColumnsJsonWriter;

/**
 *
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
/* package */ public class GridColumnGroupsMessageBodyWriter implements MessageBodyWriter<GridColumnGroups> {

  private final GridColumnsJsonWriter _writer;

  public GridColumnGroupsMessageBodyWriter(final GridColumnsJsonWriter writer) {
    ArgumentChecker.notNull(writer, "writer");
    _writer = writer;
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return type.equals(GridColumnGroups.class);
  }

  @Override
  public long getSize(final GridColumnGroups gridColumnGroups,
                      final Class<?> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(final GridColumnGroups gridColumnGroups,
                      final Class<?> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType,
                      final MultivaluedMap<String, Object> httpHeaders,
                      final OutputStream entityStream) throws IOException, WebApplicationException {
    final String json = _writer.getJson(gridColumnGroups.getGroups());
    entityStream.write(json.getBytes());
  }
}
