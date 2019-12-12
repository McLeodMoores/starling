/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.annuity.derivative.AnnuityPaymentFixed;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuturesSecurity;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.analytics.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantParameters;
import com.opengamma.analytics.financial.provider.calculator.discounting.CashFlowEquivalentCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.HullWhiteIssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.IssuerProviderInterface;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscountingDecoratedIssuer;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.math.rootfinding.BracketRoot;
import com.opengamma.analytics.math.rootfinding.RidderSingleRootFinder;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Computes the par rate for different instrument. The meaning of "par rate" is instrument dependent.
 */
public final class FuturesPriceCurveSensitivityHullWhiteIssuerCalculator
    extends InstrumentDerivativeVisitorAdapter<HullWhiteIssuerProviderInterface, MulticurveSensitivity> {

  /**
   * The unique instance of the calculator.
   */
  private static final FuturesPriceCurveSensitivityHullWhiteIssuerCalculator INSTANCE = new FuturesPriceCurveSensitivityHullWhiteIssuerCalculator();

  /**
   * Gets the calculator instance.
   *
   * @return The calculator.
   */
  public static FuturesPriceCurveSensitivityHullWhiteIssuerCalculator getInstance() {
    return INSTANCE;
  }

  /**
   * Constructor.
   */
  private FuturesPriceCurveSensitivityHullWhiteIssuerCalculator() {
  }

  /** The number of points used in the numerical integration process. */
  private static final int DEFAULT_NB_POINTS = 81;
  /** The normal distribution implementation. */
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);
  /** The cash flow equivalent calculator used in computations. */
  private static final CashFlowEquivalentCalculator CFEC = CashFlowEquivalentCalculator.getInstance();
  /** The model used in computations. */
  private static final HullWhiteOneFactorPiecewiseConstantInterestRateModel MODEL = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  // ----- Futures -----

  @Override
  public MulticurveSensitivity visitBondFuturesSecurity(final BondFuturesSecurity futures,
      final HullWhiteIssuerProviderInterface multicurve) {
    return visitBondFuturesSecurity(futures, multicurve, DEFAULT_NB_POINTS);
  }

  /**
   * Computes the future price curve sensitivity.
   *
   * @param futures
   *          The future security.
   * @param data
   *          The curve and Hull-White parameters.
   * @param nbPoint
   *          The number of point in the numerical cross estimation.
   * @return The curve sensitivity.
   */
  public MulticurveSensitivity visitBondFuturesSecurity(final BondFuturesSecurity futures, final HullWhiteIssuerProviderInterface data,
      final int nbPoint) {
    ArgumentChecker.notNull(futures, "Future");
    ArgumentChecker.notNull(data, "Hull-White data bundle");
    final int nbBond = futures.getDeliveryBasketAtDeliveryDate().length;
    final LegalEntity issuer = futures.getDeliveryBasketAtDeliveryDate()[0].getIssuerEntity();
    final HullWhiteOneFactorPiecewiseConstantParameters parameters = data.getHullWhiteParameters();
    final IssuerProviderInterface issuerProvider = data.getIssuerProvider();
    final MulticurveProviderInterface multicurvesDecorated = new MulticurveProviderDiscountingDecoratedIssuer(issuerProvider,
        futures.getCurrency(), issuer);

    final double expiry = futures.getNoticeLastTime();
    final double delivery = futures.getDeliveryLastTime();
    final double dfdelivery = data.getIssuerProvider().getDiscountFactor(issuer, delivery);
    // Constructing non-homogeneous point series for the numerical estimations.
    final int nbPtWing = (int) Math.floor(nbPoint / 20.); // Number of point on each wing.
    final int nbPtCenter = nbPoint - 2 * nbPtWing;
    final double prob = 1.0 / (2.0 * nbPtCenter);
    final double xStart = NORMAL.getInverseCDF(prob);
    final double[] x = new double[nbPoint];
    for (int i = 0; i < nbPtWing; i++) {
      x[i] = xStart * (1.0 + (nbPtWing - i) / 2.0);
      x[nbPoint - 1 - i] = -xStart * (1.0 + (nbPtWing - i) / 2.0);
    }
    for (int i = 0; i < nbPtCenter; i++) {
      x[nbPtWing + i] = xStart + i * (-2.0 * xStart) / (nbPtCenter - 1);
    }
    // Figures for each bond
    final double[][] cfTime = new double[nbBond][];
    final double[][] df = new double[nbBond][];
    final double[][] alpha = new double[nbBond][];
    final double[][] beta = new double[nbBond][];
    final double[][] cfaAdjusted = new double[nbBond][];
    final double[] e = new double[nbBond];
    final double[][] pv = new double[nbPoint][nbBond];
    final AnnuityPaymentFixed[] cf = new AnnuityPaymentFixed[nbBond];
    for (int i = 0; i < nbBond; i++) {
      cf[i] = futures.getDeliveryBasketAtDeliveryDate()[i].accept(CFEC, multicurvesDecorated);
      final int nbCf = cf[i].getNumberOfPayments();
      cfTime[i] = new double[nbCf];
      df[i] = new double[nbCf];
      alpha[i] = new double[nbCf];
      beta[i] = new double[nbCf];
      cfaAdjusted[i] = new double[nbCf];
      for (int j = 0; j < nbCf; j++) {
        cfTime[i][j] = cf[i].getNthPayment(j).getPaymentTime();
        df[i][j] = issuerProvider.getDiscountFactor(issuer, cfTime[i][j]);
        alpha[i][j] = MODEL.alpha(parameters, 0.0, expiry, delivery, cfTime[i][j]);
        beta[i][j] = MODEL.futuresConvexityFactor(parameters, expiry, cfTime[i][j], delivery);
        cfaAdjusted[i][j] = df[i][j] / dfdelivery * beta[i][j]
            * cf[i].getNthPayment(j).getAmount() / futures.getConversionFactor()[i];
        for (int looppt = 0; looppt < nbPoint; looppt++) {
          pv[looppt][i] += cfaAdjusted[i][j]
              * Math.exp(-alpha[i][j] * alpha[i][j] / 2.0 - alpha[i][j] * x[looppt]);
        }
      }
      e[i] = futures.getDeliveryBasketAtDeliveryDate()[i].getAccruedInterest() / futures.getConversionFactor()[i];
      for (int looppt = 0; looppt < nbPoint; looppt++) {
        pv[looppt][i] -= e[i];
      }
    }
    // Minimum: create a list of index of the CTD in each interval and a first estimate of the crossing point (x[]).
    final double[] pvMin = new double[nbPoint];
    final int[] indMin = new int[nbPoint];
    for (int i = 0; i < nbPoint; i++) {
      pvMin[i] = Double.POSITIVE_INFINITY;
      for (int j = 0; j < nbBond; j++) {
        if (pv[i][j] < pvMin[i]) {
          pvMin[i] = pv[i][j];
          indMin[i] = j;
        }
      }
    }
    final ArrayList<Double> refx = new ArrayList<>();
    final ArrayList<Integer> ctd = new ArrayList<>();
    int lastInd = indMin[0];
    ctd.add(indMin[0]);
    for (int i = 1; i < nbPoint; i++) {
      if (indMin[i] != lastInd) {
        ctd.add(indMin[i]);
        lastInd = indMin[i];
        refx.add(x[i]);
      }
    }
    // Sum on each interval
    final int nbInt = ctd.size();
    final double[] kappa = new double[nbInt - 1];
    // double price = 0.0;
    if (nbInt != 1) {
      // The intersections
      final BracketRoot bracketer = new BracketRoot();
      final double accuracy = 1.0E-8;
      final RidderSingleRootFinder rootFinder = new RidderSingleRootFinder(accuracy);
      for (int i = 1; i < nbInt; i++) {
        final BondDifference cross = new BondDifference(cfaAdjusted[ctd.get(i - 1)], alpha[ctd.get(i - 1)],
            e[ctd.get(i - 1)], cfaAdjusted[ctd.get(i)],
            alpha[ctd.get(i)], e[ctd.get(i)]);
        final double[] range = bracketer.getBracketedPoints(cross, refx.get(i - 1) - 0.01, refx.get(i - 1) + 0.01);
        kappa[i - 1] = rootFinder.getRoot(cross, range[0], range[1]);
      }
    }

    // === Backward Sweep ===
    final double priceBar = 1.0;
    final double[][] cfaAdjustedBar = new double[nbBond][];
    final double[][] dfBar = new double[nbBond][];
    for (int i = 0; i < nbBond; i++) {
      final int nbCf = cf[i].getNumberOfPayments();
      cfaAdjustedBar[i] = new double[nbCf];
      dfBar[i] = new double[nbCf];
    }
    double dfdeliveryBar = 0.0;
    final Map<String, List<DoublesPair>> resultMap = new HashMap<>();
    final List<DoublesPair> listCredit = new ArrayList<>();
    if (nbInt == 1) {
      for (int i = 0; i < cfaAdjusted[ctd.get(0)].length; i++) {
        cfaAdjustedBar[ctd.get(0)][i] = priceBar;
        dfBar[ctd.get(0)][i] = beta[ctd.get(0)][i] / dfdelivery * cf[ctd.get(0)].getNthPayment(i).getAmount()
            / futures.getConversionFactor()[ctd.get(0)]
            * cfaAdjustedBar[ctd.get(0)][i];
        listCredit.add(
            DoublesPair.of(cfTime[ctd.get(0)][i], -cfTime[ctd.get(0)][i] * df[ctd.get(0)][i] * dfBar[ctd.get(0)][i]));
        dfdeliveryBar += -cfaAdjusted[ctd.get(0)][i] / dfdelivery * cfaAdjustedBar[ctd.get(0)][i];
      }
      listCredit.add(DoublesPair.of(delivery, -delivery * dfdelivery * dfdeliveryBar));
    } else {
      // From -infinity to first cross.
      for (int i = 0; i < cfaAdjusted[ctd.get(0)].length; i++) {
        cfaAdjustedBar[ctd.get(0)][i] = NORMAL.getCDF(kappa[0] + alpha[ctd.get(0)][i]) * priceBar;
      }
      // Between cross
      for (int i = 1; i < nbInt - 1; i++) {
        for (int j = 0; j < cfaAdjusted[ctd.get(i)].length; j++) {
          cfaAdjustedBar[ctd.get(i)][j] = (NORMAL.getCDF(kappa[i] + alpha[ctd.get(i)][j])
              - NORMAL.getCDF(kappa[i - 1] + alpha[ctd.get(i)][j])) * priceBar;
        }
      }
      // From last cross to +infinity
      for (int loopcf = 0; loopcf < cfaAdjusted[ctd.get(nbInt - 1)].length; loopcf++) {
        cfaAdjustedBar[ctd.get(nbInt - 1)][loopcf] = (1.0 - NORMAL.getCDF(kappa[nbInt - 2] + alpha[ctd.get(nbInt - 1)][loopcf])) * priceBar;
      }
      for (int i = 0; i < nbBond; i++) { // Could be reduced to only the ctd intervals.
        for (int j = 0; j < cfaAdjusted[i].length; j++) {
          dfBar[i][j] = beta[i][j] / dfdelivery * cf[i].getNthPayment(j).getAmount()
              / futures.getConversionFactor()[i] * cfaAdjustedBar[i][j];
          listCredit.add(DoublesPair.of(cfTime[i][j], -cfTime[i][j] * df[i][j] * dfBar[i][j]));
          dfdeliveryBar += -cfaAdjusted[i][j] / dfdelivery * cfaAdjustedBar[i][j];
        }
      }
      listCredit.add(DoublesPair.of(delivery, -delivery * dfdelivery * dfdeliveryBar));
    }
    resultMap.put(data.getIssuerProvider().getName(issuer), listCredit);
    return MulticurveSensitivity.ofYieldDiscounting(resultMap);
  }

  /**
   * Internal class to estimate the price difference between two bonds (used for bond futures).
   */
  private static final class BondDifference implements Function<Double, Double> {
    private final double[] _cfa1;
    private final double[] _alpha1;
    private final double _e1;
    private final double[] _cfa2;
    private final double[] _alpha2;
    private final double _e2;

    BondDifference(final double[] cfa1, final double[] alpha1, final double e1, final double[] cfa2, final double[] alpha2,
        final double e2) {
      _cfa1 = cfa1;
      _alpha1 = alpha1;
      _e1 = e1;
      _cfa2 = cfa2;
      _alpha2 = alpha2;
      _e2 = e2;
    }

    @Override
    public Double apply(final Double x) {
      double pv = 0.0;
      for (int i = 0; i < _cfa1.length; i++) {
        pv += _cfa1[i] * Math.exp(-_alpha1[i] * _alpha1[i] / 2.0 - _alpha1[i] * x);
      }
      pv -= _e1;
      for (int i = 0; i < _cfa2.length; i++) {
        pv -= _cfa2[i] * Math.exp(-_alpha2[i] * _alpha2[i] / 2.0 - _alpha2[i] * x);
      }
      pv += _e2;
      return pv;
    }
  }

}
