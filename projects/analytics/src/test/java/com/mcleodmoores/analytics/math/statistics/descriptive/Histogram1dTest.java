/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.math.statistics.descriptive;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.internal.junit.ArrayAsserts.assertArrayEquals;

import org.testng.annotations.Test;

/**
 * Unit tests for {@link Histogram1d}.
 */
public class Histogram1dTest {

  /**
   * Tests the case where there is only one bucket.
   */
  @Test
  public void testOneBucket1() {
    final int n = 100;
    final double[] data = new double[n];
    final double expectedBinWidth = 99;
    final double[] expectedLowerBinValues = new double[] {0};
    final int[] expectedBinHeights = new int[] {n};
    for (int i = 0; i < n; i++) {
      data[i] = i;
    }
    final int numberOfBuckets = 1;
    final Histogram1d histogram = Histogram1d.ofNumberOfBuckets(numberOfBuckets, data);
    assertEquals(histogram.getBinWidth(), expectedBinWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedBinHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests the case where there is only one bucket.
   */
  @Test
  public void testOneBucket2() {
    final int n = 100;
    final double[] data = new double[n];
    final double binWidth = 101;
    final double[] expectedLowerBinValues = new double[] {0};
    final int[] expectedBinHeights = new int[] {n};
    for (int i = 0; i < n; i++) {
      data[i] = i;
    }
    final Histogram1d histogram = Histogram1d.ofBinWidth(binWidth, data);
    assertEquals(histogram.getBinWidth(), binWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedBinHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests the case where each value falls on a lower boundary and is unique.
   */
  @Test
  public void testOneValueInEachBucket1() {
    final int n = 100;
    final double[] data = new double[n];
    final double expectedBinWidth = 1;
    final double[] expectedLowerBinValues = new double[n];
    final int[] expectedBinHeights = new int[n];
    for (int i = 0; i < n; i++) {
      data[i] = i;
      expectedLowerBinValues[i] = i;
      expectedBinHeights[i] = 1;
    }
    final int numberOfBuckets = n;
    final Histogram1d histogram = Histogram1d.ofNumberOfBuckets(numberOfBuckets, data);
    assertEquals(histogram.getBinWidth(), expectedBinWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedBinHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests the case where each value falls on a lower boundary and is unique.
   */
  @Test
  public void testOneValueInEachBucket2() {
    final int n = 100;
    final double[] data = new double[n];
    final double binWidth = 1;
    final double[] expectedLowerBinValues = new double[n];
    final int[] expectedBinHeights = new int[n];
    for (int i = 0; i < n; i++) {
      data[i] = i;
      expectedLowerBinValues[i] = i;
      expectedBinHeights[i] = 1;
    }
    final Histogram1d histogram = Histogram1d.ofBinWidth(binWidth, data);
    assertEquals(histogram.getBinWidth(), binWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedBinHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests the case where each value is in the same bucket.
   */
  @Test
  public void testAllValuesInOneBucket1() {
    final int n = 100;
    final double value = 7;
    final double[] data = new double[n];
    final double expectedBinWidth = 2 * value / (n - 1);
    final double[] expectedLowerBinValues = new double[n];
    final int[] expectedBinHeights = new int[n];
    for (int i = 0; i < n; i++) {
      data[i] = value;
      expectedLowerBinValues[i] = i * expectedBinWidth;
      expectedBinHeights[i] = i == n / 2 - 1 ? n : 0;
    }
    final int numberOfBuckets = n;
    final Histogram1d histogram = Histogram1d.ofNumberOfBuckets(numberOfBuckets, data);
    assertEquals(histogram.getBinWidth(), expectedBinWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedBinHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests the case where each value is in the same bucket.
   */
  @Test
  public void testAllValuesInOneBucket2() {
    final int n = 100;
    final double value = 7;
    final double[] data = new double[n];
    final double binWidth = 2 * value / (n - 1);
    final double[] expectedLowerBinValues = new double[n];
    final int[] expectedBinHeights = new int[n];
    for (int i = 0; i < n; i++) {
      data[i] = value;
      expectedLowerBinValues[i] = i * binWidth;
      expectedBinHeights[i] = i == n / 2 - 1 ? n : 0;
    }
    final Histogram1d histogram = Histogram1d.ofBinWidth(binWidth, data);
    assertEquals(histogram.getBinWidth(), binWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedBinHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests a small histogram where each value falls on a lower boundary.
   */
  @Test
  public void testValuesOnBoundaries1() {
    final double[] data = new double[] {0, 1, 1, 1, 3, 4, 4, 4, 4, 6, 9, 9, 9, 5};
    final int numberOfBuckets = 10;
    final double expectedBinWidth = 1;
    final double[] expectedLowerBinValues = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    final int[] expectedHeights = new int[] {1, 3, 0, 1, 4, 1, 1, 0, 0, 3};
    final Histogram1d histogram = Histogram1d.ofNumberOfBuckets(numberOfBuckets, data);
    assertEquals(histogram.getBinWidth(), expectedBinWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests a small histogram where each value falls on a lower boundary.
   */
  @Test
  public void testValuesOnBoundaries2() {
    final double[] data = new double[] {0, 1, 1, 1, 3, 4, 4, 4, 4, 6, 9, 9, 9, 5};
    final double binWidth = 1;
    final double[] expectedLowerBinValues = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    final int[] expectedHeights = new int[] {1, 3, 0, 1, 4, 1, 1, 0, 0, 3};
    final Histogram1d histogram = Histogram1d.ofBinWidth(binWidth, data);
    assertEquals(histogram.getBinWidth(), binWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests a small histogram.
   */
  @Test
  public void testValuesInBins1() {
    final double[] data = new double[] {0, 0.9, 1.5, 1.76, 3, 4.99, 4.3, 4.2, 4.1, 6.6, 8.99999999, 9, 8.123, 5.4};
    final int numberOfBuckets = 10;
    final double expectedBinWidth = 1;
    final double[] expectedLowerBinValues = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    final int[] expectedHeights = new int[] {2, 2, 0, 1, 4, 1, 1, 0, 2, 1};
    final Histogram1d histogram = Histogram1d.ofNumberOfBuckets(numberOfBuckets, data);
    assertEquals(histogram.getBinWidth(), expectedBinWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests a small histogram.
   */
  @Test
  public void testValuesInBins2() {
    final double[] data = new double[] {0, 0.9, 1.5, 1.76, 3, 4.99, 4.3, 4.2, 4.1, 6.6, 8.99999999, 9, 8.123, 5.4};
    final double binWidth = 1;
    final double[] expectedLowerBinValues = new double[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    final int[] expectedHeights = new int[] {2, 2, 0, 1, 4, 1, 1, 0, 2, 1};
    final Histogram1d histogram = Histogram1d.ofBinWidth(binWidth, data);
    assertEquals(histogram.getBinWidth(), binWidth, 1e-15);
    assertArrayEquals(histogram.getHeights(), expectedHeights);
    assertArrayEquals(histogram.getLowerBinValues(), expectedLowerBinValues, 1e-15);
  }

  /**
   * Tests the object.
   */
  @Test
  public void testObject() {
    final double[] data = new double[] {1.0, 0.9, 1.5, 1.76, 3, 4.99, 4.3, 4.2, 4.1, 6.6, 8.99999999, 9, 8.123, 5.4};
    final double binWidth = 1;
    final Histogram1d histogram = Histogram1d.ofBinWidth(binWidth, data);
    assertEquals(histogram.size(), 9);
    assertEquals(histogram.toString(), "Histogram1d[[0.9, 1.9)=4, [1.9, 2.9)=0, [2.9, 3.9)=1, [3.9, 4.9)=3, [4.9, 5.9)=2, [5.9, 6.9)=1, [6.9, 7.9)=0, [7.9, 8.9)=1, [[8.9, 9.9)=2]");
    Histogram1d other = Histogram1d.ofBinWidth(binWidth, data);
    assertEquals(histogram, other);
    assertEquals(histogram.hashCode(), other.hashCode());
    other = Histogram1d.ofBinWidth(2, data);
    assertNotEquals(histogram, other);
  }

  /**
   * Tests getting individual values for the lower bin value and height.
   */
  @Test
  public void testIndividualValues() {
    final double[] data = new double[] {1.0, 0.9, 1.5, 1.76, 3, 4.99, 4.3, 4.2, 4.1, 6.6, 8.99999999, 9, 8.123, 5.4};
    final double binWidth = 0.82;
    final Histogram1d histogram = Histogram1d.ofBinWidth(binWidth, data);
    assertEquals(histogram.getHeightForValue(0.9), 3);
    assertEquals(histogram.getHeightForValue(1.9), 1);
    assertEquals(histogram.getHeightForValue(8.45), 2);
    assertEquals(histogram.getNthHeight(5), 1);
    assertEquals(histogram.getNthLowerBound(3), 3.36, 1e-15);
  }

  /**
   * Tests that the data cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData1() {
    Histogram1d.ofBinWidth(100, null);
  }

  /**
   * Tests that the data cannot be null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullData2() {
    Histogram1d.ofNumberOfBuckets(100, null);
  }

  /**
   * Tests that the data cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData1() {
    Histogram1d.ofBinWidth(100, new double[0]);
  }

  /**
   * Tests that the data cannot be empty.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testEmptyData2() {
    Histogram1d.ofNumberOfBuckets(100, new double[0]);
  }

  /**
   * Tests that the number of buckets must be positive.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNumberOfBucketsIsGreaterThanZero() {
    Histogram1d.ofNumberOfBuckets(-1, new double[] {1, 2, 3});
  }

  /**
   * Tests that the bin width must be positive.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testBinWidthIsGreaterThanZero() {
    Histogram1d.ofBinWidth(-20, new double[] {2, 5, 6});
  }

  /**
   * Tests that the value must be within the bin range.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValueBelowMin() {
    Histogram1d.ofNumberOfBuckets(2, new double[] {1, 2, 3}).getHeightForValue(0);
  }

  /**
   * Tests that the value must be within the bin range.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testValueAboveMax() {
    Histogram1d.ofBinWidth(1, new double[] {1, 2, 3}).getHeightForValue(4);
  }

}
