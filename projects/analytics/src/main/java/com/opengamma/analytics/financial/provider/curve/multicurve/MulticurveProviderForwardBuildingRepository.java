/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2016-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.analytics.financial.provider.curve.multicurve;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlock;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderForward;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.ParameterSensitivityMulticurveMatrixCalculator;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.linearalgebra.DecompositionFactory;
import com.opengamma.analytics.math.matrix.CommonsMatrixAlgebra;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.analytics.math.matrix.MatrixAlgebra;
import com.opengamma.analytics.math.rootfinding.newton.BroydenVectorRootFinder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

/**
 * Functions to build curves.
 */
//TODO: REVIEW: Embed in a better object.
public class MulticurveProviderForwardBuildingRepository {

  /**
   * The absolute tolerance for the root finder.
   */
  private final double _toleranceAbs;
  /**
   * The relative tolerance for the root finder.
   */
  private final double _toleranceRel;
  /**
   * The relative tolerance for the root finder.
   */
  private final int _stepMaximum;
  /**
   * The root finder used for curve calibration.
   */
  private final BroydenVectorRootFinder _rootFinder;
  /**
   * The matrix algebra used for matrix inversion.
   */
  private static final MatrixAlgebra MATRIX_ALGEBRA = new CommonsMatrixAlgebra();

  /**
   * Constructor.
   * @param toleranceAbs The absolute tolerance for the root finder.
   * @param toleranceRel The relative tolerance for the root finder.
   * @param stepMaximum The maximum number of step for the root finder.
   */
  public MulticurveProviderForwardBuildingRepository(final double toleranceAbs, final double toleranceRel, final int stepMaximum) {
    _toleranceAbs = toleranceAbs;
    _toleranceRel = toleranceRel;
    _stepMaximum = stepMaximum;
    _rootFinder = new BroydenVectorRootFinder(_toleranceAbs, _toleranceRel, _stepMaximum,
        DecompositionFactory.getDecomposition(DecompositionFactory.SV_COLT_NAME));
    // TODO: make the root finder flexible.
  }

  /**
   * Build a unit of curves.
   * @param instruments The instruments used for the unit calibration.
   * @param initGuess The initial parameters guess.
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param generatorsMap The generators map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended)
   * or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return The new curves and the calibrated parameters.
   */
  private Pair<MulticurveProviderForward, Double[]> makeUnit(final InstrumentDerivative[] instruments, final double[] initGuess,
      final MulticurveProviderForward knownData, final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IborIndex> forwardIborMap,
      final LinkedHashMap<String, IndexON> forwardONMap, final LinkedHashMap<String, GeneratorYDCurve> generatorsMap,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    final GeneratorMulticurveProviderForward generator =
        new GeneratorMulticurveProviderForward(knownData, discountingMap, forwardIborMap, forwardONMap, generatorsMap);
    final MulticurveProviderForwardBuildingData data = new MulticurveProviderForwardBuildingData(instruments, generator);
    final Function1D<DoubleMatrix1D, DoubleMatrix1D> curveCalculator = new MulticurveProviderForwardFinderFunction(calculator, data);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MulticurveProviderForwardFinderJacobian(
        new ParameterSensitivityMulticurveMatrixCalculator(sensitivityCalculator), data);
    final double[] parameters = _rootFinder.getRoot(curveCalculator, jacobianCalculator, new DoubleMatrix1D(initGuess)).getData();
    final MulticurveProviderForward newCurves = data.getGeneratorMarket().evaluate(new DoubleMatrix1D(parameters));
    return Pairs.of(newCurves, ArrayUtils.toObject(parameters));
  }

