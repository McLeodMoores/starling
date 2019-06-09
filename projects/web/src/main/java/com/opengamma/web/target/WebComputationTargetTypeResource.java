/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.target.ComputationTargetTypeProvider;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractWebResource;

/**
 * RESTful resource for the Web GUI to work with computation target types and specifications.
 */
@Path("/computationTarget")
public class WebComputationTargetTypeResource extends AbstractWebResource {

  private static final Comparator<ComputationTargetType> SORT_ORDER = new Comparator<ComputationTargetType>() {
    @Override
    public int compare(final ComputationTargetType type1, final ComputationTargetType type2) {
      return type1.getName().compareToIgnoreCase(type2.getName());
    }
  };

  /**
   * Source of the known computation target types.
   */
  private final ComputationTargetTypeProvider _types;

  /**
   * @param types
   *          the computation target types, not null
   */
  public WebComputationTargetTypeResource(final ComputationTargetTypeProvider types) {
    ArgumentChecker.notNull(types, "types");
    _types = types;
  }

  /**
   * Converts the types to a JSON array.
   *
   * @param types
   *          the types
   * @return the types as a JSON string
   */
  protected String typesJSONResponse(final Collection<ComputationTargetType> types) {
    final List<ComputationTargetType> sorted = new ArrayList<>(types);
    Collections.sort(sorted, SORT_ORDER);
    try {
      final JSONWriter response = new JSONStringer().object().key("types").array();
      for (final ComputationTargetType type : sorted) {
        response.object().key("label").value(type.getName()).key("value").value(type.toString()).endObject();
      }
      return response.endArray().endObject().toString();
    } catch (final JSONException e) {
      return null;
    }
  }

  /**
   * Gets the simple computation target types as a JSON response.
   *
   * @return the computation target types
   */
  @GET
  @Path("simpleTypes")
  @Produces(MediaType.APPLICATION_JSON)
  public String getSimpleTypes() {
    return typesJSONResponse(_types.getSimpleTypes());
  }

  /**
   * Gets additional computation target types as a JSON response.
   *
   * @return the computation target types
   */
  @GET
  @Path("additionalTypes")
  @Produces(MediaType.APPLICATION_JSON)
  public String getAdditionalTypes() {
    return typesJSONResponse(_types.getAdditionalTypes());
  }

  /**
   * Gets all computation target types as a JSON response.
   *
   * @return the computation target types
   */
  @GET
  @Path("allTypes")
  @Produces(MediaType.APPLICATION_JSON)
  public String getAllTypes() {
    return typesJSONResponse(_types.getAllTypes());
  }

}
