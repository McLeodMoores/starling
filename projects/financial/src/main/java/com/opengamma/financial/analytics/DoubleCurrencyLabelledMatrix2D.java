/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.CompareUtils;
import com.opengamma.util.money.Currency;

/**
 * Represents a two-dimensional matrix labelled by tenor and currency.
 */
public class DoubleCurrencyLabelledMatrix2D extends LabelledMatrix2D<Double, Currency> {

  private static final Logger LOGGER = LoggerFactory.getLogger(DoubleCurrencyLabelledMatrix2D.class);

  public DoubleCurrencyLabelledMatrix2D(final Double[] xKeys, final Currency[] yKeys, final double[][] values) {
    super(xKeys, yKeys, values);
  }

  public DoubleCurrencyLabelledMatrix2D(final Double[] xKeys, final Object[] xLabels, final Currency[] yKeys, final Object[] yLabels, final double[][] values) {
    super(xKeys, xLabels, yKeys, yLabels, values);
  }

  public DoubleCurrencyLabelledMatrix2D(final Double[] xKeys, final Object[] xLabels, final String xTitle, final Currency[] yKeys, final Object[] yLabels,
      final String yTitle, final double[][] values, final String valuesTitle) {
    super(xKeys, xLabels, xTitle, yKeys, yLabels, yTitle, values, valuesTitle);
  }

  @Override
  public <X> int compareX(final Double d1, final Double d2, final X tolerance) {
    try {
      final double tol = (Double) tolerance;
      return CompareUtils.compareWithTolerance(d1, d2, tol);
    } catch (final Exception e) {
      throw new OpenGammaRuntimeException(e.getMessage());
    }
  }

  @Override
  public <Y> int compareY(final Currency key1, final Currency key2, final Y tolerance) {
    return key1.compareTo(key2);
  }

  @Override
  public DoubleCurrencyLabelledMatrix2D getMatrix(final Double[] xKeys, final Object[] xLabels, final String xTitle, final Currency[] yKeys,
      final Object[] yLabels, final String yTitle, final double[][] values, final String valuesTitle) {
    return new DoubleCurrencyLabelledMatrix2D(xKeys, xLabels, xTitle, yKeys, yLabels, yTitle, values, valuesTitle);
  }

  @Override
  public DoubleCurrencyLabelledMatrix2D getMatrix(final Double[] xKeys, final Object[] xLabels, final Currency[] yKeys, final Object[] yLabels,
      final double[][] values) {
    return new DoubleCurrencyLabelledMatrix2D(xKeys, xLabels, yKeys, yLabels, values);
  }

