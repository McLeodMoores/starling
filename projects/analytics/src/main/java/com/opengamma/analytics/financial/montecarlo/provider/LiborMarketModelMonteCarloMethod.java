/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.montecarlo.provider;

import java.util.Arrays;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.interestrate.definition.LiborMarketModelDisplacedDiffusionParameters;
import com.opengamma.analytics.financial.montecarlo.DecisionSchedule;
import com.opengamma.analytics.financial.montecarlo.MonteCarloIborRateDataBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.LiborMarketModelDisplacedDiffusionProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.random.RandomNumberGenerator;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Monte Carlo pricing method in the Libor Market Model with Displaced Diffusion.
 */
public class LiborMarketModelMonteCarloMethod extends MonteCarloMethod {

  /**
   * The maximum length of a jump in the path generation.
   */
  private final double _maxJump;

  /**
   * The decision schedule calculator (calculate the exercise dates, the cash flow dates and the reference amounts).
   */
  private static final DecisionScheduleCalculator DC = DecisionScheduleCalculator.getInstance();
  /**
   * The calculator from discount factors (calculate the price from simulated discount factors and the reference amounts).
   */
  private static final MonteCarloIborRateCalculator MCC = MonteCarloIborRateCalculator.getInstance();
  /**
   * The number of paths in one block.
   */
  private static final int BLOCK_SIZE = 1000;
  /**
   * The default maximum length of a jump in the path generation.
   */
  private static final double MAX_JUMP_DEFAULT = 1.0;

  /**
   * Constructor.
   *
   * @param numberGenerator
   *          The random number generator. Generate Normally distributed numbers.
   * @param nbPath
   *          The number of paths.
   */
  public LiborMarketModelMonteCarloMethod(final RandomNumberGenerator numberGenerator, final int nbPath) {
    super(numberGenerator, nbPath);
    _maxJump = MAX_JUMP_DEFAULT;
  }

  /**
   * Constructor.
   *
   * @param numberGenerator
   *          The random number generator. Generate Normally distributed numbers.
   * @param nbPath
   *          The number of paths.
   * @param maxJump
   *          The maximum length of a jump in the path generation.
   */
  public LiborMarketModelMonteCarloMethod(final RandomNumberGenerator numberGenerator, final int nbPath, final double maxJump) {
    super(numberGenerator, nbPath);
    _maxJump = maxJump;
  }

  public MultipleCurrencyAmount presentValue(final InstrumentDerivative instrument, final Currency ccy,
      final LiborMarketModelDisplacedDiffusionProvider lmmData) {
    final MulticurveProviderInterface multicurves = lmmData.getMulticurveProvider();
    final LiborMarketModelDisplacedDiffusionParameters parameters = lmmData.getLMMParameters();
    // The numeraire is the last time in the LMM description.
    final DecisionSchedule decision = instrument.accept(DC, multicurves);
    final int[][] impactIndex = index(decision.getImpactTime(), parameters);

    final int nbPeriodLMM = parameters.getNbPeriod();
    final double[] initL = new double[nbPeriodLMM];
    final double[] deltaLMM = parameters.getAccrualFactor();
    final double[] dfL = new double[nbPeriodLMM + 1];
    for (int i = 0; i < nbPeriodLMM + 1; i++) {
      dfL[i] = multicurves.getDiscountFactor(ccy, parameters.getIborTime()[i]);
    }
    for (int i = 0; i < nbPeriodLMM; i++) {
      initL[i] = (dfL[i] / dfL[i + 1] - 1.0) / deltaLMM[i];
    }

    final int nbBlock = (int) Math.round(Math.ceil(getNbPath() / (double) BLOCK_SIZE));
    final int[] nbPath2 = new int[nbBlock];
    for (int i = 0; i < nbBlock - 1; i++) {
      nbPath2[i] = BLOCK_SIZE;
    }
    nbPath2[nbBlock - 1] = getNbPath() - (nbBlock - 1) * BLOCK_SIZE;

    double price = 0.0;
    for (int i = 0; i < nbBlock; i++) {
      final double[][] initLPath = new double[nbPeriodLMM][nbPath2[i]];
      for (int j = 0; j < nbPeriodLMM; j++) {
        for (int k = 0; k < nbPath2[i]; k++) {
          initLPath[j][k] = initL[j];
        }
      }
      final double[][][] pathIbor = pathgeneratorlibor(decision.getDecisionTime(), initLPath, parameters);
      price += instrument.accept(MCC, new MonteCarloIborRateDataBundle(pathIbor, deltaLMM, decision.getImpactAmount(), impactIndex));
    }
    price *= multicurves.getDiscountFactor(ccy, parameters.getIborTime()[parameters.getIborTime().length - 1]) / getNbPath();
    return MultipleCurrencyAmount.of(ccy, price);
  }

