/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.index;

import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;
import static com.opengamma.masterdb.security.hibernate.Converters.indexWeightingTypeBeanToIndexWeightingType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.financial.security.index.EquityIndex;
import com.opengamma.financial.security.index.EquityIndexComponent;
import com.opengamma.financial.security.index.IndexWeightingType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.ExternalIdBean;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.IndexWeightingTypeBean;
import com.opengamma.masterdb.security.hibernate.OperationContext;

/**
 * Hibernate bean/security conversion operations.
 */
public final class EquityIndexBeanOperation extends AbstractSecurityBeanOperation<EquityIndex, EquityIndexBean> {

  /**
   * Singleton instance.
   */
  public static final EquityIndexBeanOperation INSTANCE = new EquityIndexBeanOperation();

  private EquityIndexBeanOperation() {
    super(EquityIndex.INDEX_TYPE, EquityIndex.class, EquityIndexBean.class);
  }

  @Override
  public EquityIndexBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final EquityIndex index) {
    final EquityIndexBean bean = new EquityIndexBean();
    bean.setDescription(index.getDescription());
    final List<EquityIndexComponent> equityComponents = index.getEquityComponents();
    final List<EquityIndexComponentBean> equityComponentBeans = new ArrayList<>();
    long i = 0;
    for (final EquityIndexComponent equityComponent : equityComponents) {
      final EquityIndexComponentBean equityComponentBean = new EquityIndexComponentBean();
      equityComponentBean.setWeight(equityComponent.getWeight());
      final Set<ExternalIdBean> idBundle = new HashSet<>();
      for (final ExternalId id : equityComponent.getEquityIdentifier().getExternalIds()) {
        idBundle.add(externalIdToExternalIdBean(id));
      }
      equityComponentBean.setIdentifiers(idBundle);
      equityComponentBean.setPosition(i);
      i++;
      equityComponentBeans.add(equityComponentBean);
    }
    bean.setEquityComponents(equityComponentBeans);
    final IndexWeightingTypeBean indexWeightingTypeBean = secMasterSession.getOrCreateIndexWeightingTypeBean(index.getWeightingType().name());
    bean.setWeightingType(indexWeightingTypeBean);
    if (index.getIndexFamilyId() != null) {
      bean.setIndexFamilyId(externalIdToExternalIdBean(index.getIndexFamilyId()));
    }
    return bean;
  }

  @Override
  public EquityIndexBean resolve(final OperationContext context,
                                 final HibernateSecurityMasterDao secMasterSession, final Date now,
                                 final EquityIndexBean bean) {
    final List<EquityIndexComponentBean> indexComponents = secMasterSession.getEquityIndexComponentBeans(bean);
    bean.setEquityComponents(new ArrayList<>(indexComponents));
    return bean;
  }

  @Override
  public void postPersistBean(final OperationContext context,
      final HibernateSecurityMasterDao secMasterSession, final Date now,
      final EquityIndexBean bean) {
    secMasterSession.persistEquityIndexComponentBeans(bean);
  }

  @Override
  public EquityIndex createSecurity(final OperationContext context, final EquityIndexBean bean) {
    final String description = bean.getDescription();
    final IndexWeightingType weightingType = indexWeightingTypeBeanToIndexWeightingType(bean.getWeightingType());
    final List<EquityIndexComponentBean> equityComponents = bean.getEquityComponents();
    if (equityComponents == null) {
      throw new OpenGammaRuntimeException("null returned by getEquityComponents, which breaks contract.");
    }
    final List<EquityIndexComponent> components = new ArrayList<>();
    for (final EquityIndexComponentBean component : equityComponents) {
      final Set<ExternalIdBean> identifiers = component.getIdentifiers();
      final List<ExternalId> ids = new ArrayList<>();
      for (final ExternalIdBean idBean : identifiers) {
        final ExternalId externalId = externalIdBeanToExternalId(idBean);
        ids.add(externalId);
      }
      final ExternalIdBundle externalIdBundle = ExternalIdBundle.of(ids);
      final BigDecimal weight = component.getWeight().stripTrailingZeros();
      final EquityIndexComponent equityIndexComponent = new EquityIndexComponent(externalIdBundle, weight);
      components.add(equityIndexComponent);
    }
    final EquityIndex equityIndex = new EquityIndex("", description, components, weightingType);
    if (bean.getIndexFamilyId() != null) {
      equityIndex.setIndexFamilyId(externalIdBeanToExternalId(bean.getIndexFamilyId()));
    }
    return equityIndex;
  }

}
