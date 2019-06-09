/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.option;

import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryBeanToExpiry;
import static com.opengamma.masterdb.security.hibernate.Converters.expiryToExpiryBean;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.security.option.ExerciseType;
import com.opengamma.financial.security.option.ExerciseTypeVisitorImpl;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.Converters;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Expiry;

/**
 * NonDeliverableFXOptionSecurityBeanOperation
 */
public final class NonDeliverableFxOptionSecurityBeanOperation
    extends AbstractSecurityBeanOperation<NonDeliverableFXOptionSecurity, NonDeliverableFXOptionSecurityBean> {

  /**
   * Singleton
   */
  public static final NonDeliverableFxOptionSecurityBeanOperation INSTANCE = new NonDeliverableFxOptionSecurityBeanOperation();

  private NonDeliverableFxOptionSecurityBeanOperation() {
    super(NonDeliverableFXOptionSecurity.SECURITY_TYPE, NonDeliverableFXOptionSecurity.class, NonDeliverableFXOptionSecurityBean.class);
  }

  @Override
  public NonDeliverableFXOptionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession,
      final NonDeliverableFXOptionSecurity security) {
    final NonDeliverableFXOptionSecurityBean bean = new NonDeliverableFXOptionSecurityBean();
    bean.setCallAmount(security.getCallAmount());
    bean.setPutAmount(security.getPutAmount());
    bean.setCallCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCallCurrency().getCode()));
    bean.setPutCurrency(secMasterSession.getOrCreateCurrencyBean(security.getPutCurrency().getCode()));
    bean.setExpiry(expiryToExpiryBean(security.getExpiry()));
    bean.setSettlementDate(Converters.dateTimeWithZoneToZonedDateTimeBean(security.getSettlementDate()));
    bean.setIsLong(security.isLong());
    bean.setOptionExerciseType(OptionExerciseType.identify(security.getExerciseType()));
    bean.setIsDeliveryInCallCurrency(security.isDeliveryInCallCurrency());
    return bean;
  }

  @Override
  public NonDeliverableFXOptionSecurity createSecurity(final OperationContext context, final NonDeliverableFXOptionSecurityBean bean) {
    final ExerciseType exerciseType = bean.getOptionExerciseType().accept(new ExerciseTypeVisitorImpl());
    final Currency putCurrency = currencyBeanToCurrency(bean.getPutCurrency());
    final Currency callCurrency = currencyBeanToCurrency(bean.getCallCurrency());
    final Expiry expiry = expiryBeanToExpiry(bean.getExpiry());
    final ZonedDateTime settlementDate = Converters.zonedDateTimeBeanToDateTimeWithZone(bean.getSettlementDate());
    final boolean isDeliveryInCallCurrency = bean.getIsDeliveryInCallCurrency();
    final NonDeliverableFXOptionSecurity sec = new NonDeliverableFXOptionSecurity(putCurrency, callCurrency, bean.getPutAmount(), bean.getCallAmount(), expiry,
        settlementDate, bean.getIsLong(), exerciseType, isDeliveryInCallCurrency);
    return sec;
  }

}
