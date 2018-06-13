/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

import it.unimi.dsi.fastutil.ints.IntArrayList;

/**
 * For an instrument, computes the sensitivity of a value (often the present value or a par spread) to the parameters used in the curve.
 * The meaning of "parameters" will depend of the way the curve is stored (interpolated yield, function parameters, etc.).
 * The return format is ParameterSensitivity object.
 */
public class ParameterSensitivityMulticurveUnderlyingMatrixCalculator extends ParameterSensitivityMulticurveMatrixAbstractCalculator {

  /**
   * Constructor
   * @param curveSensitivityCalculator The curve sensitivity calculator.
   */
  public ParameterSensitivityMulticurveUnderlyingMatrixCalculator(final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> curveSensitivityCalculator) {
    super(curveSensitivityCalculator);
  }

  /**
   * Computes the sensitivity with respect to the parameters from the point sensitivities to the continuously compounded rate.
   * @param sensitivity The point sensitivity.
   * @param multicurves The multi-curve provider. Not null.
   * @param curveNames The set of curves for which the sensitivity will be computed. Not null.
   * @return The sensitivity (as a ParameterSensitivity). The order of the sensitivity is by curve as provided by the sensicurveNamesSet.
   */
  @Override
  public DoubleMatrix1D pointToParameterSensitivity(final MulticurveSensitivity sensitivity, final MulticurveProviderInterface multicurves, final Set<String> curveNames) {
    // TODO: The first part depends only of the multicurves and curvesSet, not the sensitivity. Should it be refactored and done only once?
    final Set<String> allCurveNames = multicurves.getAllNames();
    // Implementation note: Check sensicurve are in multicurve
    ArgumentChecker.isTrue(allCurveNames.containsAll(curveNames), "curve in the names set not in the multi-curve provider");
    final int nCurves = allCurveNames.size();
    final int[] curveParametersCount = new int[nCurves];
    // Populate the curve names and numbers for the curves in the multicurve
    int ii = 0;
    // store the index of a curve in the list of names
    // store the total number of curve parameters
    final boolean hasSpreadCurves = false;
    final Map<String, Pair<List<String>, Integer>> nameReference = new HashMap<>();
    for (final String name : allCurveNames) {
      if (multicurves.getUnderlyingCurvesNames(name).size() > 0) {
        //hasSpreadCurves = true;
      }
      nameReference.put(name, Pairs.of(multicurves.getUnderlyingCurvesNames(name), ii));
      curveParametersCount[ii] = multicurves.getNumberOfParameters(name);
      ii++;
    }
    if (!hasSpreadCurves) {
      ii = 0;
      final double[][] sensitivities = new double[nCurves][];
      final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getYieldDiscountingSensitivities();
      final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getForwardSensitivities();
      for (final String curveNumber : allCurveNames) {
        final int num = nameReference.get(curveNumber).getSecond();
        sensitivities[num] = new double[curveParametersCount[num]];
        final double[] discountingSensitivities = multicurves.parameterSensitivity(curveNumber, sensitivityDsc.get(curveNumber));
        final double[] forwardSensitivities = multicurves.parameterForwardSensitivity(curveNumber, sensitivityFwd.get(curveNumber));
        for (int i = 0; i < curveParametersCount[num]; i++) {
          sensitivities[num][i] = discountingSensitivities[i] + forwardSensitivities[i];
        }
      }
      double[] result = new double[0];
      for (final String name : curveNames) {
        // removes parameters for curves that aren't being fitted
        final int num = nameReference.get(name).getSecond();
        result = ArrayUtils.addAll(result, sensitivities[num]);
      }
      return new DoubleMatrix1D(result);
    }
    ii = 0;
    // store the total number of unique parameters (i.e. subtract the number of parameters in any underlying curves)
    // store the indices of any underlying curves
    // store the start index for each curve
    // store the start index of each underlying curve
    final int[] uniqueCurveParametersCount = new int[nCurves];
    final int[][] underlyingCurveReferences = new int[nCurves][];
    final int[] curveStartIndices = new int[nCurves];
    final int[][] underlyingCurveStartIndices = new int[nCurves][];
    for (final String name : allCurveNames) {
      int curveStartIndex = 0;
      final int reference = nameReference.get(name).getSecond();
      final List<String> underlyingCurveNames = nameReference.get(name).getFirst();
      final int nUnderlying = underlyingCurveNames.size();
      final int[] underlyingCurveReference = new int[nUnderlying];
      final int[] underlyingStartIndex = new int[nUnderlying];
      int uniqueParameterCount = curveParametersCount[ii];
      int j = 0;
      for (final String underlyingCurveName : underlyingCurveNames) {
        final Integer underlyingReference = nameReference.get(underlyingCurveName).getSecond();
        if (underlyingReference != null) {
          curveStartIndex += curveParametersCount[underlyingReference];
          uniqueParameterCount -= curveParametersCount[underlyingReference];
          underlyingCurveReference[j] = underlyingReference;
          underlyingStartIndex[j] = curveStartIndex;
          j++;
        }
      }
      uniqueCurveParametersCount[ii] = uniqueParameterCount;
      underlyingCurveReferences[ii] = underlyingCurveReference;
      final IntArrayList startUnderlyingParamList = new IntArrayList();
      for (final String u : underlyingCurveNames) {
        final Integer underlyingReference = nameReference.get(u).getSecond();
        if (underlyingReference != null) {
          startUnderlyingParamList.add(curveStartIndex);
          curveStartIndex += uniqueCurveParametersCount[underlyingReference]; // Implementation note: Rely on underlying curves being first and then the new parameters
        }
      }
      curveStartIndices[reference] = curveStartIndex;
      underlyingCurveStartIndices[reference] = startUnderlyingParamList.toIntArray();
      ii++;
    }
    // Implementation note: Compute the "dirty" sensitivity, i.e. the sensitivity to all the parameters in each curve. The underlying are taken into account in the "clean" step.
    final double[][] dirtySensitivities = new double[nCurves][];
    final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getYieldDiscountingSensitivities();
    final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getForwardSensitivities();
    for (final String name : allCurveNames) { // loop over all curves (by name)
      final int num = nameReference.get(name).getSecond();
      dirtySensitivities[num] = new double[curveParametersCount[num]];
      final double[] sDsc1Name = multicurves.parameterSensitivity(name, sensitivityDsc.get(name));
      final double[] sFwd1Name = multicurves.parameterForwardSensitivity(name, sensitivityFwd.get(name));
      for (int i = 0; i < curveParametersCount[num]; i++) {
        dirtySensitivities[num][i] = sDsc1Name[i] + sFwd1Name[i];
      }
    }
    // Implementation note: "clean" the sensitivity, i.e. add the parts on the same curves together.
    final double[][] cleanSensitivities = new double[nCurves][];
    for (int i = 0; i < nCurves; i++) {
      cleanSensitivities[i] = new double[curveParametersCount[i]];
    }
    for (final String name : allCurveNames) { // loop over all curves (by name)
      final int num = nameReference.get(name).getSecond();
      // Direct sensitivity
      for (int i = 0; i < curveParametersCount[num]; i++) {
        cleanSensitivities[num][i] += dirtySensitivities[num][curveStartIndices[num] + i];
      }
      // Underlying (indirect) sensitivity
      for (int i = 0; i < underlyingCurveStartIndices[num].length; i++) {
        for (int j = 0; j < curveParametersCount[underlyingCurveReferences[num][i]]; j++) {
          cleanSensitivities[underlyingCurveReferences[num][i]][j] += dirtySensitivities[num][underlyingCurveStartIndices[num][i] + j];
        }
      }
    }
    double[] result = new double[0];
    for (final String name : curveNames) {
      final int num = nameReference.get(name).getSecond();
      result = ArrayUtils.addAll(result, cleanSensitivities[num]);
    }
    return new DoubleMatrix1D(result);
  }

}
