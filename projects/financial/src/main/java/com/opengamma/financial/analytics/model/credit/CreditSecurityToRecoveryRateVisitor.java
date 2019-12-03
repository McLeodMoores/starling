/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.credit.CdsRecoveryRateIdentifier;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.financial.security.cds.StandardVanillaCDSSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Retrieves a market-data identifier for the recovery rate for CDS securities.
 */
public final class CreditSecurityToRecoveryRateVisitor extends FinancialSecurityVisitorAdapter<CdsRecoveryRateIdentifier> {
  private final SecuritySource _securitySource;

  /**
   * @param securitySource
   *          a security source, not null
   */
  public CreditSecurityToRecoveryRateVisitor(final SecuritySource securitySource) {
    ArgumentChecker.notNull(securitySource, "security source");
    _securitySource = securitySource;
  }

  @Deprecated
  @Override
  public CdsRecoveryRateIdentifier visitStandardVanillaCDSSecurity(final StandardVanillaCDSSecurity security) {
    final ExternalId redCode = security.getReferenceEntity();
    final Currency currency = security.getNotional().getCurrency();
    final String seniority = security.getDebtSeniority().name();
    final String restructuringClause = security.getRestructuringClause().name();
    return CdsRecoveryRateIdentifier.forSamedayCds(redCode.getValue(), currency, seniority, restructuringClause);
  }

  @Deprecated
  @Override
  public CdsRecoveryRateIdentifier visitLegacyVanillaCDSSecurity(final LegacyVanillaCDSSecurity security) {
    final ExternalId redCode = security.getReferenceEntity();
    final Currency currency = security.getNotional().getCurrency();
    final String seniority = security.getDebtSeniority().name();
    final String restructuringClause = security.getRestructuringClause().name();
    return CdsRecoveryRateIdentifier.forSamedayCds(redCode.getValue(), currency, seniority, restructuringClause);
  }

  @Override
  public CdsRecoveryRateIdentifier visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    // TODO version correction?
    @SuppressWarnings("deprecation")
    final CreditDefaultSwapSecurity underlyingSwap = (CreditDefaultSwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    return underlyingSwap.accept(this);
  }

  @Override
  public CdsRecoveryRateIdentifier visitStandardCDSSecurity(final StandardCDSSecurity security) {
    return CdsRecoveryRateIdentifier.forSamedayCds(security.getReferenceEntity().getValue(), security.getNotional().getCurrency(),
        security.getDebtSeniority().name());
  }

  @Override
  public CdsRecoveryRateIdentifier visitLegacyCDSSecurity(final LegacyCDSSecurity security) {
    return CdsRecoveryRateIdentifier.forSamedayCds(security.getReferenceEntity().getValue(), security.getNotional().getCurrency(),
        security.getDebtSeniority().name(), security.getRestructuringClause().name());
  }
}
