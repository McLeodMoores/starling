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
 * Tests a renaming function for {@link com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration} where
 * the currency is fixed.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultCsbcRenamingFunctionTest {
  /** The curve specification builder configuration name */
  private static final String NAME = "DEFAULT";
  /** The currency string */
  private static final String CCY = "ABC";

  /**
   * Tests renaming without an intermediate string.
   */
  @Test
  public static void testWithoutExtraInformation() {
    final Function2<String, String, String> f = new FixedCurrencyCsbcRenamingFunction(CCY);
    assertEquals(NAME + " " + CCY, f.apply(NAME, CCY));
    Function2<String, String, String> other = new FixedCurrencyCsbcRenamingFunction(CCY);
    assertEquals(f, other);
    assertEquals(f.hashCode(), other.hashCode());
    other = new FixedCurrencyCsbcRenamingFunction(CCY + "1");
    assertFalse(f.equals(other));
  }

  /**
   * Tests renaming with an intermediate string.
   */
  @Test
  public static void testWithExtraInformation() {
    final String s = "DEF";
    final Function2<String, String, String> f = new FixedCurrencyCsbcRenamingFunction(CCY, s);
    assertEquals(NAME + " " + CCY + " " + s, f.apply(NAME, CCY));
    Function2<String, String, String> other = new FixedCurrencyCsbcRenamingFunction(CCY, s);
    assertEquals(f, other);
    assertEquals(f.hashCode(), other.hashCode());
    other = new FixedCurrencyCsbcRenamingFunction(CCY, s + "1");
    assertFalse(f.equals(other));
    other = new FixedCurrencyCsbcRenamingFunction(CCY + "1", s);
    assertFalse(f.equals(other));
  }
}
