/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited
 * Modified from APLv2 code Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * Please see distribution for license.
 */
package com.opengamma.web.security;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.DataNotFoundException;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.legalentity.LegalEntityMaster;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityHistoryRequest;
import com.opengamma.master.security.SecurityHistoryResult;
import com.opengamma.master.security.SecurityLoader;
import com.opengamma.master.security.SecurityLoaderRequest;
import com.opengamma.master.security.SecurityLoaderResult;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.master.security.SecurityMetaDataRequest;
import com.opengamma.master.security.SecurityMetaDataResult;
import com.opengamma.master.security.SecuritySearchRequest;
import com.opengamma.master.security.SecuritySearchResult;
import com.opengamma.master.security.SecuritySearchSortOrder;
import com.opengamma.master.security.impl.DelegatingSecurityMaster;
import com.opengamma.util.JodaBeanSerialization;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.opengamma.web.analytics.rest.MasterType;
import com.opengamma.web.analytics.rest.Subscribe;
import com.opengamma.web.analytics.rest.SubscribeMaster;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * RESTful resource for all securities.
 * <p>
 * The securities resource represents the whole of a security master.
 */
@Path("/securities")
public class MinimalWebSecuritiesResource extends AbstractMinimalWebSecurityResource {

  /**
   * Creates the resource.
   * @param securityMaster  the security master, not null
   */
  public MinimalWebSecuritiesResource(final SecurityMaster securityMaster) {
    super(securityMaster);
  }

  /**
   * Creates the resource.
   * @param securityMaster  the security master, not null
   * @param securityLoader  the security loader, not null
   */
  public MinimalWebSecuritiesResource(final SecurityMaster securityMaster, final SecurityLoader securityLoader) {
    super(securityMaster, securityLoader);
  }

