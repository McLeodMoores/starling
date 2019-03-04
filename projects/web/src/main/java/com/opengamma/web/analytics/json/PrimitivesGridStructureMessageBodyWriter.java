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
import com.opengamma.web.analytics.GridColumnsJsonWriter;
import com.opengamma.web.analytics.PrimitivesGridStructure;

/**
 * Writes an instance of {@link PortfolioGridStructure} to JSON.
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class PrimitivesGridStructureMessageBodyWriter implements MessageBodyWriter<PrimitivesGridStructure> {

  /** Writes the JSON. */
  private final GridColumnsJsonWriter _writer;

  /**
   * @param writer Writes the JSON
   */
  public PrimitivesGridStructureMessageBodyWriter(final GridColumnsJsonWriter writer) {
    ArgumentChecker.notNull(writer, "writer");
    _writer = writer;
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return type.equals(PrimitivesGridStructure.class);
  }

  @Override
  public long getSize(final PrimitivesGridStructure gridStructure,
                      final Class<?> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType) {
    // TODO this means unknown size. is it worth encoding it twice to find out the size?
    return -1;
  }

  @Override
  public void writeTo(final PrimitivesGridStructure gridStructure,
                      final Class<?> type,
                      final Type genericType,
                      final Annotation[] annotations,
                      final MediaType mediaType,
                      final MultivaluedMap<String, Object> httpHeaders,
                      final OutputStream entityStream) throws IOException, WebApplicationException {
    final String columnsJson = _writer.getJson(gridStructure.getColumnStructure().getGroups());
    entityStream.write(("{\"columnSets\":" + columnsJson + ",\"rowCount\":" + gridStructure.getRowCount() + "}").getBytes());
  }
}
