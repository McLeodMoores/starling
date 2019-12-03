/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.conversion;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Tests {@link DoubleValueSignificantFiguresFormatter}
 */
@Test(groups = TestGroup.UNIT)
public class PercentageValueSignificantFiguresFormatterTest {

  /**
   * Tests a 3 s.f. converter.
   */
  @Test
  public void test3SF() {
    final PercentageValueSignificantFiguresFormatter formatter = new PercentageValueSignificantFiguresFormatter(3, false);
    assertEquals("-123,412%", format(formatter, -1234.123));
    assertEquals("-123%", format(formatter, -1.22678));
    assertEquals("0.0%", format(formatter, 0));
    assertEquals("0.000123%", format(formatter, 0.00000123456));
    assertEquals("12,346%", format(formatter, 123.456));
    assertEquals("12,345,679%", format(formatter, 123456.789));
  }

  /**
   * Tests that the locale is used.
   */
  @Test
  public void testLocale() {
    PercentageValueSignificantFiguresFormatter formatter = new PercentageValueSignificantFiguresFormatter(2, false,
        DecimalFormatSymbols.getInstance(Locale.GERMAN));
    assertEquals("-123.443%", format(formatter, -1234.432));
    assertEquals("-1.212%", format(formatter, -12.123));
    assertEquals("0,0%", format(formatter, 0));
    assertEquals("12%", format(formatter, 0.12));
    assertEquals("123.356%", format(formatter, 1233.56));

    formatter = new PercentageValueSignificantFiguresFormatter(5, false, DecimalFormatSymbols.getInstance(Locale.FRENCH));
    final String nbsp = "\u00A0";
    assertEquals("123" + nbsp + "446%", format(formatter, 1234.4567));
  }

  private static String format(final DoubleValueFormatter formatter, final double number) {
    return formatter.format(BigDecimal.valueOf(number));
  }

}
