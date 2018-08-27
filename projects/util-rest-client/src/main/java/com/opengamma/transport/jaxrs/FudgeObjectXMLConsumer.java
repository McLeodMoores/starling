/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeMessageBuilder;
import org.fudgemsg.wire.FudgeMsgReader;
import org.fudgemsg.wire.xml.FudgeXMLStreamReader;

import com.google.common.base.Charsets;

/**
 * A JAX-RS provider to convert RESTful responses to Fudge XML encoded messages.
 * <p>
 * This converts directly to Fudge from the RESTful resource without the need to manually
 * create the message in application code.
 */
@Provider
@Consumes(MediaType.APPLICATION_XML)
public class FudgeObjectXMLConsumer extends FudgeBase implements MessageBodyReader<Object> {

  /**
   * Creates the consumer.
   */
  public FudgeObjectXMLConsumer() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    final FudgeMessageBuilder<?> builder = getFudgeContext().getObjectDictionary().getMessageBuilder(type);
    return type != String.class &&  // allow manually created JSON string to work
        (builder != null && !builder.getClass().getCanonicalName().equals("org.fudgemsg.mapping.JavaBeanBuilder") ||
          type == FudgeMsgEnvelope.class);
  }

  @Override
  public Object readFrom(
      final Class<Object> type,
      final Type genericType,
      final Annotation[] annotations,
      final MediaType mediaType,
      final MultivaluedMap<String, String> httpHeaders,
      final InputStream entityStream) throws IOException, WebApplicationException {

    final InputStreamReader entityReader = new InputStreamReader(entityStream, Charsets.UTF_8);
    @SuppressWarnings("resource")  // wraps stream that cannot be closed here
    final
    FudgeMsgReader reader = new FudgeMsgReader(new FudgeXMLStreamReader(getFudgeContext(), entityReader));
    final FudgeMsg message = reader.nextMessage();
    if (message == null) {
      return null;
    }
    final FudgeDeserializer deser = new FudgeDeserializer(getFudgeContext());
    return deser.fudgeMsgToObject(type, message);
  }

}
