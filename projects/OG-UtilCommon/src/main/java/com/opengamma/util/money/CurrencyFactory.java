/**
 * Copyright (C) 2015 - present by McLeod Moores Software Limited.
 * based on APLv2 code Copyright(C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies.
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import java.util.Locale;

import org.joda.convert.FromString;

import com.opengamma.util.AbstractNamedInstanceFactory;
import com.opengamma.util.ArgumentChecker;

/**
 * Factory for {@link Currency} named instances.
 */
//TODO this isn't an exhausive list - https://en.wikipedia.org/wiki/ISO_4217
// plus it should be possible to easily add a currency and have it stored somewhere
public final class CurrencyFactory extends AbstractNamedInstanceFactory<Currency> {

  /**
   * Singleton instance of {@code CurrencyFactory}.
   */
  public static final CurrencyFactory INSTANCE = new CurrencyFactory();

  /**
   * Restricted constructor.
   */
  private CurrencyFactory() {
    super(Currency.class);
    addInstance(Currency.USD, "USD");
    addInstance(Currency.EUR, "EUR");
    addInstance(Currency.JPY, "JPY");
    addInstance(Currency.GBP, "GBP");
    addInstance(Currency.CHF, "CHF");
    addInstance(Currency.AUD, "AUD");
    addInstance(Currency.CAD, "CAD");
    addInstance(Currency.NZD, "NZD");
    addInstance(Currency.DKK, "DKK");
    addInstance(Currency.DEM, "DEM");
    addInstance(Currency.CZK, "CZK");
    addInstance(Currency.SEK, "SEK");
    addInstance(Currency.SKK, "SKK");
    addInstance(Currency.HUF, "HUF");
    addInstance(Currency.NOK, "NOK");
    addInstance(Currency.HKD, "HKD");
    addInstance(Currency.BRL, "BRL");
    addInstance(Currency.ZAR, "ZAR");
    addInstance(Currency.PLN, "PLN");
    addInstance(Currency.SGD, "SGD");
    addInstance(Currency.MXN, "MXN");
    addInstance(Currency.ITL, "ITL");
    addInstance(Currency.FRF, "FRF");
    addInstance(Currency.DEM, "DEM");
    addInstance(Currency.parse("GRD"), "GRD");
    addInstance(Currency.parse("GRN"), "GRN");
    final String[] ccys = new String[] {"AED", "AFN", "ALL", "AMD", "ANG", "AOA", "ARS", "AUD", "AWG", "AZN", "BAM", "BBD", "BDT", "BGN", "BHD", "BIF", "BMD", "BND", "BOB", "BOV", "BRL",
        "BSD", "BTN", "BWP", "BYR", "BZD", "CAD", "CDF", "CHE", "CHF", "CHW", "CLF", "CLP", "CNY", "COP", "COU", "CRC", "CUC", "CUP", "CVE", "CZK", "DJF", "DKK", "DOP", "DZD", "EGP", "ERN", "ETB", "EUR",
        "FJD", "FKP", "GBP", "GEL", "GHS", "GIP", "GMD", "GNF", "GTQ", "GYD", "HKD", "HNL", "HRK", "HTG", "HUF", "IDR", "ILS", "INR", "IQD", "IRR", "ISK", "JMD", "JOD", "JPY", "KES", "KGS", "KHR", "KMF",
        "KPW", "KRW", "KWD", "KYD", "KZT", "LAK", "LBP", "LKR", "LRD", "LSL", "LYD", "MAD", "MDL", "MGA", "MKD", "MMK", "MNT", "MOP", "MRO", "MUR", "MVR", "MWK", "MXN", "MXV", "MYR", "MZN", "NAD", "NGN",
        "NIO", "NOK", "NPR", "NZD", "OMR", "PAB", "PEN", "PGK", "PHP", "PKR", "PLN", "PYG", "QAR", "RON", "RSD", "RUB", "RWF", "SAR", "SBD", "SCR", "SDG", "SEK", "SGD", "SHP", "SLL", "SOS", "SRD", "SSP",
        "STD", "SVC", "SYP", "SZL", "THB", "TJS", "TMT", "TND", "TOP", "TRY", "TTD", "TWD", "TZS", "UAH", "UGX", "USD", "USN", "UYI", "UYU", "UZS", "VEF", "VND", "VUV", "WST", "XAF", "XAG", "XAU", "XBA",
        "XBB", "XBC", "XBD", "XCD", "XDR", "XOF", "XPD", "XPF", "XPT", "XSU", "XTS", "XUA", "XXX", "YER", "ZAR", "ZMW", "ZWL" };
    for (final String ccy : ccys) {
      addInstance(Currency.parse(ccy), ccy);
    }
    addInstance(Currency.parse("BTC"), "BTC");
    addInstance(Currency.parse("XBT"), "XBT");
  }

  /**
   * Finds a currency by name, ignoring case.
   * <p>
   * This method dynamically creates the currency if it is missing.
   * @param name  the name of the instance to find, not null
   * @return  the currency type, null if not found
   */
  @FromString
  public Currency of(final String name) {
    try {
      return INSTANCE.instance(name);
    } catch (final IllegalArgumentException e) {
      ArgumentChecker.notNull(name, "name");
      final Currency currency = Currency.parse(name.toUpperCase(Locale.ENGLISH));
      return addInstance(currency);
    }
  }
}