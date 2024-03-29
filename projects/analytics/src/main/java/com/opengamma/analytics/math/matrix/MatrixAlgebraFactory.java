/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.matrix;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Factory class for various types of matrix algebra calculators.
 */
public final class MatrixAlgebraFactory {
  /** Label for Colt matrix algebra. */
  public static final String COLT = "Colt";
  /** Label for Commons matrix algebra. */
  public static final String COMMONS = "Commons";
  /** Label for OpenGamma matrix algebra. */
  public static final String OG = "OG";
  /** {@link ColtMatrixAlgebra}. */
  public static final ColtMatrixAlgebra COLT_ALGEBRA = new ColtMatrixAlgebra();
  /** {@link CommonsMatrixAlgebra}. */
  public static final CommonsMatrixAlgebra COMMONS_ALGEBRA = new CommonsMatrixAlgebra();
  /** {@link OGMatrixAlgebra}. */
  public static final OGMatrixAlgebra OG_ALGEBRA = new OGMatrixAlgebra();
  private static final Map<String, MatrixAlgebra> INSTANCES;
  private static final Map<Class<?>, String> INSTANCE_NAMES;

  static {
    INSTANCES = new HashMap<>();
    INSTANCE_NAMES = new HashMap<>();
    INSTANCES.put(COLT, COLT_ALGEBRA);
    INSTANCE_NAMES.put(ColtMatrixAlgebra.class, COLT);
    INSTANCES.put(COMMONS, COMMONS_ALGEBRA);
    INSTANCE_NAMES.put(CommonsMatrixAlgebra.class, COMMONS);
    INSTANCES.put(OG, OG_ALGEBRA);
    INSTANCE_NAMES.put(OGMatrixAlgebra.class, OG);
  }

  private MatrixAlgebraFactory() {
  }

  /**
   * Given a name, returns an instance of the matrix algebra calculator.
   * 
   * @param algebraName
   *          The name of the matrix algebra calculator
   * @return The matrix algebra calculator
   * @throws IllegalArgumentException
   *           If the calculator name is null or there is no calculator for that name
   */
  public static MatrixAlgebra getMatrixAlgebra(final String algebraName) {
    if (INSTANCES.containsKey(algebraName)) {
      return INSTANCES.get(algebraName);
    }
    throw new IllegalArgumentException("Matrix algebra " + algebraName + " not found");
  }

  /**
   * Given a matrix algebra calculator, returns its name.
   * 
   * @param algebra
   *          The algebra
   * @return The name of that calculator (null if not found)
   */
  public static String getMatrixAlgebraName(final MatrixAlgebra algebra) {
    if (algebra == null) {
      return null;
    }
    return INSTANCE_NAMES.get(algebra.getClass());
  }
}
