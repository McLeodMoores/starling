/**
 * Copyright (C) 2010 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.bond.CorporateBondSecurity;
import com.opengamma.financial.security.bond.GovernmentBondSecurity;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;
import com.opengamma.financial.security.option.CommodityFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexDividendFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexFutureOptionSecurity;
import com.opengamma.financial.security.option.EquityIndexOptionSecurity;
import com.opengamma.financial.security.option.EquityOptionSecurity;
import com.opengamma.financial.security.option.FxFutureOptionSecurity;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Collects Underlying identifiers for loaded securities.
 */
public final class UnderlyingIdentifierCollector {

  private final Set<ExternalIdBundle> _underlyings = Sets.newHashSet();
  private final FinancialSecurityVisitor<Void> _financialSecurityVisitor;

  /**
   * Creates an instance.
   */
  public UnderlyingIdentifierCollector() {
    final FinancialSecurityVisitor<Void> underlyingIdentifierCollector = new FinancialSecurityVisitorAdapter<Void>() {

      @Override
      public Void visitEquitySecurity(final EquitySecurity security) {
        return null;
      }

      @Override
      public Void visitGovernmentBondSecurity(final GovernmentBondSecurity security) {
        return null;
      }

      @Override
      public Void visitCorporateBondSecurity(final CorporateBondSecurity security) {
        return null;
      }

      @Override
      public Void visitAgricultureFutureSecurity(final AgricultureFutureSecurity security) {
        return null;
      }

      @Override
      public Void visitBondFutureSecurity(final BondFutureSecurity security) {
        final List<BondFutureDeliverable> basketList = security.getBasket();
        for (final BondFutureDeliverable deliverable : basketList) {
          final ExternalIdBundle identifiers = deliverable.getIdentifiers();
          if (identifiers != null) {
            _underlyings.add(identifiers);
          }
        }
        return null;
      }

      @Override
      public Void visitEnergyFutureSecurity(final EnergyFutureSecurity security) {
        final ExternalId identifier = security.getUnderlyingId();
        if (identifier != null) {
          _underlyings.add(ExternalIdBundle.of(identifier));
        }
        return null;
      }

      @Override
      public Void visitFXFutureSecurity(final FXFutureSecurity security) {
        return null;
      }

      @Override
      public Void visitIndexFutureSecurity(final IndexFutureSecurity security) {
        final ExternalId identifier = security.getUnderlyingId();
        if (identifier != null) {
          _underlyings.add(ExternalIdBundle.of(identifier));
        }
        return null;
      }

      @Override
      public Void visitInterestRateFutureSecurity(final InterestRateFutureSecurity security) {
        final ExternalId identifier = security.getUnderlyingId();
        if (identifier != null) {
          _underlyings.add(ExternalIdBundle.of(identifier));
        }
        return null;
      }

      @Override
      public Void visitMetalFutureSecurity(final MetalFutureSecurity security) {
        final ExternalId identifier = security.getUnderlyingId();
        if (identifier != null) {
          _underlyings.add(ExternalIdBundle.of(identifier));
        }
        return null;
      }

      @Override
      public Void visitStockFutureSecurity(final StockFutureSecurity security) {
        final ExternalId identifier = security.getUnderlyingId();
        if (identifier != null) {
          _underlyings.add(ExternalIdBundle.of(identifier));
        }
        return null;
      }

      @Override
      public Void visitEquityFutureSecurity(final EquityFutureSecurity security) {
        final ExternalId identifier = security.getUnderlyingId();
        if (identifier != null) {
          _underlyings.add(ExternalIdBundle.of(identifier));
        }
        return null;
      }

      @Override
      public Void visitEquityIndexDividendFutureSecurity(final EquityIndexDividendFutureSecurity security) {
        final ExternalId identifier = security.getUnderlyingId();
        if (identifier != null) {
          _underlyings.add(ExternalIdBundle.of(identifier));
        }
        return null;
      }

      @Override
      public Void visitEquityOptionSecurity(final EquityOptionSecurity equityOptionSecurity) {
        final ExternalId identifier = equityOptionSecurity.getUnderlyingId();
        if (identifier != null) {
          _underlyings.add(ExternalIdBundle.of(identifier));
        }
        return null;
      }

      @Override
      public Void visitEquityIndexOptionSecurity(final EquityIndexOptionSecurity security) {
        final ExternalId identifier = security.getUnderlyingId();
        if (identifier != null) {
          _underlyings.add(ExternalIdBundle.of(identifier));
        }
        return null;
      }

      @Override
      public Void visitIRFutureOptionSecurity(final IRFutureOptionSecurity security) {
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        if (underlyingIdentifier != null) {
          _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
        }
        return null;
      }

      @Override
      public Void visitEquityIndexFutureOptionSecurity(final EquityIndexFutureOptionSecurity security) {
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        if (underlyingIdentifier != null) {
          _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
        }
        return null;
      }

      @Override
      public Void visitEquityIndexDividendFutureOptionSecurity(final EquityIndexDividendFutureOptionSecurity security) {
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        if (underlyingIdentifier != null) {
          _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
        }
        return null;
      }

      @Override
      public Void visitCommodityFutureOptionSecurity(final CommodityFutureOptionSecurity security) {
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        if (underlyingIdentifier != null) {
          _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
        }
        return null;
      }

      @Override
      public Void visitFxFutureOptionSecurity(final FxFutureOptionSecurity security) {
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        if (underlyingIdentifier != null) {
          _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
        }
        return null;
      }

      @Override
      public Void visitBondFutureOptionSecurity(final BondFutureOptionSecurity security) {
        final ExternalId underlyingIdentifier = security.getUnderlyingId();
        if (underlyingIdentifier != null) {
          _underlyings.add(ExternalIdBundle.of(underlyingIdentifier));
        }
        return null;
      }
    };
    _financialSecurityVisitor = underlyingIdentifierCollector;
  }

  /**
   * Gets the underlyings.
   * 
   * @return the underlyings
   */
  public Set<ExternalIdBundle> getUnderlyings() {
    return _underlyings;
  }

  /**
   * Gets the financialSecurityVisitor.
   * 
   * @return the financialSecurityVisitor
   */
  public FinancialSecurityVisitor<Void> getFinancialSecurityVisitor() {
    return _financialSecurityVisitor;
  }

}
