/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.fail;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.future.QuandlFutureCurveInstrumentProvider;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.IndexType;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link QuandlFutureCurveInstrumentProvider}.
 */
@Test
public class QuandlFutureCurveInstrumentProviderTest {
  /** The future prefix */
  private static final String PREFIX = "ED";
  /** The data field */
  private static final String DATA_FIELD = MarketDataRequirementNames.MARKET_VALUE;
  /** The field type */
  private static final DataFieldType FIELD_TYPE = DataFieldType.OUTRIGHT;
  /** The provider */
  private static final QuandlFutureCurveInstrumentProvider PROVIDER = new QuandlFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, FIELD_TYPE);

  /**
   * Tests the behavior when the prefix is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullPrefix() {
    new QuandlFutureCurveInstrumentProvider(null, DATA_FIELD, FIELD_TYPE);
  }

  /**
   * Tests the behaviour when the data field is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullDataField() {
    new QuandlFutureCurveInstrumentProvider(PREFIX, null, FIELD_TYPE);
  }

  /**
   * Tests the behaviour when the field type is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullFieldType() {
    new QuandlFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, null);
  }

  /**
   * Tests unsupported methods.
   */
  @Test
  public void testUnsupportedMethods() {
    final LocalDate date = LocalDate.of(2015, 1, 1);
    try {
      PROVIDER.getInstrument(date, Tenor.THREE_MONTHS);
      fail();
    } catch (final UnsupportedOperationException e) {
      // expected
    }
    try {
      PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 1, 2);
      fail();
    } catch (final UnsupportedOperationException e) {
      // expected
    }
    try {
      PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 4, true);
      fail();
    } catch (final UnsupportedOperationException e) {
      // expected
    }
    try {
      PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, IndexType.BBSW);
      fail();
    } catch (final UnsupportedOperationException e) {
      // expected
    }
    try {
      PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, Tenor.ONE_MONTH, Tenor.TWO_MONTHS, IndexType.BBSW, IndexType.Euribor);
      fail();
    } catch (final UnsupportedOperationException e) {
      // expected
    }
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(PROVIDER.getFuturePrefix(), PREFIX);
    assertEquals(PROVIDER.getMarketDataField(), DATA_FIELD);
    assertEquals(PROVIDER.getDataFieldType(), FIELD_TYPE);
    QuandlFutureCurveInstrumentProvider other = new QuandlFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, FIELD_TYPE);
    assertEquals(PROVIDER, PROVIDER);
    assertEquals(other, PROVIDER);
    assertEquals(other.hashCode(), PROVIDER.hashCode());
    assertNotEquals(null, other);
    assertNotEquals(new BloombergFutureCurveInstrumentProvider(PREFIX, DATA_FIELD), other);
    other = new QuandlFutureCurveInstrumentProvider(PREFIX + "A", DATA_FIELD, FIELD_TYPE);
    assertNotEquals(PROVIDER, other);
    other = new QuandlFutureCurveInstrumentProvider(PREFIX, DATA_FIELD + "A", FIELD_TYPE);
    assertNotEquals(PROVIDER, other);
    other = new QuandlFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, DataFieldType.POINTS);
    assertNotEquals(PROVIDER, other);
  }

  /**
   * Tests the generated codes.
   */
  @Test
  public void testGeneratedCode() {
    final LocalDate date = LocalDate.of(2015, 1, 1);
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 1), QuandlConstants.ofCode("EDH2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 2), QuandlConstants.ofCode("EDM2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 3), QuandlConstants.ofCode("EDU2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 4), QuandlConstants.ofCode("EDZ2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 5), QuandlConstants.ofCode("EDH2016"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 1), QuandlConstants.ofCode("EDF2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 2), QuandlConstants.ofCode("EDG2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 3), QuandlConstants.ofCode("EDH2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 4), QuandlConstants.ofCode("EDJ2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 5), QuandlConstants.ofCode("EDK2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 6), QuandlConstants.ofCode("EDM2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 7), QuandlConstants.ofCode("EDN2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 8), QuandlConstants.ofCode("EDQ2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 9), QuandlConstants.ofCode("EDU2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 10), QuandlConstants.ofCode("EDV2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 11), QuandlConstants.ofCode("EDX2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 12), QuandlConstants.ofCode("EDZ2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 13), QuandlConstants.ofCode("EDF2016"));
    Tenor offsetTenor = Tenor.TWO_MONTHS;
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 1), QuandlConstants.ofCode("EDH2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 2), QuandlConstants.ofCode("EDM2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 3), QuandlConstants.ofCode("EDU2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 4), QuandlConstants.ofCode("EDZ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 5), QuandlConstants.ofCode("EDH2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 1), QuandlConstants.ofCode("EDH2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 2), QuandlConstants.ofCode("EDJ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 3), QuandlConstants.ofCode("EDK2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 4), QuandlConstants.ofCode("EDM2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 5), QuandlConstants.ofCode("EDN2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 6), QuandlConstants.ofCode("EDQ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 7), QuandlConstants.ofCode("EDU2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 8), QuandlConstants.ofCode("EDV2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 9), QuandlConstants.ofCode("EDX2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 10), QuandlConstants.ofCode("EDZ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 11), QuandlConstants.ofCode("EDF2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 12), QuandlConstants.ofCode("EDG2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 13), QuandlConstants.ofCode("EDH2016"));
    offsetTenor = Tenor.SIX_MONTHS;
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 1), QuandlConstants.ofCode("EDU2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 2), QuandlConstants.ofCode("EDZ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 3), QuandlConstants.ofCode("EDH2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 4), QuandlConstants.ofCode("EDM2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 5), QuandlConstants.ofCode("EDU2016"));
  }
}
