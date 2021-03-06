/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.master;

import java.util.Collection;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.position.impl.DataTrackingPositionMaster;
import com.opengamma.master.position.impl.ParallelQuerySplittingPositionMaster;
import com.opengamma.master.position.impl.PermissionedPositionMaster;
import com.opengamma.master.position.impl.QuerySplittingPositionMaster;
import com.opengamma.master.position.impl.RemotePositionMaster;
import com.opengamma.masterdb.position.DataDbPositionMasterResource;
import com.opengamma.masterdb.position.DbPositionMaster;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * Component factory for the database position master.
 */
@BeanDefinition
public class DbPositionMasterComponentFactory extends AbstractDocumentDbMasterComponentFactory<PositionMaster, DbPositionMaster> {

  /**
   * The maximum number of get requests to pass in one hit - see {@link QuerySplittingPositionMaster#get(Collection)}.
   */
  @PropertyDefinition
  private Integer _maxGetRequestSize;
  /**
   * The maximum size of search request to pass in one hit - see {@link QuerySplittingPositionMaster#search}.
   */
  @PropertyDefinition
  private Integer _maxSearchRequestSize;
  /**
   * Whether to use parallel search queries - see {@link ParallelQuerySplittingPositionMaster}.
   */
  @PropertyDefinition
  private boolean _parallelSearchQueries;

  /**
   * Creates an instance.
   */
  public DbPositionMasterComponentFactory() {
    super("pos", PositionMaster.class);
  }

  @Override
  protected Class<? extends AbstractRemoteMaster> getRemoteInterface() {
    return RemotePositionMaster.class;
  }

  // -------------------------------------------------------------------------
  @Override
  protected DbPositionMaster createDbDocumentMaster() {
    return new DbPositionMaster(getDbConnector());
  }

  @Override
  protected PositionMaster postProcess(final DbPositionMaster master) {
    return PermissionedPositionMaster.wrap(splitQueries(master));
  }

  @Override
  protected PositionMaster wrapMasterWithTrackingInterface(final PositionMaster postProcessedMaster) {
    return new DataTrackingPositionMaster(postProcessedMaster);
  }

  @Override
  protected AbstractDataResource createPublishedResource(final DbPositionMaster dbMaster, final PositionMaster postProcessedMaster) {
    // note - the db instance is required for this resource
    return new DataDbPositionMasterResource(dbMaster);
  }

