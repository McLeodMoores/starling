/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.Arrays;
import java.util.List;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveAddYieldExisiting;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldNelsonSiegel;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldPeriodicInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingStartTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.UniqueIdentifiable;

/**
 *
 */
public class DirectForwardMethodCurveTypeSetUp extends DirectForwardMethodCurveSetUp implements CurveTypeSetUpInterface {
  private String _baseCurveName;
  private Interpolator1D _interpolator;
  private LocalDateTime[] _dates;
  private boolean _typeAlreadySet;
  private CurveFunction _functionalForm;
  private boolean _continuousInterpolationOnYield;
  private boolean _periodicInterpolationOnYield;
  private int _periodsPerYear;
  private boolean _continuousInterpolationOnDiscountFactors;
  private UniqueIdentifiable _discountingCurveId;
  private List<IborTypeIndex> _iborCurveIndices;
  private List<OvernightIndex> _overnightCurveIndices;

  public DirectForwardMethodCurveTypeSetUp(final DirectForwardMethodCurveSetUp builder) {
    super(builder);
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp forDiscounting(final UniqueIdentifiable id) {
    _discountingCurveId = id;
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp forIndex(final IborTypeIndex... indices) {
    if (indices.length != 1) {
      throw new IllegalStateException();
    }
    _iborCurveIndices = Arrays.asList(indices);
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp forIndex(final OvernightIndex... indices) {
    if (indices.length != 1) {
      throw new IllegalStateException();
    }
    _overnightCurveIndices = Arrays.asList(indices);
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
    _baseCurveName = otherCurveName;
    return this;
  }

  @Override
  public DirectForwardMethodCurveTypeSetUp usingNodeDates(final LocalDateTime... dates) {
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

  UniqueIdentifiable getDiscountingCurveId() {
    return _discountingCurveId;
  }

  List<IborTypeIndex> getIborCurveIndices() {
    return _iborCurveIndices;
  }

  List<OvernightIndex> getOvernightCurveIndices() {
    return _overnightCurveIndices;
  }

  @Override
  public GeneratorYDCurve buildCurveGenerator(final ZonedDateTime valuationDate) {
    final InstrumentDerivativeVisitor<Object, Double> nodeTimeCalculator = getNodeTimeCalculator();
    if (_baseCurveName != null) {
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
          meetingTimes[i] = TimeCalculator.getTimeBetween(valuationDate, ZonedDateTime.of(_dates[i], valuationDate.getZone()));
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
      return new GeneratorCurveAddYieldExisiting(generator, false, _baseCurveName);
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
        meetingTimes[i] = TimeCalculator.getTimeBetween(valuationDate, ZonedDateTime.of(_dates[i], valuationDate.getZone()));
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

  @Override
  public InstrumentDerivativeVisitor<Object, Double> getNodeTimeCalculator() {
    return _iborCurveIndices == null || _iborCurveIndices.isEmpty()
        ? LastTimeCalculator.getInstance() : LastFixingStartTimeCalculator.getInstance();
  }
}