  private int[][] index(final double[][] time, final LiborMarketModelDisplacedDiffusionParameters lmm) {
    final int[][] index = new int[time.length][];
    for (int i = 0; i < time.length; i++) {
      index[i] = new int[time[i].length];
      for (int j = 0; j < time[i].length; j++) {
        index[i][j] = lmm.getTimeIndex(time[i][j]);
      }
    }
    return index;
  }

  /**
   * Create one step in the LMM diffusion. The step is done through several jump times. The diffusion is approximated with a
   * predictor-corrector approach.
   *
   * @param jumpTime
   *          The jump times.
   * @param initIbor
   *          Rate at the start of the period. Size: nbPeriodLMM x nbPath.
   * @return The Ibor rates at the end of the jump period. Size: nbPeriodLMM x nbPath.
   */
  private double[][] stepPC(final double[] jumpTime, final double[][] initIbor, final LiborMarketModelDisplacedDiffusionParameters lmm) {
    final double amr = lmm.getMeanReversion();
    final double[] iborTime = lmm.getIborTime();
    final double[] almm = lmm.getDisplacement();
    final double[] deltalmm = lmm.getAccrualFactor();
    final DoubleMatrix2D gammaLMM = new DoubleMatrix2D(lmm.getVolatility());
    final MatrixAlgebra algebra = new CommonsMatrixAlgebra();
    final DoubleMatrix2D s = (DoubleMatrix2D) algebra.multiply(gammaLMM, algebra.getTranspose(gammaLMM));
    final int nbJump = jumpTime.length - 1;
    final int nbPath = initIbor[0].length;
    final int nbPeriodLMM = lmm.getNbPeriod();
    final int nbFactorLMM = lmm.getNbFactor();
    final double[] dt = new double[nbJump];
    final double[] alpha = new double[nbJump];
    final double[] alpha2 = new double[nbJump];
    for (int i = 0; i < nbJump; i++) {
      dt[i] = jumpTime[i + 1] - jumpTime[i];
      alpha[i] = Math.exp(amr * jumpTime[i + 1]);
      alpha2[i] = alpha[i] * alpha[i];
    }

    final double[][] f = initIbor;
    for (int i = 0; i < nbJump; i++) {
      final double sqrtDt = Math.sqrt(dt[i]);
      int index = Arrays.binarySearch(iborTime, jumpTime[i + 1] - lmm.getTimeTolerance());
      index = -index - 1; // The index from which the rate should be evolved.
      final int nI = nbPeriodLMM - index;
      final double[] dI = new double[nI];
      for (int j = 0; j < nI; j++) {
        dI[j] = 1.0 / deltalmm[index + j];
      }
      final double[][] salpha2Array = new double[nI][nI];
      for (int j = 0; j < nI; j++) {
        for (int k = 0; k < nI; k++) {
          salpha2Array[j][k] = s.getEntry(index + j, index + k) * alpha2[i];
        }
      }
      final DoubleMatrix2D salpha2 = new DoubleMatrix2D(salpha2Array);
      // Random seed
      final double[][] dw = getNormalArray(nbFactorLMM, nbPath);
      // Common figures
      final double[] dr1 = new double[nI];
      for (int loopn = 0; loopn < nI; loopn++) {
        dr1[loopn] = -salpha2.getEntry(loopn, loopn) * dt[i] / 2.0;
      }
      final double[][] cc = new double[nI][nbPath];
      for (int j = 0; j < nI; j++) {
        for (int k = 0; k < nbPath; k++) {
          for (int loopfact = 0; loopfact < nbFactorLMM; loopfact++) {
            cc[j][k] += gammaLMM.getEntry(index + j, loopfact) * dw[loopfact][k] * sqrtDt * alpha[i];
          }
          cc[j][k] += dr1[j];
        }
      }
      // Unique step: predictor and corrector
      final double[][] mP = new double[nI][nbPath];
      final double[][] mC = new double[nI][nbPath];
      final double[][] coefP = new double[nbPath][nI - 1];
      final double[][] coefC = new double[nI][nbPath];
      for (int j = 0; j < nbPath; j++) {
        for (int k = 0; k < nI - 1; k++) {
          coefP[j][k] = (f[index + k + 1][j] + almm[index + k + 1]) / (f[index + k + 1][j] + dI[k + 1]);
        }
      }
      for (int j = nI - 1; j >= 0; j--) {
        if (j < nI - 1) {
          for (int k = 0; k < nbPath; k++) {
            coefC[j + 1][k] = (f[index + j + 1][k] + almm[index + j + 1]) / (f[index + j + 1][k] + dI[j + 1]);
            for (int l = j + 1; l < nI; l++) {
              mP[j][k] += salpha2.getEntry(l, j) * coefP[k][l - 1];
              mC[j][k] += salpha2.getEntry(l, j) * coefC[l][k];
            }
          }
          for (int k = 0; k < nbPath; k++) {
            f[j + index][k] = (f[j + index][k] + almm[index + j])
                * Math.exp(-(mP[j][k] + mC[j][k]) * dt[i] / 2.0 + cc[j][k]) - almm[index + j];
          }
        } else {
          for (int k = 0; k < nbPath; k++) {
            f[j + index][k] = (f[j + index][k] + almm[index + j]) * Math.exp(cc[j][k])
                - almm[index + j];
          }
        }
      }
    }
    return f;
  }

