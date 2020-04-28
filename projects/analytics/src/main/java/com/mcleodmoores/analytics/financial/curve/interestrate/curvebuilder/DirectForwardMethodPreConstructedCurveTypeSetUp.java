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
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 * A builder that contains information about any pre-constructed curves that will be used by the {@link DirectForwardMethodCurveBuilder}.
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
    _discountingCurveId = ArgumentChecker.notNull(id, "id");
    return this;
  }

  @Override
  public DirectForwardMethodPreConstructedCurveTypeSetUp forIndex(final IborTypeIndex... indices) {
    ArgumentChecker.notEmpty(indices, "indices");
    if (_iborCurveIndices == null) {
      _iborCurveIndices = new ArrayList<>();
    }
    _iborCurveIndices.addAll(Arrays.asList(indices));
    return this;
  }

  @Override
  public DirectForwardMethodPreConstructedCurveTypeSetUp forIndex(final OvernightIndex... indices) {
    ArgumentChecker.notEmpty(indices, "indices");
    if (_overnightCurveIndices == null) {
      _overnightCurveIndices = new ArrayList<>();
    }
    _overnightCurveIndices.addAll(Arrays.asList(indices));
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

}
