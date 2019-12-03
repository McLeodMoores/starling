/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.testng.annotations.Test;

import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test Currency.
 */
@Test(groups = TestGroup.UNIT)
public class CurrencyTest {

  //-----------------------------------------------------------------------
  // constants
  //-----------------------------------------------------------------------
  /**
   * Tests some of the pre-defined currencies.
   */
  public void testConstants() {
    assertEquals(Currency.USD, Currency.of("USD"));
    assertEquals(Currency.EUR, Currency.of("EUR"));
    assertEquals(Currency.JPY, Currency.of("JPY"));
    assertEquals(Currency.GBP, Currency.of("GBP"));
    assertEquals(Currency.CHF, Currency.of("CHF"));
    assertEquals(Currency.AUD, Currency.of("AUD"));
    assertEquals(Currency.CAD, Currency.of("CAD"));
  }

  //-----------------------------------------------------------------------
  // getAvailableCurrencies()
  //-----------------------------------------------------------------------
  /**
   * Gets that pre-defined currencies are available.
   */
  public void testGetAvailable() {
    final Set<Currency> available = Currency.getAvailableCurrencies();
    assertTrue(available.contains(Currency.USD));
    assertTrue(available.contains(Currency.EUR));
    assertTrue(available.contains(Currency.JPY));
    assertTrue(available.contains(Currency.GBP));
    assertTrue(available.contains(Currency.CHF));
    assertTrue(available.contains(Currency.AUD));
    assertTrue(available.contains(Currency.CAD));
  }

  //-----------------------------------------------------------------------
  // of(Currency)
  //-----------------------------------------------------------------------
  /**
   * Tests that a pre-defined currency will be returned where available.
   */
  public void testOfCurrency() {
    final Currency test = Currency.of(java.util.Currency.getInstance("GBP"));
    assertEquals("GBP", test.getCode());
    assertSame(Currency.GBP, test);
  }

  /**
   * Tests that the currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfCurrencyNullCurrency() {
    Currency.of((java.util.Currency) null);
  }

  //-----------------------------------------------------------------------
  // of(String)
  //-----------------------------------------------------------------------
  /**
   * Tests that a pre-defined currency will be returned where available.
   */
  public void testOfString() {
    final Currency test = Currency.of("SEK");
    assertEquals("SEK", test.getCode());
    assertSame(Currency.of("SEK"), test);
  }

  /**
   * Tests that an undefined currency will be added to the cache.
   */
  public void testOfStringUnknownCurrencyCreated() {
    final Currency test = Currency.of("AAA");
    assertEquals("AAA", test.getCode());
    assertSame(Currency.of("AAA"), test);
  }

  /**
   * Tests that the currency string must be upper case.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfStringLowerCase() {
    try {
      Currency.of("gbp");
    } catch (final IllegalArgumentException ex) {
      assertEquals("Invalid currency code: gbp", ex.getMessage());
      throw ex;
    }
  }

  /**
   * Tests that the currency string cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfStringEmpty() {
    Currency.of("");
  }

  /**
   * Tests that the currency string must contain three letters.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfStringTooShort() {
    Currency.of("AB");
  }

  /**
   * Tests that the currency string must contain three letters.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfStringTooLong() {
    Currency.of("ABCD");
  }

  /**
   * Tests that the currency string cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testOfStringNullString() {
    Currency.of((String) null);
  }

  //-----------------------------------------------------------------------
  // parse(String)
  //-----------------------------------------------------------------------
  /**
   * Tests that a pre-defined currency will be returned where available.
   */
  public void testParseString() {
    final Currency test = Currency.parse("GBP");
    assertEquals("GBP", test.getCode());
    assertSame(Currency.GBP, test);
  }

  /**
   * Tests that an undefined currency will be added to the cache.
   */
  public void testParseStringUnknownCurrencyCreated() {
    final Currency test = Currency.parse("AAA");
    assertEquals("AAA", test.getCode());
    assertSame(Currency.of("AAA"), test);
  }