  /**
   * Creates the resource.
   * @param securityMaster  the security master, not null
   * @param securityLoader  the security loader, not null
   * @param htsMaster  the historical time series master, not null
   * @param legalEntityMaster the organization master, not null
   */
  public MinimalWebSecuritiesResource(final SecurityMaster securityMaster, final SecurityLoader securityLoader,
      final HistoricalTimeSeriesMaster htsMaster, final LegalEntityMaster legalEntityMaster) {
    super(securityMaster, securityLoader, htsMaster, legalEntityMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  @SubscribeMaster(MasterType.SECURITY)
  public String getHTML(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("identifier") final String identifier,
      @QueryParam("type") final String type,
      @QueryParam("uniqueIdScheme") final String uniqueIdScheme,
      @QueryParam("securityId") final List<String> securityIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final SecuritySearchSortOrder so = buildSortOrder(sort, SecuritySearchSortOrder.NAME_ASC);
    final FlexiBean out = createSearchResultData(pr, so, name, identifier, type, uniqueIdScheme, securityIdStrs, uriInfo);
    return getFreemarker().build(HTML_DIR + "securities.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @SubscribeMaster(MasterType.SECURITY)
  public String getJSON(
      @QueryParam("pgIdx") final Integer pgIdx,
      @QueryParam("pgNum") final Integer pgNum,
      @QueryParam("pgSze") final Integer pgSze,
      @QueryParam("sort") final String sort,
      @QueryParam("name") final String name,
      @QueryParam("identifier") final String identifier,
      @QueryParam("type") final String type,
      @QueryParam("uniqueIdScheme") final String uniqueIdScheme,
      @QueryParam("securityId") final List<String> securityIdStrs,
      @Context final UriInfo uriInfo) {
    final PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    final SecuritySearchSortOrder so = buildSortOrder(sort, SecuritySearchSortOrder.NAME_ASC);
    final FlexiBean out = createSearchResultData(pr, so, name, identifier, type, uniqueIdScheme, securityIdStrs, uriInfo);
    return getFreemarker().build(JSON_DIR + "securities.ftl", out);
  }

  private FlexiBean createSearchResultData(final PagingRequest pr, final SecuritySearchSortOrder so, final String name, final String identifier,
      final String type, final String uniqueIdScheme, final List<String> securityIdStrs, final UriInfo uriInfo) {
    final FlexiBean out = createRootData();

    final SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setSortOrder(so);
    searchRequest.setName(StringUtils.trimToNull(name));
    searchRequest.setExternalIdValue(StringUtils.trimToNull(identifier));
    searchRequest.setSecurityType(StringUtils.trimToNull(type));
    searchRequest.setUniqueIdScheme(StringUtils.trimToNull(uniqueIdScheme));
    for (final String securityIdStr : securityIdStrs) {
      searchRequest.addObjectId(ObjectId.parse(securityIdStr));
    }
    final MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      final ExternalId id = ExternalId.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addExternalId(id);
    }
    out.put("searchRequest", searchRequest);

    if (data().getUriInfo().getQueryParameters().size() > 0) {
      final SecuritySearchResult searchResult = data().getSecurityMaster().search(searchRequest);
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
      @FormParam("type") String type,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam(SECURITY_XML) String securityXml,
      @FormParam("uniqueIdScheme") String uniqueIdScheme) {

    uniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    type = StringUtils.defaultString(StringUtils.trimToNull(type));
    final FlexiBean out = createRootData();
    URI responseURI = null;
    switch (type) {
      case "xml":
        boolean isValidInput = true;
        try {
          securityXml = StringUtils.trimToNull(securityXml);
          if (securityXml == null) {
            out.put("err_securityXmlMissing", true);
            isValidInput = false;
          }
          if (uniqueIdScheme == null) {
            out.put("err_unqiueIdSchemeMissing", true);
            isValidInput = false;
          }
          if (!isValidInput) {
            out.put(SECURITY_XML, StringEscapeUtils.escapeJavaScript(StringUtils.defaultString(securityXml)));
            out.put("selectedUniqueIdScheme", StringUtils.defaultString(uniqueIdScheme));
            return Response.ok(buildResponseHtml(out, "securities-add.ftl")).build();
          }
          final ManageableSecurity security = addSecurity(securityXml, uniqueIdScheme);
          final MinimalWebSecuritiesUris webSecuritiesUris = new MinimalWebSecuritiesUris(data());
          responseURI =  webSecuritiesUris.security(security);
        } catch (final Exception ex) {
          out.put("err_securityXmlMsg", ex.getMessage());
          out.put(SECURITY_XML, StringEscapeUtils.escapeJavaScript(StringUtils.defaultString(securityXml)));
          out.put("selectedUniqueIdScheme", StringUtils.defaultString(uniqueIdScheme));
          return Response.ok(buildResponseHtml(out, "securities-add.ftl")).build();
        }
        break;
      case "id":
        idScheme = StringUtils.trimToNull(idScheme);
        idValue = StringUtils.trimToNull(idValue);

        if (idScheme == null || idValue == null) {
          if (idScheme == null) {
            out.put("err_idschemeMissing", true);
          }
          if (idValue == null) {
            out.put("err_idvalueMissing", true);
          }
          out.put("idscheme", idScheme);
          out.put("idvalue", idValue);
          return Response.ok(buildResponseHtml(out, "securities-add.ftl")).build();
        }

        final ExternalScheme scheme = ExternalScheme.of(idScheme);
        final Collection<ExternalIdBundle> bundles = buildSecurityRequest(scheme, idValue);
        final SecurityLoaderResult loaderResult = data().getSecurityLoader().loadSecurities(SecurityLoaderRequest.create(bundles));
        final Map<ExternalIdBundle, UniqueId> loadedSecurities = loaderResult.getResultMap();
        if (bundles.size() == 1 && loadedSecurities.size() == 1) {
          final ExternalIdBundle identifierBundle = bundles.iterator().next();
          responseURI = data().getUriInfo().getAbsolutePathBuilder().path(loadedSecurities.get(identifierBundle).toLatest().toString()).build();
        } else {
          responseURI = uri(data(), buildRequestAsExternalIdBundle(scheme, bundles));
        }
        break;
      default:
        throw new IllegalArgumentException("Can only add security by XML or ID");
    }
    return Response.seeOther(responseURI).build();
  }

  private String buildResponseHtml(final FlexiBean out, final String templateName) {
    return getFreemarker().build(HTML_DIR + "securities-add.ftl", out);
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
      @FormParam("type") String type,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue,
      @FormParam(SECURITY_XML) String securityXml,
      @FormParam("uniqueIdScheme") String uniqueIdScheme) {

    uniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    final FlexiBean out = createRootData();
    final ExternalScheme scheme = ExternalScheme.of(idScheme);
    out.put("requestScheme", scheme);

    type = StringUtils.defaultString(StringUtils.trimToNull(type));
    switch (type) {
      case "xml":
        securityXml = StringUtils.trimToNull(securityXml);
        if (securityXml == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }
        final ManageableSecurity security = addSecurity(securityXml, uniqueIdScheme);
        out.put("addedSecurities", getAddedSecurityId(security));
        break;
      case StringUtils.EMPTY: // create security by ID if type is missing
      case "id":
        idScheme = StringUtils.trimToNull(idScheme);
        idValue = StringUtils.trimToNull(idValue);
        if (idScheme == null || idValue == null) {
          return Response.status(Status.BAD_REQUEST).build();
        }
        final Collection<ExternalIdBundle> requestBundles = buildSecurityRequest(scheme, idValue);
        final SecurityLoaderResult loaderResult = data().getSecurityLoader().loadSecurities(SecurityLoaderRequest.create(requestBundles));
        out.put("addedSecurities", getLoadedSecuritiesId(loaderResult.getResultMap(), requestBundles, scheme));
        break;
      default:
        throw new IllegalArgumentException("Can only add security by XML or ID");
    }
    return Response.ok(getFreemarker().build(JSON_DIR + "securities-added.ftl", out)).build();
  }

  private ManageableSecurity addSecurity(final String securityXml, final String uniqueIdScheme) {
    final Bean securityBean = JodaBeanSerialization.deserializer().xmlReader().read(securityXml);
    final SecurityMaster securityMaster = data().getSecurityMaster();
    final ManageableSecurity manageableSecurity = (ManageableSecurity) securityBean;
    if (uniqueIdScheme != null) {
      manageableSecurity.setUniqueId(UniqueId.of(uniqueIdScheme, uniqueIdScheme));
    }
    final SecurityDocument addedSecDoc = securityMaster.add(new SecurityDocument(manageableSecurity));
    return addedSecDoc.getSecurity();
  }

  private Map<String, String> getAddedSecurityId(final ManageableSecurity security) {
    final Map<String, String> addedSecurities = new HashMap<>();
    final ExternalIdBundle externalIdBundle = security.getExternalIdBundle();
    final UniqueId uniqueId = security.getUniqueId();
    final String objectIdentifier = uniqueId.getObjectId().toString();
    String externalIdValue = StringUtils.EMPTY;
    if (!externalIdBundle.isEmpty()) {
      final ExternalId externalId = externalIdBundle.iterator().next();
      externalIdValue = externalId.getValue();
    }
    addedSecurities.put(externalIdValue, objectIdentifier);
    return addedSecurities;
  }

  private Map<String, String> getLoadedSecuritiesId(final Map<ExternalIdBundle, UniqueId> loadedSecurities, final Collection<ExternalIdBundle> requestBundles, final ExternalScheme scheme) {
    final Map<String, String> result = new HashMap<String, String>();
    for (final ExternalIdBundle identifierBundle : requestBundles) {
      final UniqueId uniqueIdentifier = loadedSecurities.get(identifierBundle);
      final String objectIdentifier = uniqueIdentifier != null ? uniqueIdentifier.getObjectId().toString() : null;
      result.put(identifierBundle.getValue(scheme), objectIdentifier);
    }
    return result;
  }

  private ExternalIdBundle buildRequestAsExternalIdBundle(final ExternalScheme scheme, final Collection<ExternalIdBundle> bundles) {
    final List<ExternalId> identifiers = new ArrayList<ExternalId>();
    for (final ExternalIdBundle bundle : bundles) {
      identifiers.add(bundle.getExternalId(scheme));
    }
    return ExternalIdBundle.of(identifiers);
  }

  private Collection<ExternalIdBundle> buildSecurityRequest(final ExternalScheme identificationScheme, final String idValue) {
    if (idValue == null) {
      return Collections.emptyList();
    }
    final String[] identifiers = StringUtils.split(idValue, "\n");
    final List<ExternalIdBundle> result = new ArrayList<ExternalIdBundle>(identifiers.length);
    for (String identifier : identifiers) {
      identifier = StringUtils.trimToNull(identifier);
      if (identifier != null) {
        result.add(ExternalIdBundle.of(ExternalId.of(identificationScheme, identifier)));
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("metaData")
  @Produces(MediaType.APPLICATION_JSON)
  public String getMetaDataJSON(@QueryParam("uniqueIdScheme") String uniqueIdScheme) {
    uniqueIdScheme = StringUtils.trimToNull(uniqueIdScheme);
    final FlexiBean out = super.createRootData();
    out.put("schemaVersion", getSecurityMasterSchemaVersion(uniqueIdScheme));
    out.put("securityTypes", data().getSecurityTypes().values());
    out.put("description2type", data().getSecurityTypes());
    return getFreemarker().build(JSON_DIR + "metadata.ftl", out);
  }

  private String getSecurityMasterSchemaVersion(final String uniqueIdScheme) {
    final SecurityMetaDataRequest request = new SecurityMetaDataRequest();
    request.setUniqueIdScheme(uniqueIdScheme);
    request.setSchemaVersion(true);
    request.setSecurityTypes(false);
    final SecurityMetaDataResult metaData = data().getSecurityMaster().metaData(request);
    return metaData.getSchemaVersion();
  }

  //-------------------------------------------------------------------------
  @Path("{securityId}")
  public MinimalWebSecurityResource findSecurity(@Subscribe @PathParam("securityId") final String idStr) {
    data().setUriSecurityId(idStr);
    final UniqueId oid = UniqueId.parse(idStr);
    try {
      final SecurityDocument doc = data().getSecurityMaster().get(oid);
      data().setSecurity(doc);
    } catch (final DataNotFoundException ex) {
      final SecurityHistoryRequest historyRequest = new SecurityHistoryRequest(oid);
      historyRequest.setPagingRequest(PagingRequest.ONE);
      final SecurityHistoryResult historyResult = data().getSecurityMaster().history(historyRequest);
      if (historyResult.getDocuments().size() == 0) {
        throw ex;
      }
      data().setSecurity(historyResult.getFirstDocument());
    }
    return new MinimalWebSecurityResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    final FlexiBean out = super.createRootData();
    final SecuritySearchRequest searchRequest = new SecuritySearchRequest();
    out.put("searchRequest", searchRequest);

    final SecurityMetaDataRequest request = new SecurityMetaDataRequest();
    request.setSchemaVersion(true);
    request.setSecurityTypes(false);

    if (data().getSecurityMaster() instanceof DelegatingSecurityMaster) {
      final Map<String, String> schemaVersionByScheme = new HashMap<>();

      final DelegatingSecurityMaster delegatingSecMaster = (DelegatingSecurityMaster) data().getSecurityMaster();
      final Map<String, SecurityMaster> delegates = delegatingSecMaster.getDelegates();
      for (final Entry<String, SecurityMaster> entry : delegates.entrySet()) {
        final SecurityMaster securityMaster = entry.getValue();
        final SecurityMetaDataResult metaData = securityMaster.metaData(request);
        schemaVersionByScheme.put(entry.getKey(), metaData.getSchemaVersion());
      }
      out.put("schemaVersionByScheme", schemaVersionByScheme);
      out.put("uniqueIdSchemes", schemaVersionByScheme.keySet());
    } else {
      final SecurityMetaDataResult metaData = data().getSecurityMaster().metaData(request);
      out.put("schemaVersion", metaData.getSchemaVersion());
    }
    out.put("description2type", data().getSecurityTypes());
    return out;
  }




  //-------------------------------------------------------------------------
  /**
   * Builds a URI for securities.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for securities.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(final WebSecuritiesData data, final ExternalIdBundle identifiers) {
    final UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(MinimalWebSecuritiesResource.class);
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
