/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.historicaltimeseries;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.DateTimeException;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Maps;
import com.opengamma.DataNotFoundException;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.id.ExternalIdDisplayComparatorUtils;
import com.opengamma.core.id.ExternalIdWithDatesDisplayComparator;
import com.opengamma.core.id.ExternalIdWithDatesDisplayComparatorUtils;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.timeseries.date.localdate.ImmutableLocalDateDoubleTimeSeries;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * RESTful resource for all historical time-series.
 * <p>
 * This resource represents the whole of a historical time-series master.
 */
@Path("/timeseries")
public class WebAllHistoricalTimeSeriesResource extends AbstractWebHistoricalTimeSeriesResource {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(WebAllHistoricalTimeSeriesResource.class);

  /**
   * Creates the resource.
   * @param master  the historical time-series master, not null
   * @param loader  the historical time-series loader, not null
   * @param configSource  the configuration source, not null
   */
  public WebAllHistoricalTimeSeriesResource(final HistoricalTimeSeriesMaster master, final HistoricalTimeSeriesLoader loader, final ConfigSource configSource) {
    super(master, loader, configSource);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.TIME_SERIES)
  public String getHTML(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("identifier") final String identifier,
      @QueryParam("dataSource") final String dataSource,
      @QueryParam("dataProvider") final String dataProvider,
      @QueryParam("dataField") final String dataField,
      @QueryParam("observationTime") final String observationTime,
      @QueryParam("name") final String name,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final FlexiBean out = createSearchResultData(pr, identifier, dataSource, dataProvider, dataField, observationTime, name, uriInfo);
    return getFreemarker().build(HTML_DIR + "alltimeseries.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.TIME_SERIES)
  public String getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("identifier") final String identifier,
      @QueryParam("dataSource") final String dataSource,
      @QueryParam("dataProvider") final String dataProvider,
      @QueryParam("dataField") final String dataField,
      @QueryParam("observationTime") final String observationTime,
      @QueryParam("name") final String name,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final FlexiBean out = createSearchResultData(pr, identifier, dataSource, dataProvider, dataField, observationTime, name, uriInfo);
    return getFreemarker().build(JSON_DIR + "alltimeseries.ftl", out);
  }

  private FlexiBean createSearchResultData(final PagingRequest pr, final String identifier, final String dataSource, final String dataProvider, final String dataField, final String observationTime, final String name, final UriInfo uriInfo) {
    final FlexiBean out = createRootData();

    final HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setExternalIdValue(StringUtils.trimToNull(identifier));
    searchRequest.setDataSource(StringUtils.trimToNull(dataSource));
    searchRequest.setDataProvider(StringUtils.trimToNull(dataProvider));
    searchRequest.setDataField(StringUtils.trimToNull(dataField));
    searchRequest.setObservationTime(StringUtils.trimToNull(observationTime));
    searchRequest.setName(StringUtils.trimToNull(name));
    final MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      final ExternalId id = ExternalId.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addExternalId(id);
    }
    out.put("searchRequest", searchRequest);

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final ExternalIdWithDatesDisplayComparator comparator = ExternalIdWithDatesDisplayComparatorUtils.getComparator(data().getConfigSource(), ExternalIdDisplayComparatorUtils.DEFAULT_CONFIG_NAME);
      final HistoricalTimeSeriesInfoSearchResult searchResult = data().getHistoricalTimeSeriesMaster().search(searchRequest);
      final Map<String, List<ExternalIdWithDates>> sorted = new HashMap<>();
      for (final HistoricalTimeSeriesInfoDocument doc : searchResult.getDocuments()) {
        final List<ExternalIdWithDates> list = new ArrayList<>(doc.getInfo().getExternalIdBundle().getExternalIds());
        Collections.sort(list, comparator);
        sorted.put(doc.getUniqueId().toString(), list);
      }
      out.put("sortedIds", sorted);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), data().getUriInfo()));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("dataProvider") final String dataProvider,
      @FormParam("dataField") final String dataField,
      @FormParam("start") final String start,
      @FormParam("end") final String end,
      @FormParam("idscheme") final String idScheme,
      @FormParam("idvalue") final String idValue) {
    final String trimmedIdScheme = StringUtils.trimToNull(idScheme);
    final String trimmedIdValue = StringUtils.trimToNull(idValue);
    final String trimmedDataField = StringUtils.trimToNull(dataField);
    final String trimmedStart = StringUtils.trimToNull(start);
    final String trimmedEnd = StringUtils.trimToNull(end);
    final String trimmedDataProvider = StringUtils.trimToNull(dataProvider);

    final FlexiBean out = createRootData();
    LocalDate startDate = null;
    boolean validStartDate = true;
    if (trimmedStart != null) {
      try {
        startDate = LocalDate.parse(trimmedStart);
      } catch (final DateTimeException e) {
        out.put("err_startInvalid", true);
        validStartDate = false;
      }
    }
    LocalDate endDate = null;
    boolean validEndDate = true;
    if (trimmedEnd != null) {
      try {
        endDate = LocalDate.parse(trimmedEnd);
      } catch (final DateTimeException e) {
        out.put("err_endInvalid", true);
        validEndDate = false;
      }
    }

    if (trimmedDataField == null || trimmedIdValue == null || !validStartDate || !validEndDate) {
      //data for repopulating the form
      out.put("scheme", trimmedIdScheme);
      out.put("dataField", trimmedDataField);
      out.put("idValue", trimmedIdValue);
      out.put("dataProvider", trimmedDataProvider);
      out.put("start", trimmedStart);
      out.put("end", trimmedEnd);

      if (trimmedDataField == null) {
        out.put("err_iddatafieldMissing", true);
      }
      if (trimmedIdValue == null) {
        out.put("err_idvalueMissing", true);
      }
      final String html = getFreemarker().build(HTML_DIR + "timeseries-add.ftl", out);
      return Response.ok(html).build();
    }

    final ExternalScheme scheme = ExternalScheme.of(trimmedIdScheme);
    final Set<ExternalId> identifiers = buildSecurityRequest(scheme, trimmedIdValue);
    final Map<ExternalId, UniqueId> added = addTimeSeries(trimmedDataProvider, trimmedDataField, identifiers, startDate, endDate);

    URI uri = null;
    if (!identifiers.isEmpty()) {
      if (identifiers.size() == 1) {
        final ExternalId requestIdentifier = identifiers.iterator().next();
        final UniqueId uniqueId = added.get(requestIdentifier);
        if (uniqueId != null) {
          uri = data().getUriInfo().getAbsolutePathBuilder().path(uniqueId.toString()).build();
        } else {
          LOGGER.warn("No time-series added for {} ", requestIdentifier);
          uri = uri(data());
        }
      } else {
        uri = uri(data(), identifiers);
      }
    }
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("dataProvider") final String dataProvider,
      @FormParam("dataField") final String dataField,
      @FormParam("start") final String start,
      @FormParam("end") final String end,
      @FormParam("idscheme") final String idScheme,
      @FormParam("idvalue") final String idValue) {

    final String trimmedIdScheme = StringUtils.trimToNull(idScheme);
    final String trimmedIdValue = StringUtils.trimToNull(idValue);
    final String trimmedDataField = StringUtils.trimToNull(dataField);
    final String trimmedStart = StringUtils.trimToNull(start);
    final String trimmedEnd = StringUtils.trimToNull(end);
    final String trimmedDataProvider = StringUtils.trimToNull(dataProvider);

    LocalDate startDate = null;
    boolean validStartDate = true;
    if (trimmedStart != null) {
      try {
        startDate = LocalDate.parse(trimmedStart);
        validStartDate = true;
      } catch (final DateTimeException e) {
        validStartDate = false;
      }
    }
    LocalDate endDate = null;
    boolean validEndDate = true;
    if (trimmedEnd != null) {
      try {
        endDate = LocalDate.parse(trimmedEnd);
        validEndDate = true;
      } catch (final DateTimeException e) {
        validEndDate = false;
      }
    }

    if (trimmedDataField == null || trimmedIdValue == null || !validStartDate || !validEndDate) {
      return Response.status(Status.BAD_REQUEST).build();
    }

    final ExternalScheme scheme = ExternalScheme.of(trimmedIdScheme);
    final Set<ExternalId> identifiers = buildSecurityRequest(scheme, trimmedIdValue);
    final Map<ExternalId, UniqueId> added = addTimeSeries(trimmedDataProvider, trimmedDataField, identifiers, startDate, endDate);

    final FlexiBean out = createPostJSONOutput(added, identifiers, scheme, trimmedDataProvider, trimmedDataField, startDate, endDate);
    final Response response = Response.ok(getFreemarker().build(JSON_DIR + "timeseries-added.ftl", out)).build();
    return response;
  }

  private FlexiBean createPostJSONOutput(
      final Map<ExternalId, UniqueId> added, final Collection<ExternalId> requests, final ExternalScheme scheme,
      final String dataProvider, final String dataField, final LocalDate startDate, final LocalDate endDate) {
    final Map<String, String> result = new HashMap<>();
    for (final ExternalId identifier : requests) {
      final UniqueId uniqueIdentifier = added.get(identifier);
      final String objectIdentifier = uniqueIdentifier != null ? uniqueIdentifier.getObjectId().toString() : null;
      result.put(identifier.getValue(), objectIdentifier);
    }
    final FlexiBean out = createRootData();
    out.put("requestScheme", scheme);
    out.put("requestDataField", dataField);
    if (dataProvider != null) {
      out.put("requestDataProvider", dataProvider);
    }
    if (startDate != null) {
      out.put("requestStartDate", startDate.toString());
    }
    if (endDate != null) {
      out.put("requestEndDate", endDate.toString());
    }
    out.put("addedTimeSeries", result);
    return out;
  }

  private Map<ExternalId, UniqueId> addTimeSeries(final String dataProvider, final String dataField, final Set<ExternalId> identifiers, final LocalDate startDate, final LocalDate endDate) {
    final HistoricalTimeSeriesLoader loader = data().getHistoricalTimeSeriesLoader();
    Map<ExternalId, UniqueId> added = Maps.newHashMap();
    if (!identifiers.isEmpty()) {
      added = loader.loadTimeSeries(identifiers, dataProvider, dataField, startDate, endDate);
    }
    return added;
  }

  private static Set<ExternalId> buildSecurityRequest(final ExternalScheme identificationScheme, final String idValue) {
    if (idValue == null) {
      return Collections.emptySet();
    }
    final String[] identifiers = StringUtils.split(idValue, "\n");
    final Set<ExternalId> result = new HashSet<>(identifiers.length);
    for (String identifier : identifiers) {
      identifier = StringUtils.trimToNull(identifier);
      if (identifier != null) {
        result.add(ExternalId.of(identificationScheme, identifier));
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Path("{timeseriesId}")
  public WebHistoricalTimeSeriesResource findSeries(@Subscribe @PathParam("timeseriesId") final String idStr) {

    data().setUriHistoricalTimeSeriesId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    HistoricalTimeSeriesInfoDocument info;
    ManageableHistoricalTimeSeries series;

    try {
      // Try to fetch HTS info
      info = data().getHistoricalTimeSeriesMaster().get(UniqueId.parse(idStr));
    } catch (final DataNotFoundException ex) {
      // If not there, try fetching a deleted one from history
      final HistoricalTimeSeriesInfoHistoryRequest historyRequest = new HistoricalTimeSeriesInfoHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      final HistoricalTimeSeriesInfoHistoryResult historyResult = data().getHistoricalTimeSeriesMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      info = historyResult.getFirstDocument();
    }
    data().setInfo(info);

    try {
      // Try to fetch the data-points
      series = data().getHistoricalTimeSeriesMaster().getTimeSeries(
          info.getInfo().getTimeSeriesObjectId(), VersionCorrection.LATEST);
    } catch (final DataNotFoundException ex) {
      // If not there, return an empty collection of data-points
      series = new ManageableHistoricalTimeSeries();
      series.setTimeSeries(ImmutableLocalDateDoubleTimeSeries.EMPTY_SERIES);
    }
    data().setTimeSeries(series);

    return new WebHistoricalTimeSeriesResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for historical time-series.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebHistoricalTimeSeriesData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebAllHistoricalTimeSeriesResource.class).build();
  }

  /**
   * Builds a URI for collection of historical time-series.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(final WebHistoricalTimeSeriesData data, final Collection<ExternalId> identifiers) {
    final UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebAllHistoricalTimeSeriesResource.class);
    if (identifiers != null) {
      final Iterator<ExternalId> it = identifiers.iterator();
      for (int i = 0; it.hasNext(); i++) {
        final ExternalId id = it.next();
        builder.queryParam("idscheme." + i, id.getScheme().getName());
        builder.queryParam("idvalue." + i, id.getValue());
      }
    }
    return builder.build();
  }

}
