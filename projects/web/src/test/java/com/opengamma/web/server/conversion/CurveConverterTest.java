/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.server.conversion;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.curve.NodalDoublesCurve;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link CurveConverter}.
 */
@Test(groups = TestGroup.UNIT)
public class CurveConverterTest {
  private static final ResultConverterCache CONVERTERS = new ResultConverterCache(OpenGammaFudgeContext.getInstance());
  private static final ValueSpecification SPEC = new ValueSpecification("curve", ComputationTargetSpecification.of(Currency.USD),
      ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "dummy").get());
  private static final CurveConverter CONVERTER = new CurveConverter();
  private static final NodalDoublesCurve CURVE = NodalDoublesCurve.from(new double[] { 1, 2 }, new double[] { 0.6, 0.7 });

  /**
   * Tests the text for a nodal curve.
   */
  public void testTextNodal() {
    assertEquals(CONVERTER.convertToText(CONVERTERS, SPEC, CURVE), "NodalDoublesCurve");
  }

  /**
   * Tests the text for an interpolated curve.
   */
  public void testTextInterpolated() {
    final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(new double[] { 1, 2 }, new double[] { 0.9, 0.7 },
        NamedInterpolator1dFactory.of("Linear"));
    final String converted = CONVERTER.convertToText(CONVERTERS, SPEC, curve);
    assertEquals(converted, "1.0=0.9; 2.0=0.7");
  }

  /**
   * Tests the formatter name.
   */
  public void testName() {
    assertEquals(CONVERTER.getFormatterName(), "CURVE");
  }

  /**
   * Tests that no history is available.
   */
  public void testHistory() {
    assertNull(CONVERTER.convertForHistory(CONVERTERS, SPEC, CURVE));
  }

  /**
   * Tests the summary.
   */
  @SuppressWarnings("unchecked")
  public void testSummaryNodal() {
    final Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, CURVE, ConversionMode.SUMMARY);
    assertTrue(converted instanceof Map);
    final Map<String, ?> data = (Map<String, ?>) converted;
    assertEquals(data.size(), 1);
    final Object summary = data.get("summary");
    assertTrue(summary instanceof List);
    final List<Double[]> list = (List<Double[]>) summary;
    assertEquals(list.size(), CURVE.size());
  }

  /**
   * Tests full conversion.
   */
  @SuppressWarnings("unchecked")
  public void testFullNodal() {
    final Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, CURVE, ConversionMode.FULL);
    assertTrue(converted instanceof Map);
    final Map<String, ?> data = (Map<String, ?>) converted;
    assertEquals(data.size(), 2);
    final Object summary = data.get("summary");
    final Object detailed = data.get("detailed");
    assertTrue(summary instanceof List);
    assertTrue(detailed instanceof List);
    assertEquals(((List<Double[]>) summary).size(), CURVE.size());
    assertEquals(((List<Double[]>) detailed).size(), CURVE.size());
  }

  /**
   * Tests the summary.
   */
  @SuppressWarnings("unchecked")
  public void testSummaryInterpolated() {
    final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(new double[] { 1, 2 }, new double[] { 0.9, 0.7 },
        NamedInterpolator1dFactory.of("Linear"));
    final Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, curve, ConversionMode.SUMMARY);
    assertTrue(converted instanceof Map);
    final Map<String, ?> data = (Map<String, ?>) converted;
    assertEquals(data.size(), 1);
    final Object summary = data.get("summary");
    assertTrue(summary instanceof List);
    final List<Double[]> list = (List<Double[]>) summary;
    assertEquals(list.size(), CURVE.size());
  }

  /**
   * Tests full conversion.
   */
  @SuppressWarnings("unchecked")
  public void testFullInterpolated() {
    final InterpolatedDoublesCurve curve = InterpolatedDoublesCurve.from(new double[] { 1, 2 }, new double[] { 0.9, 0.7 },
        NamedInterpolator1dFactory.of("Linear"));
    final Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, curve, ConversionMode.FULL);
    assertTrue(converted instanceof Map);
    final Map<String, ?> data = (Map<String, ?>) converted;
    assertEquals(data.size(), 2);
    final Object summary = data.get("summary");
    final Object detailed = data.get("detailed");
    assertTrue(summary instanceof List);
    assertTrue(detailed instanceof List);
    assertEquals(((List<Double[]>) summary).size(), CURVE.size());
    assertEquals(((List<Double[]>) detailed).size(), 100);
  }

  /**
   * Tests that the curve must be interpolated or nodal.
   */
  @SuppressWarnings("unchecked")
  public void testCurveType() {
    final ConstantDoublesCurve curve = ConstantDoublesCurve.from(0.2);
    Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, curve, ConversionMode.SUMMARY);
    assertTrue(converted instanceof Map);
    Map<String, ?> data = (Map<String, ?>) converted;
    assertEquals(data.size(), 1);
    assertTrue(data.get("summary") instanceof String);
    converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, curve, ConversionMode.FULL);
    assertTrue(converted instanceof Map);
    data = (Map<String, ?>) converted;
    assertEquals(data.size(), 1);
    assertTrue(data.get("summary") instanceof String);
  }
}
