/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.equity;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.validateFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.equity.EquityVarianceSwapSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;

/**
 * A Hibernate helper.
 */
public final class EquityVarianceSwapSecurityBeanOperation extends AbstractSecurityBeanOperation<EquityVarianceSwapSecurity, EquityVarianceSwapSecurityBean> {

  /**
   * Singleton.
   */
  public static final EquityVarianceSwapSecurityBeanOperation INSTANCE = new EquityVarianceSwapSecurityBeanOperation();

  private EquityVarianceSwapSecurityBeanOperation() {
    super(EquityVarianceSwapSecurity.SECURITY_TYPE, EquityVarianceSwapSecurity.class, EquityVarianceSwapSecurityBean.class);
  }

  @Override
  public EquityVarianceSwapSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession,
      final EquityVarianceSwapSecurity security) {
    validateFrequency(security.getObservationFrequency().getName());

    final EquityVarianceSwapSecurityBean securityBean = new EquityVarianceSwapSecurityBean();
    securityBean.setAnnualizationFactor(security.getAnnualizationFactor());
    securityBean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    securityBean.setFirstObservationDate(dateTimeWithZoneToZonedDateTimeBean(security.getFirstObservationDate()));
    securityBean.setLastObservationDate(dateTimeWithZoneToZonedDateTimeBean(security.getLastObservationDate()));
    securityBean.setNotional(security.getNotional());
    securityBean.setObservationFrequency(secMasterSession.getOrCreateFrequencyBean(security.getObservationFrequency().getName()));
    securityBean.setParameterisedAsVariance(security.isParameterizedAsVariance());
    securityBean.setRegion(externalIdToExternalIdBean(security.getRegionId()));
    securityBean.setSettlementDate(dateTimeWithZoneToZonedDateTimeBean(security.getSettlementDate()));
    securityBean.setSpotUnderlyingIdentifier(externalIdToExternalIdBean(security.getSpotUnderlyingId()));
    securityBean.setStrike(security.getStrike());
    return securityBean;
  }

  @Override
  public EquityVarianceSwapSecurity createSecurity(final OperationContext context, final EquityVarianceSwapSecurityBean bean) {

    final Currency currency = currencyBeanToCurrency(bean.getCurrency());
    final ZonedDateTime firstObservationDate = zonedDateTimeBeanToDateTimeWithZone(bean.getFirstObservationDate());
    final ZonedDateTime lastObservationDate = zonedDateTimeBeanToDateTimeWithZone(bean.getLastObservationDate());
    final Frequency observationFrequency = frequencyBeanToFrequency(bean.getObservationFrequency());
    final ExternalId region = externalIdBeanToExternalId(bean.getRegion());
    final ZonedDateTime settlementDate = zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate());
    final ExternalId spotUnderlingId = externalIdBeanToExternalId(bean.getSpotUnderlyingIdentifier());

    return new EquityVarianceSwapSecurity(spotUnderlingId, currency, bean.getStrike(), bean.getNotional(),
        bean.isParameterisedAsVariance(), bean.getAnnualizationFactor(), firstObservationDate, lastObservationDate, settlementDate, region,
        observationFrequency);
  }

}
