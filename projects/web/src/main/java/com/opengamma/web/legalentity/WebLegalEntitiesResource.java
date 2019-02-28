/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.legalentity;

import java.net.URI;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.legalentity.LegalEntityDocument;
import com.opengamma.master.legalentity.LegalEntityHistoryRequest;
import com.opengamma.master.legalentity.LegalEntityHistoryResult;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.legalentity.LegalEntitySearchRequest;
import com.opengamma.master.legalentity.LegalEntitySearchResult;
import com.opengamma.master.legalentity.LegalEntitySearchSortOrder;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * RESTful resource for all legalEntity documents.
 * <p/>
 * The legalEntity documents resource represents all the data for one element type in the legalEntity master.
 */
@Path("/legalentities")
public class WebLegalEntitiesResource extends AbstractWebLegalEntityResource {

  /**
   * Creates the resource.
   *
   * @param legalEntityMaster the legalEntity master, not null
   * @param securityMaster the securityMaster master, not null
   */
  public WebLegalEntitiesResource(final LegalEntityMaster legalEntityMaster, final SecurityMaster securityMaster) {
    super(legalEntityMaster, securityMaster);
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
   * @param sort
   *          how to sort the results, can be null
   * @param name
   *          the legal entity name, not null
   * @param type
   *          the type, can be null
   * @param legalEntityIdStrs
   *          the identifiers of the legal entity, not null
   * @param uriInfo
   *          the URI info, not null
   * @return the Freemarker output
   */
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.LEGAL_ENTITY)
  public String getHTML(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("type") final String type,
      @QueryParam("legalEntityId") final List<String> legalEntityIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final LegalEntitySearchSortOrder so = buildSortOrder(sort, LegalEntitySearchSortOrder.NAME_ASC);
    final FlexiBean out = search(pr, so, name, type, legalEntityIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "legalentities.ftl", out);
  }

