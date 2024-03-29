/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.web.json.convention;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.WordUtils;
import org.json.JSONArray;

import com.opengamma.analytics.financial.interestrate.CompoundingType;
import com.opengamma.financial.convention.StubType;
import com.opengamma.financial.convention.expirycalc.ExchangeTradedInstrumentExpiryCalculatorFactory;
import com.opengamma.financial.convention.rolldate.RollDateAdjuster;
import com.opengamma.financial.convention.rolldate.RollDateAdjusterFactory;
import com.opengamma.financial.security.swap.InterpolationMethod;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.time.TenorFactory;

/**
 * Utility methods to get data (e.g. all possible stub types) that is used to create conventions.
 */
@Path("conventionutils")
public class WebConventionsUtils {

  /**
   * Gets all possible stub types as an array of <code>[stub type]|[display name]</code>.
   *
   * @return the stub types
   */
  @GET
  @Path("stubtype")
  @Produces(MediaType.APPLICATION_JSON)
  public String getStubType() {
    return convertEnumToJsonArray(Arrays.asList(StubType.values()), EnumComparator.INSTANCE);
  }

  /**
   * Gets all possible interpolation methods as an array of <code>[interpolation method]|[display name]</code>.
   *
   * @return the interpolation methods
   */
  @GET
  @Path("interpolationmethod")
  @Produces(MediaType.APPLICATION_JSON)
  public String getInterpolationMethod() {
    return convertEnumToJsonArray(Arrays.asList(InterpolationMethod.values()), EnumComparator.INSTANCE);
  }

  /**
   * Gets all compounding types as an array of <code>[compounding type]|display name]</code>.
   *
   * @return the compounding types
   */
  @GET
  @Path("compoundingtype")
  @Produces(MediaType.APPLICATION_JSON)
  public String getCompoundingTypes() {
    return convertEnumToJsonArray(Arrays.asList(CompoundingType.values()), EnumComparator.INSTANCE);
  }

  /**
   * Gets all roll date adjusters stored in the {@link RollDateAdjusterFactory} as an array of <code>[display name]</code>.
   *
   * @return the roll date adjusters
   */
  @GET
  @Path("rolldateadjuster")
  @Produces(MediaType.APPLICATION_JSON)
  public String getRollDateTypes() {
    final Map<String, RollDateAdjuster> adjusters = RollDateAdjusterFactory.INSTANCE.instanceMap();
    final List<String> results = new ArrayList<>(adjusters.keySet());
    Collections.sort(results);
    return new JSONArray(results).toString();
  }

  /**
   * Gets all tenors stored in the {@link TenorFactory} as an array of <code>[display name]</code>.
   *
   * @return the tenors
   */
  @GET
  @Path("tenor")
  @Produces(MediaType.APPLICATION_JSON)
  public String getTenors() {
    final Map<String, Tenor> tenors = TenorFactory.INSTANCE.instanceMap();
    final SortedSet<Tenor> sorted = new TreeSet<>(tenors.values());
    final List<String> results = new ArrayList<>();
    for (final Tenor tenor : sorted) {
      results.add(tenor.toFormattedString());
    }
    return new JSONArray(results).toString();
  }

  /**
   * Gets all expiry calculators stored in the {@link ExchangeTradedInstrumentExpiryCalculatorFactory} as an array of
   * <code>[expiry calculator]|display name]</code>.
   *
   * @return the calculators
   */
  @GET
  @Path("expirycalculator")
  @Produces(MediaType.APPLICATION_JSON)
  public String getExpiryCalculators() {
    final SortedSet<String> sorted = new TreeSet<>(ExchangeTradedInstrumentExpiryCalculatorFactory.INSTANCE.instanceMap().keySet());
    final List<String> results = new ArrayList<>();
    for (final String item : sorted) {
      final String displayName;
      if (item.startsWith("IMM")) {
        displayName = "IMM " + item.substring(3, item.indexOf("ExpiryCalculator")).replaceAll("(.)([A-Z0-9]\\w)", "$1 $2");
      } else if (item.startsWith("IR")) {
        displayName = "IR " + item.substring(2, item.indexOf("ExpiryCalculator")).replaceAll("(.)([A-Z0-9]\\w)", "$1 $2");
      } else if (item.startsWith("VIX")) {
        displayName = "VIX " + item.substring(3, item.indexOf("ExpiryCalculator")).replaceAll("(.)([A-Z0-9]\\w)", "$1 $2");
      } else {
        displayName = item.substring(0, item.indexOf("ExpiryCalculator")).replaceAll("(.)([A-Z]\\w)", "$1 $2");
      }
      results.add(item + "|" + displayName);
    }
    return new JSONArray(results).toString();
  }

  private static String convertEnumToJsonArray(final List<? extends Enum<?>> input, final Comparator<Enum<?>> comparator) {
    Collections.sort(input, comparator);
    final List<String> results = new ArrayList<>();
    for (final Enum<?> item : input) {
      results.add(item.name() + "|" + WordUtils.capitalize(item.name().toLowerCase().replace('_', ' ')));
    }
    return new JSONArray(results).toString();
  }

  private static final class EnumComparator implements Comparator<Enum<?>> {
    public static final Comparator<Enum<?>> INSTANCE = new EnumComparator();

    @Override
    public int compare(final Enum<?> arg0, final Enum<?> arg1) {
      return arg0.name().compareTo(arg1.name());
    }

    private EnumComparator() {
    }
  }

}
