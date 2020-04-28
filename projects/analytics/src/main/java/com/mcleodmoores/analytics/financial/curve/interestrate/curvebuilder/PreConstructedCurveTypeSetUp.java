/**
 * Copyright (C) 2017 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.List;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.id.UniqueIdentifiable;

/**
 * A builder that contains information about any pre-constructed curves that are used in curve construction.
 *
 * For example, curves might be constructed using FX spot and forward rates that uses a fixed USD curve for all currencies. If the sensitivities to the USD
 * curve are not required, then it is more efficient to use a pre-constructed curve.
 */
public interface PreConstructedCurveTypeSetUp {

  /**
   * Use this curve to discount payments with this identifier.
   *
   * @param id
   *          the identifier, not null
   * @return this builder
   */
  PreConstructedCurveTypeSetUp forDiscounting(UniqueIdentifiable id);

  /**
   * Use this curve to calculate forward *IBOR rates for these indices.
   *
   * @param indices
   *          the indices, not null
   * @return this builder
   */
  PreConstructedCurveTypeSetUp forIndex(IborTypeIndex... indices);

  /**
   * Use this curve for calculate forward overnight rates for these indices.
   *
   * @param indices
   *          the indices, not null
   * @return this builder
   */
  PreConstructedCurveTypeSetUp forIndex(OvernightIndex... indices);

  /**
   * Gets the identifier.
   *
   * @return the identifier
   */
  UniqueIdentifiable getDiscountingCurveId();

  /**
   * Gets the *IBOR indices.
   *
   * @return the indices
   */
  List<IborTypeIndex> getIborCurveIndices();

  /**
   * Gets the overnight indices.
   *
   * @return the indices
   */
  List<OvernightIndex> getOvernightCurveIndices();
}
