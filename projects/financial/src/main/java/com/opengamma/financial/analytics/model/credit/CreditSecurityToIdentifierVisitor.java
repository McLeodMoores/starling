/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexSecurity;
import com.opengamma.financial.security.cds.CreditDefaultSwapSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.financial.security.option.CreditDefaultSwapOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.credit.CreditCurveIdentifier;
import com.opengamma.util.money.Currency;

/**
 * Retrieves a {@link CreditCurveIdentifier} from a security representing a CDS or CDS option.
 */
public final class CreditSecurityToIdentifierVisitor extends FinancialSecurityVisitorAdapter<CreditCurveIdentifier> {
  private final SecuritySource _securitySource;
  private final String _prefix;

  /**
   * @param securitySource
   *          a security source, not null
   */
  public CreditSecurityToIdentifierVisitor(final SecuritySource securitySource) {
    this(securitySource, null);
  }

  /**
   * @param securitySource
   *          a security source, not null
   * @param prefix
   *          an optional prefix used for the identifier
   */
  public CreditSecurityToIdentifierVisitor(final SecuritySource securitySource, final String prefix) {
    ArgumentChecker.notNull(securitySource, "security source");
    _securitySource = securitySource;
    _prefix = prefix;
  }

  @Override
  public CreditCurveIdentifier visitCreditDefaultSwapOptionSecurity(final CreditDefaultSwapOptionSecurity security) {
    // TODO version correction?
    @SuppressWarnings("deprecation")
    final CreditDefaultSwapSecurity underlyingSwap = (CreditDefaultSwapSecurity) _securitySource.getSingle(ExternalIdBundle.of(security.getUnderlyingId()));
    return underlyingSwap.accept(this);
  }

  @Override
  public CreditCurveIdentifier visitCreditDefaultSwapIndexSecurity(final CreditDefaultSwapIndexSecurity security) {
    final ExternalId redCode = security.getReferenceEntity();
    return CreditCurveIdentifier.of(redCode);
  }

  @Override
  public CreditCurveIdentifier visitStandardCDSSecurity(final StandardCDSSecurity security) {
    final ExternalId redCode = security.getReferenceEntity();
    final Currency currency = security.getNotional().getCurrency();
    final String seniority = security.getDebtSeniority().name();
    if (_prefix == null) {
      return CreditCurveIdentifier.of(redCode, currency, seniority);
    }
    return CreditCurveIdentifier.of(_prefix, redCode.getValue(), currency, seniority);
  }

  @Override
  public CreditCurveIdentifier visitLegacyCDSSecurity(final LegacyCDSSecurity security) {
    final ExternalId redCode = security.getReferenceEntity();
    final Currency currency = security.getNotional().getCurrency();
    final String seniority = security.getDebtSeniority().name();
    final String restructuringClause = security.getRestructuringClause().name();
    if (_prefix == null) {
      return CreditCurveIdentifier.of(redCode, currency, seniority, restructuringClause);
    }
    return CreditCurveIdentifier.of(_prefix, redCode.getValue(), currency, seniority, restructuringClause);
  }
}