  /**
   * Adds a matrix to this one and returns a new matrix.
   *
   * In this implementation, the labels are used in place of the numeric keys to aggregate in the first dimension, and the result matrix will contain the
   * lowest-valued numeric key as its key for that label.
   *
   * @param other
   *          the matrix to add, not null
   * @return the sum of the matrices, not null
   */
  public DoubleCurrencyLabelledMatrix2D addUsingDoubleLabels(final DoubleCurrencyLabelledMatrix2D other) {
    ArgumentChecker.notNull(other, "other");

    final int maxXSize = getXKeys().length + other.getXKeys().length;
    Object[] resultXLabels = new Object[maxXSize];
    Double[] resultXKeys = new Double[maxXSize];
    final int[] xIdxMap = new int[maxXSize];
    final int[] otherXIdxMap = new int[maxXSize];
    int x = 0;
    int otherX = 0;

    int resultIdx = 0;

    while (x < getXLabels().length && otherX < other.getXLabels().length) {
      final Object xLabel = getXLabels()[x];
      final double xKey = getXKeys()[x];
      final Object otherXLabel = other.getXLabels()[otherX];
      final double otherXKey = other.getXKeys()[otherX];
      int xIdxMapValue;
      int otherXIdxMapValue;
      if (xLabel.equals(otherXLabel)) {
        resultXLabels[resultIdx] = xLabel;
        resultXKeys[resultIdx] = Math.min(xKey, otherXKey);
        xIdxMapValue = x;
        otherXIdxMapValue = otherX;
        x++;
        otherX++;
      } else if (xKey < otherXKey) {
        resultXLabels[resultIdx] = xLabel;
        resultXKeys[resultIdx] = xKey;
        xIdxMapValue = x;
        otherXIdxMapValue = -1;
        x++;
      } else if (otherXKey < xKey) {
        resultXLabels[resultIdx] = otherXLabel;
        resultXKeys[resultIdx] = otherXKey;
        xIdxMapValue = -1;
        otherXIdxMapValue = otherX;
        otherX++;
      } else {
        LOGGER.debug("Same key " + xKey + " used for different labels in the two matrices: " + xLabel + " and " + otherXLabel);
        resultXLabels[resultIdx] = xLabel;
        resultXKeys[resultIdx] = xKey;
        xIdxMapValue = x;
        otherXIdxMapValue = otherX;
        x++;
        otherX++;
      }
      xIdxMap[resultIdx] = xIdxMapValue;
      otherXIdxMap[resultIdx] = otherXIdxMapValue;
      resultIdx++;
    }
    for (; x < getXLabels().length; x++) {
      resultXLabels[resultIdx] = getXLabels()[x];
      resultXKeys[resultIdx] = getXKeys()[x];
      xIdxMap[resultIdx] = x;
      otherXIdxMap[resultIdx] = -1;
      resultIdx++;
    }
    for (; otherX < other.getXLabels().length; otherX++) {
      resultXLabels[resultIdx] = other.getXLabels()[otherX];
      resultXKeys[resultIdx] = other.getXKeys()[otherX];
      xIdxMap[resultIdx] = -1;
      otherXIdxMap[resultIdx] = otherX;
      resultIdx++;
    }
    resultXKeys = Arrays.copyOf(resultXKeys, resultIdx);
    resultXLabels = Arrays.copyOf(resultXLabels, resultIdx);

    resultIdx = 0;

    final int maxYSize = getYKeys().length + other.getYKeys().length;
    Object[] resultYLabels = new Object[maxYSize];
    Currency[] resultYKeys = new Currency[maxYSize];
    final int[] yIdxMap = new int[maxYSize];
    final int[] otherYIdxMap = new int[maxYSize];
    int y = 0;
    int otherY = 0;

    while (y < getYLabels().length && otherY < other.getYLabels().length) {
      final Object yLabel = getYLabels()[y];
      final Currency yKey = getYKeys()[y];
      final Object otherYLabel = other.getYLabels()[otherY];
      final Currency otherYKey = other.getYKeys()[otherY];
      int yIdxMapValue;
      int otherYIdxMapValue;
      if (yKey.equals(otherYKey)) {
        resultYLabels[resultIdx] = yLabel;
        resultYKeys[resultIdx] = yKey;
        yIdxMapValue = y;
        otherYIdxMapValue = otherY;
        y++;
        otherY++;
      } else if (yKey.compareTo(otherYKey) < 0) {
        resultYLabels[resultIdx] = yLabel;
        resultYKeys[resultIdx] = yKey;
        yIdxMapValue = y;
        otherYIdxMapValue = -1;
        y++;
      } else {
        resultYLabels[resultIdx] = otherYLabel;
        resultYKeys[resultIdx] = otherYKey;
        yIdxMapValue = -1;
        otherYIdxMapValue = otherY;
        otherY++;
      }
      yIdxMap[resultIdx] = yIdxMapValue;
      otherYIdxMap[resultIdx] = otherYIdxMapValue;
      resultIdx++;
    }
    for (; y < getYLabels().length; y++) {
      resultYLabels[resultIdx] = getYLabels()[y];
      resultYKeys[resultIdx] = getYKeys()[y];
      yIdxMap[resultIdx] = y;
      otherYIdxMap[resultIdx] = -1;
      resultIdx++;
    }
    for (; otherY < other.getYKeys().length; otherY++) {
      resultYLabels[resultIdx] = other.getYLabels()[otherY];
      resultYKeys[resultIdx] = other.getYKeys()[otherY];
      yIdxMap[resultIdx] = -1;
      otherYIdxMap[resultIdx] = otherY;
      resultIdx++;
    }
    resultYKeys = Arrays.copyOf(resultYKeys, resultIdx);
    resultYLabels = Arrays.copyOf(resultYLabels, resultIdx);

    final double[][] resultValues = new double[resultYKeys.length][resultXKeys.length];
    for (int resultX = 0; resultX < resultXKeys.length; resultX++) {
      final int xIdx = xIdxMap[resultX];
      final int otherXIdx = otherXIdxMap[resultX];
      for (int resultY = 0; resultY < resultYKeys.length; resultY++) {
        final int yIdx = yIdxMap[resultY];
        final int otherYIdx = otherYIdxMap[resultY];
        if (xIdx >= 0 && yIdx >= 0) {
          resultValues[resultY][resultX] = getValues()[yIdx][xIdx];
        }
        if (otherXIdx >= 0 && otherYIdx >= 0) {
          resultValues[resultY][resultX] += other.getValues()[otherYIdx][otherXIdx];
        }
      }
    }

    return getMatrix(resultXKeys, resultXLabels, getXTitle(), resultYKeys, resultYLabels, getYTitle(), resultValues, getValuesTitle());
  }

}
