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
import com.opengamma.analytics.math.interpolation.factory.MonotonicConstrainedCubicSplineInterpolator1dAdapter;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.DiscountingCurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurvePointsInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CashNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.analytics.ircurve.strips.FXForwardNode;
import com.opengamma.id.ExternalId;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMasterUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

public class ExamplesFxImpliedCurveConfigsPopulator {
  public static final String USD_DEPOSIT_CURVE_NAME = "USD Deposit";
  private static final List<Tenor> USD_DEPOSIT_TENORS = Arrays.asList(Tenor.ONE_WEEK, Tenor.TWO_WEEKS, Tenor.THREE_WEEKS,
      Tenor.ONE_MONTH, Tenor.TWO_MONTHS, Tenor.THREE_MONTHS, Tenor.FOUR_MONTHS, Tenor.FIVE_MONTHS, Tenor.SIX_MONTHS, Tenor.NINE_MONTHS,
      Tenor.TWELVE_MONTHS, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS);
  private static final Tenor ZERO = Tenor.of(Period.ZERO);
  private static final List<Currency> CURRENCIES = Arrays.asList(Currency.JPY, Currency.EUR, Currency.GBP, Currency.CHF, Currency.AUD, Currency.NZD);
  private static final List<Tenor> WEEK_TENORS = Arrays.asList(Tenor.ONE_WEEK, Tenor.TWO_WEEKS, Tenor.THREE_WEEKS);
  private static final List<Tenor> MONTH_TENORS = Arrays.asList(Tenor.ONE_MONTH, Tenor.THREE_MONTHS,
      Tenor.SIX_MONTHS, Tenor.NINE_MONTHS, Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS,
      Tenor.SIX_YEARS, Tenor.SEVEN_YEARS, Tenor.EIGHT_YEARS, Tenor.NINE_YEARS, Tenor.TEN_YEARS);
  private static final String USD_CONFIG_NAME = "USD Deposit Config";
  private static final ExternalId CONVENTION_ID = ExternalId.of("CONVENTION", "FX Forward");
  private static final List<Currency> DOMINANT_CURRENCY = Arrays.asList(Currency.EUR, Currency.GBP, Currency.AUD, Currency.NZD);

  public static void populateConfigMaster(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    makeUsdConfigs(configMaster);
    for (final Currency currency : CURRENCIES) {
      makeTwoCurveConfiguration(currency, configMaster);
      makeCurveDefinition(currency, configMaster);
    }
  }

  private static void makeUsdConfigs(final ConfigMaster configMaster) {
    final DiscountingCurveTypeConfiguration discountingCurveType = new DiscountingCurveTypeConfiguration(Currency.USD.getCode());
    final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
    curveTypes.put(USD_DEPOSIT_CURVE_NAME, Arrays.asList(discountingCurveType));
    final CurveGroupConfiguration group = new CurveGroupConfiguration(0, curveTypes);
    final List<CurveGroupConfiguration> groups = Arrays.asList(group);
    ConfigMasterUtils.storeByName(configMaster,
        ExampleConfigUtils.makeConfig(new CurveConstructionConfiguration(USD_CONFIG_NAME, groups, Collections.<String>emptyList())));
    final String idMapperName = "USD Deposit Tickers";
    final Map<Tenor, CurveInstrumentProvider> cashIds = new HashMap<>();
    final Set<CurveNode> nodes = new LinkedHashSet<>();
    for (final Tenor tenor : USD_DEPOSIT_TENORS) {
      nodes.add(new CashNode(ZERO, tenor, ExternalId.of("CONVENTION", "USD Deposit"), idMapperName));
      cashIds.put(tenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("USDCASH" + tenor.toFormattedString())));
    }
    final CurveDefinition definition = new InterpolatedCurveDefinition(USD_DEPOSIT_CURVE_NAME, nodes,
        MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    final CurveNodeIdMapper nodeIds = CurveNodeIdMapper.builder()
        .name(idMapperName)
        .cashNodeIds(cashIds)
        .build();
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(definition));
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(nodeIds));
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
    ConfigMasterUtils.storeByName(configMaster,
        ExampleConfigUtils.makeConfig(new CurveConstructionConfiguration(name, groups, Collections.singletonList(USD_CONFIG_NAME))));
  }

  private static void makeCurveDefinition(final Currency ccy, final ConfigMaster configMaster) {
    final String curveName = ExampleConfigUtils.generateFxImpliedCurveName(ccy.getCode());
    final String idMapperName = ccy.getCode() + "/USD Tickers";
    final Map<Tenor, CurveInstrumentProvider> fxForwardIds = new HashMap<>();
    final Set<CurveNode> nodes = new LinkedHashSet<>();
    final String underlying = DOMINANT_CURRENCY.contains(ccy) ? ccy.getCode() + "USD" : "USD" + ccy.getCode();
    for (final Tenor tenor : WEEK_TENORS) {
      final String tenorString = tenor.getPeriod().getDays() / 7 + "W";
      nodes.add(new FXForwardNode(ZERO, tenor, CONVENTION_ID, ccy, Currency.USD, idMapperName));
      fxForwardIds.put(tenor, new StaticCurvePointsInstrumentProvider(ExternalSchemes.syntheticSecurityId(ccy.getCode() + "USD" + tenorString + "FXFORWARD"),
          MarketDataRequirementNames.MARKET_VALUE, DataFieldType.POINTS, ExternalSchemes.syntheticSecurityId(underlying), MarketDataRequirementNames.MARKET_VALUE));
    }
    for (final Tenor tenor : MONTH_TENORS) {
      final String tenorString = tenor.toFormattedString().substring(1);
      nodes.add(new FXForwardNode(ZERO, tenor, CONVENTION_ID, ccy, Currency.USD, idMapperName));
      fxForwardIds.put(tenor, new StaticCurvePointsInstrumentProvider(ExternalSchemes.syntheticSecurityId(ccy.getCode() + "USD" + tenorString + "FXFORWARD"),
          MarketDataRequirementNames.MARKET_VALUE, DataFieldType.POINTS, ExternalSchemes.syntheticSecurityId(underlying), MarketDataRequirementNames.MARKET_VALUE));
    }
    final CurveDefinition definition = new InterpolatedCurveDefinition(curveName, nodes,
        MonotonicConstrainedCubicSplineInterpolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
    final CurveNodeIdMapper nodeIds = CurveNodeIdMapper.builder()
        .name(idMapperName)
        .fxForwardNodeIds(fxForwardIds)
        .build();
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(definition));
    ConfigMasterUtils.storeByName(configMaster, ExampleConfigUtils.makeConfig(nodeIds));
  }
}
