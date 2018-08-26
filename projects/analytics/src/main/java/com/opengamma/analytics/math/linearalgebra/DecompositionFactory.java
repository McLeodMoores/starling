/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.math.linearalgebra;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for different types of decompositions.
 */
public final class DecompositionFactory {
  /** Commons LU decomposition */
  public static final String LU_COMMONS_NAME = "LU_COMMONS";
  /** Commons QR decomposition */
  public static final String QR_COMMONS_NAME = "QR_COMMONS";
  /** Colt SV decomposition */
  public static final String SV_COLT_NAME = "SV_COLT";
  /** Commons SV decomposition */
  public static final String SV_COMMONS_NAME = "SV_COMMONS";
  /** {@link LUDecompositionCommons} */
  public static final Decomposition<?> LU_COMMONS = new LUDecompositionCommons();
  /** {@link QRDecompositionCommons} */
  public static final Decomposition<?> QR_COMMONS = new QRDecompositionCommons();
  /** {@link SVDecompositionColt} */
  public static final Decomposition<?> SV_COLT = new SVDecompositionColt();
  /** {@link SVDecompositionCommons} */
  public static final Decomposition<?> SV_COMMONS = new SVDecompositionCommons();
  private static final Map<String, Decomposition<?>> INSTANCES;
  private static final Map<Class<?>, String> INSTANCE_NAMES;

  static {
    INSTANCES = new HashMap<>();
    INSTANCES.put(LU_COMMONS_NAME, LU_COMMONS);
    INSTANCES.put(QR_COMMONS_NAME, QR_COMMONS);
    INSTANCES.put(SV_COLT_NAME, SV_COLT);
    INSTANCES.put(SV_COMMONS_NAME, SV_COMMONS);
    INSTANCE_NAMES = new HashMap<>();
    INSTANCE_NAMES.put(LU_COMMONS.getClass(), LU_COMMONS_NAME);
    INSTANCE_NAMES.put(QR_COMMONS.getClass(), QR_COMMONS_NAME);
    INSTANCE_NAMES.put(SV_COLT.getClass(), SV_COLT_NAME);
    INSTANCE_NAMES.put(SV_COMMONS.getClass(), SV_COMMONS_NAME);
  }

  private DecompositionFactory() {
  }

  /**
   * Given a name, returns an instance of that decomposition method
   * @param decompositionName The name of the decomposition method
   * @return The decomposition method
   * @throws IllegalArgumentException If the decomposition name is null or there is no decomposition method of that name
   */
  public static Decomposition<?> getDecomposition(final String decompositionName) {
    final Decomposition<?> decomposition = INSTANCES.get(decompositionName);
    if (decomposition != null) {
      return decomposition;
    }
    throw new IllegalArgumentException("Could not get decomposition " + decompositionName);
  }

  /**
   * Given a decomposition method, returns its name
   * @param decomposition The decomposition method
   * @return The name of the decomposition method (null if not found)
   */
  public static String getDecompositionName(final Decomposition<?> decomposition) {
    if (decomposition == null) {
      return null;
    }
    return INSTANCE_NAMES.get(decomposition.getClass());
  }
}
