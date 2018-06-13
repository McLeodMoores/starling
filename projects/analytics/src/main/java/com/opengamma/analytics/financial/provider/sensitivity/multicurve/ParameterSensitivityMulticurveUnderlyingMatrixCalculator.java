/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.sensitivity.multicurve;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.DoublesPair;

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
  public DoubleMatrix1D pointToParameterSensitivity(final MulticurveSensitivity sensitivity, final MulticurveProviderInterface multicurves, final Set<String> sensicurveNamesSet) {
    // TODO: The first part depends only of the multicurves and curvesSet, not the sensitivity. Should it be refactored and done only once?
    final Set<String> multicurveNamesSet = multicurves.getAllNames();
    // Implementation note: Check sensicurve are in multicurve
    ArgumentChecker.isTrue(multicurveNamesSet.containsAll(sensicurveNamesSet), "curve in the names set not in the multi-curve provider");
    final int nbMultiCurve = multicurveNamesSet.size();
    // Populate the name names and numbers for the curves in the multicurve
    int loopname = 0;
    final LinkedHashMap<String, Integer> multicurveNum = new LinkedHashMap<>();
    for (final String name : multicurveNamesSet) { // loop over all curves in multicurves (by name)
      multicurveNum.put(name, loopname++);
    }
    final int[] nbNewParameters = new int[nbMultiCurve];
    final int[] nbParameters = new int[nbMultiCurve];
    // Implementation note: nbNewParameters - number of new parameters in the curve, parameters not from an underlying curve which is another curve of the bundle.
    loopname = 0;
    for (final String name : multicurveNamesSet) { // loop over all curves in multicurves (by name)
      nbParameters[loopname] = multicurves.getNumberOfParameters(name);
      nbNewParameters[loopname] = nbParameters[loopname];
      loopname++;
    }
    loopname = 0;
    for (final String name : multicurveNamesSet) { // loop over all curves in multicurves (by name)
      final List<String> underlyingCurveNames = multicurves.getUnderlyingCurvesNames(name);
      for (final String u : underlyingCurveNames) {
        final Integer i = multicurveNum.get(u);
        if (i != null) {
          nbNewParameters[loopname] -= nbNewParameters[i]; // Only one level: a curve used as an underlying can not have an underlying itself.
        }
      }
      loopname++;
    }
    // Implementation note: nbNewParamSensiCurve
    final int[][] indexOtherMulticurve = new int[nbMultiCurve][];
    // Implementation note: indexOtherMultiCurve - for each curve in the multi-curve, the index of the underlying curves in the same set
    final int[] startOwnParameter = new int[nbMultiCurve];
    // Implementation note: The start index of the parameters of the own (new) parameters.
    final int[][] startUnderlyingParameter = new int[nbMultiCurve][];
    // Implementation note: The start index of the parameters of the underlying curves
    loopname = 0;
    for (final String name : multicurveNamesSet) { // loop over all curves in multi-curve (by name)
      final List<String> underlyingCurveNames = multicurves.getUnderlyingCurvesNames(name);
      final IntArrayList indexOtherMulticurveList = new IntArrayList();
      for (final String u : underlyingCurveNames) {
        final Integer i = multicurveNum.get(u);
        if (i != null) {
          indexOtherMulticurveList.add(i);
        }
      }
      indexOtherMulticurve[loopname] = indexOtherMulticurveList.toIntArray();
      loopname++;
    }
    for (final String name : multicurveNamesSet) { // loop over all curves (by name)
      int loopstart = 0;
      final int num = multicurveNum.get(name);
      final IntArrayList startUnderlyingParamList = new IntArrayList();
      final List<String> underlyingCurveNames = multicurves.getUnderlyingCurvesNames(name);
      for (final String u : underlyingCurveNames) {
        final Integer i = multicurveNum.get(u);
        if (i != null) {
          startUnderlyingParamList.add(loopstart);
          loopstart += nbNewParameters[i]; // Implementation note: Rely on underlying curves being first and then the new parameters
        }
      }
      startOwnParameter[num] = loopstart;
      startUnderlyingParameter[num] = startUnderlyingParamList.toIntArray();
    }
    // Implementation note: Compute the "dirty" sensitivity, i.e. the sensitivity to all the parameters in each curve. The underlying are taken into account in the "clean" step.
    final double[][] sensiDirty = new double[nbMultiCurve][];
    final Map<String, List<DoublesPair>> sensitivityDsc = sensitivity.getYieldDiscountingSensitivities();
    final Map<String, List<ForwardSensitivity>> sensitivityFwd = sensitivity.getForwardSensitivities();
    for (final String name : multicurveNamesSet) { // loop over all curves (by name)
      final int num = multicurveNum.get(name);
      sensiDirty[num] = new double[nbParameters[num]];
      final double[] sDsc1Name = multicurves.parameterSensitivity(name, sensitivityDsc.get(name));
      final double[] sFwd1Name = multicurves.parameterForwardSensitivity(name, sensitivityFwd.get(name));
      for (int loopp = 0; loopp < nbParameters[num]; loopp++) {
        sensiDirty[num][loopp] = sDsc1Name[loopp] + sFwd1Name[loopp];
      }
    }
    // Implementation note: "clean" the sensitivity, i.e. add the parts on the same curves together.
    final double[][] sensiClean = new double[nbMultiCurve][];
    for (int loopc = 0; loopc < nbMultiCurve; loopc++) {
      sensiClean[loopc] = new double[nbNewParameters[loopc]];
    }
    for (final String name : multicurveNamesSet) { // loop over all curves (by name)
      final int num = multicurveNum.get(name);
      // Direct sensitivity
      for (int loopi = 0; loopi < nbNewParameters[num]; loopi++) {
        sensiClean[num][loopi] += sensiDirty[num][startOwnParameter[num] + loopi];
      }
      // Underlying (indirect) sensitivity
      for (int loopu = 0; loopu < startUnderlyingParameter[num].length; loopu++) {
        for (int loopi = 0; loopi < nbNewParameters[indexOtherMulticurve[num][loopu]]; loopi++) {
          sensiClean[indexOtherMulticurve[num][loopu]][loopi] += sensiDirty[num][startUnderlyingParameter[num][loopu] + loopi];
        }
      }
    }
    double[] result = new double[0];
    for (final String name : sensicurveNamesSet) {
      final int num = multicurveNum.get(name);
      result = ArrayUtils.addAll(result, sensiClean[num]);
    }
    return new DoubleMatrix1D(result);
  }

  /*@Override
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
    boolean hasUnderlyingCurves = false;
    final Map<String, Pair<List<String>, Integer>> nameReference = new HashMap<>();
    for (final String name : allCurveNames) {
      if (multicurves.getUnderlyingCurvesNames(name).size() > 0) {
        hasUnderlyingCurves = true;
      }
      nameReference.put(name, Pairs.of(multicurves.getUnderlyingCurvesNames(name), ii));
      curveParametersCount[ii] = multicurves.getNumberOfParameters(name);
      ii++;
    }
    if (!hasUnderlyingCurves) {
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
        System.out.println(underlyingCurveName + "\t\t\t\t" + nameReference + "\t\t\t\t" + allCurveNames);
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
*/
}