  /**
   * Tests that the currency string must be upper case.
   */
  public void testParseStringLowerCase() {
    final Currency test = Currency.parse("gbp");
    assertEquals("GBP", test.getCode());
    assertSame(Currency.GBP, test);
  }

  /**
   * Tests that the currency string cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseStringEmpty() {
    Currency.parse("");
  }

  /**
   * Tests that the currency string must contain three letters.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseStringTooShort() {
    Currency.parse("AB");
  }

  /**
   * Tests that the currency string must contain three letters.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseStringTooLong() {
    Currency.parse("ABCD");
  }

  /**
   * Tests that the currency string cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testParseStringNullString() {
    Currency.parse((String) null);
  }

  //-----------------------------------------------------------------------
  // Serialisation
  //-----------------------------------------------------------------------
  /**
   * Tests the Java serialization of a currency.
   *
   * @throws Exception  if there is a problem with the serialization
   */
  public void testSerializationGBP() throws Exception {
    final Currency cu = Currency.of("GBP");
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(cu);
      oos.close();
      final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      final Currency input = (Currency) ois.readObject();
      assertSame(input, cu);
    }
  }

  /**
   * Tests the Java serialization of a currency.
   *
   * @throws Exception  if there is a problem with the serialization
   */
  public void testSerializationAAB() throws Exception {
    final Currency cu = Currency.of("AAB");
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
      oos.writeObject(cu);
      oos.close();
      final ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
      final Currency input = (Currency) ois.readObject();
      assertSame(input, cu);
    }
  }

  //-----------------------------------------------------------------------
  // gets
  //-----------------------------------------------------------------------
  /**
   * Tests the get methods.
   */
  public void testGets() {
    final Currency test = Currency.of("GBP");
    assertEquals("GBP", test.getCode());
    assertEquals("GBP", test.getName());
    assertEquals(ObjectId.of("CurrencyISO", "GBP"), test.getObjectId());
    assertEquals(UniqueId.of("CurrencyISO", "GBP"), test.getUniqueId());
    assertEquals(java.util.Currency.getInstance("GBP"), test.toCurrency());
  }

  //-----------------------------------------------------------------------
  // compareTo()
  //-----------------------------------------------------------------------
  /**
   * Tests currency comparisons.
   */
  public void testCompareTo() {
    final Currency a = Currency.EUR;
    final Currency b = Currency.GBP;
    final Currency c = Currency.JPY;
    assertEquals(a.compareTo(a), 0);
    assertEquals(b.compareTo(b), 0);
    assertEquals(c.compareTo(c), 0);

    assertTrue(a.compareTo(b) < 0);
    assertTrue(b.compareTo(a) > 0);

    assertTrue(a.compareTo(c) < 0);
    assertTrue(c.compareTo(a) > 0);

    assertTrue(b.compareTo(c) < 0);
    assertTrue(c.compareTo(b) > 0);
  }

  /**
   * Tests that a currency cannot be compared to null.
   */
  @Test(expectedExceptions = NullPointerException.class)
  public void testCompareToNull() {
    Currency.EUR.compareTo(null);
  }

  //-----------------------------------------------------------------------
  // equals() hashCode()
  //-----------------------------------------------------------------------
  /**
   * Tests the equals and hashcode methods.
   */
  public void testEqualsHashCode() {
    final Currency a = Currency.GBP;
    final Currency b = Currency.of("GBP");
    final Currency c = Currency.EUR;
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
   * Tests values that are not equal.
   */
  public void testEqualsFalse() {
    final Currency a = Currency.GBP;
    assertNotEquals(null, a);
    assertNotEquals("GBP", a);
    assertNotEquals(new Object(), a);
  }

  //-----------------------------------------------------------------------
  // toString()
  //-----------------------------------------------------------------------
  /**
   * Tests the toString method.
   */
  public void testToString() {
    final Currency test = Currency.GBP;
    assertEquals(test.toString(), "GBP");
  }

}
