/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.google.common.primitives.Ints;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve. The meaning of
 * "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.). The return format is ParameterSensitivity object.
 */
public class ParameterSensitivityMulticurveUnderlyingMatrixCalculator extends ParameterSensitivityMulticurveMatrixAbstractCalculator {

  /**
   * Constructor.
   *
   * @param curveSensitivityCalculator
   *          The curve sensitivity calculator.
   */
  public ParameterSensitivityMulticurveUnderlyingMatrixCalculator(
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   *
   * @param sensitivity
   *          The point sensitivity.
   * @param data
   *          The multi-curve provider. Not null.
   * @param curveNames
   *          The set of curves for which the sensitivity will be computed. Not null.
   * @return The sensitivity (as a ParameterSensitivity). The order of the sensitivity is by curve as provided by the sensicurveNamesSet.
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final MulticurveSensitivity sensitivity, final MulticurveProviderInterface data,
      final Set<String> curveNames) {
    final Set<String> allCurveNames = data.getAllNames();
    ArgumentChecker.isTrue(allCurveNames.containsAll(curveNames), "curve in the names set not in the multi-curve provider");
    final int nCurves = allCurveNames.size();
    int i = 0;
    final Map<String, Integer> curveReferenceIndices = new HashMap<>();
    final Map<String, List<String>> underlyingCurveNames = new HashMap<>();
    final int[] nParameters = new int[nCurves];
    final int[] nParametersWithoutUnderlyingCurve = new int[nCurves];
    boolean hasUnderlyingCurves = false;
    for (final String name : allCurveNames) {
      curveReferenceIndices.put(name, i);
      nParameters[i] = data.getNumberOfParameters(name);
      final List<String> underlyingNames = data.getUnderlyingCurvesNames(name);
      underlyingCurveNames.put(name, underlyingNames);
      if (underlyingNames != null && !underlyingNames.isEmpty()) {
        hasUnderlyingCurves = true;
      }
      nParametersWithoutUnderlyingCurve[i] = nParameters[i];
      i++;
    }
    final double[][] sensitivities = getSensitivities(sensitivity, data, allCurveNames, nCurves, curveReferenceIndices, nParameters);
    if (!hasUnderlyingCurves) {
      return convertToMatrix(curveNames, curveReferenceIndices, sensitivities);
    }
    final int[][] underlyingCurveReferenceIndices = new int[nCurves][];
    final int[] startIndices = new int[nCurves];
    final int[][] startUnderlyingIndices = new int[nCurves][];
    i = 0;
    for (final String name : allCurveNames) {
      final List<String> underlyingNames = underlyingCurveNames.get(name);
      final List<Integer> indexOtherMulticurveList = new ArrayList<>();
      for (final String u : underlyingNames) {
        final Integer index = curveReferenceIndices.get(u);
        if (index != null) {
          // TODO relies on ordering (underlying curves first) and doesn't allow an underlying curve with underlyings to be used e.g. spread on spread
          nParametersWithoutUnderlyingCurve[i] -= nParametersWithoutUnderlyingCurve[index];
          indexOtherMulticurveList.add(index);
        }
      }
      underlyingCurveReferenceIndices[i++] = Ints.toArray(indexOtherMulticurveList);
    }
    for (final String name : allCurveNames) { // loop over all curves (by name)
      int startIndex = 0;
      final int num = curveReferenceIndices.get(name);
      final List<Integer> startUnderlyingParamList = new ArrayList<>();
      final List<String> underlyingNames = underlyingCurveNames.get(name);
      for (final String u : underlyingNames) {
        final Integer index = curveReferenceIndices.get(u);
        if (index != null) {
          startUnderlyingParamList.add(startIndex);
          startIndex += nParametersWithoutUnderlyingCurve[index];
        }
      }
      startIndices[num] = startIndex;
      startUnderlyingIndices[num] = Ints.toArray(startUnderlyingParamList);
    }
    final double[][] combinedSensitivities = combineWithUnderlyingSensitivities(sensitivities, allCurveNames, nCurves, curveReferenceIndices,
        nParametersWithoutUnderlyingCurve, underlyingCurveReferenceIndices, startIndices, startUnderlyingIndices);
    return convertToMatrix(curveNames, curveReferenceIndices, combinedSensitivities);
  }

  private static DoubleMatrix1D convertToMatrix(final Set<String> curvesToFit, final Map<String, Integer> referenceIndices, final double[][] sensitivities) {
    double[] result = new double[0];
    for (final String name : curvesToFit) {
      final int num = referenceIndices.get(name);
      result = ArrayUtils.addAll(result, sensitivities[num]);
    }
    return new DoubleMatrix1D(result);
  }

  /**
   * Calculates the sensitivities to all parameters in each curve and adds the discounting and forward sensitivities.
   */
  private static double[][] getSensitivities(final MulticurveSensitivity sensitivity, final MulticurveProviderInterface data, final Set<String> curveNames,
      final int nCurves, final Map<String, Integer> curveReferenceIndices, final int[] numberOfParameters) {
    final double[][] result = new double[nCurves][];
    final Map<String, List<DoublesPair>> discountingSensitivities = sensitivity.getYieldDiscountingSensitivities();
    final Map<String, List<ForwardSensitivity>> forwardSensitivities = sensitivity.getForwardSensitivities();
    for (final String name : curveNames) {
      final int referenceIndex = curveReferenceIndices.get(name);
      final int nParams = numberOfParameters[referenceIndex];
      result[referenceIndex] = new double[nParams];
      final double[] discounting = data.parameterSensitivity(name, discountingSensitivities.get(name));
      final double[] forward = data.parameterForwardSensitivity(name, forwardSensitivities.get(name));
      for (int i = 0; i < nParams; i++) {
        result[referenceIndex][i] = discounting[i] + forward[i];
      }
    }
    return result;
  }

  /**
   * Add sensitivities on the same curves together. This takes into account sensitivities on underlying curves.
   */
  private static double[][] combineWithUnderlyingSensitivities(final double[][] sensitivities, final Set<String> curveNames,
      final int nCurves, final Map<String, Integer> curveReferenceIndices, final int[] nNewParameters, final int[][] underlyingCurveReferenceIndices,
      final int[] curveStartIndices, final int[][] underlyingCurveStartIndices) {
    final double[][] result = new double[nCurves][];
    // initialise arrays first because the reference index can be different to the start index
    for (int i = 0; i < nCurves; i++) {
      result[i] = new double[nNewParameters[i]];
    }
    for (final String name : curveNames) { // loop over all curves (by name)
      final int referenceIndex = curveReferenceIndices.get(name);
      final int newParametersForCurve = nNewParameters[referenceIndex];
      // Direct sensitivity
      for (int i = 0; i < newParametersForCurve; i++) {
        final int startIndex = curveStartIndices[referenceIndex];
        result[referenceIndex][i] += sensitivities[referenceIndex][startIndex + i];
      }
      // Underlying (indirect) sensitivity
      final int[] underlyingStartIndices = underlyingCurveStartIndices[referenceIndex];
      for (int i = 0; i < underlyingStartIndices.length; i++) {
        final int underlyingReferenceIndex = underlyingCurveReferenceIndices[referenceIndex][i];
        for (int j = 0; j < nNewParameters[underlyingReferenceIndex]; j++) {
          result[underlyingReferenceIndex][j] += sensitivities[referenceIndex][underlyingStartIndices[i] + j];
        }
      }
    }
    return result;
  }
}
