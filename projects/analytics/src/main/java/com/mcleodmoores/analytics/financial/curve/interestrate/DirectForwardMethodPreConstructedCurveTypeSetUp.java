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
public class DirectForwardMethodPreConstructedCurveTypeSetUp extends DirectForwardMethodCurveSetUp implements PreConstructedCurveTypeSetUp {
  private UniqueIdentifiable _discountingCurveId;
  private List<IborTypeIndex> _iborCurveIndices;
  private List<OvernightIndex> _overnightCurveIndices;

  DirectForwardMethodPreConstructedCurveTypeSetUp(final DirectForwardMethodCurveSetUp builder) {
    super(builder);
  }

  @Override
  public DirectForwardMethodPreConstructedCurveTypeSetUp forDiscounting(final UniqueIdentifiable id) {
    _discountingCurveId = id;
    return this;
  }

  @Override
  public DirectForwardMethodPreConstructedCurveTypeSetUp forIndex(final IborTypeIndex... indices) {
    _iborCurveIndices = Arrays.asList(indices);
    return this;
  }

  @Override
  public DirectForwardMethodPreConstructedCurveTypeSetUp forIndex(final OvernightIndex... indices) {
    _overnightCurveIndices = Arrays.asList(indices);
    return this;
  }

  UniqueIdentifiable getDiscountingCurveId() {
    return _discountingCurveId;
  }

  List<IborTypeIndex> getIborCurveIndices() {
    return _iborCurveIndices;
  }

  List<OvernightIndex> getOvernightCurveIndices() {
    return _overnightCurveIndices;
  }

}
