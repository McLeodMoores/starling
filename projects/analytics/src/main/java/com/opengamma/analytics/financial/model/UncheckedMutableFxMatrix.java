/**
 *
 */
package com.opengamma.analytics.financial.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.CurrencyAmount;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * A mutable implementation of an FX matrix that does not perform any calculations of cross rates or enforce consistency between
 * rates, but contains only those rates that have been added.
 */
public final class UncheckedMutableFxMatrix extends FXMatrix {
  //TODO addOrUpdate rather than two methods

  /**
   * Gets an empty mutable matrix.
   * @return  an empty matrix
   */
  public static UncheckedMutableFxMatrix of() {
    return new UncheckedMutableFxMatrix();
  }

  /** The order that the currencies are stored in the matrix */
  private final List<Currency> _currencyList;
  /** The FX rates, stored as the upper triangle of the FX matrix */
  private double[][] _rates;

  /**
   * Restricted constructor.
   */
  private UncheckedMutableFxMatrix() {
    _currencyList = new ArrayList<>();
    _rates = new double[0][0];
  }

  @Override
  public void addCurrency(final Currency numerator, final Currency denominator, final double fxRate) {
    ArgumentChecker.notNull(numerator, "numerator");
    ArgumentChecker.notNull(denominator, "denominator");
    ArgumentChecker.isFalse(numerator.equals(denominator), "Cannot have equal numerator and denominator currency: {}", numerator);
    ArgumentChecker.isTrue(fxRate > 0, "FX rate must be greater than zero: have {}", fxRate);
    final int numeratorIndex = _currencyList.indexOf(numerator);
    final int denominatorIndex = _currencyList.indexOf(denominator);
    if (denominatorIndex < 0) {
      // numerator not found
      if (numeratorIndex < 0) {
        // denominator not found i.e. neither currency was in the matrix
        final int originalEntries = _rates.length;
        // copy original matrix, adding -1 for missing currencies
        final double[][] fxRates = new double[originalEntries + 2][];
        for (int i = 0; i < originalEntries; i++) {
          final double[] originalRow = _rates[i];
          final int originalLength = originalRow.length;
          final double[] newRow = new double[originalLength + 2];
          System.arraycopy(originalRow, 0, newRow, 0, originalLength);
          // add -1 for each cross that doesn't have a rate set
          newRow[originalLength] = -1;
          newRow[originalLength + 1] = -1;
          fxRates[i] = newRow;
        }
        _rates = fxRates;
        _currencyList.add(denominator);
        _currencyList.add(numerator);
        _rates[originalEntries] = new double[] {fxRate};
        _rates[originalEntries + 1] = new double[0];
      } else {
        // numerator found
        final int originalEntries = _rates.length;
        final double[][] fxRates = new double[originalEntries + 1][];
        for (int i = 0; i < originalEntries; i++) {
          final double[] originalRow = _rates[i];
          final int originalLength = originalRow.length;
          final double[] newRow = new double[originalLength + 1];
          System.arraycopy(originalRow, 0, newRow, 0, originalLength);
          if (i == numeratorIndex) {
            newRow[originalLength] = 1 / fxRate;
          } else {
            newRow[originalLength] = -1;
          }
          fxRates[i] = newRow;
        }
        _rates = fxRates;
        _currencyList.add(denominator);
        _rates[originalEntries] = new double[0];
      }
    } else {
      if (numeratorIndex >= 0) {
        // numerator and denominator found
        final double existingRate;
        if (numeratorIndex > denominatorIndex) {
          existingRate = _rates[denominatorIndex][numeratorIndex - denominatorIndex - 1];
        } else {
          existingRate = _rates[numeratorIndex][denominatorIndex - numeratorIndex - 1];
        }
        if (Double.compare(existingRate, -1) != 0) {
          // already have a value for this pair
          throw new IllegalStateException("Already have a value for " + numerator + "/" + denominator);
        }
        if (numeratorIndex > denominatorIndex) {
          _rates[denominatorIndex][numeratorIndex - denominatorIndex - 1] = fxRate;
        } else {
          _rates[numeratorIndex][denominatorIndex - numeratorIndex - 1] = 1 / fxRate;
        }
      } else {
        // denominator found
        final int originalEntries = _rates.length;
        final double[][] fxRates = new double[originalEntries + 1][];
        for (int i = 0; i < originalEntries; i++) {
          final double[] originalRow = _rates[i];
          final int originalLength = originalRow.length;
          final double[] newRow = new double[originalLength + 1];
          System.arraycopy(originalRow, 0, newRow, 0, originalLength);
          if (i == denominatorIndex) {
            newRow[originalLength] = fxRate;
          } else {
            newRow[originalLength] = -1;
          }
          fxRates[i] = newRow;
        }
        _rates = fxRates;
        _currencyList.add(numerator);
        _rates[originalEntries] = new double[0];
      }
    }
  }

