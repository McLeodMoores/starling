/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.position;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.position.ManageablePosition;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.master.position.PositionDocument;
import com.opengamma.master.position.PositionHistoryRequest;
import com.opengamma.master.position.PositionHistoryResult;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.PositionSearchRequest;
import com.opengamma.master.position.PositionSearchResult;
import com.opengamma.master.position.impl.DelegatingPositionMaster;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;

/**
 * RESTful resource for all positions.
 * <p>
 * The positions resource represents the whole of a position master.
 */
@Path("/positions")
public class WebPositionsResource extends AbstractWebPositionResource {

  /**
   * Creates the resource.
   * @param positionMaster  the position master, not null
   * @param securityLoader  the security loader, not null
   * @param securitySource  the security source, not null
   * @param htsSource  the historical time series source, not null
   * @param externalSchemes the map of external schemes, with {@link ExternalScheme} as key and description as value
   */
  public WebPositionsResource(final PositionMaster positionMaster, final SecurityLoader securityLoader, final SecuritySource securitySource,
      final HistoricalTimeSeriesSource htsSource, final Map<ExternalScheme, String> externalSchemes) {
    super(positionMaster, securityLoader, securitySource, htsSource, externalSchemes);
  }

  //-------------------------------------------------------------------------
  /**
   * Produces an HTML GET request.
   *
   * @param pgIdx
   *          the paging first-item index, can be null
   * @param pgNum
   *          the paging page, can be null
   * @param pgSze
   *          the page size, can be null
   * @param identifier
   *          the security identifier, can be null
   * @param minQuantityStr
   *          the minimum quantity, can be null
   * @param maxQuantityStr
   *          the maximum quantity, can be null
   * @param positionIdStrs
   *          the identifiers of the position, not null
   * @param tradeIdStrs
   *          the identifiers of the trades, not null
   * @param uniqueIdScheme
   *          the identifier scheme, can be null
   * @return the Freemarker output
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.POSITION)
  public String getHTML(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("identifier") final String identifier,
      @QueryParam("minquantity") final String minQuantityStr,
      @QueryParam("maxquantity") final String maxQuantityStr,
      @QueryParam("positionId") final List<String> positionIdStrs,
      @QueryParam("tradeId") final List<String> tradeIdStrs,
      @QueryParam("uniqueIdScheme") final String uniqueIdScheme) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final FlexiBean out = createSearchResultData(pr, identifier, minQuantityStr, maxQuantityStr, positionIdStrs, tradeIdStrs, uniqueIdScheme);
    return getFreemarker().build(HTML_DIR + "positions.ftl", out);
  }

  /**
   * Produces a JSON GET request.
   *
   * @param pgIdx
   *          the paging first-item index, can be null
   * @param pgNum
   *          the paging page, can be null
   * @param pgSze
   *          the page size, can be null
   * @param identifier
   *          the security identifier, can be null
   * @param minQuantityStr
   *          the minimum quantity, can be null
   * @param maxQuantityStr
   *          the maximum quantity, can be null
   * @param positionIdStrs
   *          the identifiers of the position, not null
   * @param tradeIdStrs
   *          the identifiers of the trades, not null
   * @param uniqueIdScheme
   *          the identifier scheme, can be null
   * @return the Freemarker output
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.POSITION)
  public String getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("identifier") final String identifier,
      @QueryParam("minquantity") final String minQuantityStr,
      @QueryParam("maxquantity") final String maxQuantityStr,
      @QueryParam("positionId") final List<String> positionIdStrs,
      @QueryParam("tradeId") final List<String> tradeIdStrs,
      @QueryParam("uniqueIdScheme") final String uniqueIdScheme) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final FlexiBean out = createSearchResultData(pr, identifier, minQuantityStr, maxQuantityStr, positionIdStrs, tradeIdStrs, uniqueIdScheme);
    return getFreemarker().build(JSON_DIR + "positions.ftl", out);
  }

  private FlexiBean createSearchResultData(final PagingRequest pr, final String identifier, final String minQuantityStr,
      final String maxQuantityStr, final List<String> positionIdStrs, final List<String> tradeIdStrs, final String uniqueIdScheme) {
    final String trimmedMinQuantityStr = StringUtils.defaultString(minQuantityStr).replace(",", "");
    final String trimmedMaxQuantityStr = StringUtils.defaultString(maxQuantityStr).replace(",", "");
    final FlexiBean out = createRootData();

    final PositionSearchRequest searchRequest = new PositionSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSecurityIdValue(StringUtils.trimToNull(identifier));
    searchRequest.setUniqueIdScheme(StringUtils.trimToNull(uniqueIdScheme));
    if (NumberUtils.isNumber(trimmedMinQuantityStr)) {
      searchRequest.setMinQuantity(NumberUtils.createBigDecimal(trimmedMinQuantityStr));
    }
    if (NumberUtils.isNumber(trimmedMaxQuantityStr)) {
      searchRequest.setMaxQuantity(NumberUtils.createBigDecimal(trimmedMaxQuantityStr));
    }
    for (final String positionIdStr : positionIdStrs) {
      searchRequest.addPositionObjectId(ObjectId.parse(positionIdStr));
    }
    for (final String tradeIdStr : tradeIdStrs) {
      searchRequest.addPositionObjectId(ObjectId.parse(tradeIdStr));
    }
    out.put("searchRequest", searchRequest);

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final PositionSearchResult searchResult = data().getPositionMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), data().getUriInfo()));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates an HTML POST response.
   *
   * @param quantityStr
   *          the quantity, can be null
   * @param idScheme
   *          the id scheme, can be null
   * @param idValue
   *          the id value, can be null
   * @param type
   *          the message type, can be empty
   * @param positionXml
   *          the XML describing the position
   * @param uniqueIdScheme
   *          the unique id scheme, can be null
   * @return the POST response
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("quantity") final String quantityStr,
      @FormParam("idscheme") final String idScheme,
      @FormParam("idvalue") final String idValue,
      @FormParam("type") final String type,
      @FormParam(POSITION_XML) final String positionXml,
      @FormParam("uniqueIdScheme") final String uniqueIdScheme) {
    final String trimmedUniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    final String trimmedType = StringUtils.trimToEmpty(type);
    URI uri = null;
    switch (trimmedType) {
      case "xml":
        final String trimmedPositionXml = StringUtils.trimToNull(positionXml);
        if (trimmedPositionXml == null) {
          final FlexiBean out = createRootData();
          out.put("err_xmlMissing", true);
          final String html = getFreemarker().build(HTML_DIR + "positions-add.ftl", out);
          return Response.ok(html).build();
        }
        uri = addPosition(trimmedPositionXml, trimmedUniqueIdScheme);
        break;
      case StringUtils.EMPTY:
        final String trimmedQuantityStr = StringUtils.replace(StringUtils.trimToNull(quantityStr), ",", "");
        final BigDecimal quantity = trimmedQuantityStr != null && NumberUtils.isNumber(trimmedQuantityStr) ? new BigDecimal(trimmedQuantityStr) : null;
        final String trimmedIdScheme = StringUtils.trimToNull(idScheme);
        final String trimmedIdValue = StringUtils.trimToNull(idValue);
        if (quantity == null || trimmedIdScheme == null || trimmedIdValue == null) {
          final FlexiBean out = createRootData();
          if (trimmedQuantityStr == null) {
            out.put("err_quantityMissing", true);
          }
          if (quantity == null) {
            out.put("err_quantityNotNumeric", true);
          }
          if (trimmedIdScheme == null) {
            out.put("err_idschemeMissing", true);
          }
          if (trimmedIdValue == null) {
            out.put("err_idvalueMissing", true);
          }
          out.put("quantity", trimmedQuantityStr);
          out.put("idvalue", trimmedIdValue);
          final String html = getFreemarker().build(HTML_DIR + "positions-add.ftl", out);
          return Response.ok(html).build();
        }
        final ExternalIdBundle id = ExternalIdBundle.of(ExternalId.of(trimmedIdScheme, trimmedIdValue));
        final UniqueId secUid = getSecurityUniqueId(id);
        if (secUid == null) {
          final FlexiBean out = createRootData();
          out.put("err_idvalueNotFound", true);
          final String html = getFreemarker().build(HTML_DIR + "positions-add.ftl", out);
          return Response.ok(html).build();
        }
        uri = addPosition(quantity, secUid, trimmedUniqueIdScheme);
        break;
      default:
        throw new IllegalArgumentException("Can only add position by XML or completing provided web form");
    }
    return Response.seeOther(uri).build();
  }

  /**
   * Creates a JSON POST response.
   *
   * @param quantityStr
   *          the quantity, can be null
   * @param idScheme
   *          the id scheme, can be null
   * @param idValue
   *          the id value, can be null
   * @param tradesJson
   *          the trade information, can be null
   * @param type
   *          the message type, can be empty
   * @param positionXml
   *          the XML describing the position
   * @param uniqueIdScheme
   *          the unique id scheme, can be null
   * @return the POST response
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("quantity") final String quantityStr,
      @FormParam("idscheme") final String idScheme,
      @FormParam("idvalue") final String idValue,
      @FormParam("tradesJson") final String tradesJson,
      @FormParam("type") final String type,
      @FormParam(POSITION_XML) final String positionXml,
      @FormParam("uniqueIdScheme") final String uniqueIdScheme) {

    final String trimmedUniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    final String trimmedType = StringUtils.trimToEmpty(type);
    final String trimmedPositionXml = StringUtils.trimToNull(positionXml);
    URI uri = null;
    switch (trimmedType) {
      case "xml":
        if (trimmedPositionXml == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }
        uri = addPosition(trimmedPositionXml, trimmedUniqueIdScheme);
        break;
      case StringUtils.EMPTY:
        final String trimmedQuantityStr = StringUtils.replace(StringUtils.trimToNull(quantityStr), ",", "");
        final BigDecimal quantity = trimmedQuantityStr != null && NumberUtils.isNumber(trimmedQuantityStr) ? new BigDecimal(trimmedQuantityStr) : null;
        final String trimmedIdScheme = StringUtils.trimToNull(idScheme);
        final String trimmedIdValue = StringUtils.trimToNull(idValue);
        final String trimmedTradesJson = StringUtils.trimToNull(tradesJson);

        if (quantity == null || trimmedIdScheme == null || trimmedIdValue == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }

        final ExternalIdBundle id = ExternalIdBundle.of(ExternalId.of(trimmedIdScheme, trimmedIdValue));
        final UniqueId secUid = getSecurityUniqueId(id);
        if (secUid == null) {
          throw new DataNotFoundException("invalid " + trimmedIdScheme + "~" + trimmedIdValue);
        }
        Collection<ManageableTrade> trades = null;
        if (trimmedTradesJson != null) {
          trades = parseTrades(trimmedTradesJson);
        } else {
          trades = Collections.<ManageableTrade>emptyList();
        }
        uri = addPosition(quantity, secUid, trades, trimmedUniqueIdScheme);
        break;
      default:
        throw new IllegalArgumentException("Can only add position by XML or completing provided web form");
    }
    return Response.created(uri).build();
  }

  private URI addPosition(final String positionXml, final String uniqueIdScheme) {
    final String trimmedPositionXml = StringUtils.trimToEmpty(positionXml);
    final Bean positionBean = JodaBeanSerialization.deserializer().xmlReader().read(trimmedPositionXml);
    final PositionMaster positionMaster = data().getPositionMaster();
    final ManageablePosition manageablePosition = (ManageablePosition) positionBean;
    if (uniqueIdScheme != null) {
      // TODO unique id looks wrong
      manageablePosition.setUniqueId(UniqueId.of(uniqueIdScheme, uniqueIdScheme));
    }
    final ManageablePosition position = positionMaster.add(new PositionDocument(manageablePosition)).getPosition();
    return new WebPositionsUris(data()).position(position);
  }

  private UniqueId getSecurityUniqueId(final ExternalIdBundle id) {
    UniqueId result = null;
    final Security security = data().getSecuritySource().getSingle(id);
    if (security != null) {
      result = security.getUniqueId();
    } else {
      result = data().getSecurityLoader().loadSecurity(id);
    }
    return result;
  }

  private URI addPosition(final BigDecimal quantity, final UniqueId secUid, final String uniqueIdScheme) {
    return addPosition(quantity, secUid, Collections.<ManageableTrade>emptyList(), uniqueIdScheme);
  }

  private URI addPosition(final BigDecimal quantity, final UniqueId secUid, final Collection<ManageableTrade> trades, final String uniqueIdScheme) {
    final ExternalIdBundle secId = data().getSecuritySource().get(secUid).getExternalIdBundle();
    final ManageablePosition position = new ManageablePosition(quantity, secId);
    if (uniqueIdScheme != null) {
      position.setUniqueId(UniqueId.of(uniqueIdScheme, uniqueIdScheme));
    }
    for (final ManageableTrade trade : trades) {
      trade.setSecurityLink(new ManageableSecurityLink(secId));
      position.addTrade(trade);
    }
    PositionDocument doc = new PositionDocument(position);
    doc = data().getPositionMaster().add(doc);
    data().setPosition(doc);
    return WebPositionResource.uri(data());
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a position by unique id. If there is no position for that UID, the
   * history is searched if the master supports this functionality. If no value
   * is found, an exception is thrown.
   *
   * @param idStr
   *          the position identifier
   * @return the position
   */
  @Path("{positionId}")
  public WebPositionResource findPosition(@Subscribe @PathParam("positionId") final String idStr) {
    data().setUriPositionId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    try {
      final PositionDocument doc = data().getPositionMaster().get(oid);
      data().setPosition(doc);
    } catch (final DataNotFoundException ex) {
      final PositionHistoryRequest historyRequest = new PositionHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      final PositionHistoryResult historyResult = data().getPositionMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setPosition(historyResult.getFirstDocument());
    }
    return new WebPositionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final PositionSearchRequest searchRequest = new PositionSearchRequest();
    out.put("searchRequest", searchRequest);
    if (data().getPositionMaster() instanceof DelegatingPositionMaster) {
      final DelegatingPositionMaster delegatingPositionMaster = (DelegatingPositionMaster) data().getPositionMaster();
      final Map<String, PositionMaster> delegates = delegatingPositionMaster.getDelegates();
      out.put("uniqueIdSchemes", delegates.keySet());
    }
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for positions.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebPositionsData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebPositionsResource.class).build();
  }

}
