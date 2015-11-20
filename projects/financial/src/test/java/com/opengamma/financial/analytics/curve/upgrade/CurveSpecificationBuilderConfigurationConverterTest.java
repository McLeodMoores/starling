/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;


/**
 *
 */
public class CurveSpecificationBuilderConfigurationConverterTest {
  //  /** The scheme for the identifiers */
  //  private static final String SCHEME = "Test";
  //  /** Basis swaps instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> BASIS_SWAPS = new HashMap<>();
  //  /** CDOR instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> CDORS = new HashMap<>();
  //  /** CIBOR instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> CIBORS = new HashMap<>();
  //  /** Continuous zero deposit instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> CONTINUOUS_ZERO_DEPOSITS = new HashMap<>();
  //  /** Deposit instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> DEPOSITS = new HashMap<>();
  //  /** Euribor instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> EURIBORS = new HashMap<>();
  //  /** 3m FRA instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> FRA_3MS = new HashMap<>();
  //  /** 6m FRA instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> FRA_6MS = new HashMap<>();
  //  /** IR future instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> FUTURES = new HashMap<>();
  //  /** LIBOR instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> LIBORS = new HashMap<>();
  //  /** OIS swap instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> OIS_SWAPS = new HashMap<>();
  //  /** Periodic zero deposit instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> PERIODIC_ZERO_DEPOSITS = new HashMap<>();
  //  /** Simple zero deposit instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> SIMPLE_ZERO_DEPOSITS = new HashMap<>();
  //  /** STIBOR instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> STIBORS = new HashMap<>();
  //  /** 28d reset swap instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> SWAP_28DS = new HashMap<>();
  //  /** 3m reset swap instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> SWAP_3MS = new HashMap<>();
  //  /** 6m reset swap instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> SWAP_6MS = new HashMap<>();
  //  /** 12m reset swap instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> SWAP_12MS = new HashMap<>();
  //  /** Tenor swap instrument providers */
  //  private static final Map<Tenor, CurveInstrumentProvider> TENOR_SWAPS = new HashMap<>();
  //
  //  static {
  //    FUTURES.put(Tenor.of(Period.ZERO), new SyntheticFutureCurveInstrumentProvider("US"));
  //    for (int i = 1; i < 31; i++) {
  //      BASIS_SWAPS.put(Tenor.ofYears(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.BASIS_SWAP, ExternalScheme.of(SCHEME)));
  //      FRA_3MS.put(Tenor.ofYears(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.FRA_3M, ExternalScheme.of(SCHEME)));
  //      FRA_6MS.put(Tenor.ofYears(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.FRA_6M, ExternalScheme.of(SCHEME)));
  //      OIS_SWAPS.put(Tenor.ofYears(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.OIS_SWAP, ExternalScheme.of(SCHEME)));
  //      SWAP_28DS.put(Tenor.ofYears(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.SWAP_28D, ExternalScheme.of(SCHEME)));
  //      SWAP_3MS.put(Tenor.ofYears(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.SWAP_3M, ExternalScheme.of(SCHEME)));
  //      SWAP_6MS.put(Tenor.ofYears(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.SWAP_6M, ExternalScheme.of(SCHEME)));
  //      SWAP_12MS.put(Tenor.ofYears(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.SWAP_12M, ExternalScheme.of(SCHEME)));
  //      TENOR_SWAPS.put(Tenor.ofYears(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.TENOR_SWAP,
  //          ExternalScheme.of(SCHEME)));
  //    }
  //    for (int i = 1; i < 12; i++) {
  //      CDORS.put(Tenor.ofMonths(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.CAD,
  //          StripInstrumentType.CDOR, ExternalScheme.of(SCHEME)));
  //      CIBORS.put(Tenor.ofMonths(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.DKK,
  //          StripInstrumentType.CIBOR, ExternalScheme.of(SCHEME)));
  //      CONTINUOUS_ZERO_DEPOSITS.put(Tenor.ofMonths(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.CONTINUOUS_ZERO_DEPOSIT, ExternalScheme.of(SCHEME)));
  //      DEPOSITS.put(Tenor.ofMonths(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.CASH, ExternalScheme.of(SCHEME)));
  //      EURIBORS.put(Tenor.ofMonths(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.EUR,
  //          StripInstrumentType.EURIBOR, ExternalScheme.of(SCHEME)));
  //      LIBORS.put(Tenor.ofMonths(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.LIBOR, ExternalScheme.of(SCHEME)));
  //      PERIODIC_ZERO_DEPOSITS.put(Tenor.ofMonths(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.PERIODIC_ZERO_DEPOSIT, ExternalScheme.of(SCHEME)));
  //      SIMPLE_ZERO_DEPOSITS.put(Tenor.ofMonths(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.USD,
  //          StripInstrumentType.SIMPLE_ZERO_DEPOSIT, ExternalScheme.of(SCHEME)));
  //      STIBORS.put(Tenor.ofMonths(i), new SyntheticIdentifierCurveInstrumentProvider(Currency.SEK,
  //          StripInstrumentType.STIBOR, ExternalScheme.of(SCHEME)));
  //    }
  //  }
  //
  //  /**
  //   * Tests that an empty {@link CurveSpecificationBuilderConfiguration} converts to an
  //   * empty {@link CurveNodeIdMapper}.
  //   */
  //  @Test
  //  public void testEmpty() {
  //    final String name = "EMPTY";
  //    final CurveSpecificationBuilderConfiguration originalConfig = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null,
  //        null, null, null, null, null, null, null, null, null, null, null, null, null);
  //    final Collection<CurveNodeIdMapper> newConfigs = CurveSpecificationBuilderConfigurationConverter.convert("USD",
  //        Collections.singletonMap(name, originalConfig));
  //    assertTrue(newConfigs.isEmpty());
  //  }
  //
  //  /**
  //   * Tests conversion of a simple curve specification builder configuration containing only
  //   * cash and OIS swap rates. In this case, the name should be appended with the currency
  //   * and the id mapper should contain entries for cash and OIS.
  //   */
  //  @Test
  //  public void testCashOisConfig() {
  //    final String originalName = "DEFAULT";
  //    final CurveSpecificationBuilderConfiguration originalConfig = new CurveSpecificationBuilderConfiguration(DEPOSITS, null, null, null, null,
  //        null, null, null, null, null, null, null, null, OIS_SWAPS, null, null, null, null, null);
  //    final String expectedName = "DEFAULT USD";
  //    final CurveNodeIdMapper expectedConfig = CurveNodeIdMapper.builder().name(expectedName)
  //        .cashNodeIds(DEPOSITS)
  //        .swapNodeIds(OIS_SWAPS)
  //        .build();
  //    final Collection<CurveNodeIdMapper> newConfigs =
  //        CurveSpecificationBuilderConfigurationConverter.convert("USD", Collections.singletonMap(originalName, originalConfig));
  //    assertEquals(1, newConfigs.size());
  //    assertEquals(expectedConfig, Iterables.getOnlyElement(newConfigs));
  //  }
  //
  //  /**
  //   * Tests conversion of a curve specification builder configuration with Euribor and
  //   * EUR Libor rates. Two curve node id mappers, one with "Euribor" and the other with
  //   * "Libor" appended to the name should be returned.
  //   */
  //  @Test
  //  public void testEuriborLiborConfig() {
  //    final String originalName = "DEFAULT";
  //    final CurveSpecificationBuilderConfiguration originalConfig = new CurveSpecificationBuilderConfiguration(null, null, null, LIBORS, EURIBORS,
  //        null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  //    final String expectedEuriborName = "DEFAULT EUR Euribor";
  //    final String expectedLiborName = "DEFAULT EUR Libor";
  //    final CurveNodeIdMapper expectedEuriborConfig = CurveNodeIdMapper.builder().name(expectedEuriborName)
  //        .cashNodeIds(EURIBORS)
  //        .build();
  //    final CurveNodeIdMapper expectedLiborConfig = CurveNodeIdMapper.builder().name(expectedLiborName)
  //        .cashNodeIds(LIBORS)
  //        .build();
  //    final Collection<CurveNodeIdMapper> newConfigs =
  //        CurveSpecificationBuilderConfigurationConverter.convert("EUR", Collections.singletonMap(originalName, originalConfig));
  //    assertEquals(2, newConfigs.size());
  //    int count = 0;
  //    for (final CurveNodeIdMapper config : newConfigs) {
  //      if (config.getName().equals(expectedEuriborName)) {
  //        assertEquals(expectedEuriborConfig, config);
  //        count++;
  //      } else if (config.getName().equals(expectedLiborName)) {
  //        assertEquals(expectedLiborConfig, config);
  //        count++;
  //      }
  //    }
  //    assertEquals(2, count);
  //  }
  //
  //  /**
  //   * Tests conversion of a curve specification builder configuration with 28d, 3m, 6m and
  //   * 12m strips. Four curve node id mappers of the form "{NAME} {PERIOD} {CURRENCY}" should
  //   * be returned.
  //   */
  //  @Test
  //  public void testSwapsConfig() {
  //    final String originalName = "DEFAULT";
  //    final CurveSpecificationBuilderConfiguration originalConfig = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null,
  //        null, null, null, SWAP_6MS, SWAP_3MS, null, null, null, null, null, null, SWAP_12MS, SWAP_28DS);
  //    final String expected28dName = "DEFAULT USD 28d";
  //    final String expected3mName = "DEFAULT USD 3m";
  //    final String expected6mName = "DEFAULT USD 6m";
  //    final String expected12mName = "DEFAULT USD 12m";
  //    final CurveNodeIdMapper expected28dConfig = CurveNodeIdMapper.builder().name(expected28dName)
  //        .swapNodeIds(SWAP_28DS)
  //        .build();
  //    final CurveNodeIdMapper expected3mConfig = CurveNodeIdMapper.builder().name(expected3mName)
  //        .swapNodeIds(SWAP_3MS)
  //        .build();
  //    final CurveNodeIdMapper expected6mConfig = CurveNodeIdMapper.builder().name(expected6mName)
  //        .swapNodeIds(SWAP_6MS)
  //        .build();
  //    final CurveNodeIdMapper expected12mConfig = CurveNodeIdMapper.builder().name(expected12mName)
  //        .swapNodeIds(SWAP_12MS)
  //        .build();
  //    final Collection<CurveNodeIdMapper> newConfigs =
  //        CurveSpecificationBuilderConfigurationConverter.convert("USD", Collections.singletonMap(originalName, originalConfig));
  //    assertEquals(4, newConfigs.size());
  //    int count = 0;
  //    for (final CurveNodeIdMapper config : newConfigs) {
  //      if (config.getName().equals(expected28dName)) {
  //        assertEquals(expected28dConfig, config);
  //        count++;
  //      } else if (config.getName().equals(expected3mName)) {
  //        assertEquals(expected3mConfig, config);
  //        count++;
  //      } else if (config.getName().equals(expected6mName)) {
  //        assertEquals(expected6mConfig, config);
  //        count++;
  //      } else if (config.getName().equals(expected12mName)) {
  //        assertEquals(expected12mConfig, config);
  //        count++;
  //      }
  //    }
  //    assertEquals(4, count);
  //  }
  //
  //  @Test
  //  public void testTwoCurveSetup() {
  //    final String originalName = "DEFAULT";
  //    final CurveSpecificationBuilderConfiguration originalConfig = new CurveSpecificationBuilderConfiguration(DEPOSITS, FRA_3MS, null, LIBORS, null,
  //        null, null, FUTURES, null, SWAP_6MS, null, null, OIS_SWAPS, null, null, null, null, null, null);
  //  }
}
