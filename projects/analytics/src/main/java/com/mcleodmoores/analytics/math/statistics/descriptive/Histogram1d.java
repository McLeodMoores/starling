/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 * A class representing a one-dimensional histogram.
 */
public final class Histogram1d {
  //TODO add ability to set max and min values
  //TODO int[], long[] etc

  /**
   * Creates a histogram given a number of buckets and the data. If all values in the data set are the same, the
   * data is approximately centered in the histogram, with the bucket width set to one.
   * @param numberOfBuckets  the number of buckets, greater than zero
   * @param data  the data, not null or empty
   * @return  a histogram
   */
  public static Histogram1d ofNumberOfBuckets(final int numberOfBuckets, final double[] data) {
    ArgumentChecker.notEmpty(data, "data");
    ArgumentChecker.isTrue(numberOfBuckets > 0, "Number of buckets must be greater than zero: have {}", numberOfBuckets);
    final int n = data.length;
    final double[] sortedData = new double[n];
    System.arraycopy(data, 0, sortedData, 0, n);
    Arrays.sort(sortedData);
    final double min = sortedData[0];
    final double max = sortedData[n - 1];
    final double binWidth;
    final boolean startFromZero;
    if (numberOfBuckets == 1) {
      return new Histogram1d(sortedData, numberOfBuckets, max - min, min, false);
    }
    if (Double.compare(min, max) == 0) {
      // make a decision - set lowest bucket to 0 and approximately centre the values
      binWidth = 2 * max / (numberOfBuckets - 1);
      startFromZero = true;
    } else {
      binWidth = (max - min) / (numberOfBuckets - 1);
      startFromZero = false;
    }
    return new Histogram1d(sortedData, numberOfBuckets, binWidth, min, startFromZero);
  }

  /**
   * Creates a histogram given a bin width and the data. If all values in the data set are the same, the
   * data is approximately centered in the histogram, with the lowest bucket value set to zero.
   * @param binWidth  the bin width, greater than zero
   * @param data  the data, not null or empty
   * @return  a histogram
   */
  public static Histogram1d ofBinWidth(final double binWidth, final double[] data) {
    ArgumentChecker.notEmpty(data, "data");
    ArgumentChecker.isTrue(binWidth > 0, "Bin width must be greater than zero: have {}", binWidth);
    final int n = data.length;
    final double[] sortedData = new double[n];
    System.arraycopy(data, 0, sortedData, 0, n);
    Arrays.sort(sortedData);
    final double min = sortedData[0];
    final double max = sortedData[n - 1];
    final int numberOfBuckets;
    final boolean startFromZero;
    if (Double.compare(min, max) == 0) {
      // make a decision - set lowest bucket to 0 and approximately centre the values
      startFromZero = true;
      numberOfBuckets = (int) (2 * max / binWidth) + 1;
    } else {
      startFromZero = false;
      numberOfBuckets = (int) ((max - min) / binWidth) + 1;
    }
    return new Histogram1d(sortedData, numberOfBuckets, binWidth, min, startFromZero);
  }

  private final double[] _lowerBinValues;
  private final double _binWidth;
  private final int[] _heights;

  private Histogram1d(final double[] sortedData, final int numberOfBuckets, final double binWidth, final double min, final boolean startFromZero) {
    _lowerBinValues = new double[numberOfBuckets];
    _heights = new int[numberOfBuckets];
    if (numberOfBuckets == 1) {
      _lowerBinValues[0] = min;
      _heights[0] = sortedData.length;
    } else {
      for (int i = 0; i < numberOfBuckets; i++) {
        _lowerBinValues[i] = i * binWidth + (startFromZero ? 0 : min);
      }
      for (final double d : sortedData) {
        final int bin = (int) ((d - (startFromZero ? 0 : min))/ binWidth);
        _heights[bin]++;
      }
    }
    _binWidth = binWidth;
  }

  /**
   * Gets the array of lower bin values.
   * @return  the lower bin values
   */
  public double[] getLowerBinValues() {
    return _lowerBinValues;
  }

  /**
   * Gets the array of heights.
   * @return  the heights
   */
  public int[] getHeights() {
    return _heights;
  }

  /**
   * Gets the bin width.
   * @return  the bin width
   */
  public double getBinWidth() {
    return _binWidth;
  }

  /**
   * Gets the height (i.e. number of entries) for a value. The value must be greater than or equal to the
   * lowest bin value and less than the highest bin value.
   * @param value  the value
   * @return  the height
   */
  public int getHeightForValue(final double value) {
    if (value < _lowerBinValues[0] || value >= _lowerBinValues[size() - 1] + _binWidth) {
      throw new IllegalArgumentException("Value " + value + " outside of the domain of the histogram " + _lowerBinValues[0] + " to " + _lowerBinValues[size() - 1]);
    }
    if (value > _lowerBinValues[size() - 1]) {
      return _heights[size() - 1];
    }
    final int index = Arrays.binarySearch(_lowerBinValues, value);
    if (index < 0) {
      return _heights[-index - 1];
    }
    return _heights[index];
  }

  /**
   * Gets the nth lower bin value. Throws <code>ArrayIndexOutOfBoundsException</code> if the value is less than zero or greater
   * than the number of bins.
   * @param n  the number
   * @return  the lower bin value
   */
  public double getNthLowerBound(final int n) {
    return _lowerBinValues[n];
  }

  /**
   * Gets the nth height (i.e. number of entries). Throws <code>ArrayIndexOutOfBoundsException</code> if the value is less than zero or greater
   * than the number of bins.
   * @param n  the number
   * @return  the height
   */
  public int getNthHeight(final int n) {
    return _heights[n];
  }

  /**
   * Gets the number of bins.
   * @return  the number of bins
   */
  public int size() {
    return _lowerBinValues.length;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_heights);
    result = prime * result + Arrays.hashCode(_lowerBinValues);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof Histogram1d)) {
      return false;
    }
    final Histogram1d other = (Histogram1d) obj;
    if (!Arrays.equals(_heights, other._heights)) {
      return false;
    }
    if (!Arrays.equals(_lowerBinValues, other._lowerBinValues)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Histogram1d[");
    for (int i = 0; i < size() - 1; i++) {
      sb.append("[");
      sb.append(_lowerBinValues[i]);
      sb.append(", ");
      sb.append(_lowerBinValues[i + 1]);
      sb.append(")=");
      sb.append(_heights[i]);
      sb.append(", ");
    }
    sb.append("[");
    sb.append("[");
    sb.append(_lowerBinValues[size() - 1]);
    sb.append(", ");
    sb.append(_lowerBinValues[size() - 1] + _binWidth);
    sb.append(")=");
    sb.append(_heights[size() - 1]);
    sb.append("]");
    return sb.toString();
  }

}
