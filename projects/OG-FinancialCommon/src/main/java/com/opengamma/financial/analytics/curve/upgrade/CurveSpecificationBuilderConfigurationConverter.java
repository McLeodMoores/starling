/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.BANKERS_ACCEPTANCE;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.BASIS_SWAP;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CASH;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CDOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.CONTINUOUS_ZERO_DEPOSIT;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.EURIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FRA;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FRA_3M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FRA_6M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.FUTURE;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.LIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.OIS_SWAP;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.PERIODIC_ZERO_DEPOSIT;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SIMPLE_ZERO_DEPOSIT;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SPREAD;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.STIBOR;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_12M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_28D;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_3M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.SWAP_6M;
import static com.opengamma.financial.analytics.ircurve.StripInstrumentType.TENOR_SWAP;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;

/**
 * Converts a {@link CurveSpecificationBuilderConfiguration} into {@link CurveNodeIdMapper}.
 */
public class CurveSpecificationBuilderConfigurationConverter {
  private static final Map<StripInstrumentType, InstrumentProviderPopulator> CONVERTERS = new EnumMap<>(StripInstrumentType.class);

  static {
    CONVERTERS.put(BANKERS_ACCEPTANCE, new FutureInstrumentProviderPopulator(BANKERS_ACCEPTANCE, new FixedCurrencyCsbcRenamingFunction("CAD", "BA")));
    CONVERTERS.put(BASIS_SWAP, new BasisSwapInstrumentProviderPopulator(BASIS_SWAP));
    CONVERTERS.put(CASH, new CashInstrumentProviderPopulator(CASH));
    CONVERTERS.put(CDOR, new CashInstrumentProviderPopulator(CDOR, new FixedCurrencyCsbcRenamingFunction("CAD", "CDOR")));
    CONVERTERS.put(CIBOR, new CashInstrumentProviderPopulator(CIBOR, new FixedCurrencyCsbcRenamingFunction("DKK", "Cibor")));
    CONVERTERS.put(CONTINUOUS_ZERO_DEPOSIT, new ContinuouslyCompoundedRateInstrumentProviderPopulator());
    CONVERTERS.put(EURIBOR, new CashInstrumentProviderPopulator(EURIBOR, new FixedCurrencyCsbcRenamingFunction("EUR", "Euribor")));
    CONVERTERS.put(FRA, new NoOpInstrumentProviderPopulator(FRA));
    CONVERTERS.put(FRA_3M, new FraInstrumentProviderPopulator(FRA_3M, new DefaultCsbcRenamingFunction("3m")));
    CONVERTERS.put(FRA_6M, new FraInstrumentProviderPopulator(FRA_6M, new DefaultCsbcRenamingFunction("6m")));
    CONVERTERS.put(FUTURE, new FutureInstrumentProviderPopulator(FUTURE, new DefaultCsbcRenamingFunction("STIR")));
    CONVERTERS.put(LIBOR, new CashInstrumentProviderPopulator(LIBOR, new DefaultCsbcRenamingFunction("Libor")));
    CONVERTERS.put(OIS_SWAP, new SwapInstrumentProviderPopulator(OIS_SWAP, new DefaultCsbcRenamingFunction("Overnight")));
    CONVERTERS.put(PERIODIC_ZERO_DEPOSIT, new PeriodicZeroDepositInstrumentProviderPopulator());
    CONVERTERS.put(SIMPLE_ZERO_DEPOSIT, new NoOpInstrumentProviderPopulator(SIMPLE_ZERO_DEPOSIT));
    CONVERTERS.put(SPREAD, new NoOpInstrumentProviderPopulator(SPREAD));
    CONVERTERS.put(STIBOR, new CashInstrumentProviderPopulator(STIBOR, new FixedCurrencyCsbcRenamingFunction("SEK", "Stibor")));
    CONVERTERS.put(SWAP, new NoOpInstrumentProviderPopulator(SWAP));
    CONVERTERS.put(SWAP_28D, new SwapInstrumentProviderPopulator(SWAP_28D, new DefaultCsbcRenamingFunction("28d")));
    CONVERTERS.put(SWAP_3M, new SwapInstrumentProviderPopulator(SWAP_3M, new DefaultCsbcRenamingFunction("3m")));
    CONVERTERS.put(SWAP_6M, new SwapInstrumentProviderPopulator(SWAP_6M, new DefaultCsbcRenamingFunction("6m")));
    CONVERTERS.put(SWAP_12M, new SwapInstrumentProviderPopulator(SWAP_12M, new DefaultCsbcRenamingFunction("12m")));
    CONVERTERS.put(TENOR_SWAP, new BasisSwapInstrumentProviderPopulator(TENOR_SWAP));
  }

  public static Collection<CurveNodeIdMapper> convert(final String currency, final Map<String, CurveSpecificationBuilderConfiguration> configMap) {
    return convert(currency, configMap, CONVERTERS);
  }

  public static Collection<CurveNodeIdMapper> convert(final String currency, final Map<String, CurveSpecificationBuilderConfiguration> configMap,
      final Map<StripInstrumentType, InstrumentProviderPopulator> converters) {
    return null;
    //    final Map<String, CurveNodeIdMapper.Builder> convertedWithNames = new HashMap<>();
    //    for (final Map.Entry<String, CurveSpecificationBuilderConfiguration> convertedEntry : configMap.entrySet()) {
    //      final String originalName = convertedEntry.getKey();
    //      final CurveSpecificationBuilderConfiguration originalConfig = convertedEntry.getValue();
    //      for (final Map.Entry<StripInstrumentType, InstrumentProviderPopulator> entry : converters.entrySet()) {
    //        final InstrumentProviderPopulator converter = entry.getValue();
    //        final String remappedName = converter.rename(originalName, currency);
    //        final Builder remappedNameBuilder = convertedWithNames.get(remappedName);
    //        final Pair<String, CurveNodeIdMapper.Builder> pair;
    //        if (remappedNameBuilder != null) {
    //          pair = Pairs.of(remappedName, converter.apply(remappedNameBuilder.name(originalName).build(), originalConfig, currency).getSecond());
    //        } else {
    //          pair = converter.apply(CurveNodeIdMapper.builder().name(originalName).build(), originalConfig, currency);
    //        }
    //        convertedWithNames.put(remappedName, pair.getSecond());
    //      }
    //    }
    //    final Set<CurveNodeIdMapper> converted = new HashSet<>();
    //    for (final Map.Entry<String, CurveNodeIdMapper.Builder> entry : convertedWithNames.entrySet()) {
    //      final CurveNodeIdMapper idMapper = entry.getValue().build();
    //      if (idMapper.getAllTenors().size() != 0) {
    //        converted.add(idMapper);
    //      }
    //    }
    //    return converted;
  }

}
