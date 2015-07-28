/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.LocalDate;

import com.opengamma.id.VersionCorrection;

/**
 * RESTful URIs for the yield curve source.
 */
public class DataInterpolatedYieldCurveSpecificationBuilderUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @param curveDate the curve date, not null
   * @param version the configuration version, not null
   * @return the URI, not null
   */
  public static URI uriBuildCurve(URI baseUri, LocalDate curveDate, VersionCorrection version) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/builder/{date}/{version}");
    return bld.build(curveDate, version);
  }

}
