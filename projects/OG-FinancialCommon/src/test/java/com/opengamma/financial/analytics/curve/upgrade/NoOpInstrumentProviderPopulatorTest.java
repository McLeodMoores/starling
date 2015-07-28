/**
 *
 */
package com.opengamma.financial.analytics.curve.upgrade;

import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.CSBC;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_3M_FRA_INSTRUMENTS;
import static com.opengamma.financial.analytics.curve.upgrade.CurveUpgradeTestUtils.USD_DEPOSIT_INSTRUMENTS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pair;

/**
 * Unit tests for {@link NoOpInstrumentProviderPopulator}.
 */
public class NoOpInstrumentProviderPopulatorTest {
  /** The no-op instrument provider populator */
  private static final InstrumentProviderPopulator NO_OP = new NoOpInstrumentProviderPopulator(StripInstrumentType.BANKERS_ACCEPTANCE);

  /**
   * Tests that the correct exception is thrown for a null strip instrument type.
   */
  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testNullStripInstrumentType() {
    new NoOpInstrumentProviderPopulator(null);
  }

  /**
   * Tests that an empty map is returned.
   */
  @Test
  public void testGetInstrumentProviders() {
    final Map<Tenor, CurveInstrumentProvider> map = NO_OP.getInstrumentProviders(null);
    assertNotNull(map);
    assertTrue(map.isEmpty());
  }

  /**
   * Tests that no providers are added to the curve node id mapper.
   */
  @Test
  public void testBuilderCreation() {
    final String name = "Name";
    final String expectedName = name + " USD";
    final CurveNodeIdMapper mapper = CurveNodeIdMapper.builder()
        .name(name)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS)
        .fraNodeIds(USD_3M_FRA_INSTRUMENTS)
        .build();
    final CurveNodeIdMapper expectedMapper = CurveNodeIdMapper.builder()
        .name(expectedName)
        .cashNodeIds(USD_DEPOSIT_INSTRUMENTS)
        .fraNodeIds(USD_3M_FRA_INSTRUMENTS)
        .build();
    final Pair<String, CurveNodeIdMapper.Builder> nameBuilderPair = NO_OP.apply(mapper, CSBC, "USD");
    assertEquals(nameBuilderPair.getFirst(), expectedName);
    assertEquals(nameBuilderPair.getSecond().build(), expectedMapper);
  }

}
