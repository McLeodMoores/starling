/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.convention;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.joda.convert.StringConvert;
import org.joda.convert.StringConverter;
import org.json.JSONArray;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCountFactory;
import com.opengamma.financial.conversion.JodaBeanConverters;

/**
 *
 */
@Path("conventions")
public class ConventionLookupResource {

  static {
    JodaBeanConverters.getInstance();
  }

  private final StringConvert _stringConverter = new StringConvert();

  @GET
  @Path("daycountconventions")
  @Produces(MediaType.APPLICATION_JSON)
  public String getDayCountConventions() {
    return convertToJson(DayCount.class, DayCountFactory.INSTANCE.instanceMap().values().iterator());
  }

  @SuppressWarnings("unchecked")
  private String convertToJson(final Class<?> type, final Iterator<?> iter) {
    final StringConverter<Object> converter = (StringConverter<Object>) _stringConverter.findConverter(type);
    final List<String> results = new ArrayList<>();
    while (iter.hasNext()) {
      final Object item = iter.next();
      results.add(converter.convertToString(item));
    }
    return new JSONArray(results).toString();
  }
}
