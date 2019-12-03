/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.math.NumberUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentServer;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.web.FreemarkerOutputter;
import com.opengamma.web.WebHomeUris;

/**
 * RESTful resource for exposing managed components.
 * <p>
 * This resource receives and processes RESTful calls to all managed components.
 */
@Path("components")
public class DataComponentServerResource extends AbstractDataResource {

  /**
   * Sort classes by simple name.
   */
  private static final Comparator<Class<?>> ORDER_CLASS = new Comparator<Class<?>>() {
    @Override
    public int compare(final Class<?> cls1, final Class<?> cls2) {
      return cls1.getSimpleName().compareTo(cls2.getSimpleName());
    }
  };
  /**
   * Sort classes by level, then classifier name.
   */
  private static final Comparator<ComponentInfo> ORDER_CLASSIFIER = new Comparator<ComponentInfo>() {
    @Override
    public int compare(final ComponentInfo info1, final ComponentInfo info2) {
      int cmp = 0;
      if (info1.getAttributes().containsKey(ComponentInfoAttributes.LEVEL) && !info2.getAttributes().containsKey(ComponentInfoAttributes.LEVEL)) {
        return -1;
      }
      if (!info1.getAttributes().containsKey(ComponentInfoAttributes.LEVEL) && info2.getAttributes().containsKey(ComponentInfoAttributes.LEVEL)) {
        return 1;
      }
      if (info1.getAttributes().containsKey(ComponentInfoAttributes.LEVEL) && info2.getAttributes().containsKey(ComponentInfoAttributes.LEVEL)) {
        final String str1 = info1.getAttribute(ComponentInfoAttributes.LEVEL);
        final String str2 = info2.getAttribute(ComponentInfoAttributes.LEVEL);
        cmp = NumberUtils.toInt(str2) - NumberUtils.toInt(str1); // reverse order
      }
      cmp = cmp == 0 ? info1.getClassifier().compareTo(info2.getClassifier()) : cmp;
      return cmp;
    }
  };

  /**
   * The local components.
   */
  private final List<RestComponent> _localComponents;
  /**
   * The remote components.
   */
  private final List<ComponentInfo> _remoteComponents;

  /**
   * Creates the resource.
   *
   * @param localComponents
   *          the managed components, not null
   * @param remoteComponents
   *          the republished remote components, not null
   */
  public DataComponentServerResource(final Iterable<RestComponent> localComponents, final Iterable<ComponentInfo> remoteComponents) {
    ArgumentChecker.notNull(localComponents, "localComponents");
    _localComponents = ImmutableList.copyOf(localComponents);
    _remoteComponents = ImmutableList.copyOf(remoteComponents);
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the components.
   *
   * @return the components, not null
   */
  public List<RestComponent> getComponents() {
    return _localComponents;
  }

  // -------------------------------------------------------------------------
  @HEAD
  public Response status() {
    // simple GET to quickly return as a ping
    return responseOk();
  }

  @GET
  public Response getComponentInfos() {
    final ComponentServer server = createServerInfo();
    return responseOkObject(server);
  }

  @GET
  @Produces(value = MediaType.TEXT_HTML)
  public String getComponentInfosHtml(@Context final ServletContext servletContext, @Context final UriInfo uriInfo) {
    final ComponentServer server = createServerInfo();
    server.setUri(uriInfo.getBaseUri());
    final Multimap<Class<?>, ComponentInfo> byType = TreeMultimap.create(ORDER_CLASS, ORDER_CLASSIFIER);
    for (final ComponentInfo info : server.getComponentInfos()) {
      byType.put(info.getType(), info);
    }
    final FreemarkerOutputter freemarker = new FreemarkerOutputter(servletContext);
    final FlexiBean out = FreemarkerOutputter.createRootData(uriInfo);
    out.put("componentServer", server);
    out.put("infosByType", byType);
    out.put("uris", new WebHomeUris(uriInfo));
    return freemarker.build("data/componentserver.ftl", out);
  }

  private ComponentServer createServerInfo() {
    final ComponentServer server = new ComponentServer(URI.create("components"));
    server.getComponentInfos().addAll(_remoteComponents);
    for (final RestComponent component : _localComponents) {
      server.getComponentInfos().add(component.getInfo());
    }
    return server;
  }

  @Path("{type}/{classifier}")
  public Object findComponent(@PathParam("type") final String type, @PathParam("classifier") final String classifier) {
    for (final RestComponent component : _localComponents) {
      if (component.getInfo().getType().getSimpleName().equalsIgnoreCase(type) && component.getInfo().getClassifier().equalsIgnoreCase(classifier)) {
        return component.getInstance();
      }
    }
    return null;
  }
}
