/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.threeten.bp.LocalDate;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeMapWrapper;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for time-series.
 * <p>
 * The time-series resource receives and processes RESTful calls to the time-series source.
 */
@Path("htsSource")
public class DataHistoricalTimeSeriesSourceResource extends AbstractDataResource {

  /**
   * The time-series source.
   */
  private final HistoricalTimeSeriesSource _htsSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param htsSource  the underlying time-series source, not null
   */
  public DataHistoricalTimeSeriesSourceResource(final HistoricalTimeSeriesSource htsSource) {
    ArgumentChecker.notNull(htsSource, "htsSource");
    _htsSource = htsSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the time-series source.
   *
   * @return the time-series source, not null
   */
  public HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _htsSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("hts/{htsId}")
  public Response get(
      @PathParam("htsId") final String idStr,
      @QueryParam("version") final String version,
      @QueryParam("start") final String startStr,
      @QueryParam("includeStart") final  boolean includeStart,
      @QueryParam("end") final String endStr,
      @QueryParam("includeEnd") final boolean includeEnd,
      @QueryParam("maxPoints") final Integer maxPoints) {
    final UniqueId uniqueId = ObjectId.parse(idStr).atVersion(version);
    final LocalDate start = startStr != null ? LocalDate.parse(startStr) : null;
    final LocalDate end = endStr != null ? LocalDate.parse(endStr) : null;
    final HistoricalTimeSeries result;
    if (start == null && end == null && maxPoints == null) {
      result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(uniqueId);
    } else if (maxPoints != null) {
      result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd, maxPoints);
    } else {
      result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(uniqueId, start, includeStart, end, includeEnd);
    }
    return responseOkObject(result);
  }

  @GET
  @Path("htsMeta/externalIdBundle/{htsId}")
  public Response getExternalIdBundle(
      @PathParam("htsId") final String idStr,
      @QueryParam("version") final String version) {
    final UniqueId uniqueId = ObjectId.parse(idStr).atVersion(version);
    final ExternalIdBundle idBundle = getHistoricalTimeSeriesSource().getExternalIdBundle(uniqueId);
    return responseOkObject(idBundle);
  }

  @GET
  @Path("htsSearches/single")
  public Response searchSingle(
      @QueryParam("id") final List<String> idStrs,
      @QueryParam("idValidityDate") final String idValidityDateStr,
      @QueryParam("dataSource") final String dataSource,
      @QueryParam("dataProvider") final String dataProvider,
      @QueryParam("dataField") final String dataField,
      @QueryParam("start") final String startStr,
      @QueryParam("includeStart") final  boolean includeStart,
      @QueryParam("end") final String endStr,
      @QueryParam("includeEnd") final boolean includeEnd,
      @QueryParam("maxPoints") final Integer maxPoints) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(idStrs);
    final LocalDate start = startStr != null ? LocalDate.parse(startStr) : null;
    final LocalDate end = endStr != null ? LocalDate.parse(endStr) : null;
    final HistoricalTimeSeries result;
    if (idValidityDateStr != null) {
      final LocalDate idValidityDate = "ALL".equals(idValidityDateStr) ? null : LocalDate.parse(idValidityDateStr);
      if (start == null && end == null && maxPoints == null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, idValidityDate, dataSource, dataProvider, dataField);
      } else if (maxPoints != null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, idValidityDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
      } else {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, idValidityDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd);
      }
    } else {
      if (start == null && end == null && maxPoints == null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, dataSource, dataProvider, dataField);
      } else if (maxPoints != null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
      } else {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            bundle, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd);
      }
    }
    return responseOkObject(result);
  }

  @GET
  @Path("htsSearches/resolve")
  public Response searchResolve(
      @QueryParam("id") final List<String> idStrs,
      @QueryParam("idValidityDate") final String idValidityDateStr,
      @QueryParam("dataField") final String dataField,
      @QueryParam("resolutionKey") final String resolutionKey,
      @QueryParam("start") final String startStr,
      @QueryParam("includeStart") final  boolean includeStart,
      @QueryParam("end") final String endStr,
      @QueryParam("includeEnd") final boolean includeEnd,
      @QueryParam("maxPoints") final Integer maxPoints) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(idStrs);
    final LocalDate start = startStr != null ? LocalDate.parse(startStr) : null;
    final LocalDate end = endStr != null ? LocalDate.parse(endStr) : null;
    final HistoricalTimeSeries result;
    if (idValidityDateStr != null) {
      final LocalDate idValidityDate = "ALL".equals(idValidityDateStr) ? null : LocalDate.parse(idValidityDateStr);
      if (start == null && end == null && maxPoints == null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, idValidityDate, resolutionKey);
      } else if (maxPoints != null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, idValidityDate, resolutionKey, start, includeStart, end, includeEnd, maxPoints);
      } else {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, idValidityDate, resolutionKey, start, includeStart, end, includeEnd);
      }
    } else {
      if (start == null && end == null && maxPoints == null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, resolutionKey);
      } else if (maxPoints != null) {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, resolutionKey, start, includeStart, end, includeEnd, maxPoints);
      } else {
        result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
            dataField, bundle, resolutionKey, start, includeStart, end, includeEnd);
      }
    }
    return responseOkObject(result);
  }

  @SuppressWarnings("unchecked")
  @POST
  @Path("htsSearches/bulk")
  public Response searchBulk(final FudgeMsgEnvelope request) {
    // non-ideal variant using POST
    final FudgeMsg msg = request.getMessage();
    final FudgeDeserializer deserializationContext = new FudgeDeserializer(OpenGammaFudgeContext.getInstance());
    final Set<ExternalIdBundle> identifierSet = deserializationContext.fudgeMsgToObject(Set.class, msg.getMessage("id"));
    final String dataSource = msg.getString("dataSource");
    final String dataProvider = msg.getString("dataProvider");
    final String dataField = msg.getString("dataField");
    final LocalDate start = deserializationContext.fieldValueToObject(LocalDate.class, msg.getByName("start"));
    final boolean inclusiveStart = msg.getBoolean("includeStart");
    final LocalDate end = deserializationContext.fieldValueToObject(LocalDate.class, msg.getByName("end"));
    final boolean includeEnd = msg.getBoolean("includeEnd");

    final Map<ExternalIdBundle, HistoricalTimeSeries> result = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        identifierSet, dataSource, dataProvider, dataField, start, inclusiveStart, end, includeEnd);
    return responseOkObject(FudgeMapWrapper.of(result));
  }

  //-------------------------------------------------------------------------
  /**
   * For debugging purposes only.
   *
   * @return some debug information about the state of this resource object
   */
  @GET
  @Path("debugInfo")
  public FudgeMsgEnvelope getDebugInfo() {
    final MutableFudgeMsg message = OpenGammaFudgeContext.getInstance().newMessage();
    message.add("fudgeContext", OpenGammaFudgeContext.getInstance().toString());
    message.add("historicalTimeSeriesSource", getHistoricalTimeSeriesSource().toString());
    return new FudgeMsgEnvelope(message);
  }

}
