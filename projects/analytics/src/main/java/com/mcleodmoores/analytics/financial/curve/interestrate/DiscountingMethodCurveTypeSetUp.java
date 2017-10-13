/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate;

import java.util.ArrayList;
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
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingEndTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class DiscountingMethodCurveTypeSetUp extends DiscountingMethodCurveSetUp implements CurveTypeSetUpInterface {
  private String _baseCurveName;
  private Interpolator1D _interpolator;
  private List<LocalDateTime> _dates;
  private boolean _typeAlreadySet;
  private CurveFunction _functionalForm;
  private boolean _continuousInterpolationOnYield;
  private boolean _periodicInterpolationOnYield;
  private int _periodsPerYear;
  private boolean _continuousInterpolationOnDiscountFactors;
  private boolean _timeCalculatorAlreadySet;
  private boolean _maturityCalculator;
  private boolean _lastFixingEndCalculator;
  private UniqueIdentifiable _discountingCurveId;
  private List<IborTypeIndex> _iborCurveIndices;
  private List<OvernightIndex> _overnightCurveIndices;

  /**
   * Constructor that creates an empty builder.
   */
  DiscountingMethodCurveTypeSetUp() {
    super();
  }

  /**
   * Constructor that takes an existing builder. Note that this is not a copy constructor.
   * @param builder  the builder, not null
   */
  DiscountingMethodCurveTypeSetUp(final DiscountingMethodCurveSetUp builder) {
    super(builder);
  }

  @Override
  public DiscountingMethodCurveTypeSetUp forDiscounting(final UniqueIdentifiable id) {
    _discountingCurveId = ArgumentChecker.notNull(id, "id");
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp forIndex(final IborTypeIndex... indices) {
    ArgumentChecker.notNull(indices, "indices");
    if (_iborCurveIndices == null) {
      _iborCurveIndices = new ArrayList<>();
    }
    _iborCurveIndices.addAll(Arrays.asList(indices));
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp forIndex(final OvernightIndex... indices) {
    ArgumentChecker.notNull(indices, "indices");
    if (_overnightCurveIndices == null) {
      _overnightCurveIndices = new ArrayList<>();
    }
    _overnightCurveIndices.addAll(Arrays.asList(indices));
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp functionalForm(final CurveFunction function) {
    if (_interpolator != null || _dates != null || _typeAlreadySet || _baseCurveName != null) {
      throw new IllegalStateException("Have already set up curve type");
    }
    _functionalForm = ArgumentChecker.notNull(function, "function");
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp withInterpolator(final Interpolator1D interpolator) {
    if (_functionalForm != null) {
      throw new IllegalStateException("Have already set curve type to be functional");
    }
    _interpolator = ArgumentChecker.notNull(interpolator, "interpolator");
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp asSpreadOver(final String baseCurveName) {
    if (_functionalForm != null) {
      throw new IllegalStateException("Cannot set a functional curve as a spread over another");
    }
    _baseCurveName = ArgumentChecker.notNull(baseCurveName, "baseCurveName");
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp usingNodeDates(final LocalDateTime... dates) {
    if (_functionalForm != null) {
      throw new IllegalStateException("Have already set curve type to be functional");
    }
    if (_periodicInterpolationOnYield) {
      throw new IllegalStateException("Cannot set node dates for a periodically-compounded curve");
    }
    ArgumentChecker.notNull(dates, "dates");
    if (_dates == null) {
      _dates = new ArrayList<>();
    }
    _dates.addAll(Arrays.asList(dates));
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp continuousInterpolationOnYield() {
    if (_functionalForm != null || _typeAlreadySet) {
      throw new IllegalStateException("Have already set up curve type");
    }
    _typeAlreadySet = true;
    _continuousInterpolationOnYield = true;
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp periodicInterpolationOnYield(final int compoundingPeriodsPerYear) {
    ArgumentChecker.isTrue(compoundingPeriodsPerYear > 0, "Must have at least one compounding period per year");
    if (_functionalForm != null || _typeAlreadySet) {
      throw new IllegalStateException("Have already set up curve type");
    }
    if (_dates != null) {
      throw new IllegalStateException("Cannot use a periodically-compounded curve with fixed nodes");
    }
    _typeAlreadySet = true;
    _periodicInterpolationOnYield = true;
    _periodsPerYear = compoundingPeriodsPerYear;
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp continuousInterpolationOnDiscountFactors() {
    if (_functionalForm != null || _typeAlreadySet) {
      throw new IllegalStateException("Have already set up curve type");
    }
    _typeAlreadySet = true;
    _continuousInterpolationOnDiscountFactors = true;
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp usingInstrumentMaturity() {
    if (_timeCalculatorAlreadySet) {
      throw new IllegalStateException("The node time calculator has already been set");
    }
    _timeCalculatorAlreadySet = true;
    _maturityCalculator = true;
    return this;
  }

  @Override
  public DiscountingMethodCurveTypeSetUp usingLastFixingEndTime() {
    if (_timeCalculatorAlreadySet) {
      throw new IllegalStateException("The node time calculator has already been set");
    }
    _timeCalculatorAlreadySet = true;
    _lastFixingEndCalculator = true;
    return this;
  }

  /**
   * Gets the discounting curve identifier.
   * @return  the identifier, can be null
   */
  UniqueIdentifiable getDiscountingCurveId() {
    return _discountingCurveId;
  }

  /**
   * Gets the ibor curve indices.
   * @return  the indices, can be null or empty
   */
  List<IborTypeIndex> getIborCurveIndices() {
    return _iborCurveIndices;
  }

  /**
   * Gets the overnight curve indices.
   * @return  the indices, can be null or empty
   */
  List<OvernightIndex> getOvernightCurveIndices() {
    return _overnightCurveIndices;
  }

  @Override
  public GeneratorYDCurve buildCurveGenerator(final ZonedDateTime valuationDate) {
    final InstrumentDerivativeVisitor<Object, Double> nodeTimeCalculator = getNodeTimeCalculator();
    if (_functionalForm != null) {
      switch (_functionalForm) {
        case NELSON_SIEGEL:
          return new GeneratorCurveYieldNelsonSiegel();
        default:
          throw new IllegalStateException("Unsupported functional form " + _functionalForm);
      }
    }
    if (_interpolator == null) {
      throw new IllegalStateException("Must supply an interpolator to create an interpolated curve");
    }
    GeneratorYDCurve generator;
    if (_dates != null) {
      ArgumentChecker.isTrue(_dates.size() > 1, "Must have at least two node dates to interpolate");
      final double[] meetingTimes = new double[_dates.size()];
      int i = 0;
      for (final LocalDateTime date : _dates) {
        meetingTimes[i++] = TimeCalculator.getTimeBetween(valuationDate, ZonedDateTime.of(date, valuationDate.getZone()));
      }
      if (_continuousInterpolationOnYield) {
        generator = new GeneratorCurveYieldInterpolatedNode(meetingTimes, _interpolator);
      } else if (_continuousInterpolationOnDiscountFactors) {
        generator = new GeneratorCurveDiscountFactorInterpolatedNode(meetingTimes, _interpolator);
      } else if (_typeAlreadySet) {
        throw new IllegalStateException("Could not create curve generator for this curve type: " + toString());
      } else {
        generator = new GeneratorCurveYieldInterpolatedNode(meetingTimes, _interpolator);
      }
    } else {
      if (_continuousInterpolationOnYield) {
        generator = new GeneratorCurveYieldInterpolated(nodeTimeCalculator, _interpolator);
      } else if (_continuousInterpolationOnDiscountFactors) {
        generator = new GeneratorCurveDiscountFactorInterpolated(nodeTimeCalculator, _interpolator);
      } else if (_periodicInterpolationOnYield) {
        generator = new GeneratorCurveYieldPeriodicInterpolated(nodeTimeCalculator, _periodsPerYear, _interpolator);
      } else if (_typeAlreadySet) {
        throw new IllegalStateException("Could not create curve generator for this curve type: " + toString());
      } else {
        generator = new GeneratorCurveYieldInterpolated(nodeTimeCalculator, _interpolator);
      }
    }
    if (_baseCurveName != null) {
      //TODO positive or negative spread
      return new GeneratorCurveAddYieldExisiting(generator, false, _baseCurveName);
    }
    return generator;
  }

  private InstrumentDerivativeVisitor<Object, Double> getNodeTimeCalculator() {
    if (_maturityCalculator) {
      return LastTimeCalculator.getInstance();
    } else if (_lastFixingEndCalculator) {
      return LastFixingEndTimeCalculator.getInstance();
    }
    return LastTimeCalculator.getInstance();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("DiscountingMethodCurveTypeSetUp[discountingCurveId=");
    sb.append(_discountingCurveId);
    sb.append(", iborIndices=");
    sb.append(_iborCurveIndices);
    sb.append(", overnightIndices=");
    sb.append(_overnightCurveIndices);
    if (_functionalForm != null) {
      sb.append(", functionalForm=");
      sb.append(_functionalForm);
    } else {
      sb.append(", interpolator=");
      sb.append(_interpolator);
      if (_dates != null) {
        sb.append(", nodeDates=");
        sb.append(_dates);
      }
      if (_periodicInterpolationOnYield) {
        sb.append(", periodsPerYear=");
        sb.append(_periodsPerYear);
        sb.append(", interpolation on yield");
      }
      if (_continuousInterpolationOnYield) {
        sb.append(", interpolation on yield");
      }
      if (_continuousInterpolationOnDiscountFactors) {
        sb.append(", interpolation on discount factors");
      }
      if (_maturityCalculator) {
        sb.append(", using instrument maturity");
      }
      if (_lastFixingEndCalculator) {
        sb.append(", using last fixing period end");
      }
    }
    if (_baseCurveName != null) {
      sb.append(", baseCurve=");
      sb.append(_baseCurveName);
    }
    sb.append("]");
    return sb.toString();
  }
}
