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
 * Tests the class that adds FRA instrument providers to a {@link CurveNodeIdMapper}
 * from a {@link CurveSpecificationBuilderConfiguration}.
 *
 */
public class FraInstrumentProviderPopulatorTest {
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class for 3m FRA strips that uses the default renaming function */
  private static final InstrumentProviderPopulator FRA_3M_DEFAULT_PROVIDER = new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M);
  /** A converting class for 3m FRA strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator FRA_3M_RENAMING_PROVIDER;
  /** A converting class for 6m FRA strips that uses the default renaming function */
  private static final InstrumentProviderPopulator FRA_6M_DEFAULT_PROVIDER = new FraInstrumentProviderPopulator(StripInstrumentType.FRA_6M);
  /** A converting class for 6m FRA strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator FRA_6M_RENAMING_PROVIDER;

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
    FRA_3M_RENAMING_PROVIDER = new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, renamingFunction);
    FRA_6M_RENAMING_PROVIDER = new FraInstrumentProviderPopulator(StripInstrumentType.FRA_6M, renamingFunction);
  }

  /**
   * Tests the behaviour if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName1() {
    new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, null);
  }

  /**
   * Tests the behaviour if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M, null);
  }

  /**
   * Tests the behaviour if the strip instrument type is not mappable to FRA.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType1() {
    new FraInstrumentProviderPopulator(StripInstrumentType.CASH);
  }

  /**
   * Tests the behaviour if the strip instrument type is not mappable to FRA.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType2() {
    new FraInstrumentProviderPopulator(StripInstrumentType.CASH, new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(FRA_3M_DEFAULT_PROVIDER, new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M));
    assertEquals(FRA_3M_DEFAULT_PROVIDER.hashCode(), new FraInstrumentProviderPopulator(StripInstrumentType.FRA_3M).hashCode());
    assertNotEquals(FRA_3M_DEFAULT_PROVIDER, FRA_3M_RENAMING_PROVIDER);
    assertNotEquals(FRA_3M_RENAMING_PROVIDER, new FraInstrumentProviderPopulator(StripInstrumentType.FRA_6M));
  }

  /**
   * Tests that the FRA instrument provider is unpopulated if an empty curve specification builder configuration is supplied.
   */
  @Test
  public void testEmptyCurveSpecificationBuilderConfiguration() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null);
    assertNull(FRA_3M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(FRA_3M_RENAMING_PROVIDER.getInstrumentProviders(csbc));
    assertNull(FRA_6M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(FRA_6M_RENAMING_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the FRA instrument provider is unpopulated if a curve specification builder that has no FRA instrument providers
   * is supplied.
   */
  public void testNoFras() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(USD_DEPOSIT_INSTRUMENTS,
        null, null, USD_LIBOR_INSTRUMENTS, EURIBOR_INSTRUMENTS, CDOR_INSTRUMENTS, CIBOR_INSTRUMENTS, STIBOR_INSTRUMENTS,
        USD_STIR_FUTURE_INSTRUMENTS, USD_6M_SWAP_INSTRUMENTS, USD_3M_SWAP_INSTRUMENTS, USD_BASIS_SWAP_INSTRUMENTS, USD_TENOR_SWAP_INSTRUMENTS,
        USD_OIS_INSTRUMENTS, USD_SIMPLE_ZERO_INSTRUMENTS, USD_PERIODIC_ZERO_INSTRUMENTS, USD_CONTINUOUS_ZERO_INSTRUMENTS,
        USD_12M_SWAP_INSTRUMENTS, USD_28D_SWAP_INSTRUMENTS);
    assertNull(FRA_3M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(FRA_6M_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the FRA instrument provider is populated using the 3m FRA instrument providers from the curve specification builder
   * configuration.
   */
  @Test
  public void testFra3mStrips() {
    assertEquals(USD_3M_FRA_INSTRUMENTS, FRA_3M_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the FRA instrument provider is populated using the 6m FRA instrument providers from the curve specification builder
   * configuration.
   */
  @Test
  public void testFra6mStrips() {
    assertEquals(USD_6M_FRA_INSTRUMENTS, FRA_6M_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the 3m FRA instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForFra3m() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .fraNodeIds(USD_3M_FRA_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = FRA_3M_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getFRANodeIds());
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .fraNodeIds(USD_3M_FRA_INSTRUMENTS)
        .build();
    nameBuilderPair = FRA_3M_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getFRANodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the 6m FRA instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForFra6m() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .fraNodeIds(USD_6M_FRA_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = FRA_6M_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getFRANodeIds());
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .fraNodeIds(USD_6M_FRA_INSTRUMENTS)
        .build();
    nameBuilderPair = FRA_6M_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getFRANodeIds());
  }

  /**
   * Tests that an existing FRA mapper is over-written with values from the 3m FRA provider in the curve specification builder configuration
   * when the 3m FRA converter is used.
   */
  @Test
  public void testOverwriteWithFra3m() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithFra = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(USD_6M_FRA_INSTRUMENTS) // will be over-written with 3m FRA nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .fraNodeIds(USD_3M_FRA_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = FRA_3M_DEFAULT_PROVIDER.apply(mapperWithFra, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithFra.getFRANodeIds(), USD_6M_FRA_INSTRUMENTS);
  }

  /**
   * Tests that an existing FRA mapper is over-written with values from the 6m FRA provider in the curve specification builder configuration
   * when the 6m FRA converter is used.
   */
  @Test
  public void testOverwriteWithFra6m() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithFra = CurveNodeIdMapper.builder()
        .name(NAME)
        .fraNodeIds(USD_3M_FRA_INSTRUMENTS) // will be over-written with 6m FRA nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .fraNodeIds(USD_6M_FRA_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = FRA_6M_DEFAULT_PROVIDER.apply(mapperWithFra, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithFra.getFRANodeIds(), USD_3M_FRA_INSTRUMENTS);
  }
}
