/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.portfolio;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.convention.businessday.BusinessDayConventionAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.examples.simulated.loader.legalentity.ExamplesLegalEntityLoader;
import com.mcleodmoores.financial.function.credit.configs.CreditCurveDefinition;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.analytics.util.time.TenorUtils;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.legalentity.SeniorityLevel;
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.CreditSpreadNode;
import com.opengamma.financial.analytics.ircurve.strips.CurveNode;
import com.opengamma.financial.analytics.model.credit.CreditSecurityToIdentifierVisitor;
import com.opengamma.financial.convention.businessday.BusinessDayConventions;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.generator.AbstractPortfolioGeneratorTool;
import com.opengamma.financial.generator.LeafPortfolioNodeGenerator;
import com.opengamma.financial.generator.NameGenerator;
import com.opengamma.financial.generator.PortfolioGenerator;
import com.opengamma.financial.generator.PortfolioNodeGenerator;
import com.opengamma.financial.generator.PositionGenerator;
import com.opengamma.financial.generator.QuantityGenerator;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.generator.SecurityPersister;
import com.opengamma.financial.generator.SimplePositionGenerator;
import com.opengamma.financial.generator.StaticNameGenerator;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.swap.InterestRateNotional;
import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.scripts.Scriptable;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Loads an example portfolio containing corporate bonds and CDS and creates and stores the appropriate configurations to calculate the hazard rate curves used
 * in pricing CDS ({@link CreditCurveDefinition} and {@link CurveNodeIdMapper}) in the config master.
 */
@Scriptable
public class ExamplesCreditPortfolioGenerator extends AbstractPortfolioGeneratorTool {
  private static final long NOTIONAL = 10000000L;
  private static final int N = 25;
  private static final ExternalScheme SCHEME = ExternalSchemes.MARKIT_RED_CODE;
  private static final ZonedDateTime TODAY = ZonedDateTime.now();
  private static final List<ManageableLegalEntity> ENTITIES = new ArrayList<>();

  static {
    final ManageableLegalEntity entity1 = new ManageableLegalEntity("ABC Corp", ExternalIdBundle.of(SCHEME, "123456"));
    entity1.setRatings(Arrays.asList(new Rating("S&P", CreditRating.B, SeniorityLevel.SNRFOR)));
    final ManageableLegalEntity entity2 = new ManageableLegalEntity("X Ltd", ExternalIdBundle.of(SCHEME, "234567"));
    entity2.setRatings(Arrays.asList(new Rating("S&P", CreditRating.A, SeniorityLevel.SNRFOR)));
    final ManageableLegalEntity entity3 = new ManageableLegalEntity("CCC Inc", ExternalIdBundle.of(SCHEME, "345678"));
    entity3.setRatings(Arrays.asList(new Rating("S&P", CreditRating.AA, SeniorityLevel.SNRFOR)));
    final ManageableLegalEntity entity4 = new ManageableLegalEntity("AD4 Group", ExternalIdBundle.of(SCHEME, "456789"));
    entity4.setRatings(Arrays.asList(new Rating("S&P", CreditRating.C, SeniorityLevel.SNRFOR)));
    final ManageableLegalEntity entity5 = new ManageableLegalEntity("GHJ Co", ExternalIdBundle.of(SCHEME, "567890"));
    entity5.setRatings(Arrays.asList(new Rating("S&P", CreditRating.CC, SeniorityLevel.SNRFOR)));
    ENTITIES.add(entity1);
    ENTITIES.add(entity2);
    ENTITIES.add(entity3);
    ENTITIES.add(entity4);
    ENTITIES.add(entity5);
  }

  /**
   * Sets up the security generator.
   */
  public ExamplesCreditPortfolioGenerator() {
  }

  /**
   * Runs the tool.
   *
   * @param context
   *          a tool context, not null
   * @param portfolioName
   *          the portfolio name, not null
   * @param write
   *          true to write to the security, position and portfolio masters
   */
  public void run(final ToolContext context, final String portfolioName, final boolean write) {
    super.run(context, portfolioName, this, write, null);
    generateCreditSpreadCurves();
  }

  @Override
  public PortfolioGenerator createPortfolioGenerator(final NameGenerator portfolioNameGenerator) {
    final SecurityGenerator<FinancialSecurity> generator = new CreditSecurityGenerator();
    configure(generator);
    final PositionGenerator positions = new SimplePositionGenerator<>(generator, getSecurityPersister(), getCounterPartyGenerator());
    final PortfolioNodeGenerator rootNode = new LeafPortfolioNodeGenerator(new StaticNameGenerator("Credit"), positions, N * 2);
    return new PortfolioGenerator(rootNode, portfolioNameGenerator);
  }

  @Override
  public PortfolioNodeGenerator createPortfolioNodeGenerator(final int portfolioSize) {
    final SecurityGenerator<FinancialSecurity> generator = new CreditSecurityGenerator();
    configure(generator);
    final PositionGenerator positions = new SimplePositionGenerator<>(generator, getSecurityPersister(), getCounterPartyGenerator());
    return new LeafPortfolioNodeGenerator(new StaticNameGenerator("Credit"), positions, portfolioSize);
  }

