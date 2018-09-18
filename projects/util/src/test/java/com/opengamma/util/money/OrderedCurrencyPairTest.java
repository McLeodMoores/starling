/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link OrderedCurrencyPair}.
 */
@Test(groups = TestGroup.UNIT)
public class OrderedCurrencyPairTest extends AbstractFudgeBuilderTestCase {

  /**
   * Tests construction from two currencies.
   */
  @Test
  public void testOfCurrencyCurrencyOrder1() {
    final OrderedCurrencyPair test = OrderedCurrencyPair.of(Currency.GBP, Currency.USD);
    assertEquals(Currency.GBP, test.getFirstCurrency());
    assertEquals(Currency.USD, test.getSecondCurrency());
    assertEquals("OrderedCurrencyPair{firstCurrency=GBP, secondCurrency=USD}", test.toString());
  }

  /**
   * Tests construction from two currencies.
   */
  @Test
  public void testOfCurrencyCurrencyOrder2() {
    final OrderedCurrencyPair test = OrderedCurrencyPair.of(Currency.USD, Currency.GBP);
    assertEquals(Currency.USD, test.getFirstCurrency());
    assertEquals(Currency.GBP, test.getSecondCurrency());
    assertEquals("OrderedCurrencyPair{firstCurrency=USD, secondCurrency=GBP}", test.toString());
  }

  /**
   * Tests that the first currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfCurrencyNullCurrency1() {
    OrderedCurrencyPair.of((Currency) null, Currency.USD);
  }

  /**
   * Tests that the second currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfCurrencyNullCurrency2() {
    OrderedCurrencyPair.of(Currency.USD, (Currency) null);
  }

  /**
   * Tests string parsing.
   */
  @Test
  public void testParseString() {
    assertEquals(OrderedCurrencyPair.of(Currency.AUD, Currency.EUR), OrderedCurrencyPair.parse("AUDEUR"));
    assertEquals(OrderedCurrencyPair.of(Currency.EUR, Currency.AUD), OrderedCurrencyPair.parse("EURAUD"));
  }

  /**
   * Provides strings that cannot be parsed.
   *
   * @return  strings
   */
  @DataProvider(name = "badParse")
  Object[][] dataBadParse() {
    return new Object[][] {
      {"AUD"},
      {"AUDEURX"},
      {"123456"},
      {null},
    };
  }

  /**
   * Tests unparsable strings.
   *
   * @param input  the input
   */
  @Test(dataProvider = "badParse", expectedExceptions = IllegalArgumentException.class)
  public void testParseStringBad(final String input) {
    OrderedCurrencyPair.parse(input);
  }

  /**
   * Tests the equals() and hashCode() methods.
   */
  @Test
  public void testEqualsHashCode() {
    final OrderedCurrencyPair a = OrderedCurrencyPair.of(Currency.GBP, Currency.USD);
    final OrderedCurrencyPair b = OrderedCurrencyPair.of(Currency.USD, Currency.GBP);
    final OrderedCurrencyPair c = OrderedCurrencyPair.of(Currency.USD, Currency.EUR);
    final OrderedCurrencyPair d = OrderedCurrencyPair.of(Currency.GBP, Currency.USD);

    assertEquals(a.equals(a), true);
    assertEquals(b.equals(b), true);
    assertEquals(c.equals(c), true);
    assertEquals(d.equals(d), true);

    assertEquals(a.equals(b), false);
    assertEquals(b.equals(a), false);
    assertEquals(a.hashCode() == d.hashCode(), true);
    assertEquals(a.equals(d), true);

    assertEquals(a.equals(c), false);
    assertEquals(b.equals(c), false);
  }

  /**
   * Tests the equals() methods.
   */
  @Test
  public void testEqualsFalse() {
    final OrderedCurrencyPair a = OrderedCurrencyPair.of(Currency.GBP, Currency.USD);
    assertEquals(a.equals(null), false);
    assertNotEquals("GBPUSD", a);
    assertEquals(a.equals(new Object()), false);
  }

  /**
   * Tests construction from a unique id.
   */
  @Test
  public void testOfUniqueId() {
    final OrderedCurrencyPair test = OrderedCurrencyPair.of(UniqueId.of(OrderedCurrencyPair.OBJECT_SCHEME, "USDGBP"));
    assertEquals(Currency.USD, test.getFirstCurrency());
    assertEquals(Currency.GBP, test.getSecondCurrency());
    assertEquals(ObjectId.of(OrderedCurrencyPair.OBJECT_SCHEME, "USDGBP"), test.getObjectId());
    assertEquals(UniqueId.of(OrderedCurrencyPair.OBJECT_SCHEME, "USDGBP"), test.getUniqueId());
  }

  /**
   * Tests construction with the wrong scheme.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfUniqueIdWrongScheme() {
    OrderedCurrencyPair.of(UniqueId.of("Foo", "USDGBP"));
  }

  /**
   * Tests construction with the wrong value.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfUniqueIdWrongValue() {
    OrderedCurrencyPair.of(UniqueId.of(OrderedCurrencyPair.OBJECT_SCHEME, "USD"));
  }

  /**
   * Tests the inverse.
   */
  @Test
  public void testInverse() {
    final OrderedCurrencyPair pair = OrderedCurrencyPair.of(Currency.CHF, Currency.CAD);
    assertTrue(pair.isInverse(pair.getInverse()));
    assertFalse(pair.isInverse(pair));
    assertFalse(pair.isInverse(OrderedCurrencyPair.of(Currency.CHF, Currency.EUR)));
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final OrderedCurrencyPair pair = OrderedCurrencyPair.of(Currency.CHF, Currency.CAD);
    assertNotNull(pair.metaBean());
    assertNotNull(pair.metaBean().firstCurrency());
    assertNotNull(pair.metaBean().secondCurrency());
    assertEquals(pair.metaBean().firstCurrency().get(pair), Currency.CHF);
    assertEquals(pair.metaBean().secondCurrency().get(pair), Currency.CAD);
    assertEquals(pair.property("firstCurrency").get(), Currency.CHF);
    assertEquals(pair.property("secondCurrency").get(), Currency.CAD);
  }

  /**
   * Tests a cycle.
   */
  @Test
  public void testCycle() {
    final OrderedCurrencyPair pair = OrderedCurrencyPair.of(Currency.CHF, Currency.CAD);
    assertEquals(cycleObjectJodaXml(OrderedCurrencyPair.class, pair), pair);
  }
}
