/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.future;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.util.Quandl4OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Unit tests for {@link QuandlFutureCurveInstrumentProvider}.
 */
@Test(groups = TestGroup.UNIT)
public class QuandlFedFundsFutureCurveInstrumentProviderTest {
  /** The future prefix */
  private static final String PREFIX = "FF";
  /** The data field */
  private static final String DATA_FIELD = MarketDataRequirementNames.MARKET_VALUE;
  /** The field type */
  private static final DataFieldType FIELD_TYPE = DataFieldType.OUTRIGHT;
  /** The underlying id */
  private static final ExternalId UNDERLYING_ID = QuandlConstants.ofCode("ON");
  /** The underlying data field */
  private static final String UNDERLYING_DATA_FIELD = MarketDataRequirementNames.LAST;
  /** The provider */
  private static final QuandlFedFundsFutureCurveInstrumentProvider PROVIDER = new QuandlFedFundsFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, FIELD_TYPE,
      UNDERLYING_ID, UNDERLYING_DATA_FIELD);

  /**
   * Tests the behavior when the underlying id is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullPrefix() {
    new QuandlFedFundsFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, FIELD_TYPE, null, UNDERLYING_DATA_FIELD);
  }

  /**
   * Tests the behaviour when the underlying data field is null.
   */
  @Test(expectedExceptions = Quandl4OpenGammaRuntimeException.class)
  public void testNullUnderlyingDataField() {
    new QuandlFedFundsFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, FIELD_TYPE, UNDERLYING_ID, null);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(PROVIDER.getFuturePrefix(), PREFIX);
    assertEquals(PROVIDER.getMarketDataField(), DATA_FIELD);
    assertEquals(PROVIDER.getDataFieldType(), FIELD_TYPE);
    assertEquals(PROVIDER.getUnderlyingDataField(), UNDERLYING_DATA_FIELD);
    assertEquals(PROVIDER.getUnderlyingId(), UNDERLYING_ID);
    QuandlFedFundsFutureCurveInstrumentProvider other = new QuandlFedFundsFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, FIELD_TYPE,
        UNDERLYING_ID, UNDERLYING_DATA_FIELD);
    assertEquals(PROVIDER, PROVIDER);
    assertEquals(other, PROVIDER);
    assertEquals(other.hashCode(), PROVIDER.hashCode());
    assertNotEquals(null, other);
    assertNotEquals(new BloombergFutureCurveInstrumentProvider(PREFIX, DATA_FIELD), other);
    other = new QuandlFedFundsFutureCurveInstrumentProvider(PREFIX + "A", DATA_FIELD, FIELD_TYPE, UNDERLYING_ID, UNDERLYING_DATA_FIELD);
    assertNotEquals(PROVIDER, other);
    other = new QuandlFedFundsFutureCurveInstrumentProvider(PREFIX, DATA_FIELD + "A", FIELD_TYPE, UNDERLYING_ID, UNDERLYING_DATA_FIELD);
    assertNotEquals(PROVIDER, other);
    other = new QuandlFedFundsFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, FIELD_TYPE, QuandlConstants.ofCode("TEST"), UNDERLYING_DATA_FIELD);
    assertNotEquals(PROVIDER, other);
    other = new QuandlFedFundsFutureCurveInstrumentProvider(PREFIX, DATA_FIELD, FIELD_TYPE, UNDERLYING_ID, DATA_FIELD);
    assertNotEquals(PROVIDER, other);
  }

  /**
   * Tests the generated codes.
   */
  @Test
  public void testGeneratedCode() {
    final LocalDate date = LocalDate.of(2015, 1, 1);
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 1), QuandlConstants.ofCode("FFH2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 2), QuandlConstants.ofCode("FFM2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 3), QuandlConstants.ofCode("FFU2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 4), QuandlConstants.ofCode("FFZ2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.THREE_MONTHS, 5), QuandlConstants.ofCode("FFH2016"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 1), QuandlConstants.ofCode("FFF2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 2), QuandlConstants.ofCode("FFG2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 3), QuandlConstants.ofCode("FFH2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 4), QuandlConstants.ofCode("FFJ2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 5), QuandlConstants.ofCode("FFK2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 6), QuandlConstants.ofCode("FFM2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 7), QuandlConstants.ofCode("FFN2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 8), QuandlConstants.ofCode("FFQ2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 9), QuandlConstants.ofCode("FFU2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 10), QuandlConstants.ofCode("FFV2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 11), QuandlConstants.ofCode("FFX2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 12), QuandlConstants.ofCode("FFZ2015"));
    assertEquals(PROVIDER.getInstrument(date, Tenor.ONE_MONTH, 13), QuandlConstants.ofCode("FFF2016"));
    Tenor offsetTenor = Tenor.TWO_MONTHS;
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 1), QuandlConstants.ofCode("FFH2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 2), QuandlConstants.ofCode("FFM2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 3), QuandlConstants.ofCode("FFU2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 4), QuandlConstants.ofCode("FFZ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 5), QuandlConstants.ofCode("FFH2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 1), QuandlConstants.ofCode("FFH2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 2), QuandlConstants.ofCode("FFJ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 3), QuandlConstants.ofCode("FFK2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 4), QuandlConstants.ofCode("FFM2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 5), QuandlConstants.ofCode("FFN2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 6), QuandlConstants.ofCode("FFQ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 7), QuandlConstants.ofCode("FFU2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 8), QuandlConstants.ofCode("FFV2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 9), QuandlConstants.ofCode("FFX2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 10), QuandlConstants.ofCode("FFZ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 11), QuandlConstants.ofCode("FFF2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 12), QuandlConstants.ofCode("FFG2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.ONE_MONTH, 13), QuandlConstants.ofCode("FFH2016"));
    offsetTenor = Tenor.SIX_MONTHS;
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 1), QuandlConstants.ofCode("FFU2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 2), QuandlConstants.ofCode("FFZ2015"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 3), QuandlConstants.ofCode("FFH2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 4), QuandlConstants.ofCode("FFM2016"));
    assertEquals(PROVIDER.getInstrument(date, offsetTenor, Tenor.THREE_MONTHS, 5), QuandlConstants.ofCode("FFU2016"));
  }
}
