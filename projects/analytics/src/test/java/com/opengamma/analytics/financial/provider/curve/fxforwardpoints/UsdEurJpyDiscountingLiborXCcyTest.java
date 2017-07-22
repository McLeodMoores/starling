/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve.fxforwardpoints;

import static org.testng.AssertJUnit.assertEquals;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.financial.curve.CurveUtils;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurveYieldInterpolated;
import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorYDCurve;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.derivative.Forex;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttribute;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeFX;
import com.opengamma.analytics.financial.instrument.index.GeneratorAttributeIR;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorDepositON;
import com.opengamma.analytics.financial.instrument.index.GeneratorFRA;
import com.opengamma.analytics.financial.instrument.index.GeneratorForexSwap;
import com.opengamma.analytics.financial.instrument.index.GeneratorInstrument;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedIborMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedON;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapFixedONMaster;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapIborIbor;
import com.opengamma.analytics.financial.instrument.index.GeneratorSwapXCcyIborIbor;
import com.opengamma.analytics.financial.instrument.index.IborIndex;
import com.opengamma.analytics.financial.instrument.index.Index;
import com.opengamma.analytics.financial.instrument.index.IndexIborMaster;
import com.opengamma.analytics.financial.instrument.index.IndexON;
import com.opengamma.analytics.financial.instrument.swap.SwapDefinition;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParSpreadMarketQuoteDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueCurveSensitivityDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.discounting.PresentValueDiscountingCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.LastTimeCalculator;
import com.opengamma.analytics.financial.provider.calculator.generic.MarketQuoteSensitivityBlockCalculator;
import com.opengamma.analytics.financial.provider.curve.CurveBuildingBlockBundle;
import com.opengamma.analytics.financial.provider.curve.MultiCurveBundle;
import com.opengamma.analytics.financial.provider.curve.SingleCurveBundle;
import com.opengamma.analytics.financial.provider.curve.multicurve.MulticurveDiscountBuildingRepository;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderDiscount;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MulticurveSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.multicurve.MultipleCurrencyParameterSensitivity;
import com.opengamma.analytics.financial.provider.sensitivity.parameter.ParameterSensitivityParameterCalculator;
import com.opengamma.analytics.financial.schedule.ScheduleCalculator;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.CombinedInterpolatorExtrapolatorFactory;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.Interpolator1DFactory;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.financial.convention.calendar.Calendar;
import com.opengamma.financial.convention.calendar.MondayToFridayCalendar;
import com.opengamma.timeseries.precise.zdt.ImmutableZonedDateTimeDoubleTimeSeries;
import com.opengamma.timeseries.precise.zdt.ZonedDateTimeDoubleTimeSeries;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.MultipleCurrencyAmount;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.tuple.Pair;

/**
 * Build of curve in several blocks with relevant Jacobian matrices.
 * Multi-currency curve calibration process. Tests the difference between forward points interpolation and yield curve interpolation.
 */
@Test(groups = TestGroup.UNIT)
public class UsdEurJpyDiscountingLiborXCcyTest {

  private static final Interpolator1D INTERPOLATOR_LINEAR = CombinedInterpolatorExtrapolatorFactory.getInterpolator(Interpolator1DFactory.LINEAR, Interpolator1DFactory.FLAT_EXTRAPOLATOR,
      Interpolator1DFactory.FLAT_EXTRAPOLATOR);

  private static final LastTimeCalculator MATURITY_CALCULATOR = LastTimeCalculator.getInstance();
  private static final double TOLERANCE_ROOT = 1.0E-10;
  private static final int STEP_MAX = 100;

  private static final Calendar TARGET = new MondayToFridayCalendar("TARGET");
  private static final Calendar NYC = new MondayToFridayCalendar("NYC");
  private static final Calendar TOKYO = new MondayToFridayCalendar("TOKYO");
  private static final Currency EUR = Currency.EUR;
  private static final Currency USD = Currency.USD;
  private static final Currency JPY = Currency.JPY;
  private static final double FX_EURUSD = 1.40;
  private static final double FX_USDJPY = 80.0;
  private static final FXMatrix FX_MATRIX = new FXMatrix(USD);
  static {
    FX_MATRIX.addCurrency(EUR, USD, FX_EURUSD);
    FX_MATRIX.addCurrency(JPY, USD, 1 / FX_USDJPY);
  }

  private static final double NOTIONAL = 1.0;

