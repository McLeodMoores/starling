/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import com.opengamma.core.marketdatasnapshot.SnapshotDataBundle;
import com.opengamma.core.marketdatasnapshot.YieldCurveKey;
import com.opengamma.core.marketdatasnapshot.YieldCurveSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableUnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableYieldCurveSnapshot;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class YieldCurveSnapper extends
    StructuredSnapper<YieldCurveKey, SnapshotDataBundle, YieldCurveSnapshot> {

  public YieldCurveSnapper() {
    super(ValueRequirementNames.YIELD_CURVE_MARKET_DATA);
  }

  @Override
  YieldCurveKey getKey(final ValueSpecification spec) {
    final Currency currency = Currency.parse(spec.getTargetSpecification().getUniqueId().getValue());
    final String curve = getSingleProperty(spec, ValuePropertyNames.CURVE);
    return YieldCurveKey.of(currency, curve);
  }

  @Override
  ManageableYieldCurveSnapshot buildSnapshot(final ViewComputationResultModel resultModel, final YieldCurveKey key,
      final SnapshotDataBundle bundle) {
    final ManageableUnstructuredMarketDataSnapshot values = getUnstructured(bundle);
    return ManageableYieldCurveSnapshot.of(resultModel.getViewCycleExecutionOptions().getValuationTime(), values);
  }
}
