/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.master;

import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.master.holiday.HolidayMaster;
import com.opengamma.master.holiday.impl.DataHolidayMasterResource;
import com.opengamma.master.holiday.impl.DataTrackingHolidayMaster;
import com.opengamma.master.holiday.impl.RemoteHolidayMaster;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.masterdb.holiday.DbHolidayMaster;
import com.opengamma.util.metric.OpenGammaMetricRegistry;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Component factory for the database holiday master.
 */
@BeanDefinition
public class DbHolidayMasterComponentFactory extends AbstractDocumentDbMasterComponentFactory<HolidayMaster, DbHolidayMaster> {


  public DbHolidayMasterComponentFactory() {
    super("exg", HolidayMaster.class);
  }

  @Override
  protected Class<? extends AbstractRemoteMaster> getRemoteInterface() {
    return RemoteHolidayMaster.class;
  }

  @Override
  protected DbHolidayMaster createDbDocumentMaster() {
    final DbHolidayMaster master = new DbHolidayMaster(getDbConnector());
    master.registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(), OpenGammaMetricRegistry.getDetailedInstance(), "DbHolidayMaster-" + getClassifier());
    return master;
  }

  @Override
  protected AbstractDataResource createPublishedResource(final DbHolidayMaster dbMaster, final HolidayMaster postProcessedMaster) {
    return new DataHolidayMasterResource(postProcessedMaster);
  }


  @Override
  protected HolidayMaster wrapMasterWithTrackingInterface(final HolidayMaster postProcessedMaster) {
    return new DataTrackingHolidayMaster(postProcessedMaster);
  }


  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DbHolidayMasterComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DbHolidayMasterComponentFactory.Meta meta() {
    return DbHolidayMasterComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DbHolidayMasterComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DbHolidayMasterComponentFactory.Meta metaBean() {
    return DbHolidayMasterComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  @Override
  public DbHolidayMasterComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(32);
    buf.append("DbHolidayMasterComponentFactory{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  @Override
  protected void toString(StringBuilder buf) {
    super.toString(buf);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DbHolidayMasterComponentFactory}.
   */
  public static class Meta extends AbstractDocumentDbMasterComponentFactory.Meta<HolidayMaster, DbHolidayMaster> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends DbHolidayMasterComponentFactory> builder() {
      return new DirectBeanBuilder<DbHolidayMasterComponentFactory>(new DbHolidayMasterComponentFactory());
    }

    @Override
    public Class<? extends DbHolidayMasterComponentFactory> beanType() {
      return DbHolidayMasterComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
