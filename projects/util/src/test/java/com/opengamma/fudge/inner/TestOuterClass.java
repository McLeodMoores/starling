/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.fudge.inner;

import java.util.Arrays;

public class TestOuterClass {
  int _fieldA = 5;
  double _fieldB = 5;
  int[] _fieldC;

/*    TestOuterClass(int fieldA) {
      this.fieldA = fieldA;
    }

    TestOuterClass(int fieldA, int fieldB, int[] fieldC) {
      this.fieldA = fieldA;
      this.fieldB = fieldB;
      this.fieldC = fieldC;
    }*/

  public TestOuterClass() {

  }

  /**
   * Default dummy implementation of eval = identity.
   * @param arg the arguemnt
   * @return the result
   */
  public double eval(final double arg) {
    return arg;
  }

  // -------------------------------------------------------------------------------------------------------------------

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TestOuterClass)) {
      return false;
    }

    final TestOuterClass that = (TestOuterClass) o;

    if (_fieldA != that._fieldA) {
      return false;
    }
    if (Double.compare(that._fieldB, _fieldB) != 0) {
      return false;
    }
    if (!Arrays.equals(_fieldC, that._fieldC)) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = _fieldA;
    temp = _fieldB != +0.0d ? Double.doubleToLongBits(_fieldB) : 0L;
    result = 31 * result + (int) (temp ^ temp >>> 32);
    result = 31 * result + (_fieldC != null ? Arrays.hashCode(_fieldC) : 0);
    return result;
  }
}
