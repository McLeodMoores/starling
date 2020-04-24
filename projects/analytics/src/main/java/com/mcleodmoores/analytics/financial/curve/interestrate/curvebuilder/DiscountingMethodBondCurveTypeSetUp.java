/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.index.IborTypeIndex;
import com.mcleodmoores.analytics.financial.index.OvernightIndex;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveAddYieldExisting;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveDiscountFactorInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolatedNode;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldNelsonSiegel;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldPeriodicInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.provider.calculator.generic.LastFixingEndTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * A builder that describes how a bond curve is to be constructed with the discounting method. Example configurations are shown below.
 *
 * <pre>
 * new DiscountingMethodBondCurveTypeSetUp()
 *     .forDiscounting(Currency.USD)
 *     .forIssuer(issuer)
 *     .withInterpolator(NamedInterpolator1dFactory("ModifiedPCHIP"))
 *     .continuousInterpolationOnYield()
 *     .usingInstrumentMaturity()
 * </pre>
 *
 * This constructs a USD discounting and bond curve that interpolates on continuous yields and uses the last maturity dates of the instruments that are used in
 * its construction.
 *
 * <pre>
 * new DiscountingMethodBondCurveTypeSetUp()
 *     .forDiscounting(Currency.USD)
 *     .forIssuer(issuer)
 *     .functionalForm(CurveFunction.NELSON_SIEGEL)
 * </pre>
 *
 * This constructs a bond curve that discounts payments. The curve is a Nelson-Siegel fit.
 */
public class DiscountingMethodBondCurveTypeSetUp extends DiscountingMethodBondCurveSetUp implements BondCurveTypeSetUpInterface {
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
  private List<Pair<Object, LegalEntityFilter<LegalEntity>>> _issuers;

  /**
   * Constructor that creates an empty builder.
   */
  DiscountingMethodBondCurveTypeSetUp() {
    super();
  }

  /**
   * Constructor that takes an existing builder. Note that this is not a copy constructor, i.e. any object references are shared.
   *
   * @param builder
   *          the builder, not null
   */
  DiscountingMethodBondCurveTypeSetUp(final DiscountingMethodBondCurveSetUp builder) {
    super(builder);
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp forDiscounting(final UniqueIdentifiable id) {
    _discountingCurveId = ArgumentChecker.notNull(id, "id");
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp forIndex(final IborTypeIndex... indices) {
    ArgumentChecker.notEmpty(indices, "indices");
    if (_iborCurveIndices == null) {
      _iborCurveIndices = new ArrayList<>();
    }
    _iborCurveIndices.addAll(Arrays.asList(indices));
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp forIssuer(final Pair<Object, LegalEntityFilter<LegalEntity>>... issuers) {
    ArgumentChecker.notEmpty(issuers, "issuers");
    ArgumentChecker.isTrue(issuers.length == 1, "Currently only one issuer per curve is supported");
    if (_issuers == null) {
      _issuers = new ArrayList<>();
    }
    _issuers.addAll(Arrays.asList(issuers));
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp forIndex(final OvernightIndex... indices) {
    ArgumentChecker.notEmpty(indices, "indices");
    if (_overnightCurveIndices == null) {
      _overnightCurveIndices = new ArrayList<>();
    }
    _overnightCurveIndices.addAll(Arrays.asList(indices));
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp functionalForm(final CurveFunction function) {
    if (_interpolator != null || _dates != null || _typeAlreadySet || _baseCurveName != null) {
      throw new IllegalStateException("Have already set up curve type");
    }
    _functionalForm = ArgumentChecker.notNull(function, "function");
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp withInterpolator(final Interpolator1D interpolator) {
    if (_functionalForm != null) {
      throw new IllegalStateException("Have already set curve type to be functional");
    }
    _interpolator = ArgumentChecker.notNull(interpolator, "interpolator");
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp asSpreadOver(final String baseCurveName) {
    if (_functionalForm != null) {
      throw new IllegalStateException("Cannot set a functional curve as a spread over another");
    }
    _baseCurveName = ArgumentChecker.notNull(baseCurveName, "baseCurveName");
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp usingNodeDates(final LocalDateTime... dates) {
    if (_functionalForm != null) {
      throw new IllegalStateException("Have already set curve type to be functional");
    }
    if (_periodicInterpolationOnYield) {
      throw new IllegalStateException("Cannot set node dates for a periodically-compounded curve");
    }
    ArgumentChecker.notEmpty(dates, "dates");
    if (_dates == null) {
      _dates = new ArrayList<>();
    }
    _dates.addAll(Arrays.asList(dates));
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp continuousInterpolationOnYield() {
    if (_functionalForm != null || _typeAlreadySet) {
      throw new IllegalStateException("Have already set up curve type");
    }
    _typeAlreadySet = true;
    _continuousInterpolationOnYield = true;
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp periodicInterpolationOnYield(final int compoundingPeriodsPerYear) {
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
  public DiscountingMethodBondCurveTypeSetUp continuousInterpolationOnDiscountFactors() {
    if (_functionalForm != null || _typeAlreadySet) {
      throw new IllegalStateException("Have already set up curve type");
    }
    _typeAlreadySet = true;
    _continuousInterpolationOnDiscountFactors = true;
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp usingInstrumentMaturity() {
    if (_timeCalculatorAlreadySet) {
      throw new IllegalStateException("The node time calculator has already been set");
    }
    _timeCalculatorAlreadySet = true;
    _maturityCalculator = true;
    return this;
  }

  @Override
  public DiscountingMethodBondCurveTypeSetUp usingLastFixingEndTime() {
    if (_timeCalculatorAlreadySet) {
      throw new IllegalStateException("The node time calculator has already been set");
    }
    _timeCalculatorAlreadySet = true;
    _lastFixingEndCalculator = true;
    return this;
  }

  /**
   * Gets the discounting curve identifier.
   *
   * @return the identifier, can be null
   */
  UniqueIdentifiable getDiscountingCurveId() {
    return _discountingCurveId;
  }

  /**
   * Gets the ibor curve indices.
   *
   * @return the indices, can be null or empty
   */
  List<IborTypeIndex> getIborCurveIndices() {
    return _iborCurveIndices == null ? null : Collections.unmodifiableList(_iborCurveIndices);
  }

  /**
   * Gets the overnight curve indices.
   *
   * @return the indices, can be null or empty
   */
  List<OvernightIndex> getOvernightCurveIndices() {
    return _overnightCurveIndices == null ? null : Collections.unmodifiableList(_overnightCurveIndices);
  }

  /**
   * Gets the issuers.
   *
   * @return the issuers, can be null or empty
   */
  List<Pair<Object, LegalEntityFilter<LegalEntity>>> getIssuers() {
    return _issuers == null ? null : Collections.unmodifiableList(_issuers);
  }

  /**
   * Gets the fixed node dates.
   *
   * @return the fixed node dates, can be null or empty.
   */
  List<LocalDateTime> getFixedNodeDates() {
    return _dates == null ? null : Collections.unmodifiableList(_dates);
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
      // TODO positive or negative spread
      return new GeneratorCurveAddYieldExisting(generator, false, _baseCurveName);
    }
    return generator;
  }

  @Override
  public InstrumentDerivativeVisitor<Object, Double> getNodeTimeCalculator() {
    if (_maturityCalculator) {
      return LastTimeCalculator.getInstance();
    } else if (_lastFixingEndCalculator) {
      return LastFixingEndTimeCalculator.getInstance();
    }
    return LastTimeCalculator.getInstance();
  }

}
