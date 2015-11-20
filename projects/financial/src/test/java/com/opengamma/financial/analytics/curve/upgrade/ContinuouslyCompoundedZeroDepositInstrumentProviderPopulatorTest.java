/**
 *
 */
package com.opengamma.financial.analytics.curve.upgrade;

import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.CDOR_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.CIBOR_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.CSBC;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.EURIBOR_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.EUR_CONTINUOUS_ZERO_INSTRUMENTS;
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
import com.opengamma.util.result.Function2;
import com.opengamma.util.tuple.Pair;

/**
 * Tests the class that adds continuously-compounded zero deposit instrument providers to a {@link CurveNodeIdMapper} from
 * a {@link CurveSpecificationBuilderConfiguration}.
 *
 */
public class ContinuouslyCompoundedZeroDepositInstrumentProviderPopulatorTest {
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class that uses the default renaming function */
  private static final InstrumentProviderPopulator DEFAULT_PROVIDER = new ContinuouslyCompoundedRateInstrumentProviderPopulator();
  /** A converting class that uses a custom renaming function */
  private static final InstrumentProviderPopulator RENAMING_PROVIDER;

  static {
    EMPTY_MAPPER = CurveNodeIdMapper.builder()
        .name(NAME)
        .build();
    final Function2<String, String, String> renamingFunction = new Function2<String, String, String>() {

      @Override
      public String apply(final String name, final String currency) {
        return name + " " + currency + " test";
      }

    };
    RENAMING_PROVIDER = new ContinuouslyCompoundedRateInstrumentProviderPopulator(renamingFunction);
  }

  /**
   * Tests that the correct exception is thrown if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new ContinuouslyCompoundedRateInstrumentProviderPopulator(null);
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(DEFAULT_PROVIDER, new ContinuouslyCompoundedRateInstrumentProviderPopulator());
    assertEquals(DEFAULT_PROVIDER.hashCode(), new ContinuouslyCompoundedRateInstrumentProviderPopulator().hashCode());
    assertNotEquals(RENAMING_PROVIDER, new ContinuouslyCompoundedRateInstrumentProviderPopulator());
  }

  /**
   * Tests that the continuous zero rate instrument provider is unpopulated if an empty curve specification builder configuration is supplied.
   */
  @Test
  public void testEmptyCurveSpecificationBuilderConfiguration() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null);
    assertNull(DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(RENAMING_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the continuous rate instrument provider is unpopulated if a curve specification builder that has no continuous zero deposit instrument
   * providers is supplied.
   */
  public void testNoContinuousZeroDeposits() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(USD_DEPOSIT_INSTRUMENTS,
        USD_3M_FRA_INSTRUMENTS, USD_6M_FRA_INSTRUMENTS, USD_LIBOR_INSTRUMENTS, EURIBOR_INSTRUMENTS, CDOR_INSTRUMENTS, CIBOR_INSTRUMENTS,
        STIBOR_INSTRUMENTS, USD_STIR_FUTURE_INSTRUMENTS, USD_6M_SWAP_INSTRUMENTS, USD_3M_SWAP_INSTRUMENTS, USD_BASIS_SWAP_INSTRUMENTS,
        USD_TENOR_SWAP_INSTRUMENTS, USD_OIS_INSTRUMENTS, USD_SIMPLE_ZERO_INSTRUMENTS, USD_PERIODIC_ZERO_INSTRUMENTS, null, USD_12M_SWAP_INSTRUMENTS,
        USD_28D_SWAP_INSTRUMENTS);
    assertNull(DEFAULT_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the continuous rate instrument provider is populated using the continuous zero instrument providers from the curve specification builder
   * configuration.
   */
  @Test
  public void testContinuousZeroDepositStrips() {
    assertEquals(USD_CONTINUOUS_ZERO_INSTRUMENTS, DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the continuous zero deposit instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreation() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .continuouslyCompoundedRateNodeIds(USD_CONTINUOUS_ZERO_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getContinuouslyCompoundedRateNodeIds());
    // test rename
    expectedName = NAME + " USD test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .continuouslyCompoundedRateNodeIds(USD_CONTINUOUS_ZERO_INSTRUMENTS)
        .build();
    nameBuilderPair = RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getContinuouslyCompoundedRateNodeIds());
  }

  /**
   * Tests that an existing continuous rate mapper is over-written.
   */
  @Test
  public void testOverwrite() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithExistingNodes = CurveNodeIdMapper.builder()
        .name(NAME)
        .periodicallyCompoundedRateNodeIds(USD_PERIODIC_ZERO_INSTRUMENTS) // will be copied
        .continuouslyCompoundedRateNodeIds(EUR_CONTINUOUS_ZERO_INSTRUMENTS) // will be over-written with USD nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .periodicallyCompoundedRateNodeIds(USD_PERIODIC_ZERO_INSTRUMENTS)
        .continuouslyCompoundedRateNodeIds(USD_CONTINUOUS_ZERO_INSTRUMENTS)
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = DEFAULT_PROVIDER.apply(mapperWithExistingNodes, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithExistingNodes.getContinuouslyCompoundedRateNodeIds(), EUR_CONTINUOUS_ZERO_INSTRUMENTS);
  }
}
