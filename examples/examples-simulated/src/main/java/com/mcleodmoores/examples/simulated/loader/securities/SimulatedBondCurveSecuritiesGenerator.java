package com.mcleodmoores.examples.simulated.loader.securities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.ircurve.strips.BillNode;
import com.opengamma.financial.analytics.ircurve.strips.BondNode;
import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.convention.frequency.PeriodFrequency;
import com.opengamma.financial.convention.yield.SimpleYieldConvention;
import com.opengamma.financial.generator.SecurityGenerator;
import com.opengamma.financial.security.bond.BillSecurity;
import com.opengamma.financial.security.bond.BondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.security.ManageableSecurity;
import com.opengamma.masterdb.legalentity.DbLegalEntityBeanMaster;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.Tenor;

/**
 * Generates bill and bond securities that are required for construction of curves
 * containing {@link BillNode} and {@link BondNode}. These securities must be
 * present in the security database for successful curve construction.
 */
public class SimulatedBondCurveSecuritiesGenerator extends AbstractSecuritiesGenerator {
  /** The bills and bonds */
  private static final List<ManageableSecurity> SECURITIES = new ArrayList<>();

  static {
    final ExternalId region = ExternalSchemes.countryRegionId(Country.US);
    DayCount dayCount = DayCounts.ACT_360;
    final Frequency frequency = PeriodFrequency.SEMI_ANNUAL;
    final ExternalId legalEntityId = ExternalId.of(DbLegalEntityBeanMaster.IDENTIFIER_SCHEME_DEFAULT, "US Government");
    final ZonedDateTime referenceDate = LocalDate.now().atStartOfDay(ZoneOffset.UTC);
    for (int i = 6; i <= 18; i += 6) {
      final Tenor tenor = Tenor.ofMonths(i);
      final BillSecurity bill = new BillSecurity(Currency.USD, new Expiry(referenceDate.plus(tenor.getPeriod())), referenceDate,
          100, 2, region, SimpleYieldConvention.INTERESTATMTY, dayCount, legalEntityId);
      String suffix;
      if (i < 10) {
        suffix = "00" + Integer.toString(i);
      } else {
        suffix = "0" + Integer.toString(i);
      }
      final String isin = "USB000000" + suffix;
      bill.setName(isin);
      bill.setExternalIdBundle(ExternalSchemes.syntheticSecurityId(isin).toBundle());
      SECURITIES.add(bill);
    }
    final Random rng = new Random(457);
    dayCount = DayCounts.ACT_ACT_ICMA;
    for (final int i : new int[] {2, 3, 5, 7, 10, 20, 30 }) {
      final double coupon = (i > 7 ? 0.5 * i : 0.2 * i) + rng.nextDouble();
      final Tenor tenor = Tenor.ofYears(i);
      final BondSecurity bond = new GovernmentBondSecurity("US TREASURY N/B", "Sovereign", "US", "US GOVERNMENT", Currency.USD, SimpleYieldConvention.US_STREET,
          new Expiry(referenceDate.plus(tenor.getPeriod())), "FIXED", coupon, frequency, dayCount, referenceDate, referenceDate, referenceDate.plusMonths(6),
          100., 1000000, 100, 100, 100, 100);
      String suffix;
      if (i < 10) {
        suffix = "00" + Integer.toString(i);
      } else if (i < 100) {
        suffix = "0" + Integer.toString(i);
      } else {
        suffix = Integer.toString(i);
      }
      final String isin = "UST000000" + suffix;
      bond.setName(isin);
      bond.setExternalIdBundle(ExternalSchemes.syntheticSecurityId(isin).toBundle());
      SECURITIES.add(bond);
    }
  }

  @Override
  public SecuritiesGenerator createSecuritiesGenerator() {
    final SecurityGenerator<ManageableSecurity> securityGenerator = new CollectionSecurityGenerator<>(SECURITIES);
    configure(securityGenerator);
    return new SecuritiesGenerator(securityGenerator, SECURITIES.size());
  }
}
