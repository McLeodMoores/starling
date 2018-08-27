/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.future;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryBeanToExpiry;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryToExpiryBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.future.AgricultureFutureSecurity;
import com.opengamma.financial.security.future.BondFutureDeliverable;
import com.opengamma.financial.security.future.BondFutureSecurity;
import com.opengamma.financial.security.future.CommodityFutureSecurity;
import com.opengamma.financial.security.future.EnergyFutureSecurity;
import com.opengamma.financial.security.future.EquityFutureSecurity;
import com.opengamma.financial.security.future.EquityIndexDividendFutureSecurity;
import com.opengamma.financial.security.future.FXFutureSecurity;
import com.opengamma.financial.security.future.FederalFundsFutureSecurity;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.IndexFutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.financial.security.future.MetalFutureSecurity;
import com.opengamma.financial.security.future.StockFutureSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * A Hibernate bean for storage.
 */
public final class FutureSecurityBeanOperation
    extends AbstractSecurityBeanOperation<FutureSecurity, FutureSecurityBean> {

  /**
   * Singleton.
   * */
  public static final FutureSecurityBeanOperation INSTANCE = new FutureSecurityBeanOperation();

  private FutureSecurityBeanOperation() {
    super(FutureSecurity.SECURITY_TYPE, FutureSecurity.class, FutureSecurityBean.class);
  }

  private static BondFutureDeliverable futureBundleBeanToBondFutureDeliverable(
    final FutureBundleBean futureBundleBean) {
    final Set<ExternalIdBean> identifierBeans = futureBundleBean
      .getIdentifiers();
    final Set<ExternalId> identifiers = new HashSet<>(
      identifierBeans.size());
    for (final ExternalIdBean identifierBean : identifierBeans) {
      identifiers.add(externalIdBeanToExternalId(identifierBean));
    }
    return new BondFutureDeliverable(ExternalIdBundle.of(identifiers),
      futureBundleBean.getConversionFactor());
  }

  @Override
  public FutureSecurity createSecurity(final OperationContext context,
                                       final FutureSecurityBean bean) {
    final FutureSecurity sec = bean.accept(
      new FutureSecurityBean.Visitor<FutureSecurity>() {

        @Override
        public FutureSecurity visitBondFutureType(final BondFutureBean bean) {
          final Set<FutureBundleBean> basketBeans = bean
            .getBasket();
          final Set<BondFutureDeliverable> basket = new HashSet<>(
            basketBeans.size());
          for (final FutureBundleBean basketBean : basketBeans) {
            basket.add(futureBundleBeanToBondFutureDeliverable(basketBean));
          }
          return new BondFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            basket,
            zonedDateTimeBeanToDateTimeWithZone(bean.getFirstDeliveryDate()),
            zonedDateTimeBeanToDateTimeWithZone(bean.getLastDeliveryDate()),
            bean.getCategory().getName()
          );
        }

        @Override
        public FutureSecurity visitFXFutureType(final ForeignExchangeFutureBean bean) {
          final FXFutureSecurity security = new FXFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()), bean
            .getTradingExchange().getName(), bean
            .getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            currencyBeanToCurrency(bean.getNumerator()),
            currencyBeanToCurrency(bean.getDenominator()),
            bean.getCategory().getName());
          security.setMultiplicationFactor(bean.getUnitNumber());
          return security;
        }

        @Override
        public FutureSecurity visitInterestRateFutureType(final InterestRateFutureBean bean) {
          return new InterestRateFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            externalIdBeanToExternalId(bean.getUnderlying()),
            bean.getCategory().getName());
        }

        @Override
        public FutureSecurity visitAgricultureFutureType(final AgricultureFutureBean bean) {
          final AgricultureFutureSecurity security = new AgricultureFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());

          security.setUnitNumber(bean.getUnitNumber());
          if (bean.getUnitName() != null) {
            security.setUnitName(bean.getUnitName().getName());
          }
          return security;
        }

        @Override
        public FutureSecurity visitEnergyFutureType(final EnergyFutureBean bean) {
          final EnergyFutureSecurity security = new EnergyFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());
          security.setUnitNumber(bean.getUnitNumber());
          if (bean.getUnitName() != null) {
            security.setUnitName(bean.getUnitName().getName());
          }
          security.setUnderlyingId(externalIdBeanToExternalId(bean
            .getUnderlying()));
          return security;
        }

        @Override
        public FutureSecurity visitMetalFutureType(final MetalFutureBean bean) {
          final MetalFutureSecurity security = new MetalFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());
          security.setUnitNumber(bean.getUnitNumber());
          if (bean.getUnitName() != null) {
            security.setUnitName(bean.getUnitName().getName());
          }
          security.setUnderlyingId(externalIdBeanToExternalId(bean
            .getUnderlying()));
          return security;
        }

        @Override
        public FutureSecurity visitIndexFutureType(final IndexFutureBean bean) {
          final IndexFutureSecurity security = new IndexFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());
          security.setUnderlyingId(externalIdBeanToExternalId(bean
            .getUnderlying()));
          return security;
        }

        @Override
        public FutureSecurity visitStockFutureType(final StockFutureBean bean) {
          final StockFutureSecurity security = new StockFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            bean.getCategory().getName());
          security.setUnderlyingId(externalIdBeanToExternalId(bean
            .getUnderlying()));
          return security;
        }

        @Override
        public FutureSecurity visitEquityFutureType(final EquityFutureBean bean) {
          final EquityFutureSecurity security = new EquityFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            expiryBeanToExpiry(bean.getExpiry()).getExpiry(), // TODO: this is a temporary hack as settlementDate isn't being stored in database
            externalIdBeanToExternalId(bean.getUnderlying()),
            bean.getCategory().getName());
          return security;
        }

        @Override
        public FutureSecurity visitEquityIndexDividendFutureType(final EquityIndexDividendFutureBean bean) {
          final EquityIndexDividendFutureSecurity security = new EquityIndexDividendFutureSecurity(
            expiryBeanToExpiry(bean.getExpiry()),
            bean.getTradingExchange().getName(),
            bean.getSettlementExchange().getName(),
            currencyBeanToCurrency(bean.getCurrency()),
            bean.getUnitAmount(),
            expiryBeanToExpiry(bean.getExpiry()).getExpiry(), // TODO: this is a temporary hack as settlementDate isn't being stored in database
            externalIdBeanToExternalId(bean.getUnderlying()),
            bean.getCategory().getName());
          return security;
        }

        @Override
        public FutureSecurity visitFederalFundsFutureType(final FederalFundsFutureBean bean) {
          final FederalFundsFutureSecurity security = new FederalFundsFutureSecurity(
              expiryBeanToExpiry(bean.getExpiry()),
              bean.getTradingExchange().getName(),
              bean.getSettlementExchange().getName(),
              currencyBeanToCurrency(bean.getCurrency()),
              bean.getUnitAmount(),
              externalIdBeanToExternalId(bean.getUnderlying()),
              bean.getCategory().getName());
          return security;
        }
      });
    return sec;
  }

  @Override
  public FutureSecurityBean resolve(final OperationContext context,
                                    final HibernateSecurityMasterDao secMasterSession, final Date now,
                                    final FutureSecurityBean bean) {
    return bean.accept(
      new FutureSecurityBean.Visitor<FutureSecurityBean>() {

        @Override
        public FutureSecurityBean visitAgricultureFutureType(final AgricultureFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitBondFutureType(final BondFutureBean bean) {
          final List<FutureBundleBean> basket = secMasterSession.getFutureBundleBeans(now, bean);
          bean.setBasket(new HashSet<>(basket));
          return bean;
        }

        @Override
        public FutureSecurityBean visitEnergyFutureType(final EnergyFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitFXFutureType(final ForeignExchangeFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitIndexFutureType(final IndexFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitInterestRateFutureType(final InterestRateFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitMetalFutureType(final MetalFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitStockFutureType(final StockFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitEquityFutureType(final EquityFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitEquityIndexDividendFutureType(final EquityIndexDividendFutureBean bean) {
          return bean;
        }

        @Override
        public FutureSecurityBean visitFederalFundsFutureType(final FederalFundsFutureBean bean) {
          return bean;
        }
      });
  }

  @Override
  public void postPersistBean(final OperationContext context,
                              final HibernateSecurityMasterDao secMasterSession, final Date now,
                              final FutureSecurityBean bean) {
    bean.accept(new FutureSecurityBean.Visitor<Object>() {

      private void postPersistFuture() {
        // No action
      }

      private void postPersistCommodityFuture() {
        postPersistFuture();
      }

      @Override
      public Object visitAgricultureFutureType(final AgricultureFutureBean bean) {
        postPersistCommodityFuture();
        return null;
      }

      @Override
      public Object visitBondFutureType(final BondFutureBean bean) {
        postPersistFuture();
        secMasterSession.persistFutureBundleBeans(now, bean);
        return null;
      }

      @Override
      public Object visitEnergyFutureType(final EnergyFutureBean bean) {
        postPersistCommodityFuture();
        return null;
      }

      @Override
      public Object visitEquityFutureType(final EquityFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitEquityIndexDividendFutureType(final EquityIndexDividendFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitFXFutureType(final ForeignExchangeFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitIndexFutureType(final IndexFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitInterestRateFutureType(final InterestRateFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitMetalFutureType(final MetalFutureBean bean) {
        postPersistCommodityFuture();
        return null;
      }

      @Override
      public Object visitStockFutureType(final StockFutureBean bean) {
        postPersistFuture();
        return null;
      }

      @Override
      public Object visitFederalFundsFutureType(final FederalFundsFutureBean bean) {
        postPersistFuture();
        return null;
      }
    });
  }

  @Override
  public FutureSecurityBean createBean(final OperationContext context,
                                       final HibernateSecurityMasterDao secMasterSession,
                                       final FutureSecurity security) {
    return security.accept(new FinancialSecurityVisitorAdapter<FutureSecurityBean>() {

      private <F extends FutureSecurityBean> F createFutureBean(final F bean, final FutureSecurity security) {
        bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
        bean.setTradingExchange(secMasterSession
          .getOrCreateExchangeBean(security.getTradingExchange(),
            null));
        bean.setSettlementExchange(secMasterSession
          .getOrCreateExchangeBean(
            security.getSettlementExchange(), null));
        bean.setCurrency(secMasterSession
          .getOrCreateCurrencyBean(security.getCurrency()
            .getCode()));
        bean.setUnitAmount(security.getUnitAmount());
        bean.setCategory(secMasterSession.getOrCreateContractCategoryBean(security.getContractCategory()));
        return bean;
      }

      private <F extends CommodityFutureBean> F createCommodityFutureBean(
        final F futureSecurityBean, final CommodityFutureSecurity security) {
        final F bean = createFutureBean(futureSecurityBean, security);
        if (security.getUnitName() != null) {
          bean.setUnitName(secMasterSession
            .getOrCreateUnitNameBean(security.getUnitName()));
        }
        if (security.getUnitNumber() != null) {
          bean.setUnitNumber(security.getUnitNumber());
        }
        return bean;
      }

      @Override
      public AgricultureFutureBean visitAgricultureFutureSecurity(
        final AgricultureFutureSecurity security) {
        return createCommodityFutureBean(new AgricultureFutureBean(), security);
      }

      @Override
      public BondFutureBean visitBondFutureSecurity(
        final BondFutureSecurity security) {
        final BondFutureBean bean = createFutureBean(new BondFutureBean(), security);
        bean.setFirstDeliveryDate(dateTimeWithZoneToZonedDateTimeBean(security
          .getFirstDeliveryDate()));
        bean.setLastDeliveryDate(dateTimeWithZoneToZonedDateTimeBean(security
          .getLastDeliveryDate()));
        final Collection<BondFutureDeliverable> basket = security
          .getBasket();
        final Set<FutureBundleBean> basketBeans = new HashSet<>(
          basket.size());
        for (final BondFutureDeliverable deliverable : basket) {
          final FutureBundleBean deliverableBean = new FutureBundleBean();
          deliverableBean.setFuture(bean);
          deliverableBean.setConversionFactor(deliverable
            .getConversionFactor());
          final Set<ExternalId> identifiers = deliverable
            .getIdentifiers().getExternalIds();
          final Set<ExternalIdBean> identifierBeans = new HashSet<>();
          for (final ExternalId identifier : identifiers) {
            identifierBeans
              .add(externalIdToExternalIdBean(identifier));
          }
          deliverableBean.setIdentifiers(identifierBeans);
          basketBeans.add(deliverableBean);
        }
        bean.setBasket(basketBeans);
        return bean;
      }

      @Override
      public EnergyFutureBean visitEnergyFutureSecurity(
        final EnergyFutureSecurity security) {
        final EnergyFutureBean bean = createCommodityFutureBean(new EnergyFutureBean(), security);
        final ExternalId underlying = security.getUnderlyingId();
        if (underlying != null) {
          bean.setUnderlying(externalIdToExternalIdBean(underlying));
        }
        return bean;
      }

      @Override
      public ForeignExchangeFutureBean visitFXFutureSecurity(
        final FXFutureSecurity security) {
        final ForeignExchangeFutureBean bean = createFutureBean(new ForeignExchangeFutureBean(), security);
        bean.setNumerator(secMasterSession
          .getOrCreateCurrencyBean(security.getNumerator()
            .getCode()));
        bean.setDenominator(secMasterSession
          .getOrCreateCurrencyBean(security.getDenominator()
            .getCode()));
        bean.setUnitNumber(security.getMultiplicationFactor());
        return bean;
      }

      @Override
      public InterestRateFutureBean visitInterestRateFutureSecurity(
        final InterestRateFutureSecurity security) {
        final InterestRateFutureBean bean = createFutureBean(new InterestRateFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
        return bean;
      }

      @Override
      public MetalFutureBean visitMetalFutureSecurity(
        final MetalFutureSecurity security) {
        final MetalFutureBean bean = createCommodityFutureBean(new MetalFutureBean(), security);
        final ExternalId underlying = security.getUnderlyingId();
        if (underlying != null) {
          bean.setUnderlying(externalIdToExternalIdBean(security.getUnderlyingId()));
        }
        return bean;
      }

      @Override
      public IndexFutureBean visitIndexFutureSecurity(
        final IndexFutureSecurity security) {
        final IndexFutureBean bean = createFutureBean(new IndexFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
          .getUnderlyingId()));
        return bean;
      }

      @Override
      public StockFutureBean visitStockFutureSecurity(
        final StockFutureSecurity security) {
        final StockFutureBean bean = createFutureBean(new StockFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
          .getUnderlyingId()));
        return bean;
      }

      @Override
      public EquityFutureBean visitEquityFutureSecurity(
        final EquityFutureSecurity security) {
        // TODO Case: Confirm this add is correct
        final EquityFutureBean bean = createFutureBean(new EquityFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
          .getUnderlyingId()));
        return bean;
      }

      @Override
      public EquityIndexDividendFutureBean visitEquityIndexDividendFutureSecurity(
        final EquityIndexDividendFutureSecurity security) {
        // TODO Case: Confirm this add is correct
        final EquityIndexDividendFutureBean bean = createFutureBean(new EquityIndexDividendFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
          .getUnderlyingId()));
        return bean;
      }

      @Override
      public FutureSecurityBean visitFederalFundsFutureSecurity(final FederalFundsFutureSecurity security) {
        final FederalFundsFutureBean bean = createFutureBean(new FederalFundsFutureBean(), security);
        bean.setUnderlying(externalIdToExternalIdBean(security
            .getUnderlyingId()));
        return bean;
      }
    });
  }

}
