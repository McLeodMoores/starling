/**
 *
 */
package com.opengamma.analytics.financial.model;

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
 * An immutable implementation of an FX matrix that does not perform any calculations of cross rates or enforce consistency between
 * rates, but contains only those rates that have been added. The only operations that are possible are retrieving data.
 */
public final class UncheckedImmutableFxMatrix extends FXMatrix {

  /**
   * Creates an immutable FX matrix. The data are copied on construction of the object.
   * @param matrix  the FX matrix, not null
   * @return  an immutable FX matrix
   */
  public static UncheckedImmutableFxMatrix of(final UncheckedMutableFxMatrix matrix) {
    return new UncheckedImmutableFxMatrix(matrix);
  }

  /** The underlying FX matrix */
  private final UncheckedMutableFxMatrix _underlying;

  /**
   * Restricted constructor.
   * @param matrix  the FX matrix, not null
   */
  private UncheckedImmutableFxMatrix(final UncheckedMutableFxMatrix matrix) {
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
    if (!(obj instanceof UncheckedImmutableFxMatrix)) {
      return false;
    }
    final UncheckedImmutableFxMatrix other = (UncheckedImmutableFxMatrix) obj;
    return Objects.equals(_underlying, other._underlying);
  }

}