  private void generateCreditSpreadCurves() {
    final SecuritySource securitySource = getToolContext().getSecuritySource();
    final ConfigMaster configMaster = getToolContext().getConfigMaster();
    final CreditSecurityToIdentifierVisitor idGenerator = new CreditSecurityToIdentifierVisitor(securitySource);
    final List<Tenor> tenors = Arrays.asList(Tenor.ONE_YEAR, Tenor.TWO_YEARS, Tenor.THREE_YEARS, Tenor.FOUR_YEARS, Tenor.FIVE_YEARS, Tenor.SIX_YEARS,
        Tenor.SEVEN_YEARS, Tenor.EIGHT_YEARS, Tenor.NINE_YEARS, Tenor.TEN_YEARS);
    for (final LegalEntity entity : ENTITIES) {
      final ExternalIdBundle eid = entity.getExternalIdBundle().getExternalId(SCHEME).toBundle();
      final Set<CurveNode> nodes = new LinkedHashSet<>();
      CreditCurveIdentifier id = null;
      String name = null;
      String value = "";
      final Map<Tenor, CurveInstrumentProvider> creditSpreadNodeIds = new HashMap<>();
      for (final Tenor tenor : tenors) {
        final ZonedDateTime maturity = TenorUtils.adjustDateByTenor(TODAY, tenor);
        // bit of a long way around to get the identifier but ensures consistency
        final StandardCDSSecurity cds = new StandardCDSSecurity(eid, "", TODAY.toLocalDate(), maturity.toLocalDate(),
            entity.getExternalIdBundle().iterator().next(), new InterestRateNotional(Currency.USD, 1), true, 0., DebtSeniority.SENIOR);
        id = cds.accept(idGenerator);
        if (name == null) {
          name = id.getObjectId().getValue();
        }
        value = id.getObjectId().getValue().replace("_", "") + tenor.toFormattedString();
        nodes.add(new CreditSpreadNode(name, tenor));
        creditSpreadNodeIds.put(tenor, new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId(value)));
      }
      final CreditCurveDefinition definition = new CreditCurveDefinition(name, id, nodes);
      final CurveNodeIdMapper idMapper = CurveNodeIdMapper.builder().creditSpreadNodeIds(creditSpreadNodeIds).name(name).build();
      configMaster.add(new ConfigDocument(ConfigItem.of(definition)));
      configMaster.add(new ConfigDocument(ConfigItem.of(idMapper)));
    }
  }

  /**
   * Generates bonds and CDS.
   */
  private static final class CreditSecurityGenerator extends SecurityGenerator<FinancialSecurity> {
    private static final MathContext MC = new MathContext(3);
    private static final DecimalFormat FORMAT = new DecimalFormat("##.##");
    private Iterator<FinancialSecurity> _securities = null;
    private boolean _set = false;

    @Override
    public FinancialSecurity createSecurity() {
      if (!_set) {
        _set = true;
        for (final ManageableLegalEntity entity : ENTITIES) {
          ExamplesLegalEntityLoader.storeLegalEntity(getLegalEntityMaster(), entity);
        }
        final List<FinancialSecurity> securities = new ArrayList<>();
        for (int i = 0; i < N * 2; i += 2) {
          final LegalEntity referenceEntity = ENTITIES.get(getRandom().nextInt(5));
          final ExternalIdBundle eid = referenceEntity.getExternalIdBundle().getExternalId(SCHEME).toBundle();
          final int term = getRandom().nextInt(10) + 1;
          final double spread = (referenceEntity.getRatings().get(0).getScore().ordinal() + 0.001) / 10000.;
          final double bondCoupon = BigDecimal.valueOf(0.03 + spread * 6 * term + (1 - getRandom().nextDouble()) / 80.).round(MC).doubleValue();
          final double cdsSpread = BigDecimal.valueOf(0.0025 + spread * term + (1 - getRandom().nextDouble()) / 9900).round(MC).doubleValue();
          final ZonedDateTime maturity = TODAY.plusYears(term);
          final CorporateBondSecurity bond = new CorporateBondSecurity(referenceEntity.getName(), "CORP", "US", "CORP", Currency.USD,
              SimpleYieldConvention.US_STREET, new Expiry(maturity), "FIXED", bondCoupon, PeriodFrequency.SEMI_ANNUAL, DayCounts.ACT_360, TODAY, TODAY,
              BusinessDayConventionAdapter.of(BusinessDayConventions.FOLLOWING).adjustDate(WeekendWorkingDayCalendar.SATURDAY_SUNDAY, TODAY.plusMonths(6)),
              100., 2500000., 1., 100., 100., 100.);
          bond.setName(maturity.getMonth().name().toUpperCase().substring(0, 3) + " " + maturity.getYear() + " " + referenceEntity.getName() + " @ "
              + FORMAT.format(bondCoupon * 100) + "%");
          final StandardCDSSecurity cds = new StandardCDSSecurity(eid, "", TODAY.toLocalDate(), maturity.toLocalDate(),
              referenceEntity.getExternalIdBundle().iterator().next(), new InterestRateNotional(Currency.USD, NOTIONAL), true, cdsSpread, DebtSeniority.SENIOR);
          cds.setName(term + "Y " + referenceEntity.getName() + " CDS @ " + FORMAT.format(cdsSpread * 10000) + "bp");
          securities.add(bond);
          securities.add(cds);
        }
        _securities = securities.iterator();
      }
      return _securities.next();
    }

    @Override
    public ManageableTrade createSecurityTrade(final QuantityGenerator quantityGenerator, final SecurityPersister securityPersister,
        final NameGenerator counterPartyGenerator) {
      ManageableTrade trade = null;
      final FinancialSecurity security = createSecurity();
      if (security != null) {
        final BigDecimal quantity = security instanceof CorporateBondSecurity ? BigDecimal.valueOf(NOTIONAL) : BigDecimal.ONE;
        final ZonedDateTime tradeDate = previousWorkingDay(ZonedDateTime.now().minusDays(getRandom(30)), getRandomCurrency());
        trade = new ManageableTrade(quantity, securityPersister.storeSecurity(security), tradeDate.toLocalDate(), tradeDate.toOffsetDateTime().toOffsetTime(),
            ExternalId.of(Counterparty.DEFAULT_SCHEME, counterPartyGenerator.createName()));
      }
      return trade;
    }

  }

}