  /**
   * Creates a JSON GET request that returns the web page.
   *
   * @param pgIdx
   *          the paging first-item index, can be null
   * @param pgNum
   *          the paging page, can be null
   * @param pgSze
   *          the page size, can be null
   * @param sort
   *          how to sort the results, can be null
   * @param name
   *          the legal entity name, not null
   * @param type
   *          the type, can be null
   * @param legalEntityIdStrs
   *          the identifiers of the legal entity, not null
   * @param uriInfo
   *          the URI info, not null
   * @return the Freemarker output
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.LEGAL_ENTITY)
  public String getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("type") final String type,
      @QueryParam("legalEntityId") final List<String> legalEntityIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final LegalEntitySearchSortOrder so = buildSortOrder(sort, LegalEntitySearchSortOrder.NAME_ASC);
    final FlexiBean out = search(pr, so, name, type, legalEntityIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "legalentities.ftl", out);
  }

  private FlexiBean search(final PagingRequest request, final LegalEntitySearchSortOrder so, final String name,
      final String typeName, final List<String> legalEntityIdStrs, final UriInfo uriInfo) {
    final FlexiBean out = createRootData();

    final LegalEntitySearchRequest searchRequest = new LegalEntitySearchRequest();
    final String trimmedTypeName = StringUtils.trimToNull(typeName);
    searchRequest.setPagingRequest(request);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    out.put("searchRequest", searchRequest);
    out.put("type", trimmedTypeName);
    for (final String legalEntityIdStr : legalEntityIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(legalEntityIdStr));
    }

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final LegalEntitySearchResult searchResult = data().getLegalEntityMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * POSTs a legal entity to the master using HTML.
   *
   * @param name
   *          the name, not null
   * @param xml
   *          the legal entity XML, not null
   * @param typeName
   *          the type name, can be null
   * @return the response
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("name") final String name,
      @FormParam("legalEntityXML") final String xml,
      @FormParam("type") final String typeName) {
    final String trimmedName = StringUtils.trimToNull(name);
    final String trimmedXml = StringUtils.trimToNull(xml);
    final String trimmedTypeName = StringUtils.trimToNull(typeName);

    if (trimmedName == null || trimmedXml == null) {
      final FlexiBean out = createRootData();
      if (trimmedName == null) {
        out.put("err_nameMissing", true);
      }
      if (trimmedXml == null) {
        out.put("err_xmlMissing", true);
      }
      out.put("name", StringUtils.defaultString(trimmedName));
      out.put("type", StringUtils.defaultString(trimmedTypeName));
      out.put("legalEntityXML", StringEscapeUtils.escapeJavaScript(StringUtils.defaultString(trimmedXml)));
      final String html = getFreemarker().build(HTML_DIR + "legalentity-add.ftl", out);
      return Response.ok(html).build();
    }

    final ManageableLegalEntity legalEntity = parseXML(trimmedXml, ManageableLegalEntity.class);
    legalEntity.setName(trimmedName);
    final LegalEntityDocument doc = new LegalEntityDocument(legalEntity);
    final LegalEntityDocument added = data().getLegalEntityMaster().add(doc);
    final URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
    return Response.seeOther(uri).build();
  }

  /**
   * POSTs a legal entity to the master using JSON.
   *
   * @param name
   *          the name, not null
   * @param json
   *          the legal entity JSON, can be null if the XML is not
   * @param xml
   *          the legal entity XML, can be null if the JSON is not
   * @param typeName
   *          the type name, can be null
   * @return the response
   */
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("name") final String name,
      @FormParam("legalEntityJSON") final String json,
      @FormParam("legalEntityXML") final String xml,
      @FormParam("type") final String typeName) {
    final String trimmedName = StringUtils.trimToNull(name);
    final String trimmedJson = StringUtils.trimToNull(json);
    final String trimmedXml = StringUtils.trimToNull(xml);
    final String trimmedTypeName = StringUtils.trimToNull(typeName);

    final Class<? extends ManageableLegalEntity> typeClazz = trimmedTypeName != null ? data().getTypeMap().get(trimmedTypeName) : null;
    Response result = null;
    if (trimmedName == null || typeClazz == null || isEmptyLegalEntityData(trimmedJson, trimmedXml)) {
      result = Response.status(Status.BAD_REQUEST).build();
    } else {
      ManageableLegalEntity legalEntity = null;
      if (trimmedJson != null) {
        legalEntity = (ManageableLegalEntity) parseJSON(trimmedJson);
      } else if (trimmedXml != null) {
        legalEntity = parseXML(trimmedXml, typeClazz);
      }
      if (legalEntity == null) {
        result = Response.status(Status.BAD_REQUEST).build();
      } else {
        legalEntity.setName(trimmedName);
        final LegalEntityDocument doc = new LegalEntityDocument(legalEntity);
        final LegalEntityDocument added = data().getLegalEntityMaster().add(doc);
        final URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
        result = Response.created(uri).build();
      }
    }
    return result;
  }

  private static boolean isEmptyLegalEntityData(final String json, final String xml) {
    return json == null && xml == null;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the meta-data.
   *
   * @return the meta-data
   */
  @GET
  @Path("metaData")
  @Produces(MediaType.APPLICATION_JSON)
  public String getMetaDataJSON() {
    final FlexiBean out = createRootData();
    return getFreemarker().build(JSON_DIR + "metadata.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Finds a legal entity by identifier. If there is no identifier for the
   * identifier, the history is searched. If nothing is found, an exception is
   * thrown.
   *
   * @param idStr
   *          the identifier
   * @return the legal entity
   */
  @Path("{legalEntityId}")
  public Object findLegalEntityHTML(@Subscribe @PathParam("legalEntityId") final String idStr) {
    data().setUriLegalEntityId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    try {
      final LegalEntityDocument doc = data().getLegalEntityMaster().get(oid);
      data().setLegalEntity(doc);
    } catch (final DataNotFoundException ex) {
      final LegalEntityHistoryRequest historyRequest = new LegalEntityHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      final LegalEntityHistoryResult historyResult = data().getLegalEntityMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setLegalEntity(historyResult.getFirstDocument());
    }
    return new WebLegalEntityResource(this);
  }

  //-------------------------------------------------------------------------

  /**
   * Creates the output root data.
   *
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final LegalEntitySearchRequest searchRequest = new LegalEntitySearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI for legalEntities.
   *
   * @param data the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebLegalEntityData data) {
    final UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebLegalEntitiesResource.class);
    return builder.build();
  }

}
