/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.Arrays;
import java.util.List;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.tuple.Pair;

/**
 *
 */
public class DiscountingMethodPreConstructedCurveTypeSetUp extends DiscountingMethodCurveSetUp implements PreConstructedCurveTypeSetUp {
  private UniqueIdentifiable _discountingCurveId;
  private List<IborTypeIndex> _iborCurveIndices;
  private List<OvernightIndex> _overnightCurveIndices;
  private List<Pair<Object, LegalEntityFilter<LegalEntity>>> _issuers;

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

}
