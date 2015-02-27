/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * based on APLv2 code Copyright(C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.time;

import java.util.Locale;

import org.joda.convert.FromString;

import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for cube quote type named instances.
 */
public final class TenorFactory extends AbstractNamedInstanceFactory<Tenor> {

  /**
   * Singleton instance of {@code CubeQuoteTypeFactory}.
   */
  public static final TenorFactory INSTANCE = new TenorFactory();

  /**
   * Restricted constructor.
   */
  private TenorFactory() {
    super(Tenor.class);
    addInstance(Tenor.TN, "TN", "T/N");
    addInstance(Tenor.ON, "ON", "O/N");
    addInstance(Tenor.SN, "SN", "S/N");
    addInstance(Tenor.ofDays(1), "P1D", "1D");
    addInstance(Tenor.ofDays(2), "P2D", "2D");
    addInstance(Tenor.ofDays(3), "P3D", "3D");
    addInstance(Tenor.ofDays(4), "P4D", "4D");
    addInstance(Tenor.ofDays(5), "P5D", "5D");
    addInstance(Tenor.ofDays(6), "P6D", "6D");
    addInstance(Tenor.ofDays(7), "P7D", "7D");
    addInstance(Tenor.ofWeeks(1), "P1W", "1W");
    addInstance(Tenor.ofWeeks(2), "P2W", "2W");
    addInstance(Tenor.ofWeeks(3), "P3W", "3W");
    addInstance(Tenor.ofWeeks(4), "P4W", "4W");
    addInstance(Tenor.ofMonths(1), "P1M", "1M");
    addInstance(Tenor.ofWeeks(6), "P6W", "6W");
    addInstance(Tenor.ofMonths(2), "P2M", "2M");
    addInstance(Tenor.ofMonths(3), "P3M", "3M");
    addInstance(Tenor.ofMonths(4), "P4M", "4M");
    addInstance(Tenor.ofMonths(5), "P5M", "5M");
    addInstance(Tenor.ofMonths(6), "P6M", "6M");
    addInstance(Tenor.ofMonths(7), "P7M", "7M");
    addInstance(Tenor.ofMonths(8), "P8M", "8M");
    addInstance(Tenor.ofMonths(9), "P9M", "9M");
    addInstance(Tenor.ofMonths(10), "P10M", "10M");
    addInstance(Tenor.ofMonths(11), "P11M", "11M");
    addInstance(Tenor.ofMonths(12), "P12M", "12M");
    addInstance(Tenor.ofYears(1), "P1Y", "1Y");
    addInstance(Tenor.ofMonths(15), "P15M", "15M");
    addInstance(Tenor.ofMonths(18), "P18M", "18M");
    addInstance(Tenor.ofMonths(21), "P21M", "21M");
    addInstance(Tenor.ofYears(2), "P2Y", "2Y");
    addInstance(Tenor.ofMonths(30), "P30M", "30M");
    addInstance(Tenor.ofYears(3), "P3Y", "3Y");
    addInstance(Tenor.ofYears(4), "P4Y", "4Y");
    addInstance(Tenor.ofYears(5), "P5Y", "5Y");
    addInstance(Tenor.ofYears(6), "P6Y", "6Y");
    addInstance(Tenor.ofYears(7), "P7Y", "7Y");
    addInstance(Tenor.ofYears(8), "P8Y", "8Y");
    addInstance(Tenor.ofYears(9), "P9Y", "9Y");
    addInstance(Tenor.ofYears(10), "P10Y", "10Y");    
    addInstance(Tenor.ofYears(11), "P11Y", "11Y");    
    addInstance(Tenor.ofYears(12), "P12Y", "12Y");    
    addInstance(Tenor.ofYears(15), "P15Y", "15Y");    
    addInstance(Tenor.ofYears(20), "P20Y", "20Y");    
    addInstance(Tenor.ofYears(25), "P25Y", "25Y");    
    addInstance(Tenor.ofYears(30), "P30Y", "30Y");    
    addInstance(Tenor.ofYears(35), "P35Y", "35Y");    
    addInstance(Tenor.ofYears(40), "P40Y", "40Y");    
    addInstance(Tenor.ofYears(45), "P45Y", "45Y");    
    addInstance(Tenor.ofYears(50), "P50Y", "50Y");    
    addInstance(Tenor.ofYears(60), "P60Y", "60Y");    
  }

  /**
   * Finds a cube quote type by name, ignoring case.
   * <p>
   * This method dynamically creates the quote type if it is missing.
   * @param name The name of the instance to find, not null
   * @return The cube quote type, null if not found
   */
  @FromString
  public Tenor of(final String name) {
    try {
      return INSTANCE.instance(name);
    } catch (final IllegalArgumentException e) {
      ArgumentChecker.notNull(name, "name");
      final Tenor tenor = Tenor.parse(name.toUpperCase(Locale.ENGLISH));
      return addInstance(tenor);
    }
  }
}