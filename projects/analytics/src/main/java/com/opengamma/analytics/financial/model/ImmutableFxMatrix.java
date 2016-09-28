/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * An immutable wrapper for FX matrices. The data are copied from the input matrix on constructions of this object.
 */
public final class ImmutableFxMatrix extends FXMatrix {

  /**
   * Creates an immutable FX matrix from a checked matrix. The data are copied on construction of the object.
   * @param matrix  the FX matrix, not null
   * @return  an immutable FX matrix
   */
  public static ImmutableFxMatrix of(final CheckedMutableFxMatrix matrix) {
    return new ImmutableFxMatrix(matrix);
  }

  /**
   * Creates an immutable FX matrix from an unchecked matrix. The data are copied on construction of the object.
   * @param matrix  the FX matrix, not null
   * @return  an immutable FX matrix
   */
  public static ImmutableFxMatrix of(final UncheckedMutableFxMatrix matrix) {
    return new ImmutableFxMatrix(matrix);
  }

  /**
   * Creates an immutable FX matrix from an FX matrix. The data are copied on construction of this object. Note
   * that the underlying data storage of this matrix is transformed from that in {@link FXMatrix} to {@link UncheckedMutableFxMatrix}.
   * @param matrix  the FX matrix, not null
   * @return   an immutable matrix
   */
  public static ImmutableFxMatrix of(final FXMatrix matrix) {
    return new ImmutableFxMatrix(matrix);
  }

  /** The underlying FX matrix. Can be unchecked because nothing inconsistent can be added. */
  private final UncheckedMutableFxMatrix _underlying;

  /**
   * Restricted constructor.
   * @param matrix  the FX matrix, not null
   */
  private ImmutableFxMatrix(final CheckedMutableFxMatrix matrix) {
    ArgumentChecker.notNull(matrix, "matrix");
    final List<Currency> currencies = matrix.getCurrencyList();
    _underlying = UncheckedMutableFxMatrix.of();
    final Currency[] currencyArray = currencies.toArray(new Currency[currencies.size()]);
    for (int i = 0; i < currencyArray.length; i++) {
      for (int j = i + 1; j < currencyArray.length; j++) {
        _underlying.addCurrency(currencyArray[j], currencyArray[i], matrix.getFxRate(currencyArray[j], currencyArray[i]));
      }
    }
  }

  /**
   * Restricted constructor.
   * @param matrix  the FX matrix, not null
   */
  private ImmutableFxMatrix(final UncheckedMutableFxMatrix matrix) {
    ArgumentChecker.notNull(matrix, "matrix");
    final List<Currency> currencies = matrix.getCurrencyList();
    _underlying = UncheckedMutableFxMatrix.of();
    final Currency[] currencyArray = currencies.toArray(new Currency[currencies.size()]);
    for (int i = 0; i < currencyArray.length; i++) {
      for (int j = i + 1; j < currencyArray.length; j++) {
        _underlying.addCurrency(currencyArray[j], currencyArray[i], matrix.getFxRate(currencyArray[j], currencyArray[i]));
      }
    }
  }

  /**
   * Restricted constructor.
   * @param matrix  the FX matrix, not null
   */
  private ImmutableFxMatrix(final FXMatrix matrix) {
    ArgumentChecker.notNull(matrix, "matrix");
    final Collection<Currency> currencies = matrix.getCurrencies().keySet();
    _underlying = UncheckedMutableFxMatrix.of();
    final Currency[] currencyArray = currencies.toArray(new Currency[currencies.size()]);
    for (int i = 0; i < currencyArray.length; i++) {
      for (int j = i + 1; j < currencyArray.length; j++) {
        _underlying.addCurrency(currencyArray[j], currencyArray[i], matrix.getFxRate(currencyArray[j], currencyArray[i]));
      }
    }
  }

  @Override
  public void addCurrency(final Currency numerator, final Currency denominator, final double fxRate) {
    throw new UnsupportedOperationException("This FX matrix is immutable");
  }

  @Override
  public void updateRates(final Currency numerator, final Currency denominator, final double fxRate) {
    throw new UnsupportedOperationException("This FX matrix is immutable");
  }

  @Override
  public double getFxRate(final Currency numerator, final Currency denominator) {
    return _underlying.getFxRate(numerator, denominator);
  }

  @Override
  public int getNumberOfCurrencies() {
    return _underlying.getNumberOfCurrencies();
  }

  @Override
  public boolean containsPair(final Currency ccy1, final Currency ccy2) {
    return _underlying.containsPair(ccy1, ccy2);
  }

  /**
   * Gets the currencies in this matrix. The list is unmodifiable.
   * @return  the currencies
   */
  public List<Currency> getCurrencyList() {
    // the underlying matrix was copied on construction, so won't have changed
    return Collections.unmodifiableList(_underlying.getCurrencyList());
  }

  /**
   * Gets the FX rates array, which is stored as an upper triangular matrix. The matrix is a copy of that
   * stored in this matrix.
   * @return  the rates
   */
  public double[][] getFxRates() {
    // the underlying matrix was copied on construction, so won't have changed
    return _underlying.getFxRates();
  }

  @Override
  public CurrencyAmount convert(final MultipleCurrencyAmount amount, final Currency ccy) {
    return _underlying.convert(amount, ccy);
  }

  @Deprecated
  @Override
  public Map<Currency, Integer> getCurrencies() {
    return _underlying.getCurrencies();
  }

  @Deprecated
  @Override
  public double[][] getRates() {
    return _underlying.getRates();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = prime;
    result = prime * result + (_underlying == null ? 0 : _underlying.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    // note that the superclass is ignored
    if (!(obj instanceof ImmutableFxMatrix)) {
      return false;
    }
    final ImmutableFxMatrix other = (ImmutableFxMatrix) obj;
    return Objects.equals(_underlying, other._underlying);
  }

}
