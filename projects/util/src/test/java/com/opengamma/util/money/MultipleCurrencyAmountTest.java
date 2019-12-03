/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.AssertJUnit.assertSame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.annotations.Test;

import com.google.common.collect.Sets;
import com.opengamma.util.test.TestGroup;

/**
 * Test MultipleCurrencyAmount.
 */
@Test(groups = TestGroup.UNIT)
public class MultipleCurrencyAmountTest {

  private static final Currency CCY1 = Currency.AUD;
  private static final Currency CCY2 = Currency.CAD;
  private static final Currency CCY3 = Currency.CHF;
  private static final double A1 = 101;
  private static final double A2 = 103;
  private static final double A3 = 107;
  private static final CurrencyAmount CA1 = CurrencyAmount.of(CCY1, A1);
  private static final CurrencyAmount CA2 = CurrencyAmount.of(CCY2, A2);
  private static final CurrencyAmount CA3 = CurrencyAmount.of(CCY3, A3);
  private static final Currency[] CCY_ARRAY;
  private static final double[] A_ARRAY;
  private static final List<Currency> CCY_LIST;
  private static final List<Double> A_LIST;
  private static final Map<Currency, Double> CCY_A_MAP;
  private static final CurrencyAmount[] CA_ARRAY;
  private static final List<CurrencyAmount> CA_LIST;
  private static final Set<CurrencyAmount> CA_SET;
  private static final MultipleCurrencyAmount MULTIPLE;

  static {
    CCY_ARRAY = new Currency[] {CCY1, CCY2, CCY3 };
    A_ARRAY = new double[] {A1, A2, A3 };
    CCY_LIST = Arrays.asList(CCY_ARRAY);
    A_LIST = Arrays.asList(A1, A2, A3);
    CCY_A_MAP = new HashMap<>();
    CCY_A_MAP.put(CCY1, A1);
    CCY_A_MAP.put(CCY2, A2);
    CCY_A_MAP.put(CCY3, A3);
    CA_ARRAY = new CurrencyAmount[] {CA1, CA2, CA3 };
    CA_LIST = Arrays.asList(CA_ARRAY);
    CA_SET = Sets.newHashSet(CA_ARRAY);
    MULTIPLE = MultipleCurrencyAmount.of(CA_ARRAY);
  }

