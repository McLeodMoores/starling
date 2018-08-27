/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.holiday.impl;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.threeten.bp.LocalDate;

import com.opengamma.core.holiday.Holiday;
import com.opengamma.core.holiday.HolidaySource;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeBooleanWrapper;
import com.opengamma.util.money.Currency;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for holidays.
 * <p>
 * The holidays resource receives and processes RESTful calls to the holiday source.
 */
@Path("holidaySource")
public class DataHolidaySourceResource extends AbstractDataResource {

  /**
   * The holiday source.
   */
  private final HolidaySource _holSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param holidaySource  the underlying holiday source, not null
   */
  public DataHolidaySourceResource(final HolidaySource holidaySource) {
    ArgumentChecker.notNull(holidaySource, "holidaySource");
    _holSource = holidaySource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the holiday source.
   *
   * @return the holiday source, not null
   */
  public HolidaySource getHolidaySource() {
    return _holSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("holidays/{holidayId}")
  public Response get(
      @PathParam("holidayId") final String idStr,
      @QueryParam("version") final String version,
      @QueryParam("versionAsOf") final String versionAsOf,
      @QueryParam("correctedTo") final String correctedTo) {

    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final Holiday result = getHolidaySource().get(objectId.atVersion(version));
      return responseOkObject(result);
    } else {
      final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
      final Holiday result = getHolidaySource().get(objectId, vc);
      return responseOkObject(result);
    }
  }

  @GET
  @Path("holidaySearches/retrieve")
  public Response get(
      @QueryParam("holidayType") final HolidayType holidayType,
      @QueryParam("currency") final String currencyCode,
      @QueryParam("id") final List<String> externalIdStrs) {

    final Collection<Holiday> result = holidayType == HolidayType.CURRENCY ?
        getHolidaySource().get(Currency.of(currencyCode)) :
        getHolidaySource().get(holidayType, ExternalIdBundle.parse(externalIdStrs));
    return responseOkObject(result);
  }

  // deprecated
  //-------------------------------------------------------------------------
  @SuppressWarnings("deprecation")
  @GET
  @Path("holidaySearches/check")
  public Response check(
      @QueryParam("date") final String localDateStr,
      @QueryParam("holidayType") final HolidayType holidayType,
      @QueryParam("currency") final String currencyCode,
      @QueryParam("id") final List<String> externalIdStrs) {

    final LocalDate date = LocalDate.parse(localDateStr);
    if (holidayType == HolidayType.CURRENCY) {
      final boolean result = getHolidaySource().isHoliday(date, Currency.of(currencyCode));
      return responseOkObject(FudgeBooleanWrapper.of(result));
    } else {
      final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
      final boolean result = getHolidaySource().isHoliday(date, holidayType, bundle);
      return responseOkObject(FudgeBooleanWrapper.of(result));
    }
  }
}
