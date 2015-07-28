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
 * Tests the class that adds cash instrument providers to a {@link CurveNodeIdMapper}
 * from a {@link CurveSpecificationBuilderConfiguration}.
 *
 */
public class CashInstrumentProviderPopulatorTest {
  /** The name of the mapper */
  private static final String NAME = "Name";
  /** A mapper with no instrument mappings set */
  private static final CurveNodeIdMapper EMPTY_MAPPER;
  /** A converting class for cash strips that uses the default renaming function */
  private static final InstrumentProviderPopulator CASH_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CASH);
  /** A converting class for cash strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator CASH_RENAMING_PROVIDER;
  /** A converting class for CDOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator CDOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CDOR);
  /** A converting class for CDOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator CDOR_RENAMING_PROVIDER;
  /** A converting class for CIBOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator CIBOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CIBOR);
  /** A converting class for CIBOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator CIBOR_RENAMING_PROVIDER;
  /** A converting class for EURIBOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator EURIBOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.EURIBOR);
  /** A converting class for EURIBOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator EURIBOR_RENAMING_PROVIDER;
  /** A converting class for LIBOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator LIBOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.LIBOR);
  /** A converting class for LIBOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator LIBOR_RENAMING_PROVIDER;
  /** A converting class for STIBOR strips that uses the default renaming function */
  private static final InstrumentProviderPopulator STIBOR_DEFAULT_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.STIBOR);
  /** A converting class for STIBOR strips that uses a custom renaming function */
  private static final InstrumentProviderPopulator STIBOR_RENAMING_PROVIDER;

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
    CASH_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CASH, renamingFunction);
    CDOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CDOR, renamingFunction);
    CIBOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.CIBOR, renamingFunction);
    EURIBOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.EURIBOR, renamingFunction);
    LIBOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.LIBOR, renamingFunction);
    STIBOR_RENAMING_PROVIDER = new CashInstrumentProviderPopulator(StripInstrumentType.STIBOR, renamingFunction);
  }

  /**
   * Tests the behaviour if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullInstrumentProviderName1() {
    new CashInstrumentProviderPopulator(StripInstrumentType.CASH, null);
  }

  /**
   * Tests the behaviour if the renaming function is null.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullRenamingFunction() {
    new CashInstrumentProviderPopulator(StripInstrumentType.CASH, null);
  }

  /**
   * Tests the behaviour if the strip instrument type is not mappable to cash.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType1() {
    new CashInstrumentProviderPopulator(StripInstrumentType.BANKERS_ACCEPTANCE);
  }

  /**
   * Tests the behaviour if the strip instrument type is not mappable to cash.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testWrongStripType2() {
    new CashInstrumentProviderPopulator(StripInstrumentType.BANKERS_ACCEPTANCE, new DefaultCsbcRenamingFunction());
  }

  /**
   * Tests the hashCode and equals methods.
   */
  @Test
  public void testHashCodeEquals() {
    assertEquals(CASH_DEFAULT_PROVIDER, new CashInstrumentProviderPopulator(StripInstrumentType.CASH));
    assertEquals(CASH_DEFAULT_PROVIDER.hashCode(), new CashInstrumentProviderPopulator(StripInstrumentType.CASH).hashCode());
    assertNotEquals(CASH_DEFAULT_PROVIDER, new CashInstrumentProviderPopulator(StripInstrumentType.CDOR));
  }

  /**
   * Tests that the cash instrument provider is unpopulated if an empty curve specification builder configuration is supplied.
   */
  @Test
  public void testEmptyCurveSpecificationBuilderConfiguration() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(null, null, null, null, null, null,
        null, null, null, null, null, null, null, null, null, null, null, null, null);
    assertNull(CASH_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(CDOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(CIBOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(EURIBOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(LIBOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(STIBOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the cash instrument provider is unpopulated if a curve specification builder that has no cash instrument providers
   * is supplied.
   */
  public void testNoCash() {
    final CurveSpecificationBuilderConfiguration csbc = new CurveSpecificationBuilderConfiguration(null,
        USD_3M_FRA_INSTRUMENTS, USD_6M_FRA_INSTRUMENTS, null, null, null, null, null, USD_STIR_FUTURE_INSTRUMENTS, USD_6M_SWAP_INSTRUMENTS,
        USD_3M_SWAP_INSTRUMENTS, USD_BASIS_SWAP_INSTRUMENTS, USD_TENOR_SWAP_INSTRUMENTS, USD_OIS_INSTRUMENTS, USD_SIMPLE_ZERO_INSTRUMENTS,
        USD_PERIODIC_ZERO_INSTRUMENTS, USD_CONTINUOUS_ZERO_INSTRUMENTS, USD_12M_SWAP_INSTRUMENTS, USD_28D_SWAP_INSTRUMENTS);
    assertNull(CASH_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(CDOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(CIBOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(EURIBOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(LIBOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
    assertNull(STIBOR_DEFAULT_PROVIDER.getInstrumentProviders(csbc));
  }

  /**
   * Tests that the cash instrument provider is populated using the deposit instrument providers from the curve specification builder
   * configuration for a cash strip instrument converter.
   */
  @Test
  public void testCashStrips() {
    assertEquals(USD_DEPOSIT_INSTRUMENTS, CASH_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the cash instrument provider is populated using the CDOR instrument providers from the curve specification builder
   * configuration for a CDOR strip instrument converter.
   */
  @Test
  public void testCdorStrips() {
    assertEquals(CDOR_INSTRUMENTS, CDOR_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the cash instrument provider is populated using the Cibor instrument providers from the curve specification builder
   * configuration for a CDOR strip instrument converter.
   */
  @Test
  public void testCiborStrips() {
    assertEquals(CIBOR_INSTRUMENTS, CIBOR_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the cash instrument provider is populated using the Euribor instrument providers from the curve specification builder
   * configuration for a Euribor strip instrument converter.
   */
  @Test
  public void testEuriborStrips() {
    assertEquals(CDOR_INSTRUMENTS, CDOR_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the cash instrument provider is populated using the Libor instrument providers from the curve specification builder
   * configuration for a Libor strip instrument converter.
   */
  @Test
  public void testLiborStrips() {
    assertEquals(USD_LIBOR_INSTRUMENTS, LIBOR_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that the cash instrument provider is populated using the Stibor instrument providers from the curve specification builder
   * configuration for a Stibor strip instrument converter.
   */
  @Test
  public void testStiborStrips() {
    assertEquals(STIBOR_INSTRUMENTS, STIBOR_DEFAULT_PROVIDER.getInstrumentProviders(CSBC));
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the deposit instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForDeposit() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = CASH_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
    // test rename
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS)
        .build();
    nameBuilderPair = CASH_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the CDOR instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForCdor() {
    String expectedName = NAME + " CAD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(CDOR_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = CDOR_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "CAD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
    // test rename
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(CDOR_INSTRUMENTS)
        .build();
    nameBuilderPair = CDOR_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the Cibor instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForCibor() {
    String expectedName = NAME + " DKK";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(CIBOR_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = CIBOR_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "DKK");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
    // test rename
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(CIBOR_INSTRUMENTS)
        .build();
    nameBuilderPair = CIBOR_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the Euribor instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForEuribor() {
    String expectedName = NAME + " EUR";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(EURIBOR_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = EURIBOR_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "EUR");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
    // test rename
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(EURIBOR_INSTRUMENTS)
        .build();
    nameBuilderPair = EURIBOR_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the Libor instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForLibor() {
    String expectedName = NAME + " USD";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(USD_LIBOR_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = LIBOR_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
    // test rename
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(USD_LIBOR_INSTRUMENTS)
        .build();
    nameBuilderPair = LIBOR_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
  }

  /**
   * Tests that an empty mapper is populated with the correct instrument provider map, in this case the Stibor instruments,
   * and that the original mapper is unchanged.
   */
  @Test
  public void testBuilderCreationForStibor() {
    String expectedName = NAME + " SEK";
    CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(STIBOR_INSTRUMENTS)
        .build();
    Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = STIBOR_DEFAULT_PROVIDER.apply(EMPTY_MAPPER, CSBC, "SEK");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
    // test rename
    expectedName = NAME + " test";
    expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(STIBOR_INSTRUMENTS)
        .build();
    nameBuilderPair = STIBOR_RENAMING_PROVIDER.apply(EMPTY_MAPPER, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertNull(EMPTY_MAPPER.getCashNodeIds());
  }

  /**
   * Tests that an existing cash mapper is over-written with values from the CDOR provider in the curve specification builder configuration.
   */
  @Test
  public void testOverwriteWithCdor() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithDeposit = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS) // will be over-written with CDOR nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(CDOR_INSTRUMENTS)
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = CDOR_DEFAULT_PROVIDER.apply(mapperWithDeposit, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithDeposit.getCashNodeIds(), USD_DEPOSIT_INSTRUMENTS);
  }

  /**
   * Tests that an existing cash mapper is over-written with values from the CIBOR provider in the curve specification builder configuration.
   */
  @Test
  public void testOverwriteWithCibor() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithDeposit = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS) // will be over-written with CIBOR nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(CIBOR_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = CIBOR_DEFAULT_PROVIDER.apply(mapperWithDeposit, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithDeposit.getCashNodeIds(), USD_DEPOSIT_INSTRUMENTS);
  }

  /**
   * Tests that an existing cash mapper is over-written with values from the Euribor provider in the curve specification builder configuration.
   */
  @Test
  public void testOverwriteWithEuribor() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithDeposit = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS) // will be over-written with Euribor nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(EURIBOR_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = EURIBOR_DEFAULT_PROVIDER.apply(mapperWithDeposit, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithDeposit.getCashNodeIds(), USD_DEPOSIT_INSTRUMENTS);
  }

  /**
   * Tests that an existing cash mapper is over-written with values from the Libor providers in the curve specification builder configuration.
   */
  @Test
  public void testOverwriteWithLibor() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithDeposit = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS) // will be over-written with Libor nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(USD_LIBOR_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = LIBOR_DEFAULT_PROVIDER.apply(mapperWithDeposit, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithDeposit.getCashNodeIds(), USD_DEPOSIT_INSTRUMENTS);
  }

  /**
   * Tests that an existing cash mapper is over-written with values from the Stibor provider in the curve specification builder configuration.
   */
  @Test
  public void testOverwriteWithStibor() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithDeposit = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS) // will be over-written with Stibor nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(STIBOR_INSTRUMENTS)
        .build();
    final Pair<String, Builder> nameBuilderPair = STIBOR_DEFAULT_PROVIDER.apply(mapperWithDeposit, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithDeposit.getCashNodeIds(), USD_DEPOSIT_INSTRUMENTS);
  }

  /**
   * Tests that an existing cash mapper is over-written with values from the deposit providers in the curve specification builder configuration.
   */
  @Test
  public void testOverwriteTenorSwapNodes() {
    final String expectedName = NAME + " USD";
    final CurveNodeIdMapper mapperWithDeposit = CurveNodeIdMapper.builder()
        .name(NAME)
        .cashNodeIds(USD_LIBOR_INSTRUMENTS) // will be over-written with deposit nodes
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS)
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = CASH_DEFAULT_PROVIDER.apply(mapperWithDeposit, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
    assertEquals(mapperWithDeposit.getCashNodeIds(), USD_LIBOR_INSTRUMENTS);
  }

}