  /**
   * Tests that the currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrency() {
    MultipleCurrencyAmount.of(null, A1);
  }

  /**
   * Tests that the currencies cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyArray() {
    MultipleCurrencyAmount.of(null, A_ARRAY);
  }

  /**
   * Tests that the currency array cannot contain a null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyArray() {
    MultipleCurrencyAmount.of(new Currency[] {CCY1, null, CCY2 }, A_ARRAY);
  }

  /**
   * Tests that the amounts cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAmountArray() {
    MultipleCurrencyAmount.of(CCY_ARRAY, null);
  }

  /**
   * Tests that the arrays must be the same length.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthAmountArray() {
    MultipleCurrencyAmount.of(CCY_ARRAY, new double[] { A1, A2 });
  }

  /**
   * Tests that the currency list cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyList() {
    MultipleCurrencyAmount.of(null, A_LIST);
  }

  /**
   * Tests that the currency list cannot contain a null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyList() {
    MultipleCurrencyAmount.of(Arrays.asList(CCY1, null, CCY2), A_LIST);
  }

  /**
   * Tests that the amount list cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAmountList() {
    MultipleCurrencyAmount.of(CCY_LIST, null);
  }

  /**
   * Tests that the amount list cannot contain a null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInAmountList() {
    MultipleCurrencyAmount.of(CCY_LIST, Arrays.asList(null, A2, A3));
  }

  /**
   * Tests that the lists must be the same size.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongLengthAmountList() {
    MultipleCurrencyAmount.of(CCY_LIST, Arrays.asList(A1, A2));
  }

  /**
   * Tests that the map cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullMap() {
    MultipleCurrencyAmount.of((Map<Currency, Double>) null);
  }

  /**
   * Tests that there cannot be a null in the currency keys.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyInMap() {
    final Map<Currency, Double> map = new HashMap<>();
    map.put(CCY1, A1);
    map.put(null, A2);
    map.put(CCY3, A3);
    MultipleCurrencyAmount.of(map);
  }

  /**
   * Tests that there cannot be a null in the amount values.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullAmountInMap() {
    final Map<Currency, Double> map = new HashMap<>();
    map.put(CCY1, A1);
    map.put(CCY2, null);
    map.put(CCY3, A3);
    MultipleCurrencyAmount.of(map);
  }

  /**
   * Tests that the currency amount array cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyAmountArray() {
    MultipleCurrencyAmount.of((CurrencyAmount[]) null);
  }

  /**
   * Tests that the currency amount array cannot contain a null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyAmountArray() {
    MultipleCurrencyAmount.of(new CurrencyAmount[] {null, CA2, CA3 });
  }

  /**
   * Tests that the currency amount list cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyAmountList() {
    MultipleCurrencyAmount.of((List<CurrencyAmount>) null);
  }

  /**
   * Tests that the currency amount list cannot contain a null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyAmountList() {
    MultipleCurrencyAmount.of(Arrays.asList(null, CA2, CA3));
  }

  /**
   * Tests that the currency amount set cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullCurrencyAmountSet() {
    MultipleCurrencyAmount.of((Set<CurrencyAmount>) null);
  }

  /**
   * Tests that the currency amount set cannot contain a null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInCurrencyAmountSet() {
    MultipleCurrencyAmount.of(Sets.newHashSet(null, CA2, CA3));
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAmountNullCurrency() {
    MULTIPLE.getAmount(null);
  }

  /**
   * Tests the behaviour when there is no amount for a currency.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetAmountWrongCurrency() {
    MULTIPLE.getAmount(Currency.DEM);
  }

  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testGetCurrencyAmountNullCurrency() {
    MULTIPLE.getCurrencyAmount(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that a null amount cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullCurrencyAmount() {
    MULTIPLE.plus((CurrencyAmount) null);
  }

  /**
   * Tests that the currency cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullCurrency() {
    MULTIPLE.plus(null, A1);
  }

  /**
   * Tests that a null amount cannot be added.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testPlusNullMultipleCurrencyAmount() {
    MULTIPLE.plus((MultipleCurrencyAmount) null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that null is not allowed as an input.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWithoutNull() {
    MULTIPLE.without(null);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    assertEquals(MULTIPLE.size(), CA_LIST.size());
    final CurrencyAmount[] ca = MULTIPLE.getCurrencyAmounts();
    assertEquals(ca.length, CA_SET.size());
    for (final CurrencyAmount element : ca) {
      assertTrue(CA_SET.contains(element));
    }
    MultipleCurrencyAmount other = MultipleCurrencyAmount.of(CA_ARRAY);
    assertEquals(MULTIPLE, other);
    assertEquals(MULTIPLE.hashCode(), other.hashCode());
    CurrencyAmount[] array = new CurrencyAmount[] {CurrencyAmount.of(CCY1, A1), CurrencyAmount.of(CCY1, A2),
        CurrencyAmount.of(CCY1, A3) };
    other = MultipleCurrencyAmount.of(array);
    assertFalse(MULTIPLE.equals(other));
    array = new CurrencyAmount[] {CurrencyAmount.of(CCY1, A1), CurrencyAmount.of(CCY2, A1), CurrencyAmount.of(CCY3, A1) };
    other = MultipleCurrencyAmount.of(array);
    assertFalse(MULTIPLE.equals(other));
    assertTrue(MULTIPLE.equals(MULTIPLE));
  }

  /**
   * Tests the equals method.
   */
  @Test
  public void testEqualsRubbish() {
    assertNotEquals("", MULTIPLE);
    assertNotEquals(null, MULTIPLE);
  }

  /**
   * Tests the toString method.
   */
  @Test
  public void testToString() {
    assertTrue(MULTIPLE.toString().contains(CA1.toString()));
    assertTrue(MULTIPLE.toString().contains(CA2.toString()));
    assertTrue(MULTIPLE.toString().contains(CA3.toString()));
  }

