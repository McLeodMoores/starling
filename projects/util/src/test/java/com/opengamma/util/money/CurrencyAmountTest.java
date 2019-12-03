/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test CurrencyAmount.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyAmountTest {

  private static final Currency CCY1 = Currency.AUD;
  private static final Currency CCY2 = Currency.CAD;
  private static final double A1 = 100;
  private static final double A2 = 200;
  private static final CurrencyAmount CCY_AMOUNT = CurrencyAmount.of(CCY1, A1);

  /**
   * Tests the getCurrency and getAmount methods.
   */
  @Test
  public void testFixture() {
    assertEquals(CCY1, CCY_AMOUNT.getCurrency());
    assertEquals(A1, CCY_AMOUNT.getAmount(), 0);
  }

  //-------------------------------------------------------------------------
  // factories
  //-------------------------------------------------------------------------
  /**
   * Tests factory construction.
   */
  public void testOfCurrency() {
    final CurrencyAmount test = CurrencyAmount.of(Currency.USD, A1);
    assertEquals(Currency.USD, test.getCurrency());
    assertEquals(A1, test.getAmount(), 0.0001d);
  }

  /**
   * Tests that the currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfCurrencyNullCurrency() {
    CurrencyAmount.of((Currency) null, A1);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests construction from a currency string and amount.
   */
  public void testOfString() {
    final CurrencyAmount test = CurrencyAmount.of("USD", A1);
    assertEquals(Currency.USD, test.getCurrency());
    assertEquals(A1, test.getAmount(), 0.0001d);
  }

  /**
   * Tests that the currency string cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfStringNullCurrency() {
    CurrencyAmount.of((String) null, A1);
  }

  //-------------------------------------------------------------------------
  // parse(String)
  //-------------------------------------------------------------------------
  /**
   * Tests the parsing of a currency amount string.
   */
  @Test
  public void testParseString() {
    assertEquals(CurrencyAmount.of(Currency.AUD, 100.001), CurrencyAmount.parse("AUD 100.001"));
    assertEquals(CurrencyAmount.of(Currency.AUD, 123.3), CurrencyAmount.parse("AUD 123.3"));
    assertEquals(CCY_AMOUNT, CurrencyAmount.parse(CCY_AMOUNT.toString()));
  }

  /**
   * Provides values that cannot be parsed.
   *
   * @return  unparseable values
   */
  @DataProvider(name = "badParse")
  Object[][] dataBadParse() {
    return new Object[][] {
      {"AUD"},
      {"AUD aa"},
      {"123"},
      {null},
    };
  }

  /**
   * Tests the behaviour when values cannot be parsed.
   *
   * @param input  unparseable values
   */
  @Test(dataProvider = "badParse", expectedExceptions = IllegalArgumentException.class)
  public void testParseStringBad(final String input) {
    CurrencyAmount.parse(input);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that addition of two currency amounts with the same currency.
   */
  public void testPlus() {
    final CurrencyAmount ccyAmount = CurrencyAmount.of(CCY1, A2);
    final CurrencyAmount test = CCY_AMOUNT.plus(ccyAmount);
    assertEquals(CCY1, test.getCurrency());
    assertEquals(A1 + A2, test.getAmount());
  }

  /**
   * Tests that null cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusAddNullOther() {
    CCY_AMOUNT.plus(null);
  }

  /**
   * Tests that the currencies must be the same.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusWrongCurrency() {
    CCY_AMOUNT.plus(CurrencyAmount.of(CCY2, A2));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests multiplication of the amount by a constant.
   */
  public void testMultipliedBy() {
    final CurrencyAmount test = CCY_AMOUNT.multipliedBy(3.5);
    assertEquals(CCY1, test.getCurrency());
    assertEquals(A1 * 3.5, test.getAmount());
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the object methods.
   */
  @Test
  public void testObject() {
    CurrencyAmount other = CurrencyAmount.of(CCY1, A1);
    assertTrue(CCY_AMOUNT.equals(CCY_AMOUNT));
    assertTrue(CCY_AMOUNT.equals(other));
    assertTrue(other.equals(CCY_AMOUNT));
    assertEquals(other.hashCode(), CCY_AMOUNT.hashCode());
    other = CurrencyAmount.of(CCY1, A1);
    assertEquals(other, CCY_AMOUNT);
    assertEquals(other.hashCode(), CCY_AMOUNT.hashCode());
    other = CurrencyAmount.of(CCY2, A1);
    assertFalse(CCY_AMOUNT.equals(other));
    other = CurrencyAmount.of(CCY1, A2);
    assertFalse(CCY_AMOUNT.equals(other));
  }

  /**
   * Tests not equal values.
   */
  @Test
  public void testEqualsRubbish() {
    assertNotEquals("", CCY_AMOUNT);
    assertNotEquals(null, CCY_AMOUNT);
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    assertEquals(CCY_AMOUNT.metaBean().amount().get(CCY_AMOUNT), A1);
    assertEquals(CCY_AMOUNT.metaBean().currency().get(CCY_AMOUNT), CCY1);
    assertEquals(CCY_AMOUNT.property("amount").get(), A1);
    assertEquals(CCY_AMOUNT.property("currency").get(), CCY1);
  }
}
