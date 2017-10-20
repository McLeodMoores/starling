/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.Arrays;
import java.util.List;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public class HullWhiteMethodPreConstructedCurveTypeSetUp extends HullWhiteMethodCurveSetUp implements PreConstructedCurveTypeSetUp {
  private UniqueIdentifiable _discountingCurveId;
  private List<IborTypeIndex> _iborCurveIndices;
  private List<OvernightIndex> _overnightCurveIndices;

  HullWhiteMethodPreConstructedCurveTypeSetUp(final HullWhiteMethodCurveSetUp builder) {
    super(builder);
  }

  @Override
  public HullWhiteMethodPreConstructedCurveTypeSetUp forDiscounting(final UniqueIdentifiable id) {
    _discountingCurveId = id;
    return this;
  }

  @Override
  public HullWhiteMethodPreConstructedCurveTypeSetUp forIndex(final IborTypeIndex... indices) {
    _iborCurveIndices = Arrays.asList(indices);
    return this;
  }

  @Override
  public HullWhiteMethodPreConstructedCurveTypeSetUp forIndex(final OvernightIndex... indices) {
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
}
