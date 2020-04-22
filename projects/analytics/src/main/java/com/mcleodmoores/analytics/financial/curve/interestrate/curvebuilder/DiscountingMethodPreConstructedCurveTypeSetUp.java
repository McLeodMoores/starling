/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.Arrays;
import java.util.List;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public class DiscountingMethodPreConstructedCurveTypeSetUp extends DiscountingMethodCurveSetUp implements PreConstructedCurveTypeSetUp {
  private UniqueIdentifiable _discountingCurveId;
  private List<IborTypeIndex> _iborCurveIndices;
  private List<OvernightIndex> _overnightCurveIndices;

  DiscountingMethodPreConstructedCurveTypeSetUp(final DiscountingMethodCurveSetUp builder) {
    super(builder);
  }

  @Override
  public DiscountingMethodPreConstructedCurveTypeSetUp forDiscounting(final UniqueIdentifiable id) {
    _discountingCurveId = id;
    return this;
  }

  @Override
  public DiscountingMethodPreConstructedCurveTypeSetUp forIndex(final IborTypeIndex... indices) {
    _iborCurveIndices = Arrays.asList(indices);
    return this;
  }

  @Override
  public DiscountingMethodPreConstructedCurveTypeSetUp forIndex(final OvernightIndex... indices) {
    _overnightCurveIndices = Arrays.asList(indices);
    return this;
  }

  @Override
  public UniqueIdentifiable getDiscountingCurveId() {
    return _discountingCurveId;
  }

  @Override
  public List<IborTypeIndex> getIborCurveIndices() {
    return _iborCurveIndices;
  }

  @Override
  public List<OvernightIndex> getOvernightCurveIndices() {
    return _overnightCurveIndices;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + (_discountingCurveId == null ? 0 : _discountingCurveId.hashCode());
    result = prime * result
        + (_iborCurveIndices == null ? 0 : _iborCurveIndices.hashCode());
    result = prime * result + (_overnightCurveIndices == null ? 0
        : _overnightCurveIndices.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof DiscountingMethodPreConstructedCurveTypeSetUp)) {
      return false;
    }
    final DiscountingMethodPreConstructedCurveTypeSetUp other = (DiscountingMethodPreConstructedCurveTypeSetUp) obj;
    if (_discountingCurveId == null) {
      if (other._discountingCurveId != null) {
        return false;
      }
    } else if (!_discountingCurveId.equals(other._discountingCurveId)) {
      return false;
    }
    if (_iborCurveIndices == null) {
      if (other._iborCurveIndices != null) {
        return false;
      }
    } else if (!_iborCurveIndices.equals(other._iborCurveIndices)) {
      return false;
    }
    if (_overnightCurveIndices == null) {
      if (other._overnightCurveIndices != null) {
        return false;
      }
    } else if (!_overnightCurveIndices.equals(other._overnightCurveIndices)) {
      return false;
    }
    return true;
  }

}
