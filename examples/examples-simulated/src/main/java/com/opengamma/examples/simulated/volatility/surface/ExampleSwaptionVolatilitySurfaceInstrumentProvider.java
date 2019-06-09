/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.examples.simulated.volatility.surface;

import org.apache.commons.lang.Validate;
import org.threeten.bp.LocalDate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.volatility.surface.SurfaceInstrumentProvider;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.Tenor;

/**
 * Generates Example codes for swaption volatilities given tenors.
 */
public class ExampleSwaptionVolatilitySurfaceInstrumentProvider implements SurfaceInstrumentProvider<Tenor, Tenor> {
  private final String _countryPrefix;
  private final String _typePrefix;
  private final String _postfix;
  private final boolean _zeroPadSwapMaturityTenor;
  private final boolean _zeroPadSwaptionExpiryTenor;
  private final String _dataFieldName; // expecting MarketDataRequirementNames.MARKET_VALUE or PX_LAST
  private static final String[] MONTHS_TABLE = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "1A", "1B", "1C",
                "1D", "1E", "1F", "1G", "1H", "1I", "1J", "1K", "1L" };

  public ExampleSwaptionVolatilitySurfaceInstrumentProvider(final String countryPrefix, final String typePrefix, final boolean zeroPadSwapMaturityTenor,
      final boolean zeroPadSwaptionExpiryTenor,
      final String postfix) {
    this(countryPrefix, typePrefix, zeroPadSwapMaturityTenor, zeroPadSwaptionExpiryTenor, postfix, MarketDataRequirementNames.MARKET_VALUE);
  }

  public ExampleSwaptionVolatilitySurfaceInstrumentProvider(final String countryPrefix, final String typePrefix, final boolean zeroPadSwapMaturityTenor,
      final boolean zeroPadSwaptionExpiryTenor,
      final String postfix, final String dataFieldName) {
    Validate.notNull(countryPrefix);
    Validate.notNull(typePrefix);
    Validate.notNull(postfix);
    _countryPrefix = countryPrefix;
    _typePrefix = typePrefix;
    _zeroPadSwapMaturityTenor = zeroPadSwapMaturityTenor;
    _zeroPadSwaptionExpiryTenor = zeroPadSwaptionExpiryTenor;
    _postfix = postfix;
    _dataFieldName = dataFieldName;
  }

  @Override
  public ExternalId getInstrument(final Tenor swapMaturityTenor, final Tenor swaptionExpiryTenor) {
    final StringBuffer ticker = new StringBuffer();
    ticker.append(_countryPrefix);
    ticker.append(_typePrefix);
    ticker.append(tenorToCode(swaptionExpiryTenor, _zeroPadSwaptionExpiryTenor));
    ticker.append(tenorToCode(swapMaturityTenor, _zeroPadSwapMaturityTenor));
    ticker.append(_postfix);
    return ExternalId.of(ExternalSchemes.OG_SYNTHETIC_TICKER, ticker.toString());
  }

  @Override
  public ExternalId getInstrument(final Tenor startTenor, final Tenor maturity, final LocalDate surfaceDate) {
    return getInstrument(startTenor, maturity);
  }

  private String tenorToCode(final Tenor tenor, final boolean prepadWithZero) {
    if (tenor.getPeriod().getYears() == 0) {
      final int months = tenor.getPeriod().getMonths();
      if (months > 0) {

        final String result = MONTHS_TABLE[months - 1];
        if (result.length() == 1 && prepadWithZero) {
          return "0" + result;
        }
        return result;
      }
      throw new OpenGammaRuntimeException("Cannot generate encoding for tenor " + tenor);
    }
    if (tenor.getPeriod().getYears() < 10 && prepadWithZero) {
      return "0" + Integer.toString(tenor.getPeriod().getYears());
    }
    return Integer.toString(tenor.getPeriod().getYears());
  }

  /**
   * Gets the countryPrefix field.
   *
   * @return the countryPrefix
   */
  public String getCountryPrefix() {
    return _countryPrefix;
  }

  /**
   * Gets the typePrefix field.
   *
   * @return the typePrefix
   */
  public String getTypePrefix() {
    return _typePrefix;
  }

  /**
   * Gets the postfix field.
   *
   * @return the postfix
   */
  public String getPostfix() {
    return _postfix;
  }

  /**
   * Gets the zeroPadSwaptionExpiryTenor field.
   *
   * @return the zeroPadSwaptionExpiryTenor
   */
  public boolean isZeroPadSwapMaturityTenor() {
    return _zeroPadSwaptionExpiryTenor;
  }

  /**
   * Gets the zeroPadSwaptionExpiryTenor field.
   *
   * @return the zeroPadSwaptionExpiryTenor
   */
  public boolean isZeroPadSwaptionExpiryTenor() {
    return _zeroPadSwaptionExpiryTenor;
  }

  /**
   * @return The data field name - should be PX_LAST
   */
  @Override
  public String getDataFieldName() {
    return _dataFieldName;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof ExampleSwaptionVolatilitySurfaceInstrumentProvider)) {
      return false;
    }
    final ExampleSwaptionVolatilitySurfaceInstrumentProvider other = (ExampleSwaptionVolatilitySurfaceInstrumentProvider) o;
    // we can avoid using ObjectUtil.equals because we validated the strings as not null.
    return getCountryPrefix().equals(other.getCountryPrefix()) &&
        getPostfix().equals(other.getPostfix()) &&
        getTypePrefix().equals(other.getTypePrefix()) &&
        isZeroPadSwapMaturityTenor() == other.isZeroPadSwapMaturityTenor() &&
        isZeroPadSwaptionExpiryTenor() == other.isZeroPadSwaptionExpiryTenor() &&
        getDataFieldName().equals(other.getDataFieldName());
  }

  @Override
  public int hashCode() {
    return getCountryPrefix().hashCode() + getTypePrefix().hashCode() + getPostfix().hashCode() + getDataFieldName().hashCode();
  }
}
