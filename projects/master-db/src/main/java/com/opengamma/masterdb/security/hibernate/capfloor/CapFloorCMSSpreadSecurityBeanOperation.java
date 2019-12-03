/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.capfloor;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.dateTimeWithZoneToZonedDateTimeBean;
import static com.opengamma.masterdb.security.hibernate.Converters.dayCountBeanToDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.frequencyBeanToFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.validateDayCount;
import static com.opengamma.masterdb.security.hibernate.Converters.validateFrequency;
import static com.opengamma.masterdb.security.hibernate.Converters.zonedDateTimeBeanToDateTimeWithZone;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.frequency.Frequency;
import com.opengamma.financial.security.capfloor.CapFloorCMSSpreadSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;

/**
 * Bean/security conversion operations.
 */
public final class CapFloorCMSSpreadSecurityBeanOperation extends AbstractSecurityBeanOperation<CapFloorCMSSpreadSecurity, CapFloorCMSSpreadSecurityBean> {

  /**
   * Singleton instance.
   */
  public static final CapFloorCMSSpreadSecurityBeanOperation INSTANCE = new CapFloorCMSSpreadSecurityBeanOperation();

  private CapFloorCMSSpreadSecurityBeanOperation() {
    super(CapFloorCMSSpreadSecurity.SECURITY_TYPE, CapFloorCMSSpreadSecurity.class, CapFloorCMSSpreadSecurityBean.class);
  }

  @Override
  public CapFloorCMSSpreadSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession,
      final CapFloorCMSSpreadSecurity security) {
    validateFrequency(security.getFrequency().getName());
    validateDayCount(security.getDayCount().getName());

    final CapFloorCMSSpreadSecurityBean bean = new CapFloorCMSSpreadSecurityBean();
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setDayCount(secMasterSession.getOrCreateDayCountBean(security.getDayCount().getName()));
    bean.setFrequency(secMasterSession.getOrCreateFrequencyBean(security.getFrequency().getName()));
    bean.setCap(security.isCap());
    bean.setPayer(security.isPayer());
    bean.setLongIdentifier(externalIdToExternalIdBean(security.getLongId()));
    bean.setMaturityDate(dateTimeWithZoneToZonedDateTimeBean(security.getMaturityDate()));
    bean.setNotional(security.getNotional());
    bean.setShortIdentifier(externalIdToExternalIdBean(security.getShortId()));
    bean.setStartDate(dateTimeWithZoneToZonedDateTimeBean(security.getStartDate()));
    bean.setStrike(security.getStrike());
    return bean;
  }

  @Override
  public CapFloorCMSSpreadSecurity createSecurity(final OperationContext context, final CapFloorCMSSpreadSecurityBean bean) {

    final ZonedDateTime startDate = zonedDateTimeBeanToDateTimeWithZone(bean.getStartDate());
    final ZonedDateTime maturityDate = zonedDateTimeBeanToDateTimeWithZone(bean.getMaturityDate());
    final ExternalId longIdentifier = externalIdBeanToExternalId(bean.getLongIdentifier());
    final ExternalId shortIdentifier = externalIdBeanToExternalId(bean.getShortIdentifier());
    final Frequency frequency = frequencyBeanToFrequency(bean.getFrequency());
    final Currency currency = currencyBeanToCurrency(bean.getCurrency());
    final DayCount dayCount = dayCountBeanToDayCount(bean.getDayCount());
    return new CapFloorCMSSpreadSecurity(startDate,
        maturityDate,
        bean.getNotional(),
        longIdentifier,
        shortIdentifier,
        bean.getStrike(),
        frequency,
        currency,
        dayCount,
        bean.isPayer(),
        bean.isCap());
  }

}
