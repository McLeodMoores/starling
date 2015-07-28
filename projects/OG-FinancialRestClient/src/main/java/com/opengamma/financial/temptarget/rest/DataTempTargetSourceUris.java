/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.temptarget.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import com.opengamma.financial.temptarget.TempTargetSource;
import com.opengamma.id.UniqueId;

/**
 * RESTful URIs for a {@link TempTargetSource}
 */
public class DataTempTargetSourceUris {

  public static URI uriGet(final URI baseUri, final UniqueId uid) {
    return UriBuilder.fromUri(baseUri).path("/target/{uid}").build(uid);
  }
  
}
