/**
 *
 */
package com.opengamma.financial.analytics.curve.upgrade;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import org.testng.annotations.Test;

import com.opengamma.util.result.Function2;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the default renaming function for {@link com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration}.
 */
@Test(groups = TestGroup.UNIT)
public class FixedCurrencyCsbcRenamingFunctionTest {
  /** The curve specification builder configuration name */
  private static final String NAME = "DEFAULT";
  /** The currency string */
  private static final String CCY = "ABC";

  /**
   * Tests that an {@link IllegalArgumentException} is thrown if the currency is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    new FixedCurrencyCsbcRenamingFunction(null);
  }

  /**
   * Tests renaming without an intermediate string.
   */
  @Test
  public static void testWithoutExtraInformation() {
    final Function2<String, String, String> f = new DefaultCsbcRenamingFunction();
    assertEquals(NAME + " " + CCY, f.apply(NAME, CCY));
    final Function2<String, String, String> other = new DefaultCsbcRenamingFunction();
    assertEquals(f, other);
    assertEquals(f.hashCode(), other.hashCode());
  }

  /**
   * Tests renaming with an intermediate string.
   */
  @Test
  public static void testWithExtraInformation() {
    final String s = "DEF";
    final Function2<String, String, String> f = new DefaultCsbcRenamingFunction(s);
    assertEquals(NAME + " " + CCY + " " + s, f.apply(NAME, CCY));
    Function2<String, String, String> other = new DefaultCsbcRenamingFunction(s);
    assertEquals(f, other);
    assertEquals(f.hashCode(), other.hashCode());
    other = new DefaultCsbcRenamingFunction(s + "1");
    assertFalse(f.equals(other));
  }
}