  /**
   * Build the Jacobian matrixes associated to a unit of curves.
   * @param instruments The instruments used for the block calibration.
   * @param startBlock The index of the first parameter of the unit in the block.
   * @param nbParameters The number of parameters for each curve in the unit.
   * @param parameters The parameters used to build each curve in the block.
   * @param knownData The known data (FX rates, other curves, model parameters, ...) for the block calibration.
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param generatorsMap The generators map.
   * @param sensitivityCalculator The parameter sensitivity calculator for the value on which the calibration is done
  (usually ParSpreadMarketQuoteDiscountingProviderCalculator (recommended) or converted present value).
   * @return The part of the inverse Jacobian matrix associated to each curve.
   * The Jacobian matrix is the transition matrix between the curve parameters and the par spread.
   * TODO: Currently only for the ParSpreadMarketQuoteDiscountingProviderCalculator.
   */
  private static DoubleMatrix2D[] makeCurveMatrix(final InstrumentDerivative[] instruments, final int startBlock, final int[] nbParameters,
      final Double[] parameters, final MulticurveProviderForward knownData, final LinkedHashMap<String, Currency> discountingMap,
      final LinkedHashMap<String, IborIndex> forwardIborMap, final LinkedHashMap<String, IndexON> forwardONMap,
      final LinkedHashMap<String, GeneratorYDCurve> generatorsMap,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    final GeneratorMulticurveProviderForward generator =
        new GeneratorMulticurveProviderForward(knownData, discountingMap, forwardIborMap, forwardONMap, generatorsMap);
    final MulticurveProviderForwardBuildingData data = new MulticurveProviderForwardBuildingData(instruments, generator);
    final Function1D<DoubleMatrix1D, DoubleMatrix2D> jacobianCalculator = new MulticurveProviderForwardFinderJacobian(
        new ParameterSensitivityMulticurveMatrixCalculator(sensitivityCalculator), data);
    final DoubleMatrix2D jacobian = jacobianCalculator.evaluate(new DoubleMatrix1D(parameters));
    final DoubleMatrix2D inverseJacobian = MATRIX_ALGEBRA.getInverse(jacobian);
    final double[][] matrixTotal = inverseJacobian.getData();
    final DoubleMatrix2D[] result = new DoubleMatrix2D[nbParameters.length];
    int startCurve = 0;
    for (int i = 0; i < nbParameters.length; i++) {
      final double[][] matrixCurve = new double[nbParameters[i]][matrixTotal.length];
      for (int loopparam = 0; loopparam < nbParameters[i]; loopparam++) {
        matrixCurve[loopparam] = matrixTotal[startBlock + startCurve + loopparam].clone();
      }
      result[i] = new DoubleMatrix2D(matrixCurve);
      startCurve += nbParameters[i];
    }
    return result;
  }