  /**
   *
   * @param jumpTime
   *          The time of the mandatory jumps.
   * @param initIbor
   *          The Ibor rates at the start. nbPeriodLMM x nbPath
   * @param lmm
   *          The LMM parameters.
   * @return The paths. Size: nbJump x nbPeriodLMM x nbPath
   */
  private double[][][] pathgeneratorlibor(final double[] jumpTime, final double[][] initIbor,
      final LiborMarketModelDisplacedDiffusionParameters lmm) {
    final int nbPeriod = initIbor.length;
    final int nbPath = initIbor[0].length;
    final int nbJump = jumpTime.length;
    double[][] initTmp = new double[nbPeriod][nbPath];
    for (int loop1 = 0; loop1 < nbPeriod; loop1++) {
      System.arraycopy(initIbor[loop1], 0, initTmp[loop1], 0, nbPath);
    }
    final double[] jumpTimeA = new double[nbJump + 1];
    jumpTimeA[0] = 0;
    System.arraycopy(jumpTime, 0, jumpTimeA, 1, nbJump);
    final double[][][] result = new double[nbJump][nbPeriod][nbPath];
    // TODO: add intermediary jump dates if necessary
    for (int i = 0; i < nbJump; i++) {
      // Intermediary jumps
      double[] jumpIn;
      if (jumpTimeA[i + 1] - jumpTimeA[i] < _maxJump) {
        jumpIn = new double[] { jumpTimeA[i], jumpTimeA[i + 1] };
      } else {
        final double jump = jumpTimeA[i + 1] - jumpTimeA[i];
        final int nbJumpIn = (int) Math.ceil(jump / _maxJump);
        jumpIn = new double[nbJumpIn + 1];
        jumpIn[0] = jumpTimeA[i];
        for (int j = 1; j <= nbJumpIn; j++) {
          jumpIn[j] = jumpTimeA[i] + j * jump / nbJumpIn;
        }
      }
      initTmp = stepPC(jumpIn, initTmp, lmm);
      for (int j = 0; j < nbPeriod; j++) {
        System.arraycopy(initTmp[j], 0, result[i][j], 0, nbPath);
      }
    }
    return result;
  }

  /**
   * Gets a 2D-array of independent normally distributed variables.
   *
   * @param nbJump
   *          The number of jumps.
   * @param nbPath
   *          The number of paths.
   * @return The array of variables.
   */
  private double[][] getNormalArray(final int nbJump, final int nbPath) {
    final double[][] result = new double[nbJump][nbPath];
    for (int i = 0; i < nbJump; i++) {
      result[i] = getNumberGenerator().getVector(nbPath);
    }
    return result;
  }

}
