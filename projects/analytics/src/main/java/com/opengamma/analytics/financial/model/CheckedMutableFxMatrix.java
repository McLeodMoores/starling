/**
 * Copyright (C) 2016 - Present McLeod Moores Software Limited.  All rights reserved.
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

public class CheckedMutableFxMatrix extends FXMatrix {

  public static CheckedMutableFxMatrix of() {
    return new CheckedMutableFxMatrix();
  }

  private final List<Currency> _currencyList;
  private boolean[][] _supplied;
  private double[][] _rates;

  private CheckedMutableFxMatrix() {
    _currencyList = new ArrayList<>();
    _supplied = new boolean[0][0];
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
        final double[][] rates = new double[originalEntries + 2][];
        final boolean[][] supplied = new boolean[originalEntries + 2][];
        for (int i = 0; i < originalEntries; i++) {
          final double[] originalRowRates = _rates[i];
          final boolean[] originalRowSupplied = _supplied[i];
          final int originalLength = originalRowRates.length;
          final double[] newRowRates = new double[originalLength + 2];
          final boolean[] newRowSupplied = new boolean[originalLength + 2];
          System.arraycopy(originalRowRates, 0, newRowRates, 0, originalLength);
          System.arraycopy(originalRowSupplied, 0, newRowSupplied, 0, originalLength);
          // add -1 for each cross that doesn't have a rate set
          newRowRates[originalLength] = -1;
          newRowRates[originalLength + 1] = -1;
          newRowSupplied[originalLength] = false;
          newRowSupplied[originalLength + 1] = false;
          rates[i] = newRowRates;
          supplied[i] = newRowSupplied;
        }
        _rates = rates;
        _supplied = supplied;
        _currencyList.add(denominator);
        _currencyList.add(numerator);
        _rates[originalEntries] = new double[] {fxRate};
        _supplied[originalEntries] = new boolean[] {true};
        _rates[originalEntries + 1] = new double[0];
        _supplied[originalEntries + 1] = new boolean[0];
      } else {
        // numerator found
        final int originalEntries = _rates.length;
        final double[][] rates = new double[originalEntries + 1][];
        final boolean[][] supplied = new boolean[originalEntries + 1][];
        for (int i = 0; i < originalEntries; i++) {
          final double[] originalRowRates = _rates[i];
          final boolean[] originalRowSupplied = _supplied[i];
          final int originalLength = originalRowRates.length;
          final double[] newRowRates = new double[originalLength + 1];
          final boolean[] newRowSupplied = new boolean[originalLength + 1];
          System.arraycopy(originalRowRates, 0, newRowRates, 0, originalLength);
          System.arraycopy(originalRowSupplied, 0, newRowSupplied, 0, originalLength);
          if (i == numeratorIndex) {
            newRowRates[originalLength] = 1 / fxRate;
            newRowSupplied[originalLength] = true; //TODO not strictly speaking true
          } else {
            newRowRates[originalLength] = -1;
          }
          rates[i] = newRowRates;
          supplied[i] = newRowSupplied;
        }
        _rates = rates;
        _supplied = supplied;
        _currencyList.add(denominator);
        _rates[originalEntries] = new double[0];
        _supplied[originalEntries] = new boolean[0];
      }
    } else {
      if (numeratorIndex >= 0) {
        // numerator and denominator found, check that the rate is consistent with that that is supplied
        final boolean supplied;
        final double existingRate = getFxRate(numerator, denominator);
        // find out if rate was previously supplied
        if (numeratorIndex > denominatorIndex) {
          supplied = _supplied[denominatorIndex][numeratorIndex - denominatorIndex - 1];
        } else {
          supplied = _supplied[numeratorIndex][denominatorIndex - numeratorIndex - 1];
        }
        if (supplied) {
          throw new IllegalStateException("Already have a value for " + numerator + "/" + denominator);
        }
        checkRatesAreConsistent(numerator, denominator, fxRate, existingRate);
        // prefer supplied rate to one that is implied
        if (numeratorIndex > denominatorIndex) {
          _rates[denominatorIndex][numeratorIndex - denominatorIndex - 1] = fxRate;
          _supplied[denominatorIndex][numeratorIndex - denominatorIndex - 1] = true;
        } else {
          _rates[numeratorIndex][denominatorIndex - numeratorIndex - 1] = 1 / fxRate;
          _supplied[numeratorIndex][denominatorIndex - numeratorIndex - 1] = true; //TODO
        }
      } else {
        // denominator found
        final int originalEntries = _rates.length;
        final double[][] rates = new double[originalEntries + 1][];
        final boolean[][] supplied = new boolean[originalEntries + 1][];
        for (int i = 0; i < originalEntries; i++) {
          final double[] originalRowRates = _rates[i];
          final boolean[] originalRowSupplied = _supplied[i];
          final int originalLength = originalRowRates.length;
          final double[] newRowRates = new double[originalLength + 1];
          final boolean[] newRowSupplied = new boolean[originalLength + 1];
          System.arraycopy(originalRowRates, 0, newRowRates, 0, originalLength);
          System.arraycopy(originalRowSupplied, 0, newRowSupplied, 0, originalLength);
          if (i == denominatorIndex) {
            newRowRates[originalLength] = fxRate;
            newRowSupplied[originalLength] = true;
          } else {
            newRowRates[originalLength] = -1;
          }
          rates[i] = newRowRates;
          supplied[i] = newRowSupplied;
        }
        _rates = rates;
        _supplied = supplied;
        _currencyList.add(numerator);
        _rates[originalEntries] = new double[0];
        _supplied[originalEntries] = new boolean[0];
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
    final double suppliedRate;
    final boolean supplied;
    // try to get the rate from values that have been populated
    if (numeratorIndex > denominatorIndex) {
      supplied = _supplied[denominatorIndex][numeratorIndex - denominatorIndex - 1];
      suppliedRate = _rates[denominatorIndex][numeratorIndex - denominatorIndex - 1];
    } else {
      supplied = _supplied[numeratorIndex][denominatorIndex - numeratorIndex - 1];
      suppliedRate = 1 / _rates[numeratorIndex][denominatorIndex - numeratorIndex - 1];
    }
    if (supplied) {
      return suppliedRate;
    }
    //TODO this logic is not right - see failing tests
    for (int i = 0; i < _rates.length; i++) {
      if (denominatorIndex == i) {
        // match on denominator, look for an appropriate cross for each rate in this row until one is found
        for (int j = 0; j < _rates[i].length; j++) {
          if (_supplied[i][j]) {
            // have something, see if there's a cross
            final int offsetRow = i + j + 1;
            final int offsetColumn = numeratorIndex - offsetRow - 1;
            if (_supplied[offsetRow][offsetColumn]) {
              final double r1 = _rates[i][j];
              final double r2 = _rates[offsetRow][offsetColumn];
              return r1 * r2;
            }
          }
        }
      } else if (numeratorIndex == i) {
        // match on numerator, look for an appropriate cross for each rate in this row until one is found
        for (int j = 0; j < _rates[i].length; j++) {
          if (_supplied[i][j]) {
            // have something, see if there's a cross
            final int offsetRow = i + j + 1;
            final int offsetColumn = denominatorIndex - offsetRow - 1;
            if (_supplied[offsetRow][offsetColumn]) {
              final double r1 = _rates[i][j];
              final double r2 = _rates[offsetRow][offsetColumn];
              return 1 / (r1 * r2);
            }
          }
        }
      } else {
        final int denominatorColumn = denominatorIndex - i - 1;
        final int numeratorColumn = numeratorIndex - i - 1;
        if (denominatorColumn >= 0 && numeratorColumn >= 0) {
          if (_supplied[i][denominatorColumn] && _supplied[i][numeratorColumn]) {
            // there are values set for both the numerator and denominator currency against this row's currency
            final double r1 = _rates[i][denominatorColumn];
            final double r2 = _rates[i][numeratorColumn];
            return r2 / r1;
          }
          // try to find a match from a different row
          if (_supplied[i][denominatorColumn] && _supplied[denominatorColumn][numeratorColumn]) {
            final double r1 = _rates[i][denominatorColumn];
            final double r2 = _rates[i][numeratorColumn];
            return r2 / r1;
          }
        }
      }
    }
    throw new IllegalStateException("Could not find two provided rates that produce " + numerator + "/" + denominator);
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
    final double existingRate = getFxRate(numerator, denominator);
    // check that the values are consistent
    checkRatesAreConsistent(numerator, denominator, fxRate, existingRate);
    if (numeratorIndex > denominatorIndex) {
      _rates[denominatorIndex][numeratorIndex - denominatorIndex - 1] = fxRate;
      _supplied[denominatorIndex][numeratorIndex - denominatorIndex - 1] = true;
    } else {
      _rates[numeratorIndex][denominatorIndex - numeratorIndex - 1] = 1 / fxRate;
      _supplied[numeratorIndex][denominatorIndex - numeratorIndex - 1] = true;
    }
  }

  /**
   * Checks that a rate that is already in the matrix is consistent with the new rate. The rates are matched to 2 decimal places if the
   * denominator is JPY, otherwise the match is done to 4 d.p.
   * @param numerator  the numerator, not null
   * @param denominator  the denominator, not null
   * @param newRate  the new rate, not null
   * @param existingRate  the existing rate, not null
   */
  private static void checkRatesAreConsistent(final Currency numerator, final Currency denominator, final double newRate,
      final double existingRate) {
    if (denominator.equals(Currency.JPY)) {
      // 2dp matching
      final int existingRate100 = (int) (existingRate * 100);
      final int fxRate100 = (int) (newRate * 100);
      if (existingRate100 != fxRate100) {
        throw new IllegalStateException("Implied FX rate for " + numerator + "/" + denominator + " " + existingRate
            + " was inconsistent with the provided rate " + newRate);
      }
    } else {
      // 4dp matching
      final int existingRate100 = (int) (existingRate * 100);
      final int fxRate100 = (int) (newRate * 100);
      if (existingRate100 != fxRate100) {
        throw new IllegalStateException("Implied FX rate for " + numerator + "/" + denominator + " " + existingRate
            + " was inconsistent with the provided rate " + newRate);
      }
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
    try {
      // nasty, but ensures consistency with getFxRates()
      getFxRate(ccy1, ccy2);
      return true;
    } catch (final IllegalStateException | IllegalArgumentException e) {
      return false;
    }
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
  public ImmutableFxMatrix asImmutable() {
    return ImmutableFxMatrix.of(this);
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
    // don't need to include supplied matrix as that is redundant information
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    // note that the superclass is ignored
    if (!(obj instanceof CheckedMutableFxMatrix)) {
      return false;
    }
    final CheckedMutableFxMatrix other = (CheckedMutableFxMatrix) obj;
    if (!Objects.equals(_currencyList, other._currencyList)) {
      return false;
    }
    if (!Arrays.deepEquals(_rates, other._rates)) {
      return false;
    }
    // don't need to include supplied matrix as that is redundant information
    return true;
  }
}
