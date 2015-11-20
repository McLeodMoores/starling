/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

/**
 * Unit tests for {@link ReflectionInstrumentProviderPopulator}.
 */
public class ReflectionInstrumentProviderPopulatorTest {

  //  /**
  //   * Tests the exception thrown when the method that is to be called in the
  //   * {@link CurveSpecificationBuilderConfiguration} does not exist.
  //   */
  //  @Test(expectedExceptions = RuntimeException.class)
  //  public void testWrongSourceMethodNameForConverter() {
  //    final Map<StripInstrumentType, InstrumentProviderPopulator> converters = new EnumMap<>(StripInstrumentType.class);
  //    converters.put(StripInstrumentType.CASH, new ReflectionInstrumentProviderPopulator(StripInstrumentType.CASH, "getDepositInstrumentIds", "getCashNodeIds",
  //        "cashNodeIds", new DefaultCsbcRenamingFunction()));
  //    final String name = "DEFAULT";
  //    final Map<Tenor, CurveInstrumentProvider> map = new HashMap<>();
  //    final CurveSpecificationBuilderConfiguration originalConfig = new CurveSpecificationBuilderConfiguration(map, null, null,
  //        null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
  //    CurveSpecificationBuilderConfigurationConverter.convert("USD", Collections.singletonMap(name, originalConfig), converters);
  //  }

  /*  @Test(expectedExceptions = RuntimeException.class)
  public void testWrongDestinationMethodNameForConverter() {
    final Map<StripInstrumentType, InstrumentProviderPopulator> converters = new EnumMap<>(StripInstrumentType.class);
    converters.put(StripInstrumentType.CASH, new ReflectionInstrumentProviderPopulator(StripInstrumentType.CASH,
        "getCashInstrumentProviders", "swapNodeIds", new DefaultCsbcRenamingFunction()));
    final String name = "DEFAULT";
    final Map<Tenor, CurveInstrumentProvider> map = new HashMap<>();
    final CurveSpecificationBuilderConfiguration originalConfig = new CurveSpecificationBuilderConfiguration(null,
        null, null, null, null, null, map, null, null, null, null, null, null, null, null, null, null, null, null);
    CurveSpecificationBuilderConfigurationConverter.convert("USD", Collections.singletonMap(name, originalConfig), converters);
  }
   */
}
