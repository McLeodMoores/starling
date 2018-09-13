/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.net.URI;

import org.fudgemsg.FudgeMsg;

import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.rest.FudgeRestClient;

/**
 * A Spring factory bean to extract the URI from an end point description.
 * <p>
 * The server publishes a configuration document that defines a URI.
 * This class reads the URI, resolves it and returns it.
 */
public class UriEndPointUriFactoryBean extends SingletonFactoryBean<URI> {

  /**
   * The configuration URI.
   */
  private URI _uri;

  /**
   * Creates an instance.
   */
  public UriEndPointUriFactoryBean() {
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the URI.
   *
   * @return  the URI
   */
  public URI getUri() {
    return _uri;
  }

  /**
   * Sets the URI.
   *
   * @param uri  the URI
   */
  public void setUri(final URI uri) {
    _uri = uri;
  }

  //-------------------------------------------------------------------------
  @Override
  protected URI createObject() {
    final FudgeRestClient restClient = FudgeRestClient.create();
    final FudgeMsg msg = restClient.accessFudge(getUri()).get(FudgeMsg.class);
    if (!UriEndPointDescriptionProvider.TYPE_VALUE.equals(msg.getString(UriEndPointDescriptionProvider.TYPE_KEY))) {
      throw new IllegalArgumentException("End point is not a URI target - " + msg);
    }
    URI uri = URI.create(msg.getString(UriEndPointDescriptionProvider.URI_KEY));
    uri = getUri().resolve(uri);
    return uri;
  }

}
