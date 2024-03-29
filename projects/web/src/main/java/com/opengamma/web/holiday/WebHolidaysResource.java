/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.holiday;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;
import org.threeten.bp.LocalDate;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.holiday.HolidayType;
import com.opengamma.core.holiday.WeekendType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.holiday.HolidayDocument;
import com.opengamma.master.holiday.HolidayHistoryRequest;
import com.opengamma.master.holiday.HolidayHistoryResult;
import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.HolidayMetaDataRequest;
import com.opengamma.master.holiday.HolidayMetaDataResult;
import com.opengamma.master.holiday.HolidaySearchRequest;
import com.opengamma.master.holiday.HolidaySearchResult;
import com.opengamma.master.holiday.HolidaySearchSortOrder;
import com.opengamma.master.holiday.ManageableHoliday;
import com.opengamma.master.holiday.ManageableHolidayWithWeekend;
import com.opengamma.util.money.Currency;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.SubscribeMaster;

/**
 * RESTful resource for all holidays.
 * <p>
 * The holidays resource represents the whole of a holiday master.
 */
@Path("/holidays")
public class WebHolidaysResource extends AbstractWebHolidayResource {

  /**
   * Creates the resource.
   * @param holidayMaster  the holiday master, not null
   */
  public WebHolidaysResource(final HolidayMaster holidayMaster) {
    super(holidayMaster);
  }

