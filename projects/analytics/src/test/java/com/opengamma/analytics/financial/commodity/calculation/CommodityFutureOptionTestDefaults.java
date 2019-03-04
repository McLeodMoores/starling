/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.commodity.calculation;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.model.volatility.surface.BlackVolatilitySurfaceStrike;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.interpolation.GridInterpolator2D;
import com.opengamma.analytics.math.interpolation.factory.DoubleQuadraticInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1d;
import com.opengamma.analytics.math.interpolation.factory.NamedInterpolator1dFactory;
import com.opengamma.analytics.math.surface.InterpolatedDoublesSurface;
import com.opengamma.id.ExternalId;
import com.opengamma.util.time.DateUtils;

/**
 * Common fields used in CommodityFutureOption calculator tests
 */
public abstract class CommodityFutureOptionTestDefaults {

  // Market data
  private static final double SPOT = 80;
  private static final double DRIFT = 0.05;
  private static final ForwardCurve FORWARD_CURVE = new ForwardCurve(SPOT, DRIFT);
  // private static final double FORWARD = 100;

  private static final YieldAndDiscountCurve DISCOUNT = YieldCurve.from(ConstantDoublesCurve.from(0.05));

  private static final double[] EXPIRIES = new double[] {0.5, 0.5, 0.5, 0.5, 1.0, 1.0, 1.0, 1.0, 5.0, 5.0, 5.0, 5.0, 10.0, 10.0, 10.0, 10.0 };
  private static final double[] STRIKES = new double[] {40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120, 40, 80, 100, 120 };
  private static final double[] VOLS = new double[] {0.28, 0.28, 0.28, 0.28, 0.25, 0.25, 0.25, 0.25, 0.26, 0.24, 0.23, 0.25, 0.20, 0.20, 0.20, 0.20 };

  private static final NamedInterpolator1d INTERPOLATOR_1D_STRIKE = NamedInterpolator1dFactory.of(DoubleQuadraticInterpolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME);

  final static NamedInterpolator1d INTERPOLATOR_1D_EXPIRY = NamedInterpolator1dFactory.of(LinearInterpolator1dAdapter.NAME, FlatExtrapolator1dAdapter.NAME,
      FlatExtrapolator1dAdapter.NAME);

  private static final InterpolatedDoublesSurface SURFACE = new InterpolatedDoublesSurface(EXPIRIES, STRIKES, VOLS, new GridInterpolator2D(INTERPOLATOR_1D_EXPIRY, INTERPOLATOR_1D_STRIKE));
  private static final BlackVolatilitySurfaceStrike VOL_SURFACE = new BlackVolatilitySurfaceStrike(SURFACE);

  protected static final StaticReplicationDataBundle MARKET = new StaticReplicationDataBundle(VOL_SURFACE, DISCOUNT, FORWARD_CURVE);

  protected static final double TOLERANCE = 1.0E-9;   // tolerance for equals()

  // future & option params
  protected final static ExternalId AN_UNDERLYING = ExternalId.of("Scheme", "value");
  protected final static ZonedDateTime FIRST_DELIVERY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  protected final static ZonedDateTime LAST_DELIVERY_DATE = DateUtils.getUTCDate(2012, 9, 21);
  protected final static ZonedDateTime SETTLEMENT_DATE = LAST_DELIVERY_DATE;
  protected final static ZonedDateTime EXPIRY_DATE = DateUtils.getUTCDate(2011, 9, 21);
  protected final static ZonedDateTime A_DATE = DateUtils.getUTCDate(2011, 7, 7);
  protected final static double UNIT_AMOUNT = 5;
  protected final static double AMOUNT = 1.11;
  protected final static ExerciseDecisionType EXERCISE = ExerciseDecisionType.EUROPEAN;
  protected final static double EXPIRY = 0.20821917808219179;
  protected final static double STRIKE = 70;
}
