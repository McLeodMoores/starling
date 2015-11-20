/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.convention.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.financial.convention.ConventionBundleSource;
import com.opengamma.id.UniqueId;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * RESTful URIs for exposing a {@link ConventionBundleSource} to remote clients.
 */
public class DataConventionBundleSourceUris {
 
  public static URI uriGetByIdentifier(final URI baseUri, final ExternalId id) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("identifier/{id}");
    return bld.buildFromEncoded(id.toString().replace("/", "%2F"));
  }

  public static URI uriGetByBundle(final URI baseUri, final ExternalIdBundle ids) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("bundle");
    bld.queryParam("id", ids.toStringList().toArray());
    return bld.build();
  }
  
  public static URI uriGetByUniqueId(final URI baseUri, final UniqueId id) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("unique/{id}");
    return bld.buildFromEncoded(id.toString().replace("/", "%2F"));
  }
}
