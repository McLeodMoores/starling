/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.rest;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

/**
 * RESTful resource for the yield curve source.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
public class DataCurveSpecificationBuilderUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param valuationTime  the valuation time, not null
   * @param curveDate  the curve date, not null
   * @return the URI, not null
   */
  public static URI uriBuildCurve(URI baseUri, Instant valuationTime, LocalDate curveDate) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/builder/{valuationTime}/{date}");
    return bld.build(valuationTime, curveDate);
  }

}
