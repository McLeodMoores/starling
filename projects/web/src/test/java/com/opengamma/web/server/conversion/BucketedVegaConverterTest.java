/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.server.conversion;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.financial.greeks.BucketedGreekResultCollection;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link BucketedVegaConverter}.
 */
@Test(groups = TestGroup.UNIT)
public class BucketedVegaConverterTest {
  private static final double[] EXPIRIES = new double[] { 1, 2, 3 };
  private static final double[][] STRIKES = new double[][] { new double[] { 100, 110, 120 }, new double[] { 100, 110, 120 }, new double[] { 100, 110, 120 } };
  private static final BucketedGreekResultCollection VEGA = new BucketedGreekResultCollection(EXPIRIES, STRIKES);
  private static final ResultConverterCache CONVERTERS = new ResultConverterCache(OpenGammaFudgeContext.getInstance());
  private static final ValueSpecification SPEC = new ValueSpecification("vega", ComputationTargetSpecification.of(Currency.USD),
      ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "dummy").get());
  private static final BucketedVegaConverter CONVERTER = new BucketedVegaConverter();

  /**
   * Tests the text.
   */
  public void testText() {
    assertEquals(CONVERTER.convertToText(CONVERTERS, SPEC, VEGA), "Bucketed Vega");
  }

  /**
   * Tests the formatter name.
   */
  public void testName() {
    assertEquals(CONVERTER.getFormatterName(), "SURFACE_DATA");
  }

  /**
   * Tests that no history is available.
   */
  public void testHistory() {
    assertNull(CONVERTER.convertForHistory(CONVERTERS, SPEC, VEGA));
  }

  /**
   * Tests empty data.
   */
  public void testEmpty() {
    final Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, VEGA, ConversionMode.SUMMARY);
    assertTrue(converted instanceof Map);
    final Map<?, ?> data = (Map<?, ?>) converted;
    assertTrue(data.isEmpty());
  }

  /**
   * Tests the summary.
   */
  public void testSummary() {
    final BucketedGreekResultCollection vega = new BucketedGreekResultCollection(EXPIRIES, STRIKES);
    vega.put(BucketedGreekResultCollection.BUCKETED_VEGA,
        new double[][] { new double[] { 0.1, 0.2, 0.3 }, new double[] { 0.4, 0.5, 0.6 }, new double[] { 0.7, 0.8, 0.9 } });
    final Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, vega, ConversionMode.SUMMARY);
    assertTrue(converted instanceof Map);
    @SuppressWarnings("unchecked")
    final Map<String, ?> data = (Map<String, ?>) converted;
    assertEquals(data.size(), 2);
    assertEquals(data.get("xCount"), 3);
    assertEquals(data.get("yCount"), 3);
  }

  /**
   * Tests full conversion.
   */
  public void testFull() {
    final BucketedGreekResultCollection vega = new BucketedGreekResultCollection(EXPIRIES, STRIKES);
    vega.put(BucketedGreekResultCollection.BUCKETED_VEGA,
        new double[][] { new double[] { 0.1, 0.2, 0.3 }, new double[] { 0.4, 0.5, 0.6 }, new double[] { 0.7, 0.8, 0.9 } });
    final Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, vega, ConversionMode.FULL);
    assertTrue(converted instanceof Map);
    @SuppressWarnings("unchecked")
    final Map<String, Object[]> data = (Map<String, Object[]>) converted;
    assertEquals(data.size(), 6);
    assertEquals(data.get("xCount"), 3);
    assertEquals(data.get("yCount"), 3);
    assertArrayEquals(data.get("xs"), new Object[] { "100", "110", "120" });
    assertArrayEquals(data.get("ys"), new Object[] { "1", "2", "3" });
    assertArrayEquals(data.get("surface"),
        new Object[] { new Object[] { 0.1, 0.2, 0.3 }, new Object[] { 0.4, 0.5, 0.6 }, new Object[] { 0.7, 0.8, 0.9 } });
    assertArrayEquals(data.get("missingValues"),
        new Object[] { new Object[] { false, false, false }, new Object[] { false, false, false }, new Object[] { false, false, false } });
  }
}
