/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.valuerequirementname;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONStringer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.web.AbstractWebResource;

/**
 * RESTful resource that returns the value requirement names for the Web GUI.
 */
@Path("/valuerequirementnames/metaData")
public class WebValueRequirementNamesResource extends AbstractWebResource {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(WebValueRequirementNamesResource.class);

  /**
   * Configuration key.
   */
  public static final String VALUE_REQUIREMENT_NAME_CLASSES = "valueRequirementNameClasses";

  /**
   * The value requirement names.
   */
  private final Set<String> _valueRequirementNames;

  //-------------------------------------------------------------------------
  /**
   * Creates the resource.
   */
  public WebValueRequirementNamesResource() {
    this(new String[] { ValueRequirementNames.class.getName() });
  }

  /**
   * Creates an instance.
   *
   * @param valueRequirementNameClasses  the classes, not null
   */
  public WebValueRequirementNamesResource(final String[] valueRequirementNameClasses) {
    ArgumentChecker.notEmpty(valueRequirementNameClasses, "valueRequirementNameClasses");
    final List<String> list = new ArrayList<>();
    for (final String className : valueRequirementNameClasses) {
      try {
        for (final Field field : Class.forName(className.trim()).getDeclaredFields()) {
          if (Modifier.isPublic(field.getModifiers()) && field.isSynthetic() == false) {
            list.add((String) field.get(null));
          }
        }
      } catch (final Exception ex) {
        LOGGER.info("Could not read in value requirement names: " + ex.getMessage());
      }
    }
    Collections.sort(list, String.CASE_INSENSITIVE_ORDER);
    _valueRequirementNames = new LinkedHashSet<>(list);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the value requirement names as JSON. The key is "types" and the values
   * are stored in a JSONArray.
   *
   * @return the values as JSON
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    String result = null;
    try {
      result = new JSONStringer()
          .object()
          .key("types")
          .value(new JSONArray(_valueRequirementNames))
          .endObject()
          .toString();
    } catch (final JSONException ex) {
      LOGGER.warn("error creating json document for valueRequirementNames: ", ex.getMessage());
    }
    return result;
  }

}
