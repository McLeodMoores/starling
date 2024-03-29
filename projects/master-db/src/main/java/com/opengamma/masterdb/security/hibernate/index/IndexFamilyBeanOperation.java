/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.masterdb.security.hibernate.index;

import static com.opengamma.masterdb.security.hibernate.Converters.externalIdBeanToExternalId;
import static com.opengamma.masterdb.security.hibernate.Converters.externalIdToExternalIdBean;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.opengamma.financial.security.index.IndexFamily;
import com.opengamma.id.ExternalId;
import com.opengamma.masterdb.security.hibernate.AbstractSecurityBeanOperation;
import com.opengamma.masterdb.security.hibernate.HibernateSecurityMasterDao;
import com.opengamma.masterdb.security.hibernate.OperationContext;
import com.opengamma.util.time.Tenor;

/**
 * Hibernate bean/security conversion operations.
 */
public final class IndexFamilyBeanOperation extends AbstractSecurityBeanOperation<IndexFamily, IndexFamilyBean> {

  /**
   * Singleton instance.
   */
  public static final IndexFamilyBeanOperation INSTANCE = new IndexFamilyBeanOperation();

  private IndexFamilyBeanOperation() {
    super(IndexFamily.METADATA_TYPE, IndexFamily.class, IndexFamilyBean.class);
  }

  @Override
  public IndexFamilyBean createBean(final OperationContext context, final HibernateSecurityMasterDao secMasterSession, final IndexFamily index) {
    final IndexFamilyBean bean = new IndexFamilyBean();
    final SortedMap<Tenor, ExternalId> members = index.getMembers();
    final Set<IndexFamilyEntryBean> entries = new HashSet<>();
    for (final Map.Entry<Tenor, ExternalId> entry : members.entrySet()) {
      final IndexFamilyEntryBean indexFamilyEntryBean = new IndexFamilyEntryBean();
      indexFamilyEntryBean.setTenor(secMasterSession.getOrCreateTenorBean(entry.getKey().toFormattedString()));
      indexFamilyEntryBean.setIdentifier(externalIdToExternalIdBean(entry.getValue()));
      entries.add(indexFamilyEntryBean);
    }
    bean.setEntries(entries);
    return bean;
  }

  @Override
  public IndexFamily createSecurity(final OperationContext context, final IndexFamilyBean bean) {
    final Set<IndexFamilyEntryBean> entries = bean.getEntries();
    final IndexFamily indexFamily = new IndexFamily();
    final SortedMap<Tenor, ExternalId> map = new TreeMap<>(); // these get ordered when inserting to the indexFamily
    for (final IndexFamilyEntryBean entry : entries) {
      final Tenor tenor = Tenor.parse(entry.getTenor().getName());
      map.put(tenor, externalIdBeanToExternalId(entry.getIdentifier()));
    }
    indexFamily.setMembers(map);
    return indexFamily;
  }

}
