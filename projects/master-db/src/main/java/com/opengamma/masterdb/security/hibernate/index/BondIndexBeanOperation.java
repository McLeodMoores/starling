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
import com.opengamma.financial.security.index.BondIndex;
import com.opengamma.financial.security.index.BondIndexComponent;
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
public final class BondIndexBeanOperation extends AbstractSecurityBeanOperation<BondIndex, BondIndexBean> {

  /**
   * Singleton instance.
   */
  public static final BondIndexBeanOperation INSTANCE = new BondIndexBeanOperation();

  private BondIndexBeanOperation() {
    super(BondIndex.INDEX_TYPE, BondIndex.class, BondIndexBean.class);
  }

  @Override
  public BondIndexBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final BondIndex index) {
    final BondIndexBean bean = new BondIndexBean();
    bean.setDescription(index.getDescription());
    final List<BondIndexComponent> bondComponents = index.getBondComponents();
    final List<BondIndexComponentBean> bondComponentBeans = new ArrayList<>();
    long i = 0;
    for (final BondIndexComponent bondComponent : bondComponents) {
      final BondIndexComponentBean bondComponentBean = new BondIndexComponentBean();
      bondComponentBean.setWeight(bondComponent.getWeight());
      final Set<ExternalIdBean> idBundle = new HashSet<>();
      for (final ExternalId id : bondComponent.getBondIdentifier().getExternalIds()) {
        idBundle.add(externalIdToExternalIdBean(id));
      }
      bondComponentBean.setIdentifiers(idBundle);
      bondComponentBean.setPosition(i);
      i++;
      bondComponentBeans.add(bondComponentBean);
    }
    bean.setBondComponents(bondComponentBeans);
    final IndexWeightingTypeBean indexWeightingTypeBean = secMasterSession.getOrCreateIndexWeightingTypeBean(index.getWeightingType().name());
    bean.setWeightingType(indexWeightingTypeBean);
    if (index.getIndexFamilyId() != null) {
      bean.setIndexFamilyId(externalIdToExternalIdBean(index.getIndexFamilyId()));
    }
    return bean;
  }

  @Override
  public BondIndexBean resolve(final OperationContext context,
                                 final HibernateSecurityMasterDao secMasterSession, final Date now,
                                 final BondIndexBean bean) {
    final List<BondIndexComponentBean> indexComponents = secMasterSession.getBondIndexComponentBeans(bean);
    bean.setBondComponents(new ArrayList<>(indexComponents));
    return bean;
  }

  @Override
  public void postPersistBean(final OperationContext context,
      final HibernateSecurityMasterDao secMasterSession, final Date now,
      final BondIndexBean bean) {
    secMasterSession.persistBondIndexComponentBeans(bean);
  }

  @Override
  public BondIndex createSecurity(final OperationContext context, final BondIndexBean bean) {
    final String description = bean.getDescription();
    final IndexWeightingType weightingType = indexWeightingTypeBeanToIndexWeightingType(bean.getWeightingType());
    final List<BondIndexComponentBean> bondComponents = bean.getBondComponents();
    if (bondComponents == null) {
      throw new OpenGammaRuntimeException("null returned by getBondComponents, which breaks contract.");
    }
    final List<BondIndexComponent> components = new ArrayList<>();
    for (final BondIndexComponentBean component : bondComponents) {
      final Set<ExternalIdBean> identifiers = component.getIdentifiers();
      final List<ExternalId> ids = new ArrayList<>();
      for (final ExternalIdBean idBean : identifiers) {
        final ExternalId externalId = externalIdBeanToExternalId(idBean);
        ids.add(externalId);
      }
      final ExternalIdBundle externalIdBundle = ExternalIdBundle.of(ids);
      final BigDecimal weight = component.getWeight().stripTrailingZeros();
      final BondIndexComponent bondIndexComponent = new BondIndexComponent(externalIdBundle, weight);
      components.add(bondIndexComponent);
    }
    final BondIndex bondIndex = new BondIndex("", description, components, weightingType);
    if (bean.getIndexFamilyId() != null) {
      bondIndex.setIndexFamilyId(externalIdBeanToExternalId(bean.getIndexFamilyId()));
    }
    return bondIndex;
  }

}