  @Override
  public double getFxRate(final Currency numerator, final Currency denominator) {
    ArgumentChecker.notNull(numerator, "numerator");
    ArgumentChecker.notNull(denominator, "denominator");
    if (numerator.equals(denominator)) {
      return 1;
    }
    final int numeratorIndex = _currencyList.indexOf(numerator);
    final int denominatorIndex = _currencyList.indexOf(denominator);
    ArgumentChecker.isTrue(numeratorIndex >= 0, "{} not found in FX matrix", numerator);
    ArgumentChecker.isTrue(denominatorIndex >= 0, "{} not found in FX matrix", denominator);
    final double fxRate;
    if (numeratorIndex > denominatorIndex) {
      fxRate = _rates[denominatorIndex][numeratorIndex - denominatorIndex - 1];
    } else {
      fxRate = 1 / _rates[numeratorIndex][denominatorIndex - numeratorIndex - 1];
    }
    if (Double.compare(fxRate, -1) == 0) {
      throw new IllegalStateException("Could not get value for " + numerator + "/" + denominator + " from " + toString());
    }
    return fxRate;
  }

  @Override
  public void updateRates(final Currency numerator, final Currency denominator, final double fxRate) {
    ArgumentChecker.notNull(numerator, "numerator");
    ArgumentChecker.notNull(denominator, "denominator");
    ArgumentChecker.isFalse(numerator.equals(denominator), "Cannot have equal numerator and denominator currency: {}", numerator);
    ArgumentChecker.isTrue(fxRate > 0, "FX rate must be greater than zero: have {}", fxRate);
    final int numeratorIndex = _currencyList.indexOf(numerator);
    final int denominatorIndex = _currencyList.indexOf(denominator);
    ArgumentChecker.isTrue(numeratorIndex >= 0, "{} not found in FX matrix", numerator);
    ArgumentChecker.isTrue(denominatorIndex >= 0, "{} not found in FX matrix", denominator);
    if (numeratorIndex > denominatorIndex) {
      _rates[denominatorIndex][numeratorIndex - denominatorIndex - 1] = fxRate;
    } else {
      _rates[numeratorIndex][denominatorIndex - numeratorIndex - 1] = 1 / fxRate;
    }
  }

  @Override
  public int getNumberOfCurrencies() {
    return _currencyList.size();
  }

  @Override
  public boolean containsPair(final Currency ccy1, final Currency ccy2) {
    ArgumentChecker.notNull(ccy1, "ccy1");
    ArgumentChecker.notNull(ccy2, "ccy2");
    final int numeratorIndex = _currencyList.indexOf(ccy1);
    final int denominatorIndex = _currencyList.indexOf(ccy2);
    if (numeratorIndex < 0 || denominatorIndex < 0) {
      return false;
    }
    final double fxRate;
    if (numeratorIndex > denominatorIndex) {
      fxRate = _rates[denominatorIndex][numeratorIndex - denominatorIndex - 1];
    } else {
      fxRate = 1 / _rates[numeratorIndex][denominatorIndex - numeratorIndex - 1];
    }
    if (Double.compare(fxRate, -1) == 0) {
      return false;
    }
    return true;
  }

  /**
   * Gets the currencies in this matrix.
   * @return  the currencies
   */
  public List<Currency> getCurrencyList() {
    return _currencyList;
  }

  /**
   * Gets the FX rates array, which is stored as an upper triangular matrix.
   * @return  the rates
   */
  public double[][] getFxRates() {
    return _rates;
  }

  @Override
  public CurrencyAmount convert(final MultipleCurrencyAmount amount, final Currency ccy) {
    ArgumentChecker.notNull(amount, "amount");
    ArgumentChecker.notNull(ccy, "ccy");
    CurrencyAmount sum = null;
    for (final CurrencyAmount ca : amount) {
      final double fxRate = getFxRate(ccy, ca.getCurrency());
      if (sum == null) {
        sum = CurrencyAmount.of(ccy, ca.getAmount() * fxRate);
      } else {
        sum = sum.plus(CurrencyAmount.of(ccy, ca.getAmount() * fxRate));
      }
    }
    return sum;
  }

  /**
   * Returns an immutable FX matrix containing the data in this object.
   * @return  an immutable FX matrix
   */
  public UncheckedImmutableFxMatrix asImmutable() {
    return UncheckedImmutableFxMatrix.of(this);
  }

  @Deprecated
  @Override
  public Map<Currency, Integer> getCurrencies() {
    throw new UnsupportedOperationException("This operation is not supported in SimpleMutableFxMatrix. Use getCurrencyList()");
  }

  @Deprecated
  @Override
  public double[][] getRates() {
    throw new UnsupportedOperationException("This operation is not supported in SimpleMutableFxMatrix. Use getFxRates()");
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = prime;
    result = prime * result + (_currencyList == null ? 0 : _currencyList.hashCode());
    result = prime * result + Arrays.deepHashCode(_rates);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    // note that the superclass is ignored
    if (!(obj instanceof UncheckedMutableFxMatrix)) {
      return false;
    }
    final UncheckedMutableFxMatrix other = (UncheckedMutableFxMatrix) obj;
    if (!Objects.equals(_currencyList, other._currencyList)) {
      return false;
    }
    if (!Arrays.deepEquals(_rates, other._rates)) {
      return false;
    }
    return true;
  }

}
