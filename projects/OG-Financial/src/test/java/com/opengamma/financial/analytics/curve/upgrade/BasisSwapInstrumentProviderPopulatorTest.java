/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.analytics.curve.upgrade;

import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.CDOR_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.CIBOR_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.CSBC;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.EURIBOR_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.STIBOR_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_12M_SWAP_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_28D_SWAP_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_3M_FRA_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_3M_SWAP_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_6M_FRA_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_6M_SWAP_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_BASIS_SWAP_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_CONTINUOUS_ZERO_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_DEPOSIT_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_LIBOR_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_OIS_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_PERIODIC_ZERO_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_SIMPLE_ZERO_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_STIR_FUTURE_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_TENOR_SWAP_INSTRUMENTS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertNull;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.result.Function2;
import com.opengamma.util.tuple.Pair;

/**
 * Unit tests for {@link BasisSwapInstrumentProviderPopulator}.
 */
public class BasisSwapInstrumentProviderPopulatorTest {
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class for basis swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator BASIS_SWAP_DEFAULT_PROVIDER = new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP);
  /** A converting class for basis swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator BASIS_SWAP_RENAMING_PROVIDER;
  /** A converting class for tenor swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator TENOR_SWAP_DEFAULT_PROVIDER = new BasisSwapInstrumentProviderPopulator(StripInstrumentType.TENOR_SWAP);
  /** A converting class for tenor swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator TENOR_SWAP_RENAMING_PROVIDER;
  /** The currency */
  private static final String CURRENCY = "USD";
  static {
    EMPTY_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .build();
    final Function2<String, String, String> renamingFunction = new Function2<String, String, String>() {

      @Override
      public String apply(final String name, final String currency) {
        return name + " test";
      }

    };
    BASIS_SWAP_RENAMING_PROVIDER = new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP, renamingFunction);
    TENOR_SWAP_RENAMING_PROVIDER = new BasisSwapInstrumentProviderPopulator(StripInstrumentType.TENOR_SWAP, renamingFunction);
  }

  /**
   * Tests the behaviour if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP, null);
  }

  /**
   * Tests the behaviour if the strip instrument type is neither basis nor tenor swap.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType1() {
    new BasisSwapInstrumentProviderPopulator(StripInstrumentType.CASH);
  }

  /**
   * Tests the behaviour if the strip instrument type is neither basis nor tenor swap.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType2() {
    new BasisSwapInstrumentProviderPopulator(StripInstrumentType.CASH, new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(BASIS_SWAP_DEFAULT_PROVIDER, new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP));
    assertEquals(BASIS_SWAP_DEFAULT_PROVIDER.hashCode(), new BasisSwapInstrumentProviderPopulator(StripInstrumentType.BASIS_SWAP).hashCode());
    assertNotEquals(BASIS_SWAP_RENAMING_PROVIDER, new BasisSwapInstrumentProviderPopulator(StripInstrumentType.TENOR_SWAP));
  }

  /**
   * Tests that the swap instrument provider is unpopulated if an empty curve specification builder configuration is supplied.
   */
  @Test
  public void testEmptyCurveSpecificationBuilderConfiguration() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null);
    assertNull(BASIS_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(TENOR_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the swap instrument provider is unpopulated if a curve specification builder configuration that has no tenor or basis
   * swap instrument providers is supplied.
   */
  @Test
  public void testNoTenorOrBasisSwaps() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(USD_DEPOSIT_INSTRUMENTS,
        USD_3M_FRA_INSTRUMENTS, USD_6M_FRA_INSTRUMENTS, USD_LIBOR_INSTRUMENTS, EURIBOR_INSTRUMENTS, CDOR_INSTRUMENTS, CIBOR_INSTRUMENTS,
        STIBOR_INSTRUMENTS, USD_STIR_FUTURE_INSTRUMENTS, USD_6M_SWAP_INSTRUMENTS, USD_3M_SWAP_INSTRUMENTS, null, null, USD_OIS_INSTRUMENTS,
        USD_SIMPLE_ZERO_INSTRUMENTS, USD_PERIODIC_ZERO_INSTRUMENTS, USD_CONTINUOUS_ZERO_INSTRUMENTS, USD_12M_SWAP_INSTRUMENTS,
        USD_28D_SWAP_INSTRUMENTS);
    assertNull(BASIS_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(TENOR_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the swap instrument provider is populated using the basis swap providers from the curve specification builder
   * configuration for a basis swap strip instrument converter.
   */
  @Test
  public void testBasisSwapStrips() {
    assertEquals(USD_BASIS_SWAP_INSTRUMENTS, BASIS_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the swap instrument provider is populated using the tenor swap providers from the curve specification builder
   * configuration for a tenor swap strip instrument converter.
   */
  @Test
  public void testTenorSwapStrips() {
    assertEquals(USD_TENOR_SWAP_INSTRUMENTS, TENOR_SWAP_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForBasisSwap() {
    final String expectedName = NAME + " " + CURRENCY;
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_BASIS_SWAP_INSTRUMENTS)
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = BASIS_SWAP_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, CURRENCY);
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map and that the original mapper is unchanged
   * and that the renaming function is used to create the mapper name.
   */
  @Test
  public void testBuilderCreationForBasisSwapWithRename() {
    final String expectedName = NAME + " test";
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_BASIS_SWAP_INSTRUMENTS)
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = BASIS_SWAP_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, CURRENCY);
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForTenorSwap() {
    final String expectedName = NAME + " " + CURRENCY;
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_TENOR_SWAP_INSTRUMENTS)
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = TENOR_SWAP_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, CURRENCY);
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map and that the original mapper is unchanged
   * and that the renaming function is used to create the mapper name.
   */
  @Test
  public void testBuilderCreationForTenorSwapWithRename() {
    final String expectedName = NAME + " test";
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_TENOR_SWAP_INSTRUMENTS)
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = TENOR_SWAP_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, CURRENCY);
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
  }

  /**
   * Tests that an existing swap mapper is over-written with values from the basis swap providers in the curve specification builder configuration.
   */
  @Test
  public void testOverwriteTenorSwapNodes() {
    final String expectedName = NAME + " " + CURRENCY;
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_BASIS_SWAP_INSTRUMENTS)
        .build();
    final CurveNodeIdMapper mapperWithTenorSwaps = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(USD_TENOR_SWAP_INSTRUMENTS) // will be over-written with basis swap nodes
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = BASIS_SWAP_DEFAULT_PROVIDER.apply(mapperWithTenorSwaps, CSBC, CURRENCY);
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithTenorSwaps.getSwapNodeIds(), USD_TENOR_SWAP_INSTRUMENTS);
  }

  /**
   * Tests that an existing swap mapper is over-written with values from the tenor swap providers in the curve specification builder configuration.
   */
  @Test
  public void testOverwriteBasisSwapNodes() {
    final String expectedName = NAME + " " + CURRENCY;
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_TENOR_SWAP_INSTRUMENTS)
        .build();
    final CurveNodeIdMapper mapperWithBasisSwaps = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(USD_BASIS_SWAP_INSTRUMENTS) // will be over-written with tenor swap nodes
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = TENOR_SWAP_DEFAULT_PROVIDER.apply(mapperWithBasisSwaps, CSBC, CURRENCY);
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithBasisSwaps.getSwapNodeIds(), USD_BASIS_SWAP_INSTRUMENTS);
  }
}
