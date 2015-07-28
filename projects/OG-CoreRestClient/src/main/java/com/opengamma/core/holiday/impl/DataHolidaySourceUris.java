/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import java.net.URI;

import javax.ws.rs.core.UriBuilder;

import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * RESTful URIs for holidays.
 */
public class DataHolidaySourceUris {

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("holidays/{holidayId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @param vc  the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("holidays/{holidayId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param currency  the currency, not null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, Currency currency) {
    return UriBuilder.fromUri(baseUri).path("holidaySearches/retrieve")
        .queryParam("currency", ArgumentChecker.notNull(currency, "currency").getCode())
        .build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param holidayType  the holiday type, not null
   * @param regionOrExchangeIds  the ids, not null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, HolidayType holidayType, ExternalIdBundle regionOrExchangeIds) {
    return UriBuilder.fromUri(baseUri).path("holidaySearches/retrieve")
        .queryParam("holidayType", ArgumentChecker.notNull(holidayType, "holidayType").name())
        .queryParam("id", ArgumentChecker.notNull(regionOrExchangeIds, "regionOrExchangeIds").toStringList().toArray())
        .build();
  }
  // deprecated
  //-------------------------------------------------------------------------

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param date  the date, not null
   * @param holidayType  the holiday type, not null
   * @param currency  the currency, may be null
   * @param regionOrExchangeIds  the ids, may be null
   * @return the URI, not null
   */
  public static URI uriSearchCheck(URI baseUri, LocalDate date, HolidayType holidayType, Currency currency, ExternalIdBundle regionOrExchangeIds) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("holidaySearches/check")
        .queryParam("date", date.toString())
        .queryParam("holidayType", holidayType.name());
    if (currency != null) {
      bld.queryParam("currency", currency.getCode());
    }
    if (regionOrExchangeIds != null) {
      bld.queryParam("id", regionOrExchangeIds.toStringList().toArray());
    }
    return bld.build();
  }
}
