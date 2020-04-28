/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.analytics.financial.curve.interestrate.curvebuilder;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterProviderInterface;
import com.opengamma.util.ArgumentChecker;

/**
 * A builder interface that describes the steps to build a set of curves using root-finding i.e.
 * <ul>
 * <li>define the order of curve construction with {@link #building}, {@link #buildingFirst} or {@link #thenBuilding}</li>
 * <li>describe what each curve will be used for with {@link #using(String)} or {@link #using(YieldAndDiscountCurve)} e.g. use the curve "SONIA" to discount GBP
 * payments</li>
 * <li>add nodes to each curve</li>
 * </ul>
 *
 * There are optional methods that are used for more specialised curve construction or to define the parameters of the root-finding.
 * <li>add any FX rates needed for cross-currency curve construction</li>
 * <li>add any data from pre-constructed curves</li>
 * <li>set absolute and relative tolerances or the number of steps used for root-finding</li>
 * </ul>
 * Implementing classes should probably use covariant return types. See implementing classes' documentation for example configurations.
 */
public interface CurveSetUpInterface {

  /**
   * Tells the {@link CurveBuilder} that all curves in the list should be built simultaneously. This contrasts with the {@link #buildingFirst} and
   * {@link #thenBuilding}, that define an order for construction.
   *
   * @param curveNames
   *          the names of the curves that are to be built, not null
   * @return this builder
   */
  public CurveSetUpInterface building(String... curveNames);

  /**
   * Tells the {@link CurveBuilder} that these curves are to be built first.
   *
   * @param curveNames
   *          the names of the curves that are to be built, not null
   * @return this builder
   */
  public CurveSetUpInterface buildingFirst(String... curveNames);

  /**
   * Tells the {@link CurveBuilder} that these curves are to be built after any built in {@link #buildingFirst}. Generally, an exception should be thrown if
   * {@link #buildingFirst} is not called.
   *
   * @param curveNames
   *          the names of the curves that are to be built, not null
   * @return this builder
   */
  public CurveSetUpInterface thenBuilding(String... curveNames);

  /**
   * Produces a {@link CurveTypeSetUpInterface} that tells the {@link CurveBuilder} what each curve should be used for e.g. discounting payments in a particular
   * currency, calculating forward overnight rates, etc.
   *
   * This method should be used to define what each curve added to the builder with the {@link #building}, {@link #buildingFirst} and {@link #thenBuilding}
   *
   * @param curveName
   *          the name of the curve, not null
   * @return a {@link CurveTypeSetUpInterface}
   */
  public CurveTypeSetUpInterface using(String curveName);

  /**
   * Produces a {@link PreConstructedCurveTypeSetUp} that describes how the provided curve should be used.
   *
   * @param curve
   *          the curve, not null
   * @return a {@link PreConstructedCurveTypeSetUp}
   */
  public PreConstructedCurveTypeSetUp using(YieldAndDiscountCurve curve);

  /**
   * Adds a node to the curve.
   *
   * @param curveName
   *          the name of the curve, not null
   * @param definition
   *          the instrument to be used at this node, not null
   * @return this builder
   */
  public CurveSetUpInterface addNode(String curveName, InstrumentDefinition<?> definition);

  /**
   * Removes all nodes for a particular curve. This method might not remove the curve name from the builder.
   *
   * @param curveName
   *          the name of the curve, not null
   * @return this builder
   */
  public CurveSetUpInterface removeNodes(String curveName);

  /**
   * Removes all nodes for a particular curve. This method should remove the curve name from the builder.
   *
   * @param curveName
   *          the name of the curve, not null
   * @return this builder
   */
  public CurveSetUpInterface removeCurve(String curveName);

  /**
   * Gets a curve builder that will construct the curves as defined.
   *
   * @return a curve builder
   */
  public CurveBuilder<? extends ParameterProviderInterface> getBuilder();

  /**
   * Produces a copy of this builder.
   *
   * @return a copy of this builder
   */
  public CurveSetUpInterface copy();

  /**
   * Provides FX information to the curve builder. This is needed for cross-currency curve construction.
   *
   * @param fxMatrix
   *          the FX matrix, not null
   * @return this builder
   */
  public CurveSetUpInterface addFxMatrix(FXMatrix fxMatrix);

  /**
   * Provides known data (curves and the Jacobians found from their construction) to the builder.
   *
   * @param bundle
   *          the known data, not null
   * @return this builder
   */
  public CurveSetUpInterface withKnownBundle(CurveBuildingBlockBundle bundle);

  /**
   * Sets the absolute tolerance for the root-finder.
   *
   * @param tolerance
   *          the tolerance, not negative or zero
   * @return this builder
   */
  CurveSetUpInterface rootFindingAbsoluteTolerance(double tolerance);

  /**
   * Sets the relative tolerance for the root-finder.
   *
   * @param tolerance
   *          the tolerance, not negative or zero
   * @return this builder
   */
  CurveSetUpInterface rootFindingRelativeTolerance(double tolerance);

  /**
   * Sets the maximum number of steps for the root-finder.
   *
   * @param maxSteps
   *          the maximum number of steps, not negative or zero
   * @return this builder
   */
  CurveSetUpInterface rootFindingMaximumSteps(int maxSteps);

  /**
   * Sets the name of the root-finding method to use.
   *
   * @param methodName
   *          the method name, not null
   * @return this builder
   */
  CurveSetUpInterface rootFindingMethodName(String methodName);

  /**
   * Stores information about the root-finder that will be used for curve construction.
   */
  class RootFinderSetUp {
    private double _absoluteTolerance = 1e-12;
    private double _relativeTolerance = 1e-12;
    private int _maxSteps = 100;
    private String _rootFinderName = "Broyden";

    void setAbsoluteTolerance(final double tolerance) {
      ArgumentChecker.isTrue(tolerance > 0, "Absolute tolerance must be greater than zero");
      _absoluteTolerance = tolerance;
    }

    double getAbsoluteTolerance() {
      return _absoluteTolerance;
    }

    void setRelativeTolerance(final double tolerance) {
      ArgumentChecker.isTrue(tolerance > 0, "Relative tolerance must be greater than zero");
      _relativeTolerance = tolerance;
    }

    double getRelativeTolerance() {
      return _relativeTolerance;
    }

    void setMaxSteps(final int max) {
      ArgumentChecker.isTrue(max > 0, "Maximum number of steps must be greater than zero");
      _maxSteps = max;
    }

    int getMaxSteps() {
      return _maxSteps;
    }

    void setRootFinderName(final String name) {
      _rootFinderName = ArgumentChecker.notNull(name, "name");
    }

    String getRootFinderName() {
      return _rootFinderName;
    }
  }
}
