/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util;

/**
 * Performs sorting. This is not "parallel" in the sense of threads, but parallel in the sense that
 * two arrays are sorted in parallel.
 */
public class ParallelArrayBinarySort {

  /**
   * Sort the content of keys and values simultaneously so that
   * both match the correct ordering. Alters the arrays in place
   * @param keys The keys
   * @param values The values
   */
  public static void parallelBinarySort(final double[] keys, final double[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and values simultaneously so that
   * both match the correct ordering. Alters the arrays in place
   * @param keys The keys
   * @param values The values
   */
  public static void parallelBinarySort(final float[] keys, final double[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and values simultaneously so that
   * both match the correct ordering. Alters the arrays in place
   * @param keys The keys
   * @param values The values
   */
  public static void parallelBinarySort(final int[] keys, final double[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }


  /**
   * Sort the content of keys and values simultaneously so that
   * both match the correct ordering. Alters the arrays in place
   * @param keys The keys
   * @param values The values
   */
  public static void parallelBinarySort(final long[] keys, final double[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and apply the same sort to the values.
   * Alters the arrays in place.
   * @param keys The keys
   * @param values The values
   */
  public static void parallelBinarySort(final float[] keys, final int[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and apply the same sort to the values.
   * Alters the arrays in place.
   * @param keys The keys
   * @param values The values
   */
  public static void parallelBinarySort(final int[] keys, final int[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and apply the same sort to the values.
   * Alters the arrays in place.
   * @param keys The keys
   * @param values The values
   */
  public static void parallelBinarySort(final long[] keys, final int[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and apply the same sort to the values.
   * Alters the arrays in place.
   * @param keys The keys
   * @param values The values
   */
  public static void parallelBinarySort(final double[] keys, final int[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and values simultaneously so that
   * both match the correct ordering. Alters the arrays in place
   * @param <T> The type of the keys
   * @param <U> The type of the values
   * @param keys The keys
   * @param values The values
   */
  public static <T extends Comparable<T>, U> void parallelBinarySort(final T[] keys, final U[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /**
   * Sort the content of keys and values simultaneously so that
   * both match the correct ordering. Alters the arrays in place
   * @param <T> The type of the values
   * @param keys The keys
   * @param values The values
   */
  public static <T> void parallelBinarySort(final double[] keys, final T[] values) {
    ArgumentChecker.notNull(keys, "keys");
    ArgumentChecker.notNull(values, "values");
    ArgumentChecker.isTrue(keys.length == values.length, "Key and value arrays must have the same length");
    final int n = keys.length;
    dualArrayQuickSort(keys, values, 0, n - 1);
  }

  /** quick sorts */
  /** hard coded types */
  private static void dualArrayQuickSort(final double[] keys, final double[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static void dualArrayQuickSort(final float[] keys, final double[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static void dualArrayQuickSort(final int[] keys, final double[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static void dualArrayQuickSort(final long[] keys, final double[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static void dualArrayQuickSort(final double[] keys, final int[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static void dualArrayQuickSort(final float[] keys, final int[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static void dualArrayQuickSort(final int[] keys, final int[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static void dualArrayQuickSort(final long[] keys, final int[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  /** bendy types */
  private static <T extends Comparable<T>, U> void dualArrayQuickSort(final T[] keys, final U[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  private static <T> void dualArrayQuickSort(final double[] keys, final T[] values, final int left, final int right) {
    if (right > left) {
      final int pivot = left + right >> 1;
      final int pivotNewIndex = partition(keys, values, left, right, pivot);
      dualArrayQuickSort(keys, values, left, pivotNewIndex - 1);
      dualArrayQuickSort(keys, values, pivotNewIndex + 1, right);
    }
  }

  /** partitions */
  /** hard coded types */
  private static int partition(final double[] keys, final double[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static int partition(final float[] keys, final double[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static int partition(final int[] keys, final double[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static int partition(final long[] keys, final double[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static int partition(final double[] keys, final int[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static int partition(final float[] keys, final int[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static int partition(final int[] keys, final int[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static int partition(final long[] keys, final int[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  /** bendy types */
  private static <T extends Comparable<T>, U> int partition(final T[] keys, final U[] values, final int left, final int right, final int pivot) {
    final T pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i].compareTo(pivotValue) < 1) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  private static <T> int partition(final double[] keys, final T[] values, final int left, final int right, final int pivot) {
    final double pivotValue = keys[pivot];
    swap(keys, values, pivot, right);
    int storeIndex = left;
    for (int i = left; i < right; i++) {
      if (keys[i] <= pivotValue) {
        swap(keys, values, i, storeIndex);
        storeIndex++;
      }
    }
    swap(keys, values, storeIndex, right);
    return storeIndex;
  }

  /** swappers */
  /** hardcoded */
  private static void swap(final double[] keys, final double[] values, final int first, final int second) {
    double t = keys[first];
    keys[first] = keys[second];
    keys[second] = t;
    t = values[first];
    values[first] = values[second];
    values[second] = t;
  }

  private static void swap(final float[] keys, final double[] values, final int first, final int second) {
    final float t = keys[first];
    double k = values[first];
    keys[first] = keys[second];
    keys[second] = t;
    k = values[first];
    values[first] = values[second];
    values[second] = k;
  }

  private static void swap(final int[] keys, final double[] values, final int first, final int second) {
    final int t = keys[first];
    double k = values[first];
    keys[first] = keys[second];
    keys[second] = t;
    k = values[first];
    values[first] = values[second];
    values[second] = k;
  }

  private static void swap(final long[] keys, final double[] values, final int first, final int second) {
    final long t = keys[first];
    double k = values[first];
    keys[first] = keys[second];
    keys[second] = t;
    k = values[first];
    values[first] = values[second];
    values[second] = k;
  }

  private static void swap(final double[] keys, final int[] values, final int first, final int second) {
    final double t = keys[first];
    int k = values[first];
    keys[first] = keys[second];
    keys[second] = t;
    k = values[first];
    values[first] = values[second];
    values[second] = k;
  }

  private static void swap(final float[] keys, final int[] values, final int first, final int second) {
    final float t = keys[first];
    int k = values[first];
    keys[first] = keys[second];
    keys[second] = t;
    k = values[first];
    values[first] = values[second];
    values[second] = k;
  }

  private static void swap(final int[] keys, final int[] values, final int first, final int second) {
    final int t = keys[first];
    int k = values[first];
    keys[first] = keys[second];
    keys[second] = t;
    k = values[first];
    values[first] = values[second];
    values[second] = k;
  }

  private static void swap(final long[] keys, final int[] values, final int first, final int second) {
    final long t = keys[first];
    int k = values[first];
    keys[first] = keys[second];
    keys[second] = t;
    k = values[first];
    values[first] = values[second];
    values[second] = k;
  }

  /* bendy types */
  private static <T extends Comparable<T>, U> void swap(final T[] keys, final U[] values, final int first, final int second) {
    final T t = keys[first];
    keys[first] = keys[second];
    keys[second] = t;
    final U u = values[first];
    values[first] = values[second];
    values[second] = u;
  }

  private static <T> void swap(final double[] keys, final T[] values, final int first, final int second) {
    final double x = keys[first];
    keys[first] = keys[second];
    keys[second] = x;
    final T t = values[first];
    values[first] = values[second];
    values[second] = t;
  }
}
