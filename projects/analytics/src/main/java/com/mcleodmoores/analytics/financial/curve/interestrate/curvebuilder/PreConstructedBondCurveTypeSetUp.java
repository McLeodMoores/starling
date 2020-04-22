/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

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
public interface PreConstructedBondCurveTypeSetUp extends PreConstructedCurveTypeSetUp {

  @Override
  PreConstructedBondCurveTypeSetUp forDiscounting(UniqueIdentifiable id);

  @Override
  PreConstructedBondCurveTypeSetUp forIndex(IborTypeIndex... indices);

  @Override
  PreConstructedBondCurveTypeSetUp forIndex(OvernightIndex... indices);

  PreConstructedBondCurveTypeSetUp forIssuer(Pair<Object, LegalEntityFilter<LegalEntity>>... issuer);

  List<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers();

}