  private static final GeneratorSwapFixedON GENERATOR_OIS_EUR = GeneratorSwapFixedONMaster.getInstance().getGenerator("EUR1YEONIA", TARGET);
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD_1 = GeneratorSwapFixedONMaster.getInstance().getGenerator("USD1YFEDFUND", TARGET);
  /** A USD overnight index */
  private static final IndexON USD_OVERNIGHT_INDEX = GENERATOR_OIS_USD_1.getIndex();
  private static final GeneratorSwapFixedON GENERATOR_OIS_USD = new GeneratorSwapFixedON("USD1YFEDFUND", USD_OVERNIGHT_INDEX, Period.ofMonths(12),
      GENERATOR_OIS_USD_1.getFixedLegDayCount(), GENERATOR_OIS_USD_1.getBusinessDayConvention(), true, 2, 2, NYC); // To avoid mat discrepancy: 0 pay lag
  private static final GeneratorSwapFixedON GENERATOR_OIS_JPY = GeneratorSwapFixedONMaster.getInstance().getGenerator("JPY1YTONAR", TARGET);
  /** A EUR overnight index */
  private static final IndexON EUR_OVERNIGHT_INDEX = GENERATOR_OIS_EUR.getIndex();
  /** A JPY overnight index */
  private static final IndexON JPY_OVERNIGHT_INDEX = GENERATOR_OIS_JPY.getIndex();
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_EUR = new GeneratorDepositON("EUR Deposit ON", EUR, TARGET, EUR_OVERNIGHT_INDEX.getDayCount());
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_USD = new GeneratorDepositON("USD Deposit ON", USD, TARGET, USD_OVERNIGHT_INDEX.getDayCount());
  private static final GeneratorDepositON GENERATOR_DEPOSIT_ON_JPY = new GeneratorDepositON("JPY Deposit ON", JPY, TARGET, JPY_OVERNIGHT_INDEX.getDayCount());
  private static final GeneratorSwapFixedIborMaster GENERATOR_SWAP_MASTER = GeneratorSwapFixedIborMaster.getInstance();
  private static final GeneratorSwapFixedIbor EUR1YEURIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("EUR1YEURIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor USD6MLIBOR3M = GENERATOR_SWAP_MASTER.getGenerator("USD6MLIBOR3M", TARGET);
  private static final GeneratorSwapFixedIbor JPY6MLIBOR6M = GENERATOR_SWAP_MASTER.getGenerator("JPY6MLIBOR6M", TARGET);
  /** A 3M EURIBOR index */
  private static final IborIndex EUR_3M_EURIBOR_INDEX = EUR1YEURIBOR3M.getIborIndex();
  /** A 3M USD LIBOR index */
  private static final IborIndex USD_3M_LIBOR_INDEX = USD6MLIBOR3M.getIborIndex();
  /** A 6M JPY LIBOR index */
  private static final IborIndex JPY_6M_LIBOR_INDEX = JPY6MLIBOR6M.getIborIndex();
  /** A 3M JPY LIBOR index */
  private static final IborIndex JPY_3M_LIBOR_INDEX = IndexIborMaster.getInstance().getIndex("JPYLIBOR3M");
  /** A 3M EUR LIBOR index */
  private static final IborIndex EUR_3M_LIBOR_INDEX = new IborIndex(EUR, Period.ofMonths(3), 2, EUR_3M_EURIBOR_INDEX.getDayCount(), EUR_3M_EURIBOR_INDEX.getBusinessDayConvention(), true, "EUROLIBOR3M");
  private static final GeneratorFRA GENERATOR_USD_FRA_3M = new GeneratorFRA("GENERATOR USD FRA 3M", USD_3M_LIBOR_INDEX, NYC);
  private static final GeneratorDepositIbor GENERATOR_EURIBOR3M = new GeneratorDepositIbor("GENERATOR_EURIBOR3M", EUR_3M_EURIBOR_INDEX, TARGET);
  private static final GeneratorDepositIbor GENERATOR_USDLIBOR3M = new GeneratorDepositIbor("GENERATOR_USDLIBOR3M", USD_3M_LIBOR_INDEX, NYC);
  private static final GeneratorDepositIbor GENERATOR_JPYLIBOR3M = new GeneratorDepositIbor("GENERATOR_JPYLIBOR3M", JPY_3M_LIBOR_INDEX, TOKYO);
  private static final GeneratorDepositIbor GENERATOR_JPYLIBOR6M = new GeneratorDepositIbor("GENERATOR_JPYLIBOR3M", JPY_6M_LIBOR_INDEX, TOKYO);
  private static final GeneratorSwapXCcyIborIbor EURIBOR3MUSDLIBOR3M = new GeneratorSwapXCcyIborIbor("EURIBOR3MUSDLIBOR3M", EUR_3M_EURIBOR_INDEX, USD_3M_LIBOR_INDEX, TARGET, NYC); // Spread on EUR leg
  private static final GeneratorSwapXCcyIborIbor JPYLIBOR3MUSDLIBOR3M = new GeneratorSwapXCcyIborIbor("JPYLIBOR3MUSDLIBOR3M", JPY_3M_LIBOR_INDEX, USD_3M_LIBOR_INDEX, TOKYO, NYC); // Spread on JPY leg
  private static final GeneratorSwapXCcyIborIbor JPYLIBOR3MEURIBOR3M = new GeneratorSwapXCcyIborIbor("JPYLIBOR3MEURIBOR3M", JPY_3M_LIBOR_INDEX, EUR_3M_EURIBOR_INDEX, TOKYO, TARGET); // Spread on JPY leg
  private static final GeneratorSwapIborIbor JPYLIBOR6MLIBOR3M = new GeneratorSwapIborIbor("JPYLIBOR6MLIBOR3M", JPY_3M_LIBOR_INDEX, JPY_6M_LIBOR_INDEX, TOKYO, TOKYO);
  private static final GeneratorForexSwap GENERATOR_FX_EURUSD = new GeneratorForexSwap("EURUSD", EUR, USD, TARGET, EUR_3M_EURIBOR_INDEX.getSpotLag(), EUR_3M_EURIBOR_INDEX.getBusinessDayConvention(), true);
  private static final GeneratorForexSwap GENERATOR_FX_USDJPY = new GeneratorForexSwap("USDJPY", USD, JPY, TARGET, EUR_3M_EURIBOR_INDEX.getSpotLag(), EUR_3M_EURIBOR_INDEX.getBusinessDayConvention(), true);

  private static final ZonedDateTime NOW = DateUtils.getUTCDate(2011, 9, 19);

  private static final ZonedDateTimeDoubleTimeSeries TS_EMPTY = ImmutableZonedDateTimeDoubleTimeSeries.ofEmptyUTC();
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });
  private static final ZonedDateTimeDoubleTimeSeries TS_ON_USD_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.07, 0.08 });

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0035, 0.0036 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_USD3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0035 });

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0060, 0.0061 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_EUR3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0060 });

  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_JPY3M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0060, 0.0061 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_JPY3M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0060 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_JPY6M_WITH_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27),
    DateUtils.getUTCDate(2011, 9, 28) }, new double[] {0.0061, 0.0062 });
  private static final ZonedDateTimeDoubleTimeSeries TS_IBOR_JPY6M_WITHOUT_TODAY = ImmutableZonedDateTimeDoubleTimeSeries.ofUTC(new ZonedDateTime[] {DateUtils.getUTCDate(2011, 9, 27) },
      new double[] {0.0061 });

  /** Fixing time series created before the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITHOUT_TODAY = new HashMap<>();
  /** Fixing time series created after the valuation date fixing is available */
  private static final Map<Index, ZonedDateTimeDoubleTimeSeries> FIXING_TS_WITH_TODAY = new HashMap<>();
  static {
    FIXING_TS_WITHOUT_TODAY.put(USD_OVERNIGHT_INDEX, TS_ON_USD_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(USD_3M_LIBOR_INDEX, TS_IBOR_USD3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUR_3M_LIBOR_INDEX, TS_IBOR_EUR3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(EUR_3M_EURIBOR_INDEX, TS_IBOR_EUR3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(JPY_3M_LIBOR_INDEX, TS_IBOR_JPY3M_WITHOUT_TODAY);
    FIXING_TS_WITHOUT_TODAY.put(JPY_6M_LIBOR_INDEX, TS_IBOR_JPY6M_WITHOUT_TODAY);
    FIXING_TS_WITH_TODAY.put(USD_OVERNIGHT_INDEX, TS_ON_USD_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(USD_3M_LIBOR_INDEX, TS_IBOR_USD3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_3M_LIBOR_INDEX, TS_IBOR_EUR3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(EUR_3M_EURIBOR_INDEX, TS_IBOR_EUR3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(JPY_3M_LIBOR_INDEX, TS_IBOR_JPY3M_WITH_TODAY);
    FIXING_TS_WITH_TODAY.put(JPY_6M_LIBOR_INDEX, TS_IBOR_JPY6M_WITH_TODAY);
  }

  private static final String CURVE_NAME_DSC_EUR = "EUR Dsc";
  private static final String CURVE_NAME_FWD3_EUR = "EUR Fwd 3M";
  private static final String CURVE_NAME_DSC_USD = "USD Dsc";
  private static final String CURVE_NAME_FWD3_USD = "USD Fwd 3M";
  private static final String CURVE_NAME_DSC_JPY = "JPY Dsc";
  private static final String CURVE_NAME_FWD3_JPY = "JPY Fwd 3M";
  private static final String CURVE_NAME_FWD6_JPY = "JPY Fwd 6M";

  /** Market values for the dsc USD curve */
  private static final double[] DSC_USD_MARKET_QUOTES = new double[] {0.0000, 0.0000, 0.0100, 0.0110, 0.0120, 0.0125, 0.0145, 0.0140, 0.0160, 0.0170, 0.0190, 0.0180, 0.0200, 0.0200 };
  /** Generators for the dsc USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_USD, GENERATOR_DEPOSIT_ON_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD, GENERATOR_OIS_USD,
    GENERATOR_OIS_USD };
  /** Tenors for the dsc USD curve */
  private static final Period[] DSC_USD_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(4),
    Period.ofMonths(6), Period.ofMonths(9), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] DSC_USD_ATTR = new GeneratorAttributeIR[DSC_USD_TENOR.length];
  static {
    for (int i = 0; i < 2; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(DSC_USD_TENOR[i], Period.ZERO);
    }
    for (int i = 2; i < DSC_USD_TENOR.length; i++) {
      DSC_USD_ATTR[i] = new GeneratorAttributeIR(DSC_USD_TENOR[i]);
    }
  }

  /** Market values for the Fwd 3M USD curve */
  private static final double[] FWD3_USD_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0060, 0.0070, 0.0080, 0.0160 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_USD_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_USDLIBOR3M, GENERATOR_USD_FRA_3M, USD6MLIBOR3M, USD6MLIBOR3M,
    USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M, USD6MLIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_USD_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
    Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD3_USD_ATTR = new GeneratorAttributeIR[FWD3_USD_TENOR.length];
  static {
    for (int i = 0; i < FWD3_USD_TENOR.length; i++) {
      FWD3_USD_ATTR[i] = new GeneratorAttributeIR(FWD3_USD_TENOR[i]);
    }
  }

  /** Market values for the dsc EUR curve */
  private static final double[] DSC_EUR_MARKET_QUOTES = new double[] {0.0000, 0.0000, 0.0004, 0.0009, 0.0015, 0.0020, 0.0036, 0.0050, -0.0050, -0.0050, -0.0050, -0.0045, -0.0040 };
  /** Generators for the dsc EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_EUR, GENERATOR_DEPOSIT_ON_EUR, GENERATOR_FX_EURUSD,
    GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, GENERATOR_FX_EURUSD, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M, EURIBOR3MUSDLIBOR3M,
    EURIBOR3MUSDLIBOR3M };
  /** Tenors for the dsc EUR curve */
  private static final Period[] DSC_EUR_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2), Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttribute[] DSC_EUR_ATTR = new GeneratorAttribute[DSC_EUR_TENOR.length];
  static {
    for (int i = 0; i < 2; i++) {
      DSC_EUR_ATTR[i] = new GeneratorAttributeIR(DSC_EUR_TENOR[i], Period.ZERO);
    }
    for (int i = 2; i < DSC_EUR_TENOR.length; i++) {
      DSC_EUR_ATTR[i] = new GeneratorAttributeFX(DSC_EUR_TENOR[i], FX_MATRIX);
    }
  }

  /** Market values for the Fwd 3M EUR curve */
  private static final double[] FWD3_EUR_MARKET_QUOTES = new double[] {0.0045, 0.0045, 0.0045, 0.0045, 0.0050, 0.0060, 0.0085, 0.0160 };
  /** Generators for the Fwd 3M USD curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_EUR_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_EURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M,
    EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M, EUR1YEURIBOR3M };
  /** Tenors for the Fwd 3M USD curve */
  private static final Period[] FWD3_EUR_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
    Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD3_EUR_ATTR = new GeneratorAttributeIR[FWD3_EUR_TENOR.length];
  static {
    for (int i = 0; i < FWD3_EUR_TENOR.length; i++) {
      FWD3_EUR_ATTR[i] = new GeneratorAttributeIR(FWD3_EUR_TENOR[i]);
    }
  }

  /** Market values for the dsc JPY curve */
  private static final double[] DSC_JPY_MARKET_QUOTES = new double[] {0.0005, 0.0005, -0.0004, -0.0008, -0.0012, -0.0024, -0.0036, -0.0048, -0.0030, -0.0040, -0.0040, -0.0045, -0.0050 };
  /** Generators for the dsc EUR curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] DSC_JPY_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_DEPOSIT_ON_JPY, GENERATOR_DEPOSIT_ON_JPY, GENERATOR_FX_USDJPY,
    GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, GENERATOR_FX_USDJPY, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M, JPYLIBOR3MUSDLIBOR3M,
    JPYLIBOR3MUSDLIBOR3M };
  /** Tenors for the dsc EUR curve */
  private static final Period[] DSC_JPY_TENOR = new Period[] {Period.ofDays(0), Period.ofDays(1), Period.ofMonths(1), Period.ofMonths(2),
    Period.ofMonths(3), Period.ofMonths(6), Period.ofMonths(9),
    Period.ofYears(1), Period.ofYears(2), Period.ofYears(3), Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttribute[] DSC_JPY_ATTR = new GeneratorAttribute[DSC_JPY_TENOR.length];
  static {
    for (int i = 0; i < 2; i++) {
      DSC_JPY_ATTR[i] = new GeneratorAttributeIR(DSC_JPY_TENOR[i], Period.ZERO);
    }
    for (int i = 2; i < DSC_JPY_TENOR.length; i++) {
      DSC_JPY_ATTR[i] = new GeneratorAttributeFX(DSC_JPY_TENOR[i], FX_MATRIX);
    }
  }

  /** Market values for the Fwd 3M JPY curve */
  private static final double[] FWD3_JPY_MARKET_QUOTES = new double[] {0.0020, 0.0010, 0.0010, 0.0010, 0.0010, 0.0015, 0.0015, 0.0015 };
  /** Generators for the Fwd 3M JPY curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD3_JPY_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_JPYLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M,
    JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M, JPYLIBOR6MLIBOR3M };
  /** Tenors for the Fwd 3M JPY curve */
  private static final Period[] FWD3_JPY_TENOR = new Period[] {Period.ofMonths(0), Period.ofMonths(6), Period.ofYears(1), Period.ofYears(2),
    Period.ofYears(3), Period.ofYears(4), Period.ofYears(5),
    Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD3_JPY_ATTR = new GeneratorAttributeIR[FWD3_JPY_TENOR.length];
  static {
    for (int i = 0; i < FWD3_JPY_TENOR.length; i++) {
      FWD3_JPY_ATTR[i] = new GeneratorAttributeIR(FWD3_JPY_TENOR[i]);
    }
  }

  /** Market values for the Fwd 6M JPY curve */
  private static final double[] FWD6_JPY_MARKET_QUOTES = new double[] {0.0035, 0.0035, 0.0035, 0.0040, 0.0040, 0.0040, 0.0075 };
  /** Generators for the Fwd 6M JPY curve */
  private static final GeneratorInstrument<? extends GeneratorAttribute>[] FWD6_JPY_GENERATORS = new GeneratorInstrument<?>[] {GENERATOR_JPYLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M,
    JPY6MLIBOR6M, JPY6MLIBOR6M, JPY6MLIBOR6M };
  /** Tenors for the Fwd 6M JPY curve */
  private static final Period[] FWD6_JPY_TENOR = new Period[] {Period.ofMonths(0), Period.ofYears(1), Period.ofYears(2), Period.ofYears(3),
    Period.ofYears(4), Period.ofYears(5), Period.ofYears(10) };
  private static final GeneratorAttributeIR[] FWD6_JPY_ATTR = new GeneratorAttributeIR[FWD6_JPY_TENOR.length];
  static {
    for (int i = 0; i < FWD6_JPY_TENOR.length; i++) {
      FWD6_JPY_ATTR[i] = new GeneratorAttributeIR(FWD6_JPY_TENOR[i]);
    }
  }

  /** Standard USD discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_USD;
  /** Standard USD Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_USD;
  /** Standard EUR discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_EUR;
  /** Standard EUR Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_EUR;
  /** Standard JPY discounting curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_DSC_JPY;
  /** Standard JPY Forward 3M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD3_JPY;
  /** Standard JPY Forward 6M curve instrument definitions */
  private static final InstrumentDefinition<?>[] DEFINITIONS_FWD6_JPY;

  /** Units of curves */
  private static final int[] NB_UNITS = new int[] {3, 3, 1 };
  private static final int NB_BLOCKS = NB_UNITS.length;
  private static final InstrumentDefinition<?>[][][][] DEFINITIONS_UNITS = new InstrumentDefinition<?>[NB_BLOCKS][][][];
  private static final GeneratorYDCurve[][][] GENERATORS_UNITS = new GeneratorYDCurve[NB_BLOCKS][][];
  private static final String[][][] NAMES_UNITS = new String[NB_BLOCKS][][];

  private static final MulticurveProviderDiscount MULTICURVE_KNOWN_DATA = new MulticurveProviderDiscount(FX_MATRIX);

  private static final LinkedHashMap<String, Currency> DSC_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IndexON[]> FWD_ON_MAP = new LinkedHashMap<>();
  private static final LinkedHashMap<String, IborIndex[]> FWD_IBOR_MAP = new LinkedHashMap<>();

  static {
    DEFINITIONS_DSC_USD = getDefinitions(DSC_USD_MARKET_QUOTES, DSC_USD_GENERATORS, DSC_USD_ATTR);
    DEFINITIONS_FWD3_USD = getDefinitions(FWD3_USD_MARKET_QUOTES, FWD3_USD_GENERATORS, FWD3_USD_ATTR);
    DEFINITIONS_DSC_EUR = getDefinitions(DSC_EUR_MARKET_QUOTES, DSC_EUR_GENERATORS, DSC_EUR_ATTR);
    DEFINITIONS_FWD3_EUR = getDefinitions(FWD3_EUR_MARKET_QUOTES, FWD3_EUR_GENERATORS, FWD3_EUR_ATTR);
    DEFINITIONS_DSC_JPY = getDefinitions(DSC_JPY_MARKET_QUOTES, DSC_JPY_GENERATORS, DSC_JPY_ATTR);
    DEFINITIONS_FWD3_JPY = getDefinitions(FWD3_JPY_MARKET_QUOTES, FWD3_JPY_GENERATORS, FWD3_JPY_ATTR);
    DEFINITIONS_FWD6_JPY = getDefinitions(FWD6_JPY_MARKET_QUOTES, FWD6_JPY_GENERATORS, FWD6_JPY_ATTR);

    for (int i = 0; i < NB_BLOCKS; i++) {
      DEFINITIONS_UNITS[i] = new InstrumentDefinition<?>[NB_UNITS[i]][][];
      GENERATORS_UNITS[i] = new GeneratorYDCurve[NB_UNITS[i]][];
      NAMES_UNITS[i] = new String[NB_UNITS[i]][];
    }
    DEFINITIONS_UNITS[0] = new InstrumentDefinition<?>[NB_UNITS[0]][][];
    DEFINITIONS_UNITS[1] = new InstrumentDefinition<?>[NB_UNITS[1]][][];
    DEFINITIONS_UNITS[2] = new InstrumentDefinition<?>[NB_UNITS[2]][][];
    DEFINITIONS_UNITS[0][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[0][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
    DEFINITIONS_UNITS[0][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_EUR, DEFINITIONS_FWD3_EUR };
    DEFINITIONS_UNITS[1][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD };
    DEFINITIONS_UNITS[1][1] = new InstrumentDefinition<?>[][] {DEFINITIONS_FWD3_USD };
    DEFINITIONS_UNITS[1][2] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_JPY, DEFINITIONS_FWD3_JPY, DEFINITIONS_FWD6_JPY };
    DEFINITIONS_UNITS[2][0] = new InstrumentDefinition<?>[][] {DEFINITIONS_DSC_USD, DEFINITIONS_FWD3_USD, DEFINITIONS_DSC_JPY, DEFINITIONS_FWD3_JPY, DEFINITIONS_FWD6_JPY };
    final GeneratorYDCurve genIntLin = new GeneratorCurveYieldInterpolated(MATURITY_CALCULATOR, INTERPOLATOR_LINEAR);
    GENERATORS_UNITS[0] = new GeneratorYDCurve[NB_UNITS[0]][];
    GENERATORS_UNITS[1] = new GeneratorYDCurve[NB_UNITS[1]][];
    GENERATORS_UNITS[2] = new GeneratorYDCurve[NB_UNITS[2]][];
    GENERATORS_UNITS[0][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[0][2] = new GeneratorYDCurve[] {genIntLin, genIntLin };
    GENERATORS_UNITS[1][0] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][1] = new GeneratorYDCurve[] {genIntLin };
    GENERATORS_UNITS[1][2] = new GeneratorYDCurve[] {genIntLin, genIntLin, genIntLin };
    GENERATORS_UNITS[2][0] = new GeneratorYDCurve[] {genIntLin, genIntLin, genIntLin, genIntLin, genIntLin };
    NAMES_UNITS[0] = new String[NB_UNITS[0]][];
    NAMES_UNITS[1] = new String[NB_UNITS[1]][];
    NAMES_UNITS[2] = new String[NB_UNITS[2]][];
    NAMES_UNITS[0][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[0][1] = new String[] {CURVE_NAME_FWD3_USD };
    NAMES_UNITS[0][2] = new String[] {CURVE_NAME_DSC_EUR, CURVE_NAME_FWD3_EUR }; //TODO: the EUR USD with spread curve for EUR dsc (to avoid fwd pts spikes).
    NAMES_UNITS[1][0] = new String[] {CURVE_NAME_DSC_USD };
    NAMES_UNITS[1][1] = new String[] {CURVE_NAME_FWD3_USD };
    NAMES_UNITS[1][2] = new String[] {CURVE_NAME_DSC_JPY, CURVE_NAME_FWD3_JPY, CURVE_NAME_FWD6_JPY };
    NAMES_UNITS[2][0] = new String[] {CURVE_NAME_DSC_USD, CURVE_NAME_FWD3_USD, CURVE_NAME_DSC_JPY, CURVE_NAME_FWD3_JPY, CURVE_NAME_FWD6_JPY };
    // Note: the sensitivity is computed in the order of the curve names. The names order should be in line with the units definition order.
    DSC_MAP.put(CURVE_NAME_DSC_USD, USD);
    DSC_MAP.put(CURVE_NAME_DSC_EUR, EUR);
    DSC_MAP.put(CURVE_NAME_DSC_JPY, JPY);
    FWD_ON_MAP.put(CURVE_NAME_DSC_USD, new IndexON[] {USD_OVERNIGHT_INDEX });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_USD, new IborIndex[] {USD_3M_LIBOR_INDEX });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_EUR, new IborIndex[] {EUR_3M_EURIBOR_INDEX, EUR_3M_LIBOR_INDEX });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD3_JPY, new IborIndex[] {JPY_3M_LIBOR_INDEX });
    FWD_IBOR_MAP.put(CURVE_NAME_FWD6_JPY, new IborIndex[] {JPY_6M_LIBOR_INDEX });
  }

  @SuppressWarnings("unchecked")
  private static InstrumentDefinition<?>[] getDefinitions(final double[] marketQuotes, @SuppressWarnings("rawtypes") final GeneratorInstrument[] generators, final GeneratorAttribute[] attribute) {
    final InstrumentDefinition<?>[] definitions = new InstrumentDefinition<?>[marketQuotes.length];
    for (int i = 0; i < marketQuotes.length; i++) {
      definitions[i] = generators[i].generateInstrument(NOW, marketQuotes[i], NOTIONAL, attribute[i]);
    }
    return definitions;
  }

  private static final List<Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle>> CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK = new ArrayList<>();

  // Calculators
  private static final PresentValueDiscountingCalculator PVDC = PresentValueDiscountingCalculator.getInstance();
  private static final PresentValueCurveSensitivityDiscountingCalculator PVCSDC = PresentValueCurveSensitivityDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteDiscountingCalculator PSMQDC = ParSpreadMarketQuoteDiscountingCalculator.getInstance();
  private static final ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator PSMQCSDC = ParSpreadMarketQuoteCurveSensitivityDiscountingCalculator.getInstance();

  private static final MulticurveDiscountBuildingRepository CURVE_BUILDING_REPOSITORY = new MulticurveDiscountBuildingRepository(TOLERANCE_ROOT, TOLERANCE_ROOT, STEP_MAX);

  private static final double TOLERANCE_CAL = 1.0E-9;

  @BeforeSuite
  static void initClass() {
    for (int i = 0; i < NB_BLOCKS; i++) {
      CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.add(makeCurvesFromDefinitions(DEFINITIONS_UNITS[i], GENERATORS_UNITS[i], NAMES_UNITS[i], MULTICURVE_KNOWN_DATA, PSMQDC,
          PSMQCSDC, false));
    }
  }

  @Test
  public void curveConstruction() {
    for (int i = 0; i < NB_BLOCKS; i++) {
      curveConstructionTest(DEFINITIONS_UNITS[i], CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(i).getFirst(), false, i);
    }
  }

  @Test(enabled = false)
  public void performance() {
    long startTime, endTime;
    final int nbTest = 10;

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[0], GENERATORS_UNITS[0], NAMES_UNITS[0], MULTICURVE_KNOWN_DATA, PSMQDC, PSMQCSDC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingDiscountingDiscountXCcyTest - " + nbTest + " curve construction / USD/EUR 3 units: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction USD/EUR 3 units: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 160 ms for 10 sets.

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[1], GENERATORS_UNITS[1], NAMES_UNITS[1], MULTICURVE_KNOWN_DATA, PSMQDC, PSMQCSDC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingDiscountingDiscountXCcyTest - " + nbTest + " curve construction / USD/JPY 3 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction USD/JPY 3 unit: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 200 ms for 10 sets.

    startTime = System.currentTimeMillis();
    for (int i = 0; i < nbTest; i++) {
      makeCurvesFromDefinitions(DEFINITIONS_UNITS[2], GENERATORS_UNITS[2], NAMES_UNITS[2], MULTICURVE_KNOWN_DATA, PSMQDC, PSMQCSDC, false);
    }
    endTime = System.currentTimeMillis();
    System.out.println("MulticurveBuildingDiscountingDiscountXCcyTest - " + nbTest + " curve construction / USD/JPY 1 unit: " + (endTime - startTime) + " ms");
    // Performance note: Curve construction USD/JPY 1 unit: 06-Nov-12: On Mac Pro 3.2 GHz Quad-Core Intel Xeon: 265 ms for 10 sets.

  }

  private void curveConstructionTest(final InstrumentDefinition<?>[][][] definitions, final MulticurveProviderDiscount curves, final boolean withToday, final int block) {
    final int nbBlocks = definitions.length;
    for (int i = 0; i < nbBlocks; i++) {
      final InstrumentDerivative[][] instruments = CurveUtils.convert(definitions[i], withToday ? FIXING_TS_WITH_TODAY : FIXING_TS_WITHOUT_TODAY, NOW);
      final double[][] pv = new double[instruments.length][];
      for (int j = 0; j < instruments.length; j++) {
        pv[j] = new double[instruments[j].length];
        for (int k = 0; k < instruments[j].length; k++) {
          pv[j][k] = curves.getFxRates().convert(instruments[j][k].accept(PVDC, curves), EUR).getAmount();
          assertEquals("Curve construction: block " + block + ", unit " + i + " - instrument " + k, 0, pv[j][k], TOLERANCE_CAL);
        }
      }
    }
  }

  /**
   * Analyzes incoherence between curve interpolation and forward points interpolation.
   */
  @Test(enabled = true)
  public void forwardPointsInterpolation() {
    final MulticurveProviderDiscount multicurves = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst();
    // FX swap description
    final double notionalEUR = 1E8; //100m
    final double fxEURUSDFwdInit = FX_EURUSD + 0.0010; // Should have no impact
    final Period startTenor = Period.ofMonths(0);
    final Period endTenor = Period.ofMonths(12);
    final ZonedDateTime spot = ScheduleCalculator.getAdjustedDate(NOW, USD_3M_LIBOR_INDEX.getSpotLag(), TARGET);
    final ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(spot, startTenor, USD_3M_LIBOR_INDEX, NYC);
    final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(spot, endTenor, USD_3M_LIBOR_INDEX, NYC);
    final double[] points = DSC_EUR_MARKET_QUOTES;
    final double[] time = ((InterpolatedDoublesCurve) ((YieldCurve) multicurves.getCurve(EUR)).getCurve()).getXDataAsPrimitive();
    final InterpolatedDoublesCurve pointsCurve = new InterpolatedDoublesCurve(time, points, INTERPOLATOR_LINEAR, true, "Points curve");
    ZonedDateTime loopdate = startDate;
    final List<Double> pvUSDCurve = new ArrayList<>();
    final List<Double> pvUSDPts = new ArrayList<>();
    final List<Double> pvUSDDiff = new ArrayList<>();
    final List<Double> ptsCurve = new ArrayList<>();
    final List<Double> ptsInt = new ArrayList<>();
    final List<Double> ptsDiff = new ArrayList<>();
    final List<Double> payTime = new ArrayList<>();
    while (!loopdate.isAfter(endDate)) {
      final ForexDefinition fxSwapDefinition = new ForexDefinition(EUR, USD, loopdate, notionalEUR, fxEURUSDFwdInit);
      final Forex fxSwap = fxSwapDefinition.toDerivative(NOW);
      final MultipleCurrencyAmount pvFxSwap = fxSwap.accept(PVDC, multicurves);
      final double pvUSDCurved = FX_MATRIX.convert(pvFxSwap, USD).getAmount();
      pvUSDCurve.add(pvUSDCurved);
      final double pvUSDPtsd = -(fxEURUSDFwdInit - FX_EURUSD - pointsCurve.getYValue(fxSwap.getPaymentTime()))
          * multicurves.getDiscountFactor(USD, fxSwap.getPaymentTime()) * notionalEUR;
      pvUSDPts.add(pvUSDPtsd);
      pvUSDDiff.add(pvUSDCurved - pvUSDPtsd);
      //      double testUSDI = (fxEURUSDFwdInit)
      //          * multicurves.getDiscountFactor(USD, fxSwap.getPaymentTime()) * notionalEUR;
      //      double testUSDC = (FX_EURUSD + pointsCurve.getYValue(fxSwap.getPaymentTime()))
      //          * multicurves.getDiscountFactor(USD, fxSwap.getPaymentTime()) * notionalEUR;
      //      double testEURUSD = pvFxSwap.getAmount(EUR) * FX_EURUSD;
      final double ptC = (multicurves.getDiscountFactor(EUR, fxSwap.getPaymentTime()) / multicurves.getDiscountFactor(USD, fxSwap.getPaymentTime()) - 1) * FX_EURUSD;
      ptsCurve.add(ptC);
      ptsInt.add(pointsCurve.getYValue(fxSwap.getPaymentTime()));
      ptsDiff.add((ptC - pointsCurve.getYValue(fxSwap.getPaymentTime())) * 10000);
      payTime.add(fxSwap.getPaymentTime());
      loopdate = ScheduleCalculator.getAdjustedDate(loopdate, 1, TARGET);
    }
  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void marketQuoteSensitivityAnalysis() {
    // Create a 3 currencies provider
    final int indexEur = 4;
    final MulticurveProviderDiscount multicurves7 = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(indexEur).getFirst();
    multicurves7.setAll(CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(1).getFirst());
    final CurveBuildingBlockBundle blocks7 = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(indexEur).getSecond();
    blocks7.addAll(CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(1).getSecond());
    final double spreadJPYEUR = 0.0010; // 10bps
    final GeneratorAttributeFX attr6Mx5Y = new GeneratorAttributeFX(Period.ofMonths(6), Period.ofYears(5), FX_MATRIX); //TODO Check dates swap
    final double notional = 100000;
    final SwapDefinition swapDefinition = JPYLIBOR3MEURIBOR3M.generateInstrument(NOW, spreadJPYEUR, notional, attr6Mx5Y);
    final InstrumentDerivative swap = swapDefinition.toDerivative(NOW);
    final ParameterSensitivityParameterCalculator<MulticurveProviderInterface> PSC = new ParameterSensitivityParameterCalculator<>(PVCSDC);
    final MarketQuoteSensitivityBlockCalculator<MulticurveProviderInterface> MQSC = new MarketQuoteSensitivityBlockCalculator<>(PSC);
    @SuppressWarnings("unused")
    final MultipleCurrencyParameterSensitivity mqs = MQSC.fromInstrument(swap, multicurves7, blocks7);
    //    int t = 0;
    //    t++;
  }

  /**
   * Analyzes the shape of the forward curve.
   */
  @Test(enabled = false)
  public void forwardAnalysis() {
    final MulticurveProviderInterface marketDsc = CURVES_PAR_SPREAD_MQ_WITHOUT_TODAY_BLOCK.get(0).getFirst();
    final int jump = 1;
    final int startIndex = 0;
    final int nbDate = 2750;
    ZonedDateTime startDate = ScheduleCalculator.getAdjustedDate(NOW, EUR_3M_EURIBOR_INDEX.getSpotLag() + startIndex * jump, TARGET);
    final double[] rateDsc = new double[nbDate];
    final double[] startTime = new double[nbDate];
    try (final FileWriter writer = new FileWriter("fwd-dsc.csv")) {
      for (int i = 0; i < nbDate; i++) {
        startTime[i] = TimeCalculator.getTimeBetween(NOW, startDate);
        final ZonedDateTime endDate = ScheduleCalculator.getAdjustedDate(startDate, EUR_3M_EURIBOR_INDEX, TARGET);
        final double endTime = TimeCalculator.getTimeBetween(NOW, endDate);
        final double accrualFactor = EUR_3M_EURIBOR_INDEX.getDayCount().getDayCountFraction(startDate, endDate);
        rateDsc[i] = marketDsc.getSimplyCompoundForwardRate(EUR_3M_EURIBOR_INDEX, startTime[i], endTime, accrualFactor);
        startDate = ScheduleCalculator.getAdjustedDate(startDate, jump, TARGET);
        writer.append(0.0 + "," + startTime[i] + "," + rateDsc[i] + "\n");
      }
      writer.flush();
      writer.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  private static Pair<MulticurveProviderDiscount, CurveBuildingBlockBundle> makeCurvesFromDefinitions(final InstrumentDefinition<?>[][][] definitions, final GeneratorYDCurve[][] curveGenerators,
      final String[][] curveNames, final MulticurveProviderDiscount knownData, final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> calculator,
      final InstrumentDerivativeVisitor<MulticurveProviderInterface, MulticurveSensitivity> sensitivityCalculator, final boolean withToday) {
    final int nUnits = definitions.length;
    final MultiCurveBundle<GeneratorYDCurve>[] curveBundles = new MultiCurveBundle[nUnits];
    for (int i = 0; i < nUnits; i++) {
      final int nCurves = definitions[i].length;
      final SingleCurveBundle<GeneratorYDCurve>[] singleCurves = new SingleCurveBundle[nCurves];
      for (int j = 0; j < nCurves; j++) {
        final int nInstruments = definitions[i][j].length;
        final InstrumentDerivative[] derivatives = new InstrumentDerivative[nInstruments];
        final double[] rates = new double[nInstruments];
        for (int k = 0; k < nInstruments; k++) {
          derivatives[k] = CurveUtils.convert(definitions[i][j][k], withToday ? FIXING_TS_WITH_TODAY : FIXING_TS_WITHOUT_TODAY, NOW);
          rates[k] = definitions[i][j][k].accept(CurveUtils.RATES_INITIALIZATION);
        }
        final GeneratorYDCurve generator = curveGenerators[i][j].finalGenerator(derivatives);
        final double[] initialGuess = generator.initialGuess(rates);
        singleCurves[j] = new SingleCurveBundle<>(curveNames[i][j], derivatives, initialGuess, generator);
      }
      curveBundles[i] = new MultiCurveBundle<>(singleCurves);
    }
    return CURVE_BUILDING_REPOSITORY.makeCurvesFromDerivatives(curveBundles, knownData, DSC_MAP, FWD_IBOR_MAP, FWD_ON_MAP, calculator, sensitivityCalculator);
  }

}
