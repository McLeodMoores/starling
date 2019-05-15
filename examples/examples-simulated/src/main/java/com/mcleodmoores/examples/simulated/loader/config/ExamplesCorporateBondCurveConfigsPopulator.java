/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.config;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.examples.simulated.loader.portfolio.ExamplesCreditPortfolioGenerator;
import com.opengamma.analytics.financial.legalentity.LegalEntity;
import com.opengamma.analytics.financial.legalentity.LegalEntityCreditRatings;
import com.opengamma.analytics.financial.legalentity.LegalEntityFilter;
import com.opengamma.analytics.financial.legalentity.LegalEntityRegion;
import com.opengamma.analytics.math.interpolation.factory.FlatExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearExtrapolator1dAdapter;
import com.opengamma.analytics.math.interpolation.factory.LinearInterpolator1dAdapter;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.financial.analytics.curve.CurveConstructionConfiguration;
import com.opengamma.financial.analytics.curve.CurveGroupConfiguration;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.curve.CurveTypeConfiguration;
import com.opengamma.financial.analytics.curve.InterpolatedCurveDefinition;
import com.opengamma.financial.analytics.curve.IssuerCurveTypeConfiguration;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.financial.convention.businessday.BusinessDayConvention;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.convention.yield.YieldConvention;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.security.SecurityDocument;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pairs;

/**
 * Populates a config master with configurations for generic corporate bonds with different ratings.
 */
public class ExamplesCorporateBondCurveConfigsPopulator {
  private static final Random RANDOM = new Random(1237);
  private static final MathContext MC = new MathContext(3);

  /**
   * Populates the config master with a curve definition, id mapper and construction configuration. Populates the security master with the corporate bonds
   * referenced by the curves.
   *
   * @param configMaster
   *          a config master, not null
   * @param securityMaster
   *          a security master, not null
   */
  public static void populateMasters(final ConfigMaster configMaster, final SecurityMaster securityMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    ArgumentChecker.notNull(securityMaster, "securityMaster");
    final String issuerType = "CORP";
    final String issuerDomicile = "US";
    final String market = "CORP";
    final Currency currency = Currency.USD;
    final YieldConvention yieldConvention = SimpleYieldConvention.US_STREET;
    final String couponType = "FIXED";
    final Frequency couponFrequency = PeriodFrequency.SEMI_ANNUAL;
    final DayCount dcc = DayCounts.ACT_360;
    final ZonedDateTime startDate = ZonedDateTime.now();
    final ZonedDateTime firstCouponDate = startDate.plusMonths(6);
    final BusinessDayConvention bdc = BusinessDayConventions.FOLLOWING;
    final double issuancePrice = 100;
    final double totalAmountIssued = 1;
    final double minimumAmount = 1;
    final double minimumIncrement = 1;
    final double parAmount = 100;
    final double redemptionValue = 100;
    int j = 0;
    final Collection<ManageableLegalEntity> entities = ExamplesCreditPortfolioGenerator.getLegalEntities();
    final List<CurveGroupConfiguration> groups = new ArrayList<>();
    for (final ManageableLegalEntity entity : entities) {
      final Set<CurveNode> nodes = new LinkedHashSet<>();
      String fitchRating = null;
      for (final Rating rating : entity.getRatings()) {
        if (rating.getRater().toUpperCase().contains("FITCH")) {
          fitchRating = rating.getScore().name();
        }
      }
      final String issuerName = "US " + fitchRating + " Corp";
      final Map<Tenor, CurveInstrumentProvider> bondNodeIds = new HashMap<>();
      for (final int i : new int[] { 1, 2, 3, 5, 10, 15 }) {
        final double bondCoupon = BigDecimal.valueOf(5 + entity.getRatings().get(0).getScore().ordinal() / 5. + i * 0.05 * RANDOM.nextDouble()).round(MC)
            .doubleValue();
        final String isinString = "USCG" + entity.getExternalIdBundle().getValue(ExternalSchemes.MARKIT_RED_CODE) + (i < 10 ? "0" : "") + i;
        final Expiry lastTradeDate = new Expiry(startDate.plusYears(i));
        final String name = "USD " + i + "Y " + fitchRating + " Corp";
        final CorporateBondSecurity bond = new CorporateBondSecurity(issuerName, issuerType, issuerDomicile, market, currency, yieldConvention, lastTradeDate,
            couponType, bondCoupon, couponFrequency, dcc, startDate, startDate, firstCouponDate, issuancePrice, totalAmountIssued, minimumAmount,
            minimumIncrement, parAmount, redemptionValue);
        bond.setName(name);
        bond.setBusinessDayConvention(bdc);
        bond.addExternalId(ExternalSchemes.isinSecurityId(isinString));
        bond.addExternalId(ExternalSchemes.syntheticSecurityId(isinString));
        for (final Rating rating : entity.getRatings()) {
          bond.addAttribute(rating.getRater(), rating.getScore().name());
        }
        nodes.add(new BondNode(Tenor.ofYears(i), issuerName));
        bondNodeIds.put(Tenor.ofYears(i), new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(isinString),
            MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID, DataFieldType.OUTRIGHT));
        securityMaster.add(new SecurityDocument(bond));
      }
      final InterpolatedCurveDefinition curveDefinition = new InterpolatedCurveDefinition(issuerName, nodes, LinearInterpolator1dAdapter.NAME,
          FlatExtrapolator1dAdapter.NAME, LinearExtrapolator1dAdapter.NAME);
      final CurveNodeIdMapper idMapper = CurveNodeIdMapper.builder().name(issuerName).bondNodeIds(bondNodeIds).build();
      final LegalEntityRegion regionFilter = new LegalEntityRegion(false, true, Collections.singleton(Country.US), false, Collections.<Currency> emptySet());
      final LegalEntityCreditRatings ratingFilter = new LegalEntityCreditRatings(true, Collections.singleton("RatingFitch"), false,
          Collections.<String> emptySet());
      final Set<Object> keys = new HashSet<>(Arrays.asList(Country.US, Pairs.of("RatingFitch", fitchRating)));
      final Set<LegalEntityFilter<LegalEntity>> filters = new HashSet<LegalEntityFilter<LegalEntity>>(Arrays.asList(ratingFilter, regionFilter));
      final CurveTypeConfiguration curveType = new IssuerCurveTypeConfiguration(keys, filters);
      final Map<String, List<? extends CurveTypeConfiguration>> curveTypes = new HashMap<>();
      curveTypes.put(issuerName, Arrays.asList(curveType));
      configMaster.add(new ConfigDocument(ConfigItem.of(curveDefinition)));
      configMaster.add(new ConfigDocument(ConfigItem.of(idMapper)));
      groups.add(new CurveGroupConfiguration(j++, curveTypes));
    }
    final CurveConstructionConfiguration ccc = new CurveConstructionConfiguration("US Corp", groups, Collections.singletonList("USD ISDA"));
    configMaster.add(new ConfigDocument(ConfigItem.of(ccc)));
  }

}
