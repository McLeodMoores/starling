/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
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
  public DiscountingMethodPreConstructedBondCurveTypeSetUp forDiscounting(final UniqueIdentifiable id) {
    _discountingCurveId = ArgumentChecker.notNull(id, "id");
    return this;
  }

  @Override
  public DiscountingMethodPreConstructedBondCurveTypeSetUp forIndex(final IborTypeIndex... indices) {
    ArgumentChecker.notEmpty(indices, "indices");
    if (_iborCurveIndices == null) {
      _iborCurveIndices = new ArrayList<>();
    }
    _iborCurveIndices.addAll(Arrays.asList(indices));
    return this;
  }

  @Override
  public DiscountingMethodPreConstructedBondCurveTypeSetUp forIndex(final OvernightIndex... indices) {
    ArgumentChecker.notEmpty(indices, "indices");
    if (_overnightCurveIndices == null) {
      _overnightCurveIndices = new ArrayList<>();
    }
    _overnightCurveIndices.addAll(Arrays.asList(indices));
    return this;
  }

  @Override
  public DiscountingMethodPreConstructedBondCurveTypeSetUp forIssuer(final Pair<Object, LegalEntityFilter<LegalEntity>>... issuers) {
    ArgumentChecker.notEmpty(issuers, "issuers");
    if (_issuers == null) {
      _issuers = new ArrayList<>();
    }
    _issuers.addAll(Arrays.asList(issuers));
    return this;
  }

  @Override
  public UniqueIdentifiable getDiscountingCurveId() {
    return _discountingCurveId;
  }

  @Override
  public List<IborTypeIndex> getIborCurveIndices() {
    return _iborCurveIndices == null ? null : Collections.unmodifiableList(_iborCurveIndices);
  }

  @Override
  public List<OvernightIndex> getOvernightCurveIndices() {
    return _overnightCurveIndices == null ? null : Collections.unmodifiableList(_overnightCurveIndices);
  }

  @Override
  public List<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers() {
    return _issuers == null ? null : Collections.unmodifiableList(_issuers);
  }

}
