/**
 * Copyright (C) 2019 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.financial.aggregation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePositionComparator;
import com.opengamma.core.security.Security;
import com.opengamma.financial.aggregation.AggregationFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.bond.MunicipalBondSecurity;
import com.opengamma.financial.security.credit.LegacyCDSSecurity;
import com.opengamma.financial.security.credit.StandardCDSSecurity;
import com.opengamma.util.ArgumentChecker;

public class IssuerAggregationFunction implements AggregationFunction<String> {
  private static final String NAME = "Issuer";
  private static final String UNKNOWN = "Unknown Issuer";
  private static final Comparator<Position> COMPARATOR = new SimplePositionComparator();

  private final LegalEntitySource _legalEntitySource;

  public IssuerAggregationFunction(final LegalEntitySource legalEntitySource) {
    _legalEntitySource = ArgumentChecker.notNull(legalEntitySource, "legalEntitySource");
  }

  @Override
  public int compare(final String issuer1, final String issuer2) {
    return issuer1.compareTo(issuer2);
  }

  @Override
  public Collection<String> getRequiredEntries() {
    return Collections.emptyList();
  }

  @Override
  public String classifyPosition(final Position position) {
    final Security security = position.getSecurity();
    if (security instanceof FinancialSecurity) {
      final FinancialSecurity financialSecurity = (FinancialSecurity) security;
      return financialSecurity.accept(new FinancialSecurityVisitorAdapter<String>() {

        @Override
        public String visitCorporateBondSecurity(final CorporateBondSecurity bond) {
          return bond.getIssuerName();
        }

        @Override
        public String visitMunicipalBondSecurity(final MunicipalBondSecurity bond) {
          return bond.getIssuerName();
        }

        @Override
        public String visitGovernmentBondSecurity(final GovernmentBondSecurity bond) {
          return bond.getIssuerName();
        }

        @Override
        public String visitLegacyCDSSecurity(final LegacyCDSSecurity cds) {
          final LegalEntity entity = _legalEntitySource.getSingle(cds.getReferenceEntity());
          return entity.getName();
        }

        @Override
        public String visitStandardCDSSecurity(final StandardCDSSecurity cds) {
          final LegalEntity entity = _legalEntitySource.getSingle(cds.getReferenceEntity());
          return entity.getName();
        }

      });
    }
    return UNKNOWN;
  }

  @Override
  public Comparator<Position> getPositionComparator() {
    return COMPARATOR;
  }

  @Override
  public String getName() {
    return NAME;
  }

}
