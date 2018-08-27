/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.ser.JodaBeanMimeType;
import org.joda.beans.ser.JodaBeanSer;

import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * A JAX-RS provider to convert RESTful Joda-Bean instances to and from XML encoded messages.
 */
@Provider
@Produces(JodaBeanMimeType.XML)
@Consumes(JodaBeanMimeType.XML)
public class JodaBeanXmlProducerConsumer implements MessageBodyReader<Object>, MessageBodyWriter<Object> {

  /**
   * Creates an instance.
   */
  public JodaBeanXmlProducerConsumer() {
    super();
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean isReadable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return Bean.class.isAssignableFrom(type) || type == FudgeResponse.class ||
        FudgeMsgEnvelope.class.isAssignableFrom(type) || FudgeMsg.class.isAssignableFrom(type);
  }

  @Override
  public Object readFrom(final Class<Object> type, final Type genericType, final Annotation[] annotations,
      final MediaType mediaType, final MultivaluedMap<String, String> httpHeaders, final InputStream entityStream) throws IOException, WebApplicationException {
    final boolean isBean = Bean.class.isAssignableFrom(type);
    final Class<? extends Bean> cls = isBean ? type.asSubclass(Bean.class) : FlexiBean.class;
    final Bean bean = JodaBeanSer.PRETTY.xmlReader().read(entityStream, cls);
    if (isBean) {
      return bean;
    }
    final FlexiBean fbean = (FlexiBean) bean;
    if ((Object) type == FudgeResponse.class) {
      return FudgeResponse.of(fbean.get("value"));
    }
    if ((Object) type == FudgeMsg.class) {
      return createMessage(bean);
    }
    if ((Object) type == FudgeMsgEnvelope.class) {
      return new FudgeMsgEnvelope(createMessage(bean));
    }
    return bean;
  }

  private MutableFudgeMsg createMessage(final Bean bean) {
    final MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    for (final MetaProperty<?> mp : bean.metaBean().metaPropertyIterable()) {
      final Object obj = mp.get(bean);
      if (obj instanceof Bean) {
        msg.add(mp.name(), createMessage((Bean) obj));
      } else  {
        msg.add(mp.name(), obj);
      }
    }
    return msg;
  }

  @Override
  public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return Bean.class.isAssignableFrom(type) || type == FudgeResponse.class ||
        FudgeMsgEnvelope.class.isAssignableFrom(type) || FudgeMsg.class.isAssignableFrom(type);
  }

  @Override
  public long getSize(final Object bean, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
    return -1;
  }

  @Override
  public void writeTo(final Object obj, final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType,
      final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream) throws IOException, WebApplicationException {
    Bean bean = null;
    if (Bean.class.isAssignableFrom(type)) {
      bean = (Bean) obj;
    } else {
      if ((Object) type == FudgeResponse.class) {
        final FudgeResponse rsp = (FudgeResponse) obj;
        final FlexiBean fb = new FlexiBean();
        fb.set("value", rsp.getValue());
        bean = fb;
      } else if ((Object) type == FudgeMsg.class) {
        final FudgeMsg msg = (FudgeMsg) obj;
        bean = createBean(msg);
      } else if ((Object) type == FudgeMsgEnvelope.class) {
        final FudgeMsgEnvelope env = (FudgeMsgEnvelope) obj;
        bean = createBean(env.getMessage());
      }
    }
    final String xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
    final byte[] bytes = xml.getBytes(StandardCharsets.UTF_8);
    entityStream.write(bytes);
  }

  private Bean createBean(final FudgeMsg msg) {
    final FlexiBean fb = new FlexiBean();
    for (final FudgeField field : msg.getAllFields()) {
      final Object obj = field.getValue();
      if (obj instanceof FudgeMsg) {
        fb.set(field.getName(), createBean((FudgeMsg) obj));
      } else  {
        fb.set(field.getName(), obj);
      }
    }
    return fb;
  }

}
