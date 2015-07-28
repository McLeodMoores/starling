/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * A labelled matrix with {@link Currency} keys and double values. This class is generally used for
 * display purposes. When a (Currency, double) pair is added to the matrix, the amount is added to
 * the existing value if the currency is already present in the matrix. Otherwise, a new row is added.
 */
public class CurrencyLabelledMatrix1D extends LabelledMatrix1D<Currency, Currency> {

  /**
   * Creates a labelled matrix that uses the keys as labels.
   * @param keys  the keys, not null
   * @param values  the values, not null
   */
  public CurrencyLabelledMatrix1D(final Currency[] keys, final double[] values) {
    super(keys, values, null);
  }

  /**
   * Creates a labelled matrix.
   * @param keys  the keys, not null
   * @param labels  the labels, not null
   * @param values  the values, not null
   */
  public CurrencyLabelledMatrix1D(final Currency[] keys, final Object[] labels, final double[] values) {
    super(keys, labels, values, null);
  }

  /**
   * Creates a labelled matrix with titles for the labels and values.
   * @param keys  the keys, not null
   * @param labels  the labels, not null
   * @param labelsTitle  the labels title, not null
   * @param values  the values, not null
   * @param valuesTitle  the values title, not null
   */
  public CurrencyLabelledMatrix1D(final Currency[] keys, final Object[] labels, final String labelsTitle, final double[] values, final String valuesTitle) {
    super(keys, labels, labelsTitle, values, valuesTitle, null);
  }

  @Override
  public int compare(final Currency key1, final Currency key2, final Currency tolerance) {
    return key1.compareTo(key2);
  }

  @Override
  public LabelledMatrix1D<Currency, Currency> getMatrix(final Currency[] keys, final Object[] labels, final String labelsTitle,
      final double[] values, final String valuesTitle) {
    return new CurrencyLabelledMatrix1D(keys, labels, labelsTitle, values, valuesTitle);
  }

  @Override
  public LabelledMatrix1D<Currency, Currency> getMatrix(final Currency[] keys, final Object[] labels, final double[] values) {
    return new CurrencyLabelledMatrix1D(keys, labels, values);
  }

  @Override
  public LabelledMatrix1D<Currency, Currency> getMatrix(final Currency[] keys, final double[] values) {
    return new CurrencyLabelledMatrix1D(keys, values);
  }

  /**
   * Gets the amount for a currency, or throws an exception if there is no entry for that currency in the matrix.
   * @param currency  the currency, not null
   * @return  the value
   */
  public double getValueForCurrency(final Currency currency) {
    ArgumentChecker.notNull(currency, "currency");
    final int index = Arrays.binarySearch(getKeys(), currency);
    if (index >= 0) {
      return getValues()[index];
    }
    throw new IllegalArgumentException("Could not get value for " + currency);
  }
}