  //-------------------------------------------------------------------------
  /**
   * Searches for a list of holidays from the master. The list could have been constrained
   * (e.g. by name fragment).
   * @param pgIdx  the page index
   * @param pgNum  the page number
   * @param pgSze  the page size
   * @param sort  the sorting parameter
   * @param name  the name
   * @param type  the type
   * @param currencyISO  the currency
   * @param holidayIdStrs  the holiday identifiers
   * @param uriInfo  the uri info
   * @return  the holiday list
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.HOLIDAY)
  public String getHTML(@QueryParam("pgIdx") final Integer pgIdx, @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze, @QueryParam("sort") final String sort, @QueryParam("name") final String name,
      @QueryParam("type") final String type, @QueryParam("currency") final String currencyISO,
      @QueryParam("holidayId") final List<String> holidayIdStrs, @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final HolidaySearchSortOrder so = buildSortOrder(sort, HolidaySearchSortOrder.NAME_ASC);
    final FlexiBean out = createSearchResultData(pr, so, name, type, currencyISO, holidayIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "holidays.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.HOLIDAY)
  public String getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("type") final String type,
      @QueryParam("currency") final String currencyISO,
      @QueryParam("holidayId") final List<String> holidayIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final HolidaySearchSortOrder so = buildSortOrder(sort, HolidaySearchSortOrder.NAME_ASC);
    final FlexiBean out = createSearchResultData(pr, so, name, type, currencyISO, holidayIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "holidays.ftl", out);
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(@FormParam("name") final String name, @FormParam("holidayxml") final String xml, @FormParam("type") final String typeName) {
    final String trimmedName = StringUtils.trimToNull(name);
    final String trimmedXml = StringUtils.trimToNull(xml);
    final String trimmedTypeName = StringUtils.trimToNull(typeName);

    HolidayType holidayType;
    try {
      holidayType = trimmedTypeName != null ? HolidayType.valueOf(trimmedTypeName) : null;
    } catch (final Exception e) {
      // for the valueOf
      holidayType = null;
    }
    if (trimmedName == null || trimmedXml == null || holidayType == null) {
      final FlexiBean out = createRootData();
      if (trimmedName == null) {
        out.put("err_nameMissing", true);
      }
      if (trimmedXml == null) {
        out.put("err_xmlMissing", true);
      }
      if (trimmedTypeName == null) {
        out.put("err_typeMissing", true);
      } else if (holidayType == null) {
        out.put("err_typeInvalid", true);
      }
      out.put("name", StringUtils.defaultString(trimmedName));
      out.put("type", StringUtils.defaultString(trimmedTypeName));
      out.put("holidayXml", StringEscapeUtils.escapeJava(StringUtils.defaultString(trimmedXml)));
      final String html = getFreemarker().build(HTML_DIR + "holiday-add.ftl", out);
      return Response.ok(html).build();
    }
    try {
      final ManageableHoliday holiday = parseXML(trimmedXml, ManageableHoliday.class);
      final HolidayDocument doc = new HolidayDocument(holiday);
      doc.setName(trimmedName);
      final HolidayDocument added = data().getHolidayMaster().add(doc);
      final URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
      return Response.seeOther(uri).build();
    } catch (final Exception ex) {
      final FlexiBean out = createRootData();
      out.put("name", StringUtils.defaultString(trimmedName));
      out.put("type", StringUtils.defaultString(trimmedTypeName));
      out.put("holidayXml", StringEscapeUtils.escapeJava(StringUtils.defaultString(xml)));
      out.put("err_holidayXmlMsg", StringUtils.defaultString(ex.getMessage()));
      final String html = getFreemarker().build(HTML_DIR + "holiday-add.ftl", out);
      return Response.ok(html).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("holidayType") final String holidayTypeStr,
      @FormParam("identifier") final String identifierStr,
      @FormParam("weekendType") final String weekendTypeStr,
      @FormParam("name") final String name,
      @FormParam("holidayJSON") final String json,
      @FormParam("holidayXML") final String xml,
      @FormParam("type") final String type) {
    final String trimmedName = StringUtils.trimToNull(name);
    final String trimmedJson = StringUtils.trimToNull(json);
    final String trimmedXml = StringUtils.trimToNull(xml);
    final String trimmedType = StringUtils.trimToEmpty(type);
    final String holidayName;
    final ManageableHoliday holiday;
    switch (trimmedType) {
      case "json":
        holiday = (ManageableHoliday) parseJSON(trimmedJson);
        holidayName = trimmedName;
        break;
      case "xml":
        holiday = (ManageableHoliday) parseXML(trimmedXml);
        holidayName = trimmedName;
        break;
      case StringUtils.EMPTY:
        if (holidayTypeStr == null || identifierStr == null || weekendTypeStr == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }
        final HolidayType holidayType = HolidayType.valueOf(holidayTypeStr);
        final ExternalId id = ExternalId.parse(identifierStr);
        final WeekendType weekendType = WeekendType.valueOf(weekendTypeStr);
        holiday = new ManageableHolidayWithWeekend();
        holiday.setType(holidayType);
        //TODO change when form defines acceptable schemes
        switch (holidayType) {
          case CURRENCY:
            holiday.setCurrency(Currency.of(id.getValue()));
            break;
          case BANK:
            holiday.setRegionExternalId(id);
            break;
          case SETTLEMENT:
          case TRADING:
            holiday.setExchangeExternalId(id);
            break;
          case CUSTOM:
            holiday.setCustomExternalId(id);
            break;
          default:
            throw new IllegalArgumentException("Unrecognised holiday type " + holidayType);
        }
        ((ManageableHolidayWithWeekend) holiday).setWeekendType(weekendType);
        holidayName = id.getValue();
        holiday.setHolidayDates(Collections.<LocalDate>emptyList());
        break;
      default:
        throw new IllegalArgumentException("Can only add holiday by XML, JSON or completing the web form");
    }
    final HolidayDocument doc = new HolidayDocument(holiday);
    doc.setName(holidayName);
    final HolidayDocument added = data().getHolidayMaster().add(doc);
    final URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return Response.created(uri).build();
  }

  private FlexiBean createSearchResultData(final PagingRequest pr, final HolidaySearchSortOrder sort, final String name,
      final String type, final String currencyISO, final List<String> holidayIdStrs, final UriInfo uriInfo) {
    final FlexiBean out = createRootData();

    final HolidaySearchRequest searchRequest = new HolidaySearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(sort);
    searchRequest.setName(StringUtils.trimToNull(name));
    if (StringUtils.isNotEmpty(type)) {
      searchRequest.setType(HolidayType.valueOf(type));
    }
    if (currencyISO != null) {
      searchRequest.setCurrency(Currency.of(currencyISO));
    }
    final MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      final ExternalId id = ExternalId.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      if (HolidayType.BANK.name().equals(type)) {
        searchRequest.addRegionExternalId(id);
      } else if (HolidayType.CUSTOM.name().equals(type)) {
        searchRequest.addCustomExternalId(id);
      } else { // assume settlement/trading
        searchRequest.addExchangeExternalId(id);
      }
    }
    for (final String holidayIdStr : holidayIdStrs) {
      searchRequest.addHolidayObjectId(ObjectId.parse(holidayIdStr));
    }
    out.put("searchRequest", searchRequest);

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final HolidaySearchResult searchResult = data().getHolidayMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("metaData")
  @Produces(MediaType.APPLICATION_JSON)
  public String getMetaDataJSON() {
    final FlexiBean out = createRootData();
    return getFreemarker().build("holidays/json/metadata.ftl", out);
  }

  //-------------------------------------------------------------------------
  @Path("{holidayId}")
  public WebHolidayResource findHoliday(@PathParam("holidayId") final String idStr) {
    data().setUriHolidayId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    try {
      final HolidayDocument doc = data().getHolidayMaster().get(oid);
      data().setHoliday(doc);
    } catch (final DataNotFoundException ex) {
      final HolidayHistoryRequest historyRequest = new HolidayHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      final HolidayHistoryResult historyResult = data().getHolidayMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setHoliday(historyResult.getFirstDocument());
    }
    return new WebHolidayResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final HolidaySearchRequest searchRequest = new HolidaySearchRequest();
    out.put("searchRequest", searchRequest);
    final HolidayMetaDataResult metaData = data().getHolidayMaster().metaData(new HolidayMetaDataRequest());
    out.put("holidayTypes", metaData.getHolidayTypes());
    out.put("holidayDescriptionMap", getHolidayTypesProvider().getDescriptionMap());
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for holidays.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebHolidayData data) {
    return uri(data, null, null);
  }

  /**
   * Builds a URI for holidays.
   * @param data  the data, not null
   * @param type  the holiday type, may be null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(final WebHolidayData data, final HolidayType type, final ExternalIdBundle identifiers) {
    final UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebHolidaysResource.class);
    if (type != null && identifiers != null) {
      builder.queryParam("type", type.name());
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
