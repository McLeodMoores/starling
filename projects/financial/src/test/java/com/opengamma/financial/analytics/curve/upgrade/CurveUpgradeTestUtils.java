/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.SyntheticFutureCurveInstrumentProvider;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public final class CurveUpgradeTestUtils {

  /**
   * Restricted constructor.
   */
  private CurveUpgradeTestUtils() {
  }

  /**
   * A map from tenor to curve instrument provider for USD deposit rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_DEPOSIT_INSTRUMENTS = new HashMap<>();
  static {
    USD_DEPOSIT_INSTRUMENTS.put(Tenor.ONE_DAY, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("DEPOSIT_USD_1D")));
    USD_DEPOSIT_INSTRUMENTS.put(Tenor.TWO_DAYS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("DEPOSIT_USD_2D")));
  }
  /**
   * A map from tenor to curve instrument provider for USD Libor rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_LIBOR_INSTRUMENTS = new HashMap<>();
  static {
    USD_LIBOR_INSTRUMENTS.put(Tenor.ONE_DAY, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("LIBOR_USD_1D")));
    USD_LIBOR_INSTRUMENTS.put(Tenor.TWO_DAYS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("LIBOR_USD_2D")));
  }
  /**
   * A map from tenor to curve instrument provider for Euribor rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> EURIBOR_INSTRUMENTS = new HashMap<>();
  static {
    EURIBOR_INSTRUMENTS.put(Tenor.ONE_DAY, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("EURIBOR_1D")));
    EURIBOR_INSTRUMENTS.put(Tenor.TWO_DAYS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("EURIBOR_2D")));
  }
  /**
   * A map from tenor to curve instrument provider for CDOR rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> CDOR_INSTRUMENTS = new HashMap<>();
  static {
    CDOR_INSTRUMENTS.put(Tenor.ONE_DAY, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("CDOR_1D")));
    CDOR_INSTRUMENTS.put(Tenor.TWO_DAYS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("CDOR_2D")));
  }
  /**
   * A map from tenor to curve instrument provider for Cibor rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> CIBOR_INSTRUMENTS = new HashMap<>();
  static {
    CIBOR_INSTRUMENTS.put(Tenor.ONE_DAY, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("CIBOR_1D")));
    CIBOR_INSTRUMENTS.put(Tenor.TWO_DAYS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("CIBOR_2D")));
  }
  /**
   * A map from tenor to curve instrument provider for Stibor rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> STIBOR_INSTRUMENTS = new HashMap<>();
  static {
    STIBOR_INSTRUMENTS.put(Tenor.ONE_DAY, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("STIBOR_1D")));
    STIBOR_INSTRUMENTS.put(Tenor.TWO_DAYS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("STIBOR_D")));
  }
  /**
   * A map from tenor to curve instrument provider for USD 3M FRA rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_3M_FRA_INSTRUMENTS = new HashMap<>();
  static {
    USD_3M_FRA_INSTRUMENTS.put(Tenor.THREE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("FRA3M_USD_3M")));
    USD_3M_FRA_INSTRUMENTS.put(Tenor.SIX_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("FRA3M_USD_6M")));
  }
  /**
   * A map from tenor to curve instrument provider for USD 6M FRA rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_6M_FRA_INSTRUMENTS = new HashMap<>();
  static {
    USD_6M_FRA_INSTRUMENTS.put(Tenor.SIX_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("FRA6M_USD_6M")));
    USD_6M_FRA_INSTRUMENTS.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("FRA6M_USD_1Y")));
  }
  /**
   * A map from tenor to future curve instrument provider for USD STIR futures.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_STIR_FUTURE_INSTRUMENTS = new HashMap<>();
  static {
    USD_STIR_FUTURE_INSTRUMENTS.put(Tenor.ONE_DAY, new SyntheticFutureCurveInstrumentProvider("ED"));
  }
  /**
   * A map from tenor to future curve instrument provider for banker's acceptance futures.
   */
  public static final Map<Tenor, CurveInstrumentProvider> BA_FUTURE_INSTRUMENTS = new HashMap<>();
  static {
    BA_FUTURE_INSTRUMENTS.put(Tenor.ONE_DAY, new SyntheticFutureCurveInstrumentProvider("BA"));
  }
  /**
   * A map from tenor to curve instrument provider for USD 3M swap rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_3M_SWAP_INSTRUMENTS = new HashMap<>();
  static {
    USD_3M_SWAP_INSTRUMENTS.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SWAP3M_USD_1Y")));
    USD_3M_SWAP_INSTRUMENTS.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SWAP3M_USD_2Y")));
  }
  /**
   * A map from tenor to curve instrument provider for USD 6M swap rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_6M_SWAP_INSTRUMENTS = new HashMap<>();
  static {
    USD_6M_SWAP_INSTRUMENTS.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SWAP6M_USD_1Y")));
    USD_6M_SWAP_INSTRUMENTS.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SWAP6M_USD_2Y")));
  }
  /**
   * A map from tenor to curve instrument provider for USD 12M swap rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_12M_SWAP_INSTRUMENTS = new HashMap<>();
  static {
    USD_12M_SWAP_INSTRUMENTS.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SWAP12M_USD_1Y")));
    USD_12M_SWAP_INSTRUMENTS.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SWAP12M_USD_2Y")));
  }
  /**
   * A map from tenor to curve instrument provider for USD 28D swap rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_28D_SWAP_INSTRUMENTS = new HashMap<>();
  static {
    USD_28D_SWAP_INSTRUMENTS.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SWAP28D_USD_1Y")));
    USD_28D_SWAP_INSTRUMENTS.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SWPA28D_USD_2Y")));
  }
  /**
   * A map from tenor to curve instrument provider for USD OIS rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_OIS_INSTRUMENTS = new HashMap<>();
  static {
    USD_OIS_INSTRUMENTS.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("OIS_USD_1Y")));
    USD_OIS_INSTRUMENTS.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("OIS_USD_2Y")));
  }
  /**
   * A map from tenor to curve instrument provider for USD basis swap rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_BASIS_SWAP_INSTRUMENTS = new HashMap<>();
  static {
    USD_BASIS_SWAP_INSTRUMENTS.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("BASIS_SWAP_USD_1Y")));
    USD_BASIS_SWAP_INSTRUMENTS.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("BASIS_SWAP_USD_2Y")));
  }
  /**
   * A map from tenor to curve instrument provider for USD tenor swap rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_TENOR_SWAP_INSTRUMENTS = new HashMap<>();
  static {
    USD_TENOR_SWAP_INSTRUMENTS.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("TENOR_SWAP_USD_1Y")));
    USD_TENOR_SWAP_INSTRUMENTS.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("TENOR_SWAP_USD_2Y")));
  }
  /**
   * A map from tenor to curve instrument provider for USD periodic zero rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_PERIODIC_ZERO_INSTRUMENTS = new HashMap<>();
  static {
    USD_PERIODIC_ZERO_INSTRUMENTS.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("PERIODIC_USD_1M")));
    USD_PERIODIC_ZERO_INSTRUMENTS.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("PERIODIC_USD_2M")));
  }
  /**
   * A map from tenor to curve instrument provider for EUR periodic zero rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> EUR_PERIODIC_ZERO_INSTRUMENTS = new HashMap<>();
  static {
    EUR_PERIODIC_ZERO_INSTRUMENTS.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("PERIODIC_EUR_1M")));
    EUR_PERIODIC_ZERO_INSTRUMENTS.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("PERIODIC_EUR_2M")));
  }
  /**
   * A map from tenor to curve instrument provider for USD simple zero rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_SIMPLE_ZERO_INSTRUMENTS = new HashMap<>();
  static {
    USD_PERIODIC_ZERO_INSTRUMENTS.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SIMPLE_USD_1M")));
    USD_PERIODIC_ZERO_INSTRUMENTS.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("SIMPLE_USD_2M")));
  }
  /**
   * A map from tenor to curve instrument provider for USD continuous zero rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> EUR_CONTINUOUS_ZERO_INSTRUMENTS = new HashMap<>();
  static {
    EUR_CONTINUOUS_ZERO_INSTRUMENTS.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("CONTINUOUS_EUR_1M")));
    EUR_CONTINUOUS_ZERO_INSTRUMENTS.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("CONTINUOUS_EUR_2M")));
  }
  /**
   * A map from tenor to curve instrument provider for USD continuous zero rates.
   */
  public static final Map<Tenor, CurveInstrumentProvider> USD_CONTINUOUS_ZERO_INSTRUMENTS = new HashMap<>();
  static {
    USD_CONTINUOUS_ZERO_INSTRUMENTS.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("CONTINUOUS_USD_1M")));
    USD_CONTINUOUS_ZERO_INSTRUMENTS.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("CONTINUOUS_USD_2M")));
  }
  /**
   * A curve specification builder configuration for USD rates.
   */
  public static final CurveSpecificationBuilderConfiguration CSBC = new CurveSpecificationBuilderConfiguration(USD_DEPOSIT_INSTRUMENTS,
      USD_3M_FRA_INSTRUMENTS, USD_6M_FRA_INSTRUMENTS, USD_LIBOR_INSTRUMENTS, EURIBOR_INSTRUMENTS, CDOR_INSTRUMENTS, CIBOR_INSTRUMENTS,
      STIBOR_INSTRUMENTS, USD_STIR_FUTURE_INSTRUMENTS, USD_6M_SWAP_INSTRUMENTS, USD_3M_SWAP_INSTRUMENTS, USD_BASIS_SWAP_INSTRUMENTS,
      USD_TENOR_SWAP_INSTRUMENTS, USD_OIS_INSTRUMENTS, USD_SIMPLE_ZERO_INSTRUMENTS, USD_PERIODIC_ZERO_INSTRUMENTS, USD_CONTINUOUS_ZERO_INSTRUMENTS,
      USD_12M_SWAP_INSTRUMENTS, USD_28D_SWAP_INSTRUMENTS);
}
