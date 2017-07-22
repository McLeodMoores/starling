/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveAddYieldExisiting;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldNelsonSiegel;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldPeriodicInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingEndTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteOneFactorProviderDiscount;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class HullWhiteMethodCurveTypeSetUp extends HullWhiteMethodCurveSetUp implements CurveTypeSetUpInterface<HullWhiteOneFactorProviderDiscount> {
  private final String _curveName;
  private String _otherCurveName;
  private Interpolator1D _interpolator;
  private ZonedDateTime[] _dates;
  private boolean _typeAlreadySet;
  private CurveFunction _functionalForm;
  private boolean _continuousInterpolationOnYield;
  private boolean _periodicInterpolationOnYield;
  private int _periodsPerYear;
  private boolean _continuousInterpolationOnDiscountFactors;
  private boolean _timeCalculatorAlreadySet;
  private boolean _maturityCalculator;
  private boolean _lastFixingEndCalculator;

  public HullWhiteMethodCurveTypeSetUp(final String curveName, final HullWhiteMethodCurveSetUp builder) {
    super(builder);
    _curveName = curveName;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp forDiscounting(final Currency currency) {
    _discountingCurves.put(_curveName, currency);
    return this;
  }

  //TODO versions that only take a single index
  //TODO should store currency, indices in this object rather than in super class
  @Override
  public HullWhiteMethodCurveTypeSetUp forIborIndex(final IborIndex... indices) {
    _iborCurves.put(_curveName, indices);
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp forOvernightIndex(final IndexON... indices) {
    _overnightCurves.put(_curveName, indices);
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp functionalForm(final CurveFunction function) {
    if (_interpolator != null || _dates != null || _typeAlreadySet) {
      throw new IllegalStateException();
    }
    switch (function) {
      case NELSON_SIEGEL:
        _functionalForm = function;
        return this;
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp withInterpolator(final Interpolator1D interpolator) {
    if (_functionalForm != null) {
      throw new IllegalStateException();
    }
    _interpolator = interpolator;
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp asSpreadOver(final String otherCurveName) {
    _otherCurveName = otherCurveName;
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp usingNodeDates(final ZonedDateTime[] dates) {
    if (_functionalForm != null) {
      throw new IllegalStateException();
    }
    _dates = dates;
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp continuousInterpolationOnYield() {
    if (_functionalForm != null) {
      throw new IllegalStateException();
    }
    if (_typeAlreadySet) {
      throw new IllegalStateException();
    }
    _typeAlreadySet = true;
    _continuousInterpolationOnYield = true;
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp periodicInterpolationOnYield(final int compoundingPeriodsPerYear) {
    if (_functionalForm != null) {
      throw new IllegalStateException();
    }
    if (_typeAlreadySet) {
      throw new IllegalStateException();
    }
    _typeAlreadySet = true;
    _periodicInterpolationOnYield = true;
    _periodsPerYear = compoundingPeriodsPerYear;
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp continuousInterpolationOnDiscountFactors() {
    if (_functionalForm != null) {
      throw new IllegalStateException();
    }
    if (_typeAlreadySet) {
      throw new IllegalStateException();
    }
    _typeAlreadySet = true;
    _continuousInterpolationOnDiscountFactors = true;
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp usingInstrumentMaturity() {
    if (_timeCalculatorAlreadySet) {
      throw new IllegalStateException();
    }
    _timeCalculatorAlreadySet = true;
    _maturityCalculator = true;
    return this;
  }

  @Override
  public HullWhiteMethodCurveTypeSetUp usingLastFixingEndTime() {
    if (_timeCalculatorAlreadySet) {
      throw new IllegalStateException();
    }
    _timeCalculatorAlreadySet = true;
    _lastFixingEndCalculator = true;
    return this;
  }

  @Override
  public GeneratorYDCurve buildCurveGenerator(final ZonedDateTime valuationDate) {
    final InstrumentDerivativeVisitor<Object, Double> nodeTimeCalculator = getNodeTimeCalculator();
    if (_otherCurveName != null) {
      //TODO duplicated code
      GeneratorYDCurve generator;
      if (_functionalForm != null) {
        switch (_functionalForm) {
          case NELSON_SIEGEL:
            return new GeneratorCurveYieldNelsonSiegel();
          default:
            throw new IllegalStateException();
        }
      }
      if (_dates != null) {
        final double[] meetingTimes = new double[_dates.length];
        for (int i = 0; i < meetingTimes.length; i++) {
          meetingTimes[i] = TimeCalculator.getTimeBetween(valuationDate, _dates[i]);
        }
        if (_continuousInterpolationOnYield) {
          generator = new GeneratorCurveYieldInterpolatedNode(meetingTimes, _interpolator);
        } else if (_continuousInterpolationOnDiscountFactors) {
          generator = new GeneratorCurveDiscountFactorInterpolatedNode(meetingTimes, _interpolator);
        } else if (_typeAlreadySet) { //i.e. some other type like periodic that there's no generator for
          throw new IllegalStateException();
        } else {
          generator = new GeneratorCurveYieldInterpolatedNode(meetingTimes, _interpolator);
        }
      }
      if (_continuousInterpolationOnYield) {
        generator = new GeneratorCurveYieldInterpolated(nodeTimeCalculator, _interpolator);
      } else if (_continuousInterpolationOnDiscountFactors) {
        generator = new GeneratorCurveDiscountFactorInterpolated(nodeTimeCalculator, _interpolator);
      } else if (_periodicInterpolationOnYield) {
        generator = new GeneratorCurveYieldPeriodicInterpolated(nodeTimeCalculator, _periodsPerYear, _interpolator);
      } else if (_typeAlreadySet) {
        throw new IllegalStateException();
      } else {
        generator = new GeneratorCurveYieldInterpolated(nodeTimeCalculator, _interpolator);
      }
      //TODO positive or negative spread
      return new GeneratorCurveAddYieldExisiting(generator, false, _otherCurveName);
    }
    if (_functionalForm != null) {
      switch (_functionalForm) {
        case NELSON_SIEGEL:
          return new GeneratorCurveYieldNelsonSiegel();
        default:
          throw new IllegalStateException();
      }
    }
    if (_dates != null) {
      final double[] meetingTimes = new double[_dates.length];
      for (int i = 0; i < meetingTimes.length; i++) {
        meetingTimes[i] = TimeCalculator.getTimeBetween(valuationDate, _dates[i]);
      }
      if (_continuousInterpolationOnYield) {
        return new GeneratorCurveYieldInterpolatedNode(meetingTimes, _interpolator);
      } else if (_continuousInterpolationOnDiscountFactors) {
        return new GeneratorCurveDiscountFactorInterpolatedNode(meetingTimes, _interpolator);
      } else if (_typeAlreadySet) {
        throw new IllegalStateException();
      } else {
        return new GeneratorCurveYieldInterpolatedNode(meetingTimes, _interpolator);
      }
    }
    if (_continuousInterpolationOnYield) {
      return new GeneratorCurveYieldInterpolated(nodeTimeCalculator, _interpolator);
    } else if (_continuousInterpolationOnDiscountFactors) {
      return new GeneratorCurveDiscountFactorInterpolated(nodeTimeCalculator, _interpolator);
    } else if (_periodicInterpolationOnYield) {
      return new GeneratorCurveYieldPeriodicInterpolated(nodeTimeCalculator, _periodsPerYear, _interpolator);
    } else if (_typeAlreadySet) {
      throw new IllegalStateException();
    } else {
      return new GeneratorCurveYieldInterpolated(nodeTimeCalculator, _interpolator);
    }
  }

  private InstrumentDerivativeVisitor<Object, Double> getNodeTimeCalculator() {
    if (_maturityCalculator) {
      return LastTimeCalculator.getInstance();
    } else if (_lastFixingEndCalculator) {
      return LastFixingEndTimeCalculator.getInstance();
    }
    return LastTimeCalculator.getInstance();
  }

}
