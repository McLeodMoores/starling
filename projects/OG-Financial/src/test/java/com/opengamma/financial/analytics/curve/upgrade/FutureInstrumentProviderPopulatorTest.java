/**
 *
 */
package com.opengamma.financial.analytics.curve.upgrade;

import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.BA_FUTURE_INSTRUMENTS;
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
 * Tests the class that adds rate future instrument providers to a {@link CurveNodeIdMapper} from
 * a {@link CurveSpecificationBuilderConfiguration}.
 */
public class FutureInstrumentProviderPopulatorTest {
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class for STIR future strips that uses the default renaming function */
  private static final InstrumentProviderPopulator FUTURE_DEFAULT_PROVIDER = new FutureInstrumentProviderPopulator(StripInstrumentType.FUTURE);
  /** A converting class for STIR future strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator FUTURE_RENAMING_PROVIDER;
  /** A converting class for BA future strips that uses the default renaming function */
  private static final InstrumentProviderPopulator BA_DEFAULT_PROVIDER = new FutureInstrumentProviderPopulator(StripInstrumentType.BANKERS_ACCEPTANCE);
  /** A converting class for BA future strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator BA_RENAMING_PROVIDER;

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
    FUTURE_RENAMING_PROVIDER = new FutureInstrumentProviderPopulator(StripInstrumentType.FUTURE, renamingFunction);
    BA_RENAMING_PROVIDER = new FutureInstrumentProviderPopulator(StripInstrumentType.BANKERS_ACCEPTANCE, renamingFunction);
  }

  /**
   * Tests the behaviour if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName1() {
    new FutureInstrumentProviderPopulator(StripInstrumentType.FUTURE, null);
  }

  /**
   * Tests the behaviour if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new FutureInstrumentProviderPopulator(StripInstrumentType.FUTURE, null);
  }

  /**
   * Tests the behaviour if the strip instrument type is not mappable to a rate future.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType1() {
    new FutureInstrumentProviderPopulator(StripInstrumentType.CASH);
  }

  /**
   * Tests the behaviour if the strip instrument type is not mappable to a rate future.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType2() {
    new FutureInstrumentProviderPopulator(StripInstrumentType.CASH, new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(FUTURE_DEFAULT_PROVIDER, new FutureInstrumentProviderPopulator(StripInstrumentType.FUTURE));
    assertEquals(FUTURE_DEFAULT_PROVIDER.hashCode(), new FutureInstrumentProviderPopulator(StripInstrumentType.FUTURE).hashCode());
    assertNotEquals(FUTURE_DEFAULT_PROVIDER, new FutureInstrumentProviderPopulator(StripInstrumentType.BANKERS_ACCEPTANCE));
    assertNotEquals(FUTURE_RENAMING_PROVIDER, new FutureInstrumentProviderPopulator(StripInstrumentType.FUTURE));
  }

  /**
   * Tests that the rate future instrument provider is unpopulated if an empty curve specification builder configuration is supplied.
   */
  @Test
  public void testEmptyCurveSpecificationBuilderConfiguration() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null);
    assertNull(FUTURE_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(FUTURE_RENAMING_PROVIDER.getInstrumentProviders(csbc));
    assertNull(BA_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(BA_RENAMING_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the rate future instrument provider is unpopulated if a curve specification builder that has no rate instrument providers
   * is supplied.
   */
  public void testNoFras() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(USD_DEPOSIT_INSTRUMENTS,
        USD_3M_FRA_INSTRUMENTS, USD_6M_FRA_INSTRUMENTS, USD_LIBOR_INSTRUMENTS, EURIBOR_INSTRUMENTS, CDOR_INSTRUMENTS, CIBOR_INSTRUMENTS, STIBOR_INSTRUMENTS,
        null, USD_6M_SWAP_INSTRUMENTS, USD_3M_SWAP_INSTRUMENTS, USD_BASIS_SWAP_INSTRUMENTS, USD_TENOR_SWAP_INSTRUMENTS,
        USD_OIS_INSTRUMENTS, USD_SIMPLE_ZERO_INSTRUMENTS, USD_PERIODIC_ZERO_INSTRUMENTS, USD_CONTINUOUS_ZERO_INSTRUMENTS,
        USD_12M_SWAP_INSTRUMENTS, USD_28D_SWAP_INSTRUMENTS);
    assertNull(FUTURE_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(BA_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the rate future instrument provider is populated using the future instrument providers from the curve specification builder
   * configuration.
   */
  @Test
  public void testFutureStrips() {
    assertEquals(USD_STIR_FUTURE_INSTRUMENTS, FUTURE_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the future instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreation() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .rateFutureNodeIds(USD_STIR_FUTURE_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = FUTURE_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getRateFutureNodeIds());
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .rateFutureNodeIds(USD_STIR_FUTURE_INSTRUMENTS)
        .build();
    nameBuilderPair = FUTURE_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getRateFutureNodeIds());
  }

  /**
   * Tests that an existing rate future mapper is over-written with values from the 3m FRA provider in the curve specification builder configuration
   * when the 3m FRA converter is used.
   */
  @Test
  public void testOverwriteWithFra3m() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithFra = CurveNodeIdMapper.builder()
        .name(NAME)
        .rateFutureNodeIds(BA_FUTURE_INSTRUMENTS) // will be over-written with STIR nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .rateFutureNodeIds(USD_STIR_FUTURE_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = FUTURE_DEFAULT_PROVIDER.apply(mapperWithFra, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithFra.getRateFutureNodeIds(), BA_FUTURE_INSTRUMENTS);
  }

}
