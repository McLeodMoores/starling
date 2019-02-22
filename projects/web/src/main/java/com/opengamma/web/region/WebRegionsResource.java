/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.region;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.core.region.RegionClassification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.region.RegionDocument;
import com.opengamma.master.region.RegionHistoryRequest;
import com.opengamma.master.region.RegionHistoryResult;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.RegionSearchRequest;
import com.opengamma.master.region.RegionSearchResult;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all regions.
 * <p>
 * The regions resource represents the whole of a region master.
 */
@Path("/regions")
public class WebRegionsResource extends AbstractWebRegionResource {

  /**
   * Creates the resource.
   * @param regionMaster  the region master, not null
   */
  public WebRegionsResource(final RegionMaster regionMaster) {
    super(regionMaster);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates a HTML GET request that returns the web page.
   *
   * @param pgIdx
   *          the paging first-item index, can be null
   * @param pgNum
   *          the paging page, can be null
   * @param pgSze
   *          the page size, can be null
   * @param name
   *          the region name, not null
   * @param classification
   *          the classification, not null
   * @param regionIdStrs
   *          the identifiers of the region, not null
   * @param uriInfo
   *          the URI info, not null
   * @return the Freemarker output
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("name") final String name,
      @QueryParam("classification") final RegionClassification classification,
      @QueryParam("regionId") final List<String> regionIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final FlexiBean out = createSearchResultData(pr, name, classification, regionIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "regions.ftl", out);
  }

  /**
   * Creates a HTML GET request that returns the web page.
   *
   * @param pgIdx
   *          the paging first-item index, can be null
   * @param pgNum
   *          the paging page, can be null
   * @param pgSze
   *          the page size, can be null
   * @param name
   *          the region name, not null
   * @param classification
   *          the classification, not null
   * @param regionIdStrs
   *          the identifiers of the region, not null
   * @param uriInfo
   *          the URI info, not null
   * @return the Freemarker output
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("name") final String name,
      @QueryParam("classification") final RegionClassification classification,
      @QueryParam("regionId") final List<String> regionIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final FlexiBean out = createSearchResultData(pr, name, classification, regionIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "regions.ftl", out);
  }

  private FlexiBean createSearchResultData(final PagingRequest pr, final String name, final RegionClassification classification,
      final List<String> regionIdStrs, final UriInfo uriInfo) {
    final FlexiBean out = createRootData();

    final RegionSearchRequest searchRequest = new RegionSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setClassification(classification);
    final MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      final ExternalId id = ExternalId.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addExternalId(id);
    }
    for (final String regionIdStr : regionIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(regionIdStr));
    }
    out.put("searchRequest", searchRequest);

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final RegionSearchResult searchResult = data().getRegionMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a region by identifier. If the region is not present in the master,
   * the latest version in the history is returned. If this is not available, an
   * exception is thrown.
   *
   * @param idStr
   *          the identifier
   * @return the region resource
   */
  @Path("{regionId}")
  public WebRegionResource findRegion(@PathParam("regionId") final String idStr) {
    data().setUriRegionId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    try {
      final RegionDocument doc = data().getRegionMaster().get(oid);
      data().setRegion(doc);
    } catch (final DataNotFoundException ex) {
      final RegionHistoryRequest historyRequest = new RegionHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      final RegionHistoryResult historyResult = data().getRegionMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setRegion(historyResult.getFirstDocument());
    }
    return new WebRegionResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final RegionSearchRequest searchRequest = new RegionSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for regions.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebRegionData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for regions.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(final WebRegionData data, final ExternalIdBundle identifiers) {
    final UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebRegionsResource.class);
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
