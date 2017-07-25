package com.mcleodmoores.examples.simulated.loader.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.Period;

import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

public class ExamplesFxImpliedCurveConfigsPopulator {
  private static final Tenor ZERO = Tenor.of(Period.ZERO);
  private static final List<Currency> CURRENCIES = Arrays.asList(Currency.JPY, Currency.EUR, Currency.GBP, Currency.CHF, Currency.AUD, Currency.NZD);
  private static final List<Tenor> TENORS = Arrays.asList(Tenor.ONE_WEEK, Tenor.TWO_WEEKS, Tenor.THREE_WEEKS, Tenor.ONE_MONTH, Tenor.THREE_MONTHS,
      Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS,
      Tenor.SIX_YEARS, Tenor.SEVEN_YEARS, Tenor.EIGHT_YEARS, Tenor.NINE_YEARS, Tenor.TEN_YEARS);
  private static final List<String> EXOGENOUS_CONFIGS = Collections.singletonList(ExampleConfigUtils.generateVanillaFixedIncomeConfigName("USD"));
  private static final ExternalId CONVENTION_ID = ExternalId.of("CONVENTION", "FX Forward");

  public static void populateConfigMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    for (final Currency currency : CURRENCIES) {
      makeTwoCurveConfiguration(currency, configMaster);
      makeCurveDefinition(currency, configMaster);
    }
  }

  private static void makeTwoCurveConfiguration(final Currency ccy, final ConfigMaster configMaster) {
    final String ccyString = ccy.getCode();
    final String name = ExampleConfigUtils.generateFxImpliedConfigName(ccyString);
    final String discountingCurveName = ExampleConfigUtils.generateFxImpliedCurveName(ccyString);
    final DiscountingCurveTypeConfiguration discountingCurveType = new DiscountingCurveTypeConfiguration(ccyString);
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
    curveTypes.put(discountingCurveName, Arrays.asList(discountingCurveType));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
    final List<CurveGroupConfiguration> groups = Arrays.asList(group);
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(new CurveConstructionConfiguration(name, groups, EXOGENOUS_CONFIGS)));
  }

  private static void makeCurveDefinition(final Currency ccy, final ConfigMaster configMaster) {
    final String curveName = ExampleConfigUtils.generateFxImpliedCurveName(ccy.getCode());
    final String idMapperName = ccy.getCode() + "/USD Tickers";
    final Map<Tenor, CurveInstrumentProvider> fxForwardIds = new HashMap<>();
    final Set<CurveNode> nodes = new LinkedHashSet<>();
    for (final Tenor tenor : TENORS) {
      final String tenorString = tenor.toFormattedString().substring(1);
      nodes.add(new FXForwardNode(ZERO, tenor, CONVENTION_ID, ccy, Currency.USD, idMapperName));
      fxForwardIds.put(tenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(ccy.getCode() + "USD" + tenorString + "FXFORWARD")));
    }
    final CurveDefinition definition = new InterpolatedCurveDefinition(curveName, nodes,
        LinearInterpolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    final CurveNodeIdMapper nodeIds = CurveNodeIdMapper.builder()
        .name(idMapperName)
        .fxForwardNodeIds(fxForwardIds)
        .build();
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(definition));
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(nodeIds));
  }
}
