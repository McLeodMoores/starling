/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceData;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceKey;
import com.opengamma.core.marketdatasnapshot.VolatilitySurfaceSnapshot;
import com.opengamma.core.marketdatasnapshot.impl.ManageableVolatilitySurfaceSnapshot;
import com.opengamma.engine.value.SurfaceAndCubePropertyNames;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.id.UniqueId;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 *
 */
@SuppressWarnings("rawtypes")
public class VolatilitySurfaceSnapper extends
    StructuredSnapper<VolatilitySurfaceKey, VolatilitySurfaceData, VolatilitySurfaceSnapshot> {
  public VolatilitySurfaceSnapper() {
    super(ValueRequirementNames.VOLATILITY_SURFACE_DATA);
  }

  @Override
  VolatilitySurfaceKey getKey(final ValueSpecification spec) {
    final UniqueId uniqueId = spec.getTargetSpecification().getUniqueId();
    final String surface = getSingleProperty(spec, ValuePropertyNames.SURFACE);
    final String instrumentType = getSingleProperty(spec, SurfaceAndCubePropertyNames.INSTRUMENT_TYPE);
    final String quoteType = getSingleProperty(spec, SurfaceAndCubePropertyNames.PROPERTY_SURFACE_QUOTE_TYPE);
    final String quoteUnits = getSingleProperty(spec, SurfaceAndCubePropertyNames.PROPERTY_SURFACE_UNITS);
    return VolatilitySurfaceKey.of(uniqueId, surface, instrumentType, quoteType, quoteUnits);
  }

  @SuppressWarnings("unchecked")
  @Override
  ManageableVolatilitySurfaceSnapshot buildSnapshot(final ViewComputationResultModel resultModel, final VolatilitySurfaceKey key,
      final VolatilitySurfaceData volatilitySurfaceData) {
    final Map<Pair<Object, Object>, ValueSnapshot> dict = new HashMap<>();
    for (final Object x : volatilitySurfaceData.getXs()) {
      for (final Object y : volatilitySurfaceData.getYs()) {
        final Double volatility = volatilitySurfaceData.getVolatility(x, y);
        final Pair<Object, Object> volKey = Pairs.of(x, y);
        dict.put(volKey, ValueSnapshot.of(volatility));
      }
    }

    final ManageableVolatilitySurfaceSnapshot ret = new ManageableVolatilitySurfaceSnapshot();
    ret.setValues(dict);
    return ret;
  }
}
