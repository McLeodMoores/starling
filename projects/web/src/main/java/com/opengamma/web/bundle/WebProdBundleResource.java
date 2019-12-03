/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.bundle;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * RESTful resource for a CSS/Javascript bundle in production mode.
 */
@Path("/bundles/prod/{bundleId}")
public class WebProdBundleResource extends AbstractWebBundleResource {

  /**
   * Creates the resource.
   *
   * @param parent  the parent resource, not null
   */
  public WebProdBundleResource(final AbstractWebBundleResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@PathParam("bundleId") final String idStr, @Context final Request request) {
    final Bundle bundle = data().getBundleManager().getBundle(idStr);
    if (bundle == null) {
      return null;
    }
    final String compressedContent = data().getCompressor().compressBundle(bundle);
    final EntityTag etag = new EntityTag(DigestUtils.md5Hex(compressedContent));
    ResponseBuilder builder = request.evaluatePreconditions(etag);
    if (builder == null) {
      final BundleType type = BundleType.getType(idStr);
      String mimeType = null;
      switch (type) {
        case JS:
          mimeType = "text/javascript";
          break;
        case CSS:
          mimeType = "text/css";
          break;
        default:
          mimeType = MediaType.TEXT_HTML;
          break;
      }
      builder = Response.ok(compressedContent).header("Content-type", mimeType);
    }
    return builder.tag(etag).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   *
   * @param data  the data, not null
   * @param bundleId the bundleId, not null
   * @return the URI, not null
   */
  public static URI uri(final WebBundlesData data, final String bundleId) {
    return data.getUriInfo().getBaseUriBuilder().path(WebProdBundleResource.class).build(bundleId);
  }

}