  /**
   * Build a block of curves.
   * @param instruments The instruments used for the block calibration.
   * @param curveGenerators The curve generators (final version). As an array of arrays, representing the units and the curves within the units.
   * @param curveNames The names of the different curves. As an array of arrays, representing the units and the curves within the units.
   * @param parametersGuess The initial guess for the parameters. As an array of arrays, representing the units and the parameters for one unit
   * (all the curves of the unit concatenated).
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator (recommended)
   * or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return A pair with the calibrated yield curve bundle (including the known data) and the CurveBuildingBlckBundle with the relevant inverse Jacobian Matrix.
   */
  public Pair<MulticurveProviderForward, CurveBuildingBlockBundle> makeCurvesFromDerivatives(final InstrumentDerivative[][][] instruments,
      final GeneratorYDCurve[][] curveGenerators, final String[][] curveNames, final double[][] parametersGuess, final MulticurveProviderForward knownData,
      final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IborIndex> forwardIborMap,
      final LinkedHashMap<String, IndexON> forwardONMap, final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    final int nUnits = curveGenerators.length;
    final MulticurveProviderForward knownSoFarData = knownData.copy();
    final List<InstrumentDerivative> instrumentsSoFar = new ArrayList<>();
    final LinkedHashMap<String, GeneratorYDCurve> generatorsSoFar = new LinkedHashMap<>();
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundleSoFar = new LinkedHashMap<>();
    final List<Double> parametersSoFar = new ArrayList<>();
    final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
    int startUnit = 0;
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = curveGenerators[i].length;
      final int[] startCurve = new int[nCurves]; // First parameter index of the curve in the unit.
      final LinkedHashMap<String, GeneratorYDCurve> gen = new LinkedHashMap<>();
      final int[] nInstruments = new int[curveGenerators[i].length];
      int totalInstrumentsInUnit = 0; // Number of instruments in the unit.
      for (int j = 0; j < nCurves; j++) {
        startCurve[j] = totalInstrumentsInUnit;
        nInstruments[j] = instruments[i][j].length;
        totalInstrumentsInUnit += nInstruments[j];
        instrumentsSoFar.addAll(Arrays.asList(instruments[i][j]));
      }
      final InstrumentDerivative[] instrumentsUnit = new InstrumentDerivative[totalInstrumentsInUnit];
      final InstrumentDerivative[] instrumentsSoFarArray = instrumentsSoFar.toArray(new InstrumentDerivative[instrumentsSoFar.size()]);
      for (int j = 0; j < nCurves; j++) {
        System.arraycopy(instruments[i][j], 0, instrumentsUnit, startCurve[j], nInstruments[j]);
      }
      for (int j = 0; j < nCurves; j++) {
        final GeneratorYDCurve tmp = curveGenerators[i][j].finalGenerator(instruments[i][j]);
        gen.put(curveNames[i][j], tmp);
        generatorsSoFar.put(curveNames[i][j], tmp);
        unitMap.put(curveNames[i][j], Pairs.of(startUnit + startCurve[j], nInstruments[j]));
      }
      final Pair<MulticurveProviderForward, Double[]> unitCal = makeUnit(instrumentsUnit, parametersGuess[i], knownSoFarData,
          discountingMap, forwardIborMap, forwardONMap, gen, calculator,
          sensitivityCalculator);
      parametersSoFar.addAll(Arrays.asList(unitCal.getSecond()));
      final DoubleMatrix2D[] mat = makeCurveMatrix(instrumentsSoFarArray, startUnit, nInstruments, parametersSoFar.toArray(new Double[parametersSoFar.size()]),
          knownData, discountingMap, forwardIborMap, forwardONMap, generatorsSoFar, sensitivityCalculator);
      for (int j = 0; j < curveGenerators[i].length; j++) {
        unitBundleSoFar.put(curveNames[i][j], Pairs.of(new CurveBuildingBlock(unitMap), mat[j]));
      }
      knownSoFarData.setAll(unitCal.getFirst());
      startUnit = startUnit + totalInstrumentsInUnit;
    }
    return Pairs.of(knownSoFarData, new CurveBuildingBlockBundle(unitBundleSoFar));
  }

  /**
   * Build a block of curves without a known CurveBuildingBlockBundle.
   * @param curveBundles The bundles of curve data used in construction.
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator
   * (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return A pair with the calibrated yield curve bundle (including the known data) and the CurveBuildingBlockBundle with the
   * relevant inverse Jacobian Matrix.
   */

  public Pair<MulticurveProviderForward, CurveBuildingBlockBundle> makeCurvesFromDerivatives(final MultiCurveBundle<GeneratorYDCurve>[] curveBundles,
      final MulticurveProviderForward knownData, final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IborIndex> forwardIborMap,
      final LinkedHashMap<String, IndexON> forwardONMap, final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    return makeCurvesFromDerivatives(curveBundles, knownData, new CurveBuildingBlockBundle(), discountingMap, forwardIborMap, forwardONMap,
        calculator, sensitivityCalculator);
  }

  /**
   * Build a block of curves with a known CurveBuildingBlockBundle.
   * @param curveBundles The bundles of curve data used in construction.
   * @param knownData The known data (fx rates, other curves, model parameters, ...)
   * @param knownBlockBundle The already build CurveBuildingBlockBundle.
   * @param discountingMap The discounting curves names map.
   * @param forwardIborMap The forward curves names map.
   * @param forwardONMap The forward curves names map.
   * @param calculator The calculator of the value on which the calibration is done (usually ParSpreadMarketQuoteCalculator
   * (recommended) or converted present value).
   * @param sensitivityCalculator The parameter sensitivity calculator.
   * @return A pair with the calibrated yield curve bundle (including the known data) and the CurveBuildingBlockBundle with
   * the relevant inverse Jacobian Matrix.
   */
  public Pair<MulticurveProviderForward, CurveBuildingBlockBundle> makeCurvesFromDerivatives(final MultiCurveBundle<GeneratorYDCurve>[] curveBundles,
      final MulticurveProviderForward knownData, final CurveBuildingBlockBundle knownBlockBundle,
      final LinkedHashMap<String, Currency> discountingMap, final LinkedHashMap<String, IborIndex> forwardIborMap,
      final LinkedHashMap<String, IndexON> forwardONMap,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator) {
    ArgumentChecker.notNull(curveBundles, "curveBundles");
    ArgumentChecker.notNull(knownData, "knownData");
    ArgumentChecker.notNull(discountingMap, "discountingMap");
    ArgumentChecker.notNull(forwardIborMap, "forwardIborMap");
    ArgumentChecker.notNull(forwardONMap, "forwardONMap");
    ArgumentChecker.notNull(calculator, "calculator");
    ArgumentChecker.notNull(sensitivityCalculator, "sensitivityCalculator");
    final int nUnits = curveBundles.length; //curveGenerators.length;
    final MulticurveProviderForward knownSoFarData = knownData.copy();
    final List<InstrumentDerivative> instrumentsSoFar = new ArrayList<>();
    final LinkedHashMap<String, GeneratorYDCurve> generatorsSoFar = new LinkedHashMap<>();
    final LinkedHashMap<String, Pair<CurveBuildingBlock, DoubleMatrix2D>> unitBundleSoFar = new LinkedHashMap<>();
    final List<Double> parametersSoFar = new ArrayList<>();
    final LinkedHashMap<String, Pair<Integer, Integer>> unitMap = new LinkedHashMap<>();
    int startUnit = 0;
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = curveBundles[i].size();
      final int[] startCurve = new int[nCurves]; // First parameter index of the curve in the unit.
      final LinkedHashMap<String, GeneratorYDCurve> gen = new LinkedHashMap<>();
      final int[] nInstruments = new int[nCurves];
      int totalInstrumentsInUnit = 0; // Number of instruments in the unit.
      for (int j = 0; j < nCurves; j++) {
        startCurve[j] = totalInstrumentsInUnit;
        nInstruments[j] = curveBundles[i].getCurveBundle(j).size(); //instruments[i][j].length;
        totalInstrumentsInUnit += nInstruments[j];
        instrumentsSoFar.addAll(Arrays.asList(curveBundles[i].getCurveBundle(j).getDerivatives()));
      }
      final InstrumentDerivative[] instrumentsUnit = new InstrumentDerivative[totalInstrumentsInUnit];
      final InstrumentDerivative[] instrumentsSoFarArray = instrumentsSoFar.toArray(new InstrumentDerivative[instrumentsSoFar.size()]);
      final double[] parametersGuess = new double[totalInstrumentsInUnit];
      for (int j = 0; j < nCurves; j++) {
        final InstrumentDerivative[] instruments = curveBundles[i].getCurveBundle(j).getDerivatives();
        System.arraycopy(instruments, 0, instrumentsUnit, startCurve[j], nInstruments[j]);
        final GeneratorYDCurve tmp = curveBundles[i].getCurveBundle(j).getCurveGenerator().finalGenerator(instruments);
        gen.put(curveBundles[i].getCurveBundle(j).getCurveName(), tmp);
        generatorsSoFar.put(curveBundles[i].getCurveBundle(j).getCurveName(), tmp);
        unitMap.put(curveBundles[i].getCurveBundle(j).getCurveName(), Pairs.of(startUnit + startCurve[j], nInstruments[j]));
        System.arraycopy(curveBundles[i].getCurveBundle(j).getStartingPoint(), startCurve[j], parametersGuess, startCurve[j], nInstruments[j]);
      }
      final Pair<MulticurveProviderForward, Double[]> unitCal = makeUnit(instrumentsUnit, parametersGuess, knownSoFarData,
          discountingMap, forwardIborMap, forwardONMap, gen, calculator, sensitivityCalculator);
      parametersSoFar.addAll(Arrays.asList(unitCal.getSecond()));
      final DoubleMatrix2D[] mat = makeCurveMatrix(instrumentsSoFarArray, startUnit, nInstruments, parametersSoFar.toArray(new Double[parametersSoFar.size()]),
          knownData, discountingMap, forwardIborMap, forwardONMap, generatorsSoFar, sensitivityCalculator);
      for (int j = 0; j < nCurves; j++) {
        unitBundleSoFar.put(curveBundles[i].getCurveBundle(j).getCurveName(), Pairs.of(new CurveBuildingBlock(unitMap), mat[j]));
      }
      knownSoFarData.setAll(unitCal.getFirst());
      startUnit = startUnit + totalInstrumentsInUnit;
    }
    return Pairs.of(knownSoFarData, new CurveBuildingBlockBundle(unitBundleSoFar));
  }
}
