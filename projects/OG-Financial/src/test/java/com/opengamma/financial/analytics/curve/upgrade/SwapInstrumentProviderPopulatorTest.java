/**
 *
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
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper.Builder;
import com.opengamma.financial.analytics.ircurve.CurveSpecificationBuilderConfiguration;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.result.Function2;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the class that adds swap instrument providers to a {@link CurveNodeIdMapper}
 * from a {@link CurveSpecificationBuilderConfiguration}.
 */
public class SwapInstrumentProviderPopulatorTest {
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class for 12m swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator SWAP_12M_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_12M);
  /** A converting class for 12m swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator SWAP_12M_RENAMING_PROVIDER;
  /** A converting class for 28d swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator SWAP_28D_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_28D);
  /** A converting class for 28d swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator SWAP_28D_RENAMING_PROVIDER;
  /** A converting class for 3m swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator SWAP_3M_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M);
  /** A converting class for 3m swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator SWAP_3M_RENAMING_PROVIDER;
  /** A converting class for 6m swap strips that uses the default renaming function */
  private static final InstrumentProviderPopulator SWAP_6M_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_6M);
  /** A converting class for 6m swap strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator SWAP_6M_RENAMING_PROVIDER;
  /** A converting class for OIS strips that uses the default renaming function */
  private static final InstrumentProviderPopulator OIS_DEFAULT_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.OIS_SWAP);
  /** A converting class for OIS strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator OIS_RENAMING_PROVIDER;

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
    SWAP_12M_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_12M, renamingFunction);
    SWAP_28D_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_28D, renamingFunction);
    SWAP_3M_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, renamingFunction);
    SWAP_6M_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_6M, renamingFunction);
    OIS_RENAMING_PROVIDER = new SwapInstrumentProviderPopulator(StripInstrumentType.OIS_SWAP, renamingFunction);
  }

  /**
   * Tests the behaviour if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName1() {
    new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, null);
  }

  /**
   * Tests the behaviour if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M, null);
  }

  /**
   * Tests the behaviour if the strip instrument type is not mappable to swap.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType1() {
    new SwapInstrumentProviderPopulator(StripInstrumentType.CASH);
  }

  /**
   * Tests the behaviour if the strip instrument type is not mappable to swap.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType2() {
    new SwapInstrumentProviderPopulator(StripInstrumentType.CASH, new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(SWAP_3M_DEFAULT_PROVIDER, new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M));
    assertEquals(SWAP_3M_DEFAULT_PROVIDER.hashCode(), new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_3M).hashCode());
    assertNotEquals(SWAP_3M_DEFAULT_PROVIDER, SWAP_3M_RENAMING_PROVIDER);
    assertNotEquals(SWAP_3M_RENAMING_PROVIDER, new SwapInstrumentProviderPopulator(StripInstrumentType.SWAP_6M));
  }

  /**
   * Tests that the swap instrument provider is unpopulated if an empty curve specification builder configuration is supplied.
   */
  @Test
  public void testEmptyCurveSpecificationBuilderConfiguration() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null);
    assertNull(SWAP_12M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_12M_RENAMING_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_28D_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_28D_RENAMING_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_3M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_3M_RENAMING_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_6M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_6M_RENAMING_PROVIDER.getInstrumentProviders(csbc));
    assertNull(OIS_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(OIS_RENAMING_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the swap instrument provider is unpopulated if a curve specification builder that has no swap instrument providers
   * is supplied.
   */
  public void testNoSwaps() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(USD_DEPOSIT_INSTRUMENTS,
        USD_3M_FRA_INSTRUMENTS, USD_6M_FRA_INSTRUMENTS, USD_LIBOR_INSTRUMENTS, EURIBOR_INSTRUMENTS, CDOR_INSTRUMENTS, CIBOR_INSTRUMENTS, STIBOR_INSTRUMENTS,
        USD_STIR_FUTURE_INSTRUMENTS, null, null, USD_BASIS_SWAP_INSTRUMENTS, USD_TENOR_SWAP_INSTRUMENTS,
        null, USD_SIMPLE_ZERO_INSTRUMENTS, USD_PERIODIC_ZERO_INSTRUMENTS, USD_CONTINUOUS_ZERO_INSTRUMENTS,
        null, null);
    assertNull(SWAP_12M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_28D_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_3M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(SWAP_6M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(OIS_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the swap instrument provider is populated using the 12m swap instrument providers from the curve specification builder
   * configuration.
   */
  @Test
  public void testSwap12mStrips() {
    assertEquals(USD_12M_SWAP_INSTRUMENTS, SWAP_12M_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the swap instrument provider is populated using the 28d swap instrument providers from the curve specification builder
   * configuration.
   */
  @Test
  public void testSwap28dStrips() {
    assertEquals(USD_28D_SWAP_INSTRUMENTS, SWAP_28D_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the swap instrument provider is populated using the 3m swap instrument providers from the curve specification builder
   * configuration.
   */
  @Test
  public void testSwap3mStrips() {
    assertEquals(USD_3M_SWAP_INSTRUMENTS, SWAP_3M_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the swap instrument provider is populated using the 6m swap instrument providers from the curve specification builder
   * configuration.
   */
  @Test
  public void testSwap6mStrips() {
    assertEquals(USD_6M_SWAP_INSTRUMENTS, SWAP_6M_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the swap instrument provider is populated using the OIS instrument providers from the curve specification builder
   * configuration.
   */
  @Test
  public void testOisStrips() {
    assertEquals(USD_OIS_INSTRUMENTS, OIS_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the 12m swap instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForSwap12m() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_12M_SWAP_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = SWAP_12M_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_12M_SWAP_INSTRUMENTS)
        .build();
    nameBuilderPair = SWAP_12M_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the 3m swap instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForSwap12dm() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_28D_SWAP_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = SWAP_28D_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_28D_SWAP_INSTRUMENTS)
        .build();
    nameBuilderPair = SWAP_28D_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the 3m swap instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForSwap3m() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_3M_SWAP_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = SWAP_3M_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_3M_SWAP_INSTRUMENTS)
        .build();
    nameBuilderPair = SWAP_3M_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the 6m swap instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForSwap6m() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_6M_SWAP_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = SWAP_6M_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_6M_SWAP_INSTRUMENTS)
        .build();
    nameBuilderPair = SWAP_6M_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the OIS instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForOis() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_OIS_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = OIS_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_OIS_INSTRUMENTS)
        .build();
    nameBuilderPair = OIS_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getSwapNodeIds());
  }

  /**
   * Tests that an existing swap mapper is over-written with values from the 12m swap provider in the curve specification builder configuration
   * when the 12m swap converter is used.
   */
  @Test
  public void testOverwriteWithSwap12m() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithSwap = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(USD_6M_SWAP_INSTRUMENTS) // will be over-written with 12m swap nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_12M_SWAP_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = SWAP_12M_DEFAULT_PROVIDER.apply(mapperWithSwap, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithSwap.getSwapNodeIds(), USD_6M_SWAP_INSTRUMENTS);
  }

  /**
   * Tests that an existing swap mapper is over-written with values from the 28d swap provider in the curve specification builder configuration
   * when the 28d swap converter is used.
   */
  @Test
  public void testOverwriteWithSwap28d() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithSwap = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(USD_6M_SWAP_INSTRUMENTS) // will be over-written with 28d swap nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_28D_SWAP_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = SWAP_28D_DEFAULT_PROVIDER.apply(mapperWithSwap, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithSwap.getSwapNodeIds(), USD_6M_SWAP_INSTRUMENTS);
  }

  /**
   * Tests that an existing swap mapper is over-written with values from the 3m swap provider in the curve specification builder configuration
   * when the 3m swap converter is used.
   */
  @Test
  public void testOverwriteWithSwap3m() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithSwap = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(USD_6M_SWAP_INSTRUMENTS) // will be over-written with 3m swap nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_3M_SWAP_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = SWAP_3M_DEFAULT_PROVIDER.apply(mapperWithSwap, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithSwap.getSwapNodeIds(), USD_6M_SWAP_INSTRUMENTS);
  }

  /**
   * Tests that an existing swap mapper is over-written with values from the 6m swap provider in the curve specification builder configuration
   * when the 6m swap converter is used.
   */
  @Test
  public void testOverwriteWithSwap6m() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithSwap = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(USD_3M_SWAP_INSTRUMENTS) // will be over-written with 6m swap nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_6M_SWAP_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = SWAP_6M_DEFAULT_PROVIDER.apply(mapperWithSwap, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithSwap.getSwapNodeIds(), USD_3M_SWAP_INSTRUMENTS);
  }

  /**
   * Tests that an existing swap mapper is over-written with values from the OIS provider in the curve specification builder configuration
   * when the OIS converter is used.
   */
  @Test
  public void testOverwriteWithOis() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithSwap = CurveNodeIdMapper.builder()
        .name(NAME)
        .swapNodeIds(USD_3M_SWAP_INSTRUMENTS) // will be over-written with OIS nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .swapNodeIds(USD_OIS_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = OIS_DEFAULT_PROVIDER.apply(mapperWithSwap, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithSwap.getSwapNodeIds(), USD_3M_SWAP_INSTRUMENTS);
  }
}