  /**
   * Tests equivalence of constructors.
   */
  @Test
  public void testConstructors() {
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CCY_LIST, A_LIST));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CCY_A_MAP));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_ARRAY));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_LIST));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_SET));
  }

  /**
   * Tests the constructors.
   */
  @Test
  public void testStaticConstruction() {
    assertEquals(MultipleCurrencyAmount.of(CCY1, A1), MultipleCurrencyAmount.of(CCY1, A1));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CCY_LIST, A_LIST));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CCY_A_MAP));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_ARRAY));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_LIST));
    assertEquals(MULTIPLE, MultipleCurrencyAmount.of(CA_SET));
  }

  /**
   * Tests that repeated currency amounts are added.
   */
  @Test
  public void testConstructionRepeatedCurrencies() {
    final CurrencyAmount ca4 = CurrencyAmount.of(CCY1, A1 * 2);
    final CurrencyAmount ca5 = CurrencyAmount.of(CCY2, A2 * 3);
    final Currency[] ccyArray = new Currency[] {CCY1, CCY2, CCY3, ca4.getCurrency(), ca5.getCurrency() };
    final double[] aArray = new double[] {A1, A2, A3, ca4.getAmount(), ca5.getAmount() };
    final List<Currency> ccyList = Arrays.asList(ccyArray);
    final List<Double> aList = Arrays.asList(A1, A2, A3, A1 * 2, A2 * 3);
    final CurrencyAmount[] caArray = new CurrencyAmount[] {CA1, CA2, CA3, ca4, ca5 };
    final List<CurrencyAmount> caList = Arrays.asList(caArray);
    final HashSet<CurrencyAmount> caSet = Sets.newHashSet(caArray);
    final Set<CurrencyAmount> expected = Sets.newHashSet(CurrencyAmount.of(CCY1, A1 * 3),
        CurrencyAmount.of(CCY2, A2 * 4), CA3);
    assertSameData(expected, MultipleCurrencyAmount.of(ccyArray, aArray));
    assertSameData(expected, MultipleCurrencyAmount.of(ccyList, aList));
    assertSameData(expected, MultipleCurrencyAmount.of(caArray));
    assertSameData(expected, MultipleCurrencyAmount.of(caList));
    assertSameData(expected, MultipleCurrencyAmount.of(caSet));
  }

  /**
   * Tests getting an amount for a currency.
   */
  @Test
  public void testGetAmount() {
    for (int i = 0; i < CCY_ARRAY.length; i++) {
      assertEquals(MULTIPLE.getAmount(CCY_ARRAY[i]), A_ARRAY[i]);
      assertEquals(MULTIPLE.getCurrencyAmount(CCY_ARRAY[i]), CA_ARRAY[i]);
    }
  }

  /**
   * Tests that null is returned when there is no amount for a currency.
   */
  @Test
  public void testGetCurrencyAmountWrongCurrency() {
    assertEquals(null, MULTIPLE.getCurrencyAmount(Currency.DEM));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests addition of amounts.
   */
  @Test
  public void testPlus1() {
    final double a = 117;
    final CurrencyAmount ca = CurrencyAmount.of(Currency.CZK, a);
    final Set<CurrencyAmount> expected = new HashSet<>(CA_SET);
    expected.add(ca);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CA_ARRAY);
    final MultipleCurrencyAmount test = mca.plus(ca);
    assertSameData(expected, test);
  }

  /**
   * Tests addition of amounts.
   */
  @Test
  public void testPlus2() {
    final double a = 117;
    final CurrencyAmount ca = CurrencyAmount.of(Currency.AUD, a);
    final Set<CurrencyAmount> expected = Sets.newHashSet(CA1.plus(ca), CA2, CA3);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CA_LIST);
    final MultipleCurrencyAmount test = mca.plus(ca);
    assertSameData(expected, test);
  }

  /**
   * Tests addition of amounts.
   */
  @Test
  public void testPlusCurrencyAmount() {
    final CurrencyAmount ca = CurrencyAmount.of(Currency.AUD, 117);
    final CurrencyAmount cb = CurrencyAmount.of(Currency.USD, 12);
    final CurrencyAmount cc = CurrencyAmount.of(Currency.AUD, 3);
    final CurrencyAmount cd = CurrencyAmount.of(Currency.NZD, 3);
    final MultipleCurrencyAmount mc1 = MultipleCurrencyAmount.of(ca, cb);
    final MultipleCurrencyAmount mc2 = MultipleCurrencyAmount.of(cc, cd);
    final Set<CurrencyAmount> expected = Sets.newHashSet(cb, cd, CurrencyAmount.of(Currency.AUD, 120));
    final MultipleCurrencyAmount test = mc1.plus(mc2);
    assertSameData(expected, test);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests multiplication of amounts.
   */
  @Test
  public void testMultipliedBy() {
    final double factor = 2.5;
    final MultipleCurrencyAmount mca1 = MultipleCurrencyAmount.of(CA1);
    final MultipleCurrencyAmount multiplied1 = mca1.multipliedBy(factor);
    final MultipleCurrencyAmount expected1 = MultipleCurrencyAmount.of(CCY1, A1 * factor);
    assertEquals(expected1, multiplied1, "MultipleCurrencyAmount: multipliedBy");
    final MultipleCurrencyAmount mca2 = MultipleCurrencyAmount.of(CA2, CA3);
    final MultipleCurrencyAmount multiplied2 = mca2.multipliedBy(factor);
    final MultipleCurrencyAmount expected2 = MultipleCurrencyAmount.of(CCY2, A2 * factor).plus(CCY3, A3 * factor);
    assertEquals(expected2, multiplied2, "MultipleCurrencyAmount: multipliedBy");
  }

  //-------------------------------------------------------------------------
  /**
   * Tests removal of a currency.
   */
  @Test
  public void testWithout() {
    final Set<CurrencyAmount> expected = Sets.newHashSet(CA1, CA3);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CCY_LIST, A_LIST);
    final MultipleCurrencyAmount test = mca.without(CCY2);
    assertSameData(expected, test);
  }

  /**
   * Tests removal of all currencies.
   */
  @Test
  public void testWithoutToEmpty() {
    final Set<CurrencyAmount> expected = Sets.newHashSet();
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CA1);
    final MultipleCurrencyAmount test = mca.without(CCY1);
    assertSameData(expected, test);
  }

  /**
   * Tests removal of all amounts.
   */
  @Test
  public void testWithoutEmpty() {
    final Set<CurrencyAmount> expected = Sets.newHashSet();
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of();
    final MultipleCurrencyAmount test = mca.without(CCY1);
    assertSameData(expected, test);
  }

  /**
   * Tests that there is no effect when removing a currency without a value.
   */
  @Test
  public void testWithoutKeyNotPresent() {
    assertSame(MULTIPLE, MULTIPLE.without(Currency.DEM));
  }

  //-------------------------------------------------------------------------
  /**
   * Tests adding a currency.
   */
  @Test
  public void testWith() {
    final double a = A2 * 10;
    final CurrencyAmount ca = CurrencyAmount.of(CCY2, a);
    final Set<CurrencyAmount> expected = Sets.newHashSet(CA1, ca, CA3);
    final MultipleCurrencyAmount mca = MultipleCurrencyAmount.of(CCY_LIST, A_LIST);
    final MultipleCurrencyAmount test = mca.with(CCY2, a);
    assertSameData(expected, test);
  }

  /**
   * Tests adding a currency.
   */
  @Test
  public void testWithKeyNotPresent() {
    final Set<CurrencyAmount> expected = Sets.newHashSet(CA1, CA2, CA3, CurrencyAmount.of(Currency.DEM, A1));
    final MultipleCurrencyAmount test = MULTIPLE.with(Currency.DEM, A1);
    assertSameData(expected, test);
  }

  //-------------------------------------------------------------------------
  /**
   * Tests that the amounts are the same.
   *
   * @param expected  the expected values
   * @param actual  the actual values
   */
  private static void assertSameData(final Set<CurrencyAmount> expected, final MultipleCurrencyAmount actual) {
    final CurrencyAmount[] amounts = actual.getCurrencyAmounts();
    assertEquals(amounts.length, expected.size());
    for (final CurrencyAmount amount : amounts) {
      assertTrue(expected.contains(amount), "Expected: " + expected + " but found: " + amount);
    }
  }
}
