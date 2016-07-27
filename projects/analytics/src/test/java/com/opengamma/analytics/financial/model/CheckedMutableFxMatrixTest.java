/**
 *
 */
package com.opengamma.analytics.financial.model;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.util.money.Currency;

/**
 *
 */
public class CheckedMutableFxMatrixTest {

  @Test
  public void test() {
    final CheckedMutableFxMatrix matrix = CheckedMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 0.8);
    matrix.addCurrency(Currency.EUR, Currency.GBP, 0.5);
    System.out.println(matrix.getFxRate(Currency.GBP, Currency.USD));
  }

  @Test
  public void testAvailableRatesHaveSameDenominator() {
    final CheckedMutableFxMatrix matrix = CheckedMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 0.8);
    matrix.addCurrency(Currency.CHF, Currency.USD, 1.1);
    matrix.addCurrency(Currency.GBP, Currency.USD, 0.5);
    assertEquals(matrix.getFxRate(Currency.GBP, Currency.EUR), 0.5 / 0.8);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.GBP), 0.8 / 0.5);
  }

  @Test
  public void testCreateCross1() {
    final CheckedMutableFxMatrix matrix = CheckedMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 0.8);
    matrix.addCurrency(Currency.CHF, Currency.USD, 1.1);
    matrix.addCurrency(Currency.GBP, Currency.EUR, 0.625);
    assertEquals(matrix.getFxRate(Currency.GBP, Currency.USD), 0.5);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.GBP), 1 / 0.5);
  }

  @Test
  public void testCreateCross2() {
    final CheckedMutableFxMatrix matrix = CheckedMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 0.8);
    matrix.addCurrency(Currency.GBP, Currency.USD, 0.5);
    matrix.addCurrency(Currency.CHF, Currency.GBP, 2.2);
    assertEquals(matrix.getFxRate(Currency.CHF, Currency.USD), 1.1);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.CHF), 1 / 1.1);
  }

  @Test
  public void testCreateCross3() {
    final CheckedMutableFxMatrix matrix = CheckedMutableFxMatrix.of();
    matrix.addCurrency(Currency.EUR, Currency.USD, 0.8);
    matrix.addCurrency(Currency.CHF, Currency.USD, 1.1);
    matrix.addCurrency(Currency.GBP, Currency.CHF, 0.5 / 1.1);
    assertEquals(matrix.getFxRate(Currency.GBP, Currency.USD), 0.5);
    assertEquals(matrix.getFxRate(Currency.USD, Currency.GBP), 1 / 0.5);
  }

  @Test
  public void testCreateCross4() {
    final CheckedMutableFxMatrix matrix = CheckedMutableFxMatrix.of();
    matrix.addCurrency(Currency.NZD, Currency.USD, 1.5);
    matrix.addCurrency(Currency.CHF, Currency.EUR, 1.375);
    matrix.addCurrency(Currency.GBP, Currency.CHF, 0.5 / 1.1);
    assertEquals(matrix.getFxRate(Currency.GBP, Currency.EUR), 0.625);
    assertEquals(matrix.getFxRate(Currency.EUR, Currency.GBP), 1 / 0.625);
  }
}
