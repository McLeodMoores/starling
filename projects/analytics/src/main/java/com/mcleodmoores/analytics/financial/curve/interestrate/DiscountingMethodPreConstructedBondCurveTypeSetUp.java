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
public class DiscountingMethodPreConstructedBondCurveTypeSetUp extends DiscountingMethodBondCurveSetUp implements PreConstructedBondCurveTypeSetUp {
  private UniqueIdentifiable _discountingCurveId;
  private List<IborTypeIndex> _iborCurveIndices;
  private List<OvernightIndex> _overnightCurveIndices;
  private List<Pair<Object, LegalEntityFilter<LegalEntity>>> _issuers;

  DiscountingMethodPreConstructedBondCurveTypeSetUp(final DiscountingMethodBondCurveSetUp builder) {
    super(builder);
  }

  @Override
  public PreConstructedBondCurveTypeSetUp forDiscounting(final UniqueIdentifiable id) {
    _discountingCurveId = id;
    return this;
  }

  @Override
  public PreConstructedBondCurveTypeSetUp forIndex(final IborTypeIndex... indices) {
    _iborCurveIndices = Arrays.asList(indices);
    return this;
  }

  @Override
  public PreConstructedBondCurveTypeSetUp forIndex(final OvernightIndex... indices) {
    _overnightCurveIndices = Arrays.asList(indices);
    return this;
  }

  @Override
  public PreConstructedBondCurveTypeSetUp forIssuer(final Pair<Object, LegalEntityFilter<LegalEntity>>... issuers) {
    _issuers = Arrays.asList(issuers);
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

  List<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers() {
    return _issuers;
  }
}
