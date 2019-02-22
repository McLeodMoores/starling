/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.web.server.conversion;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Tests for {@link ListDoubleObjectArrayConverter}.
 */
@Test(groups = TestGroup.UNIT)
public class ListDoubleObjectArrayConverterTest {
  private static final ResultConverterCache CONVERTERS = new ResultConverterCache(OpenGammaFudgeContext.getInstance());
  private static final ValueSpecification SPEC = new ValueSpecification("list", ComputationTargetSpecification.of(Currency.USD),
      ValueProperties.builder().with(ValuePropertyNames.FUNCTION, "dummy").get());
  private static final ListDoubleObjectArrayConverter CONVERTER = new ListDoubleObjectArrayConverter();
  private static final List<Double[]> VALUES = Arrays.asList(new Double[] { 1., 2., 3. }, new Double[] { 4., 5., 6. });

  /**
   * Tests that no history is available.
   */
  public void testHistory() {
    assertNull(CONVERTER.convertForHistory(CONVERTERS, SPEC, VALUES));
  }

  /**
   * Tests conversion to text.
   */
  public void testText() {
    assertEquals(CONVERTER.convertToText(CONVERTERS, SPEC, VALUES), "Labelled Matrix 2D (2 x 3)");
  }

  /**
   * Tests the formatter name.
   */
  public void testFormatterName() {
    assertEquals(CONVERTER.getFormatterName(), "LABELLED_MATRIX_2D");
  }

  /**
   * Tests creation of the summary display.
   */
  public void testSummary() {
    final Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, VALUES, ConversionMode.SUMMARY);
    assertTrue(converted instanceof Map);
    @SuppressWarnings("unchecked")
    final Map<String, Map<String, Object>> map = (Map<String, Map<String, Object>>) converted;
    assertEquals(map.size(), 1);
    final Map<String, Object> summary = map.get("summary");
    assertEquals(summary.size(), 2);
    assertEquals(summary.get("rowCount"), 2);
    assertEquals(summary.get("colCount"), 3);
  }

  /**
   * Tests creation of the full display.
   */
  @SuppressWarnings("unchecked")
  public void testFull() {
    final Object converted = CONVERTER.convertForDisplay(CONVERTERS, SPEC, VALUES, ConversionMode.FULL);
    assertTrue(converted instanceof Map);
    final Map<String, Object> map = (Map<String, Object>) converted;
    assertEquals(map.size(), 4);
    assertEquals(map.get("x"), new String[] { "", "", "" });
    assertEquals(map.get("y"), new String[] { "", "" });
    assertArrayEquals((double[][]) map.get("matrix"), new double[][] { { 1., 2., 3. }, { 4., 5., 6. } });
    final Map<String, Object> summary = (Map<String, Object>) map.get("summary");
    assertEquals(summary.size(), 2);
    assertEquals(summary.get("rowCount"), 2);
    assertEquals(summary.get("colCount"), 3);
  }
}
