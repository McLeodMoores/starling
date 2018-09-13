/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriBuilder;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.google.common.base.Supplier;
import com.opengamma.transport.jaxrs.FudgeFieldContainerBrowser;

/**
 * RESTful resource for providing a simple configuration map.
 * <p>
 * This resource receives and processes RESTful calls to part of a map of configuration.
 * This is a basic mechanism used to expose aspects of a server at a well-known URI,
 * such as available services and their URIs.
 */
@Path("configuration")
public class DataConfigurationResource extends AbstractDataResource {

  private final FudgeContext _fudgeContext;
  private final Map<String, Object> _resources;

  /**
   * Creates an instance with empty resources.
   *
   * @param fudgeContext  the context
   */
  public DataConfigurationResource(final FudgeContext fudgeContext) {
    this(fudgeContext, new ConcurrentHashMap<String, Object>());
  }

  /**
   * Creates an instance.
   *
   * @param fudgeContext  the context
   * @param resources  the resources
   */
  public DataConfigurationResource(final FudgeContext fudgeContext, final Map<String, Object> resources) {
    _fudgeContext = fudgeContext;
    _resources = resources;
  }

  /**
   * Gets the resources.
   *
   * @return  the resources
   */
  protected Map<String, Object> getResources() {
    return _resources;
  }

  /**
   * Adds a resource.
   *
   * @param name  the resource name
   * @param value  the resource value
   */
  public void addResource(final String name, final Object value) {
    getResources().put(name, value);
  }

  /**
   * Adds resources.
   *
   * @param map  the resources
   */
  public void addResources(final Map<String, Object> map) {
    getResources().putAll(map);
  }

  /**
   * Gets the Fudge context.
   *
   * @return  the context
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  @SuppressWarnings("unchecked")
  private FudgeMsg mapToMessage(final FudgeSerializer serializer, final Map<?, ?> value) {
    final MutableFudgeMsg message = serializer.newMessage();
    for (final Map.Entry<Object, Object> config : ((Map<Object, Object>) value).entrySet()) {
      Object configValue = config.getValue();
      if (configValue instanceof Supplier) {
        configValue = ((Supplier<?>) configValue).get();
      }
      if (configValue instanceof Map) {
        message.add(config.getKey().toString(), mapToMessage(serializer, (Map<?, ?>) configValue));
      } else {
        serializer.addToMessage(message, config.getKey().toString(), null, configValue);
      }
    }
    return message;
  }

  /**
   * Returns an envelope wrapping a Fudge message encoding the resources.
   *
   * @return  the envelope
   */
  @GET
  public FudgeMsgEnvelope getResource() {
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    return new FudgeMsgEnvelope(mapToMessage(serializer, getResources()));
  }

  /**
   * Gets the resources for a particular entry.
   *
   * @param entry  the resource name
   * @return  the resource
   */
  @SuppressWarnings("unchecked")
  @Path("{entry}")
  public Object getResource(@PathParam("entry") final String entry) {
    final Object object = getResources().get(entry);
    if (object == null) {
      return null;
    }
    if (object instanceof Map<?, ?>) {
      return new DataConfigurationResource(getFudgeContext(), (Map<String, Object>) object);
    }
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    return new FudgeFieldContainerBrowser(serializer.objectToFudgeMsg(object));
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI of this resource, not null
   * @param parts  the parts of the configuration, not null
   * @return the URI, not null
   */
  public static URI uri(final URI baseUri, final String... parts) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri);
    for (final String part : parts) {
      bld.path(part);
    }
    return bld.build();
  }

}
