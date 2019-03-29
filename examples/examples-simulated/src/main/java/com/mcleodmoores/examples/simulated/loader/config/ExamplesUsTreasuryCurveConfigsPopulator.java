/**
 *
 */
package com.mcleodmoores.examples.simulated.loader.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.MonotonicConstrainedCubicSplineInterpolator1dAdapter;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Populates a config master with configurations for government bond curves.
 */
public class ExamplesUsTreasuryCurveConfigsPopulator {

  /**
   * @param configMaster
   *          a config master, not null
   */
  public static void populateConfigMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    final String cccName = "US Treasury";
    final String curveName = "US Treasury";
    final String idMapperName = "US Treasury ISINs";
    final Set<Object> keys = new HashSet<>();
    keys.add(Country.US);
    keys.add(Currency.USD);
    final LegalEntityRegion filter = new LegalEntityRegion(false, true, Collections.singleton(Country.US), true, Collections.singleton(Currency.USD));
    final IssuerCurveTypeConfiguration bondCurveType = new IssuerCurveTypeConfiguration(keys, Collections.<LegalEntityFilter<LegalEntity>> singleton(filter));
    final DiscountingCurveTypeConfiguration discountingCurveType = new DiscountingCurveTypeConfiguration("USD");
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
    curveTypes.put(curveName, Arrays.asList(bondCurveType, discountingCurveType));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
    final CurveConstructionConfiguration ccc = new CurveConstructionConfiguration(cccName, Arrays.asList(group), Collections.<String> emptyList());
    final ConfigItem<CurveConstructionConfiguration> cccItem = ConfigItem.of(ccc);
    cccItem.setName(ccc.getName());
    ConfigMasterUtils.storeByName(configMaster, cccItem);
    final Set<CurveNode> curveNodes = new LinkedHashSet<>();
    curveNodes.add(new BillNode(Tenor.ofDays(28), idMapperName));
    curveNodes.add(new BillNode(Tenor.ofDays(91), idMapperName));
    curveNodes.add(new BillNode(Tenor.ofDays(182), idMapperName));
    curveNodes.add(new BillNode(Tenor.ofDays(364), idMapperName));
    curveNodes.add(new BondNode(Tenor.TWO_YEARS, idMapperName));
    curveNodes.add(new BondNode(Tenor.THREE_YEARS, idMapperName));
    curveNodes.add(new BondNode(Tenor.FIVE_YEARS, idMapperName));
    curveNodes.add(new BondNode(Tenor.SEVEN_YEARS, idMapperName));
    curveNodes.add(new BondNode(Tenor.TEN_YEARS, idMapperName));
    curveNodes.add(new BondNode(Tenor.ofYears(30), idMapperName));
    final InterpolatedCurveDefinition interpolatedCurve = new InterpolatedCurveDefinition(curveName, curveNodes,
        MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    final ConfigItem<InterpolatedCurveDefinition> curveItem = ConfigItem.of(interpolatedCurve);
    curveItem.setName(curveItem.getName());
    ConfigMasterUtils.storeByName(configMaster, curveItem);
    final Map<Tenor, CurveInstrumentProvider> billNodeIds = new HashMap<>();
    billNodeIds.put(Tenor.ofDays(28), new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("UST000000013"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    billNodeIds.put(Tenor.ofDays(91), new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("UST000000012"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    billNodeIds.put(Tenor.ofDays(182), new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("UST000000014"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    billNodeIds.put(Tenor.ofDays(364), new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("UST000000015"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    final Map<Tenor, CurveInstrumentProvider> bondNodeIds = new HashMap<>();
    bondNodeIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("USN000000005"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    bondNodeIds.put(Tenor.THREE_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("USN000000001"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    bondNodeIds.put(Tenor.FIVE_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("USN000000004"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    bondNodeIds.put(Tenor.SEVEN_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("USN000000006"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    bondNodeIds.put(Tenor.TEN_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("USN000000007"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    bondNodeIds.put(Tenor.ofYears(30), new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("USB000000011"),
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
    final CurveNodeIdMapper idMapper = CurveNodeIdMapper.builder().name(idMapperName).billNodeIds(billNodeIds).bondNodeIds(bondNodeIds).build();
    final ConfigItem<CurveNodeIdMapper> idMapperItem = ConfigItem.of(idMapper);
    idMapperItem.setName(idMapperItem.getName());
    ConfigMasterUtils.storeByName(configMaster, idMapperItem);
  }

}