  /**
   * If query splitting is enabled, wraps the position master with a query splitter.
   *
   * @param master
   *          the underlying master, not null
   * @return the original master if splitting is disabled, otherwise the splitting form
   */
  protected PositionMaster splitQueries(final PositionMaster master) {
    final QuerySplittingPositionMaster splitting = isParallelSearchQueries() ? new ParallelQuerySplittingPositionMaster(master)
        : new QuerySplittingPositionMaster(master);
    boolean wrapped = false;
    if (getMaxGetRequestSize() != null) {
      splitting.setMaxGetRequest(getMaxGetRequestSize());
      wrapped = true;
    }
    if (getMaxSearchRequestSize() != null) {
      splitting.setMaxSearchRequest(getMaxSearchRequestSize());
      wrapped = true;
    }
    if (wrapped) {
      return splitting;
    }
    return master;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DbPositionMasterComponentFactory}.
   * @return the meta-bean, not null
   */
  public static DbPositionMasterComponentFactory.Meta meta() {
    return DbPositionMasterComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DbPositionMasterComponentFactory.Meta.INSTANCE);
  }

  @Override
  public DbPositionMasterComponentFactory.Meta metaBean() {
    return DbPositionMasterComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum number of get requests to pass in one hit - see {@link QuerySplittingPositionMaster#get(Collection)}.
   * @return the value of the property
   */
  public Integer getMaxGetRequestSize() {
    return _maxGetRequestSize;
  }

  /**
   * Sets the maximum number of get requests to pass in one hit - see {@link QuerySplittingPositionMaster#get(Collection)}.
   * @param maxGetRequestSize  the new value of the property
   */
  public void setMaxGetRequestSize(Integer maxGetRequestSize) {
    this._maxGetRequestSize = maxGetRequestSize;
  }

  /**
   * Gets the the {@code maxGetRequestSize} property.
   * @return the property, not null
   */
  public final Property<Integer> maxGetRequestSize() {
    return metaBean().maxGetRequestSize().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum size of search request to pass in one hit - see {@link QuerySplittingPositionMaster#search}.
   * @return the value of the property
   */
  public Integer getMaxSearchRequestSize() {
    return _maxSearchRequestSize;
  }

  /**
   * Sets the maximum size of search request to pass in one hit - see {@link QuerySplittingPositionMaster#search}.
   * @param maxSearchRequestSize  the new value of the property
   */
  public void setMaxSearchRequestSize(Integer maxSearchRequestSize) {
    this._maxSearchRequestSize = maxSearchRequestSize;
  }

  /**
   * Gets the the {@code maxSearchRequestSize} property.
   * @return the property, not null
   */
  public final Property<Integer> maxSearchRequestSize() {
    return metaBean().maxSearchRequestSize().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets whether to use parallel search queries - see {@link ParallelQuerySplittingPositionMaster}.
   * @return the value of the property
   */
  public boolean isParallelSearchQueries() {
    return _parallelSearchQueries;
  }

  /**
   * Sets whether to use parallel search queries - see {@link ParallelQuerySplittingPositionMaster}.
   * @param parallelSearchQueries  the new value of the property
   */
  public void setParallelSearchQueries(boolean parallelSearchQueries) {
    this._parallelSearchQueries = parallelSearchQueries;
  }

  /**
   * Gets the the {@code parallelSearchQueries} property.
   * @return the property, not null
   */
  public final Property<Boolean> parallelSearchQueries() {
    return metaBean().parallelSearchQueries().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DbPositionMasterComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DbPositionMasterComponentFactory other = (DbPositionMasterComponentFactory) obj;
      return JodaBeanUtils.equal(getMaxGetRequestSize(), other.getMaxGetRequestSize()) &&
          JodaBeanUtils.equal(getMaxSearchRequestSize(), other.getMaxSearchRequestSize()) &&
          (isParallelSearchQueries() == other.isParallelSearchQueries()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaxGetRequestSize());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaxSearchRequestSize());
    hash = hash * 31 + JodaBeanUtils.hashCode(isParallelSearchQueries());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("DbPositionMasterComponentFactory{");
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
    buf.append("maxGetRequestSize").append('=').append(JodaBeanUtils.toString(getMaxGetRequestSize())).append(',').append(' ');
    buf.append("maxSearchRequestSize").append('=').append(JodaBeanUtils.toString(getMaxSearchRequestSize())).append(',').append(' ');
    buf.append("parallelSearchQueries").append('=').append(JodaBeanUtils.toString(isParallelSearchQueries())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DbPositionMasterComponentFactory}.
   */
  public static class Meta extends AbstractDocumentDbMasterComponentFactory.Meta<PositionMaster, DbPositionMaster> {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code maxGetRequestSize} property.
     */
    private final MetaProperty<Integer> _maxGetRequestSize = DirectMetaProperty.ofReadWrite(
        this, "maxGetRequestSize", DbPositionMasterComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code maxSearchRequestSize} property.
     */
    private final MetaProperty<Integer> _maxSearchRequestSize = DirectMetaProperty.ofReadWrite(
        this, "maxSearchRequestSize", DbPositionMasterComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code parallelSearchQueries} property.
     */
    private final MetaProperty<Boolean> _parallelSearchQueries = DirectMetaProperty.ofReadWrite(
        this, "parallelSearchQueries", DbPositionMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "maxGetRequestSize",
        "maxSearchRequestSize",
        "parallelSearchQueries");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -769924994:  // maxGetRequestSize
          return _maxGetRequestSize;
        case 2100076388:  // maxSearchRequestSize
          return _maxSearchRequestSize;
        case -337894953:  // parallelSearchQueries
          return _parallelSearchQueries;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DbPositionMasterComponentFactory> builder() {
      return new DirectBeanBuilder<DbPositionMasterComponentFactory>(new DbPositionMasterComponentFactory());
    }

    @Override
    public Class<? extends DbPositionMasterComponentFactory> beanType() {
      return DbPositionMasterComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code maxGetRequestSize} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxGetRequestSize() {
      return _maxGetRequestSize;
    }

    /**
     * The meta-property for the {@code maxSearchRequestSize} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxSearchRequestSize() {
      return _maxSearchRequestSize;
    }

    /**
     * The meta-property for the {@code parallelSearchQueries} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> parallelSearchQueries() {
      return _parallelSearchQueries;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -769924994:  // maxGetRequestSize
          return ((DbPositionMasterComponentFactory) bean).getMaxGetRequestSize();
        case 2100076388:  // maxSearchRequestSize
          return ((DbPositionMasterComponentFactory) bean).getMaxSearchRequestSize();
        case -337894953:  // parallelSearchQueries
          return ((DbPositionMasterComponentFactory) bean).isParallelSearchQueries();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -769924994:  // maxGetRequestSize
          ((DbPositionMasterComponentFactory) bean).setMaxGetRequestSize((Integer) newValue);
          return;
        case 2100076388:  // maxSearchRequestSize
          ((DbPositionMasterComponentFactory) bean).setMaxSearchRequestSize((Integer) newValue);
          return;
        case -337894953:  // parallelSearchQueries
          ((DbPositionMasterComponentFactory) bean).setParallelSearchQueries((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
