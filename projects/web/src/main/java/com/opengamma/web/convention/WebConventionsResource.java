/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.convention;

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

import com.google.common.collect.BiMap;
import com.opengamma.DataNotFoundException;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.convention.ConventionDocument;
import com.opengamma.master.convention.ConventionHistoryRequest;
import com.opengamma.master.convention.ConventionHistoryResult;
import com.opengamma.master.convention.ConventionMaster;
import com.opengamma.master.convention.ConventionSearchRequest;
import com.opengamma.master.convention.ConventionSearchResult;
import com.opengamma.master.convention.ConventionSearchSortOrder;
import com.opengamma.master.convention.ManageableConvention;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;
import com.opengamma.web.json.JSONBuilder;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * RESTful resource for all convention documents.
 * <p>
 * The convention documents resource represents all the data for one element type in the convention master.
 *
 */
@Path("/conventions")
public class WebConventionsResource extends AbstractWebConventionResource {

  /**
   * Creates the resource.
   * @param conventionMaster  the convention master, not null
   */
  public WebConventionsResource(final ConventionMaster conventionMaster) {
    super(conventionMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.CONFIG)
  public String getHTML(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("identifier") final String id,
      @QueryParam("type") final String type,
      @QueryParam("conventionId") final List<String> conventionIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final ConventionSearchSortOrder so = buildSortOrder(sort, ConventionSearchSortOrder.NAME_ASC);
    final FlexiBean out = search(pr, so, name, id, type, conventionIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "conventions.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.CONFIG)
  public String getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("identifier") final String id,
      @QueryParam("type") final String type,
      @QueryParam("conventionId") final List<String> conventionIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final ConventionSearchSortOrder so = buildSortOrder(sort, ConventionSearchSortOrder.NAME_ASC);
    final FlexiBean out = search(pr, so, name, id, type, conventionIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "conventions.ftl", out);
  }

  private FlexiBean search(final PagingRequest request, final ConventionSearchSortOrder so, final String name, final String id,
      String typeName, final List<String> conventionIdStrs, final UriInfo uriInfo) {
    final FlexiBean out = createRootData();

    final ConventionSearchRequest searchRequest = new ConventionSearchRequest();
    searchRequest.setExternalIdValue(StringUtils.trimToNull(id));
    typeName = StringUtils.trimToNull(typeName);
    if (typeName != null) {
      searchRequest.setConventionType(getConventionTypesProvider().getConventionTypeForClassName(typeName));
    }
    searchRequest.setPagingRequest(request);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    out.put("searchRequest", searchRequest);
    out.put("type", typeName);
    for (final String conventionIdStr : conventionIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(conventionIdStr));
    }

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final ConventionSearchResult searchResult = data().getConventionMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), uriInfo));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
      @FormParam("name") String name,
      @FormParam("conventionxml") String xml,
      @FormParam("type") String typeName) {
    name = StringUtils.trimToNull(name);
    xml = StringUtils.trimToNull(xml);
    typeName = StringUtils.trimToNull(typeName);

    final Class<? extends ManageableConvention> typeClazz = typeName != null ? data().getTypeMap().get(typeName) : null;
    if (name == null || xml == null || typeClazz == null) {
      final FlexiBean out = createRootData();
      if (name == null) {
        out.put("err_nameMissing", true);
      }
      if (xml == null) {
        out.put("err_xmlMissing", true);
      }
      if (typeName == null) {
        out.put("err_typeMissing", true);
      } else if (typeClazz == null) {
        out.put("err_typeInvalid", true);
      }
      out.put("name", StringUtils.defaultString(name));
      out.put("type", StringUtils.defaultString(typeName));
      out.put("conventionXml", StringEscapeUtils.escapeJava(StringUtils.defaultString(xml)));
      final String html = getFreemarker().build(HTML_DIR + "convention-add.ftl", out);
      return Response.ok(html).build();
    }
    try {
      final ManageableConvention convention = parseXML(xml, typeClazz);
      convention.setName(name);
      final ConventionDocument doc = new ConventionDocument(convention);
      final ConventionDocument added = data().getConventionMaster().add(doc);
      final URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
      return Response.seeOther(uri).build();
    } catch (final Exception ex) {
      final FlexiBean out = createRootData();
      out.put("name", StringUtils.defaultString(name));
      out.put("type", StringUtils.defaultString(typeName));
      out.put("conventionXml", StringEscapeUtils.escapeJava(StringUtils.defaultString(xml)));
      out.put("err_conventionXmlMsg", StringUtils.defaultString(ex.getMessage()));
      final String html = getFreemarker().build(HTML_DIR + "convention-add.ftl", out);
      return Response.ok(html).build();
    }
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("name") String name,
      @FormParam("conventionJSON") String json,
      @FormParam("conventionXML") String xml,
      @FormParam("type") String typeName) {
    name = StringUtils.trimToNull(name);
    json = StringUtils.trimToNull(json);
    xml = StringUtils.trimToNull(xml);
    typeName = StringUtils.trimToNull(typeName);

    final Class<? extends ManageableConvention> typeClazz = typeName != null ? data().getTypeMap().get(typeName) : null;
    Response result = null;
    if (name == null || typeClazz == null || isEmptyConventionData(json, xml)) {
      result = Response.status(Status.BAD_REQUEST).build();
    } else {
      ManageableConvention convention = null;
      if (json != null) {
        convention = (ManageableConvention) parseJSON(json);
      } else if (xml != null) {
        convention = parseXML(xml, typeClazz);
      }
      if (convention == null) {
        result = Response.status(Status.BAD_REQUEST).build();
      } else {
        convention.setName(name);
        final ConventionDocument doc = new ConventionDocument(convention);
        final ConventionDocument added = data().getConventionMaster().add(doc);
        final URI uri = data().getUriInfo().getAbsolutePathBuilder().path(added.getUniqueId().toLatest().toString()).build();
        result = Response.created(uri).build();
      }
    }
    return result;
  }

  private boolean isEmptyConventionData(final String json, final String xml) {
    return json == null && xml == null;
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("metaData")
  @Produces(MediaType.APPLICATION_JSON)
  public String getMetaDataJSON() {
    final FlexiBean out = createRootData();
    return getFreemarker().build(JSON_DIR + "metadata.ftl", out);
  }

  //-------------------------------------------------------------------------
  @Path("{conventionId}")
  public Object findConvention(@Subscribe @PathParam("conventionId") final String idStr) {
    data().setUriConventionId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    try {
      final ConventionDocument doc = data().getConventionMaster().get(oid);
      data().setConvention(doc);
    } catch (final DataNotFoundException ex) {
      final ConventionHistoryRequest historyRequest = new ConventionHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      final ConventionHistoryResult historyResult = data().getConventionMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setConvention(historyResult.getFirstDocument());
    }
    return new WebConventionResource(this);
  }

  @GET
  @Path("templates/{conventionType}")
  @Produces(MediaType.APPLICATION_JSON)
  public String getTemplateJSON(@PathParam("conventionType") final String conventionType) {
    final BiMap<String, Class<? extends ManageableConvention>> typeMap = data().getClassNameMap();
    final Class<? extends ManageableConvention> typeClazz = typeMap.get(conventionType);
    String template = null;
    if (typeClazz != null) {
      final JSONBuilder<?> jsonBuilder = data().getJsonBuilderMap().get(typeClazz);
      if (jsonBuilder != null) {
        template = jsonBuilder.getTemplate();
      }
    }
    final FlexiBean out = super.createRootData();
    out.put("template", template);
    if (typeClazz != null) {
      out.put("type", typeClazz.getSimpleName());
    }
    return getFreemarker().build(JSON_DIR + "template.ftl", out);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final ConventionSearchRequest searchRequest = new ConventionSearchRequest();
    out.put("searchRequest", searchRequest);
    out.put("type", "");
    out.put("conventionTypes", getConventionTypesProvider().getTypeSet());
    out.put("conventionDescriptionMap", getConventionTypesProvider().getDescriptionMap());
    out.put("conventionGroups", getConventionTypesProvider().getConventionDescriptions());
    out.put("conventionDetails", getConventionTypesProvider().getConventionDetails());
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for conventions.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebConventionData data) {
    final UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebConventionsResource.class);
    return builder.build();
  }

}
