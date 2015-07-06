/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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
public class MinimalWebPositionsResource extends AbstractMinimalWebPositionResource {

  /**
   * Creates the resource.
   * @param positionMaster  the position master, not null
   * @param securitySource  the security source, not null
   * @param externalSchemes the map of external schemes, with {@link ExternalScheme} as key and description as value
   */
  public MinimalWebPositionsResource(final PositionMaster positionMaster, final SecuritySource securitySource,
      final Map<ExternalScheme, String> externalSchemes) {
    super(positionMaster, securitySource, externalSchemes);
  }

  /**
   * Creates the resource.
   * @param positionMaster  the position master, not null
   * @param securityLoader  the security loader, not null
   * @param securitySource  the security source, not null
   * @param htsSource  the historical time series source, not null
   * @param externalSchemes the map of external schemes, with {@link ExternalScheme} as key and description as value
   */
  public MinimalWebPositionsResource(final PositionMaster positionMaster, final SecurityLoader securityLoader, final SecuritySource securitySource,
      final HistoricalTimeSeriesSource htsSource, final Map<ExternalScheme, String> externalSchemes) {
    super(positionMaster, securityLoader, securitySource, htsSource, externalSchemes);
  }

  //-------------------------------------------------------------------------
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

  private FlexiBean createSearchResultData(final PagingRequest pr, final String identifier, String minQuantityStr,
      String maxQuantityStr, final List<String> positionIdStrs, final List<String> tradeIdStrs, final String uniqueIdScheme) {
    minQuantityStr = StringUtils.defaultString(minQuantityStr).replace(",", "");
    maxQuantityStr = StringUtils.defaultString(maxQuantityStr).replace(",", "");
    final FlexiBean out = createRootData();

    final PositionSearchRequest searchRequest = new PositionSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSecurityIdValue(StringUtils.trimToNull(identifier));
    searchRequest.setUniqueIdScheme(StringUtils.trimToNull(uniqueIdScheme));
    if (NumberUtils.isNumber(minQuantityStr)) {
      searchRequest.setMinQuantity(NumberUtils.createBigDecimal(minQuantityStr));
    }
    if (NumberUtils.isNumber(maxQuantityStr)) {
      searchRequest.setMaxQuantity(NumberUtils.createBigDecimal(maxQuantityStr));
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
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("quantity") String quantityStr,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam("type") String type,
      @FormParam(POSITION_XML) String positionXml,
      @FormParam("uniqueIdScheme") String uniqueIdScheme) {
    uniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    type = StringUtils.trimToEmpty(type);
    URI uri = null;
    switch (type) {
      case "xml":
        positionXml = StringUtils.trimToNull(positionXml);
        if (positionXml == null) {
          final FlexiBean out = createRootData();
          out.put("err_xmlMissing", true);
          final String html = getFreemarker().build(HTML_DIR + "positions-add.ftl", out);
          return Response.ok(html).build();
        }
        uri = addPosition(positionXml, uniqueIdScheme);
        break;
      case StringUtils.EMPTY:
        quantityStr = StringUtils.replace(StringUtils.trimToNull(quantityStr), ",", "");
        final BigDecimal quantity = quantityStr != null && NumberUtils.isNumber(quantityStr) ? new BigDecimal(quantityStr) : null;
        idScheme = StringUtils.trimToNull(idScheme);
        idValue = StringUtils.trimToNull(idValue);
        if (quantity == null || idScheme == null || idValue == null) {
          final FlexiBean out = createRootData();
          if (quantityStr == null) {
            out.put("err_quantityMissing", true);
          }
          if (quantity == null) {
            out.put("err_quantityNotNumeric", true);
          }
          if (idScheme == null) {
            out.put("err_idschemeMissing", true);
          }
          if (idValue == null) {
            out.put("err_idvalueMissing", true);
          }
          out.put("quantity", quantityStr);
          out.put("idvalue", idValue);
          final String html = getFreemarker().build(HTML_DIR + "positions-add.ftl", out);
          return Response.ok(html).build();
        }
        final ExternalIdBundle id = ExternalIdBundle.of(ExternalId.of(idScheme, idValue));
        final UniqueId secUid = getSecurityUniqueId(id);
        if (secUid == null) {
          final FlexiBean out = createRootData();
          out.put("err_idvalueNotFound", true);
          final String html = getFreemarker().build(HTML_DIR + "positions-add.ftl", out);
          return Response.ok(html).build();
        }
        uri = addPosition(quantity, secUid, uniqueIdScheme);
        break;
      default:
        throw new IllegalArgumentException("Can only add position by XML or completing provided web form");
    }
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("quantity") String quantityStr,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam("tradesJson") String tradesJson,
      @FormParam("type") String type,
      @FormParam(POSITION_XML) final String positionXml,
      @FormParam("uniqueIdScheme") String uniqueIdScheme) {

    uniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    type = StringUtils.trimToEmpty(type);
    URI uri = null;
    switch (type) {
      case "xml":
        uri = addPosition(positionXml, uniqueIdScheme);
        break;
      case StringUtils.EMPTY:
        quantityStr = StringUtils.replace(StringUtils.trimToNull(quantityStr), ",", "");
        final BigDecimal quantity = quantityStr != null && NumberUtils.isNumber(quantityStr) ? new BigDecimal(quantityStr) : null;
        idScheme = StringUtils.trimToNull(idScheme);
        idValue = StringUtils.trimToNull(idValue);
        tradesJson = StringUtils.trimToNull(tradesJson);

        if (quantity == null || idScheme == null || idValue == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }

        final ExternalIdBundle id = ExternalIdBundle.of(ExternalId.of(idScheme, idValue));
        final UniqueId secUid = getSecurityUniqueId(id);
        if (secUid == null) {
          throw new DataNotFoundException("invalid " + idScheme + "~" + idValue);
        }
        Collection<ManageableTrade> trades = null;
        if (tradesJson != null) {
          trades = parseTrades(tradesJson);
        } else {
          trades = Collections.<ManageableTrade>emptyList();
        }
        uri = addPosition(quantity, secUid, trades, uniqueIdScheme);
        break;
      default:
        throw new IllegalArgumentException("Can only add position by XML or completing provided web form");
    }
    return Response.created(uri).build();
  }

  private URI addPosition(String positionXml, final String uniqueIdScheme) {
    positionXml = StringUtils.trimToEmpty(positionXml);
    final Bean positionBean = JodaBeanSerialization.deserializer().xmlReader().read(positionXml);
    final PositionMaster positionMaster = data().getPositionMaster();
    final ManageablePosition manageablePosition = (ManageablePosition) positionBean;
    if (uniqueIdScheme != null) {
      manageablePosition.setUniqueId(UniqueId.of(uniqueIdScheme, uniqueIdScheme));
    }
    final ManageablePosition position = positionMaster.add(new PositionDocument(manageablePosition)).getPosition();
    return new MinimalWebPositionsUris(data()).position(position);
  }

  private UniqueId getSecurityUniqueId(final ExternalIdBundle id) {
    UniqueId result = null;
    final Security security = data().getSecuritySource().getSingle(id);
    if (security != null) {
      result = security.getUniqueId();
    } else {
      if (data().getSecurityLoader() != null) {
        result = data().getSecurityLoader().loadSecurity(id);
      }
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
    return MinimalWebPositionResource.uri(data());
  }

  //-------------------------------------------------------------------------
  @Path("{positionId}")
  public MinimalWebPositionResource findPosition(@Subscribe @PathParam("positionId") final String idStr) {
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
    return new MinimalWebPositionResource(this);
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
    return data.getUriInfo().getBaseUriBuilder().path(MinimalWebPositionsResource.class).build();
  }

}
