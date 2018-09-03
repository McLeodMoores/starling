/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNotNull;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link UnorderedCurrencyPair}.
 */
@Test(groups = TestGroup.UNIT)
public class UnorderedCurrencyPairTest {

  /**
   * Tests construction from two currencies.
   */
  @Test
  public void testOfCurrencyCurrencyOrder1() {
    final UnorderedCurrencyPair test = UnorderedCurrencyPair.of(Currency.GBP, Currency.USD);
    assertEquals(Currency.GBP, test.getFirstCurrency());
    assertEquals(Currency.USD, test.getSecondCurrency());
    assertEquals("GBPUSD", test.toString());
  }

  /**
   * Tests construction from two currencies.
   */
  @Test
  public void testOfCurrencyCurrencyOrder2() {
    final UnorderedCurrencyPair test = UnorderedCurrencyPair.of(Currency.USD, Currency.GBP);
    assertEquals(Currency.GBP, test.getFirstCurrency());
    assertEquals(Currency.USD, test.getSecondCurrency());
    assertEquals("GBPUSD", test.toString());
  }

  /**
   * Tests that the first currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfCurrencyNullCurrency1() {
    UnorderedCurrencyPair.of((Currency) null, Currency.USD);
  }

  /**
   * Tests that the second currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfCurrencyNullCurrency2() {
    UnorderedCurrencyPair.of(Currency.USD, (Currency) null);
  }

  /**
   * Tests string parsing.
   */
  @Test
  public void testParseString() {
    assertEquals(UnorderedCurrencyPair.of(Currency.AUD, Currency.EUR), UnorderedCurrencyPair.parse("AUDEUR"));
    final UnorderedCurrencyPair pair1 = UnorderedCurrencyPair.of(Currency.EUR, Currency.USD);
    assertEquals(pair1, UnorderedCurrencyPair.parse(pair1.toString()));
    final UnorderedCurrencyPair pair2 = UnorderedCurrencyPair.of(Currency.USD, Currency.EUR);
    assertEquals(pair2, UnorderedCurrencyPair.parse(pair2.toString()));
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
    UnorderedCurrencyPair.parse(input);
  }

  /**
   * Tests the equals() and hashCode() methods.
   */
  @Test
  public void testEqualsHashCode() {
    final UnorderedCurrencyPair a = UnorderedCurrencyPair.of(Currency.GBP, Currency.USD);
    final UnorderedCurrencyPair b = UnorderedCurrencyPair.of(Currency.USD, Currency.GBP);
    final UnorderedCurrencyPair c = UnorderedCurrencyPair.of(Currency.USD, Currency.EUR);

    assertEquals(a.equals(a), true);
    assertEquals(b.equals(b), true);
    assertEquals(c.equals(c), true);

    assertEquals(a.equals(b), true);
    assertEquals(b.equals(a), true);
    assertEquals(a.hashCode() == b.hashCode(), true);

    assertEquals(a.equals(c), false);
    assertEquals(b.equals(c), false);
  }

  /**
   * Tests the equals() methods.
   */
  @Test
  public void testEqualsFalse() {
    final UnorderedCurrencyPair a = UnorderedCurrencyPair.of(Currency.GBP, Currency.USD);
    assertEquals(a.equals(null), false);
    assertNotEquals("GBPUSD", a);
    assertEquals(a.equals(new Object()), false);
  }

  /**
   * Tests construction from a unique id.
   */
  @Test
  public void testOfUniqueId() {
    final UnorderedCurrencyPair test = UnorderedCurrencyPair.of(UniqueId.of(UnorderedCurrencyPair.OBJECT_SCHEME, "USDGBP"));
    assertEquals(Currency.GBP, test.getFirstCurrency());
    assertEquals(Currency.USD, test.getSecondCurrency());
    assertEquals(ObjectId.of(UnorderedCurrencyPair.OBJECT_SCHEME, "GBPUSD"), test.getObjectId());
    assertEquals(UniqueId.of(UnorderedCurrencyPair.OBJECT_SCHEME, "GBPUSD"), test.getUniqueId());
  }

  /**
   * Tests construction with the wrong scheme.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfUniqueIdWrongScheme() {
    UnorderedCurrencyPair.of(UniqueId.of("Foo", "USDGBP"));
  }

  /**
   * Tests construction with the wrong value.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfUniqueIdWrongValue() {
    UnorderedCurrencyPair.of(UniqueId.of(UnorderedCurrencyPair.OBJECT_SCHEME, "USD"));
  }

  /**
   * Tests the bean.
   */
  @Test
  public void testBean() {
    final UnorderedCurrencyPair pair = UnorderedCurrencyPair.of(Currency.CHF, Currency.CAD);
    assertNotNull(pair.metaBean());
    assertNotNull(pair.metaBean().firstCurrency());
    assertNotNull(pair.metaBean().secondCurrency());
    assertEquals(pair.metaBean().firstCurrency().get(pair), Currency.CAD);
    assertEquals(pair.metaBean().secondCurrency().get(pair), Currency.CHF);
    assertEquals(pair.property("firstCurrency").get(), Currency.CAD);
    assertEquals(pair.property("secondCurrency").get(), Currency.CHF);
  }

}
