/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.cds;

import static com.opengamma.masterdb.security.hibernate.Converters.cdsIndexComponentBeanToCDSIndexComponent;
import static com.opengamma.masterdb.security.hibernate.Converters.currencyBeanToCurrency;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.tenorBeanToTenor;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.opengamma.financial.security.cds.CDSIndexComponentBundle;
import com.opengamma.financial.security.cds.CDSIndexTerms;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexComponent;
import com.opengamma.financial.security.cds.CreditDefaultSwapIndexDefinitionSecurity;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.masterdb.security.hibernate.TenorBean;
import com.opengamma.util.time.Tenor;

/**
 *
 */
public final class CreditDefaultSwapIndexDefinitionSecurityBeanOperation
    extends AbstractSecurityBeanOperation<CreditDefaultSwapIndexDefinitionSecurity, CreditDefaultSwapIndexDefinitionSecurityBean> {

  /**
   * Singleton
   */
  public static final CreditDefaultSwapIndexDefinitionSecurityBeanOperation INSTANCE = new CreditDefaultSwapIndexDefinitionSecurityBeanOperation();

  private CreditDefaultSwapIndexDefinitionSecurityBeanOperation() {
    super(CreditDefaultSwapIndexDefinitionSecurity.SECURITY_TYPE, CreditDefaultSwapIndexDefinitionSecurity.class,
        CreditDefaultSwapIndexDefinitionSecurityBean.class);
  }

  @Override
  public CreditDefaultSwapIndexDefinitionSecurityBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession,
      final CreditDefaultSwapIndexDefinitionSecurity security) {

    final CreditDefaultSwapIndexDefinitionSecurityBean bean = new CreditDefaultSwapIndexDefinitionSecurityBean();
    bean.setVersion(security.getVersion());
    bean.setSeries(security.getSeries());
    bean.setCurrency(secMasterSession.getOrCreateCurrencyBean(security.getCurrency().getCode()));
    bean.setRecoveryRate(security.getRecoveryRate());
    bean.setFamily(secMasterSession.getOrCreateCDSIFamilyBean(security.getFamily()));

    final Set<TenorBean> tenors = bean.getTenors();
    for (final Tenor tenor : security.getTerms()) {
      tenors.add(secMasterSession.getOrCreateTenorBean(tenor.getPeriod().toString()));
    }

    final Set<CDSIndexComponentBean> componentBeans = bean.getComponents();
    for (final CreditDefaultSwapIndexComponent cdsiComponent : security.getComponents()) {
      final CDSIndexComponentBean componentBean = new CDSIndexComponentBean();
      componentBean.setWeight(cdsiComponent.getWeight());
      componentBean.setBondId(externalIdToExternalIdBean(cdsiComponent.getBondId()));
      componentBean.setObligor(externalIdToExternalIdBean(cdsiComponent.getObligorRedCode()));
      componentBean.setName(cdsiComponent.getName());

      componentBeans.add(componentBean);
    }

    return bean;
  }

  @Override
  public CreditDefaultSwapIndexDefinitionSecurity createSecurity(final OperationContext context, final CreditDefaultSwapIndexDefinitionSecurityBean bean) {
    final List<Tenor> tenors = Lists.newArrayList();
    for (final TenorBean tenorBean : bean.getTenors()) {
      tenors.add(tenorBeanToTenor(tenorBean));
    }

    final List<CreditDefaultSwapIndexComponent> components = Lists.newArrayList();
    for (final CDSIndexComponentBean cdsIndexComponentBean : bean.getComponents()) {
      components.add(cdsIndexComponentBeanToCDSIndexComponent(cdsIndexComponentBean));
    }

    final CreditDefaultSwapIndexDefinitionSecurity security = new CreditDefaultSwapIndexDefinitionSecurity(bean.getVersion(),
        bean.getSeries(),
        bean.getFamily().getName(),
        currencyBeanToCurrency(bean.getCurrency()),
        bean.getRecoveryRate(),
        CDSIndexTerms.of(tenors),
        CDSIndexComponentBundle.of(components));

    return security;
  }

}
