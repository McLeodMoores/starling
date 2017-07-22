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
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingStartTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class DirectForwardMethodCurveTypeSetUp extends DirectForwardMethodCurveSetUp implements CurveTypeSetUpInterface<MulticurveProviderForward> {
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

  public DirectForwardMethodCurveTypeSetUp(final String curveName, final DirectForwardMethodCurveSetUp builder) {
    super(builder);
    _curveName = curveName;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp forDiscounting(final Currency currency) {
    _discountingCurves.put(_curveName, currency);
    return this;
  }

  //TODO versions that only take a single index
  //TODO should store currency, indices in this object rather than in super class
  @Override
  public DirectForwardMethodCurveTypeSetUp forIborIndex(final IborIndex... indices) {
    if (indices.length != 1) {
      throw new IllegalStateException();
    }
    _iborCurves.put(_curveName, indices);
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp forOvernightIndex(final IndexON... indices) {
    if (indices.length != 1) {
      throw new IllegalStateException();
    }
    _overnightCurves.put(_curveName, indices);
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp functionalForm(final CurveFunction function) {
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
  public DirectForwardMethodCurveTypeSetUp withInterpolator(final Interpolator1D interpolator) {
    if (_functionalForm != null) {
      throw new IllegalStateException();
    }
    _interpolator = interpolator;
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp asSpreadOver(final String otherCurveName) {
    _otherCurveName = otherCurveName;
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp usingNodeDates(final ZonedDateTime[] dates) {
    if (_functionalForm != null) {
      throw new IllegalStateException();
    }
    _dates = dates;
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp continuousInterpolationOnYield() {
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
  public DirectForwardMethodCurveTypeSetUp periodicInterpolationOnYield(final int compoundingPeriodsPerYear) {
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
  public DirectForwardMethodCurveTypeSetUp continuousInterpolationOnDiscountFactors() {
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
  public DirectForwardMethodCurveTypeSetUp usingInstrumentMaturity() {
    // TODO need a maturity calculator per curve
    //TODO check that this is right
    throw new IllegalStateException();
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp usingLastFixingEndTime() {
    //TODO check that this is right
    throw new IllegalStateException();
  }

  @Override
  public GeneratorYDCurve buildCurveGenerator(final ZonedDateTime valuationDate) {
    throw new IllegalStateException();
  }

  public GeneratorYDCurve buildCurveGenerator(final ZonedDateTime valuationDate, final String curveName) {
    final InstrumentDerivativeVisitor<Object, Double> nodeTimeCalculator;
    if (_iborCurves.containsKey(curveName)) { //TODO
      nodeTimeCalculator = LastFixingStartTimeCalculator.getInstance();
    } else {
      nodeTimeCalculator = LastTimeCalculator.getInstance(); //TODO hard-coding shouldn't be done here
    }
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

}
