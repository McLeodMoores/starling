/**
 * Copyright (C) 2018 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.examples.simulated.loader.portfolio;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.analytics.convention.businessday.BusinessDayConventionAdapter;
import com.mcleodmoores.date.WeekendWorkingDayCalendar;
import com.mcleodmoores.examples.simulated.loader.legalentity.ExamplesLegalEntityLoader;
import com.opengamma.analytics.financial.credit.DebtSeniority;
import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.Rating;
import com.opengamma.core.legalentity.SeniorityLevel;
import com.opengamma.core.obligor.CreditRating;
import com.opengamma.core.position.Counterparty;
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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalScheme;
import com.opengamma.master.legalentity.ManageableLegalEntity;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * Loads an example portfolio containing corporate bonds and CDS.
 */
public class ExamplesCreditPortfolioGenerator extends AbstractPortfolioGeneratorTool {
  private static final long NOTIONAL = 10000000L;
  private static final int N = 25;

  /**
   * Sets up the security generator.
   */
  public ExamplesCreditPortfolioGenerator() {
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

  /**
   * Generates bonds and CDS.
   */
  private static final class CreditSecurityGenerator extends SecurityGenerator<FinancialSecurity> {
    private static final ZonedDateTime TODAY = ZonedDateTime.now();
    private static final ExternalScheme SCHEME = ExternalScheme.of("CORP");
    private static final MathContext MC = new MathContext(3);
    private static final DecimalFormat FORMAT = new DecimalFormat("##.##");
    private static final List<ManageableLegalEntity> ENTITIES = new ArrayList<>();
    private Iterator<FinancialSecurity> _securities = null;
    private boolean _set = false;

    static {
      final ManageableLegalEntity entity1 = new ManageableLegalEntity("ABC Corp", ExternalIdBundle.of(SCHEME, "ABC Corp"));
      entity1.setRatings(Arrays.asList(new Rating("S&P", CreditRating.B, SeniorityLevel.SNRFOR)));
      final ManageableLegalEntity entity2 = new ManageableLegalEntity("X Ltd", ExternalIdBundle.of(SCHEME, "X Ltd"));
      entity2.setRatings(Arrays.asList(new Rating("S&P", CreditRating.A, SeniorityLevel.SNRFOR)));
      final ManageableLegalEntity entity3 = new ManageableLegalEntity("CCC Inc", ExternalIdBundle.of(SCHEME, "CCC Inc"));
      entity3.setRatings(Arrays.asList(new Rating("S&P", CreditRating.AA, SeniorityLevel.SNRFOR)));
      final ManageableLegalEntity entity4 = new ManageableLegalEntity("AD4 Group", ExternalIdBundle.of(SCHEME, "AD4 Group"));
      entity4.setRatings(Arrays.asList(new Rating("S&P", CreditRating.C, SeniorityLevel.SNRFOR)));
      final ManageableLegalEntity entity5 = new ManageableLegalEntity("GHJ Co", ExternalIdBundle.of(SCHEME, "GHJ Co"));
      entity5.setRatings(Arrays.asList(new Rating("S&P", CreditRating.CC, SeniorityLevel.SNRFOR)));
      ENTITIES.add(entity1);
      ENTITIES.add(entity2);
      ENTITIES.add(entity3);
      ENTITIES.add(entity4);
      ENTITIES.add(entity5);
    }

    @Override
    public FinancialSecurity createSecurity() {
      if (!_set) {
        _set = true;
        for (final ManageableLegalEntity entity : ENTITIES) {
          ExamplesLegalEntityLoader.storeLegalEntity(getLegalEntityMaster(), entity);
        }
        final List<FinancialSecurity> securities = new ArrayList<>();
        for (int i = 0; i < N * 2; i++) {
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
          bond.setExternalIdBundle(eid);
          bond.setName(maturity.getMonth().name().toUpperCase().substring(0, 3) + " " + maturity.getYear() + " " + eid.getValue(SCHEME) + " @ "
              + FORMAT.format(bondCoupon * 100) + "%");
          final StandardCDSSecurity cds = new StandardCDSSecurity(eid, "", TODAY.toLocalDate(), maturity.toLocalDate(),
              referenceEntity.getExternalIdBundle().iterator().next(), new InterestRateNotional(Currency.USD, NOTIONAL), true, cdsSpread, DebtSeniority.SENIOR);
          cds.setName(term + "Y " + eid.getValue(SCHEME) + " CDS @ " + FORMAT.format(cdsSpread * 10000) + "bp");
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
