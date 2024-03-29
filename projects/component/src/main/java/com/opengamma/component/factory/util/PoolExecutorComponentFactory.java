/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.util;

import java.util.LinkedHashMap;
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

import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.util.PoolExecutor;

/**
 * A component factory for {@link PoolExecutor}.
 * Pool size can be controlled through either the {@code loadFactor} or
 * {@code maxThreads} properties. If neither is specified, a pool that has one
 * thread for each core (as presented to the JVM) is created.
 */
@BeanDefinition
public class PoolExecutorComponentFactory extends AbstractComponentFactory {
  /**
   * The classifier under which to publish.
   */
  @PropertyDefinition(validate = "notEmpty")
  private String _classifier;
  /**
   * The name for the pool (used in logging and thread naming).
   */
  @PropertyDefinition(validate = "notEmpty")
  private String _poolName;
  /**
   * The load factor to use for pool sizing; may be null.
   */
  @PropertyDefinition
  private Double _loadFactor;
  /**
   * The maximum number of threads to contruct; may be null.
   */
  @PropertyDefinition
  private Integer _maxThreads;

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {
    final PoolExecutor executor = new PoolExecutor(determineMaxThreads(), getPoolName());
    repo.registerComponent(PoolExecutor.class, getClassifier(), executor);
  }

  protected int determineMaxThreads() {
    if (getLoadFactor() != null && getMaxThreads() != null) {
      throw new IllegalStateException("Cannot specify both loadFactor and maxThreads properties.");
    }
    if (getMaxThreads() != null) {
      return getMaxThreads();
    }
    int physicalCores = Runtime.getRuntime().availableProcessors();
    if (getLoadFactor() != null) {
      if (getLoadFactor() <= 0.0) {
        throw new IllegalArgumentException("loadFactor cannot be 0 or negative.");
      }
      final double scaledCores = Math.ceil(getLoadFactor() * physicalCores);
      physicalCores = (int) scaledCores;
    }
    physicalCores = Math.max(physicalCores, 1);
    return physicalCores;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code PoolExecutorComponentFactory}.
   * @return the meta-bean, not null
   */
  public static PoolExecutorComponentFactory.Meta meta() {
    return PoolExecutorComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(PoolExecutorComponentFactory.Meta.INSTANCE);
  }

  @Override
  public PoolExecutorComponentFactory.Meta metaBean() {
    return PoolExecutorComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier under which to publish.
   * @return the value of the property, not empty
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier under which to publish.
   * @param classifier  the new value of the property, not empty
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notEmpty(classifier, "classifier");
    this._classifier = classifier;
  }

  /**
   * Gets the the {@code classifier} property.
   * @return the property, not null
   */
  public final Property<String> classifier() {
    return metaBean().classifier().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name for the pool (used in logging and thread naming).
   * @return the value of the property, not empty
   */
  public String getPoolName() {
    return _poolName;
  }

  /**
   * Sets the name for the pool (used in logging and thread naming).
   * @param poolName  the new value of the property, not empty
   */
  public void setPoolName(String poolName) {
    JodaBeanUtils.notEmpty(poolName, "poolName");
    this._poolName = poolName;
  }

  /**
   * Gets the the {@code poolName} property.
   * @return the property, not null
   */
  public final Property<String> poolName() {
    return metaBean().poolName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the load factor to use for pool sizing; may be null.
   * @return the value of the property
   */
  public Double getLoadFactor() {
    return _loadFactor;
  }

  /**
   * Sets the load factor to use for pool sizing; may be null.
   * @param loadFactor  the new value of the property
   */
  public void setLoadFactor(Double loadFactor) {
    this._loadFactor = loadFactor;
  }

  /**
   * Gets the the {@code loadFactor} property.
   * @return the property, not null
   */
  public final Property<Double> loadFactor() {
    return metaBean().loadFactor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum number of threads to contruct; may be null.
   * @return the value of the property
   */
  public Integer getMaxThreads() {
    return _maxThreads;
  }

  /**
   * Sets the maximum number of threads to contruct; may be null.
   * @param maxThreads  the new value of the property
   */
  public void setMaxThreads(Integer maxThreads) {
    this._maxThreads = maxThreads;
  }

  /**
   * Gets the the {@code maxThreads} property.
   * @return the property, not null
   */
  public final Property<Integer> maxThreads() {
    return metaBean().maxThreads().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public PoolExecutorComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      PoolExecutorComponentFactory other = (PoolExecutorComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getPoolName(), other.getPoolName()) &&
          JodaBeanUtils.equal(getLoadFactor(), other.getLoadFactor()) &&
          JodaBeanUtils.equal(getMaxThreads(), other.getMaxThreads()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPoolName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLoadFactor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaxThreads());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("PoolExecutorComponentFactory{");
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
    buf.append("classifier").append('=').append(JodaBeanUtils.toString(getClassifier())).append(',').append(' ');
    buf.append("poolName").append('=').append(JodaBeanUtils.toString(getPoolName())).append(',').append(' ');
    buf.append("loadFactor").append('=').append(JodaBeanUtils.toString(getLoadFactor())).append(',').append(' ');
    buf.append("maxThreads").append('=').append(JodaBeanUtils.toString(getMaxThreads())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code PoolExecutorComponentFactory}.
   */
  public static class Meta extends AbstractComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code classifier} property.
     */
    private final MetaProperty<String> _classifier = DirectMetaProperty.ofReadWrite(
        this, "classifier", PoolExecutorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code poolName} property.
     */
    private final MetaProperty<String> _poolName = DirectMetaProperty.ofReadWrite(
        this, "poolName", PoolExecutorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code loadFactor} property.
     */
    private final MetaProperty<Double> _loadFactor = DirectMetaProperty.ofReadWrite(
        this, "loadFactor", PoolExecutorComponentFactory.class, Double.class);
    /**
     * The meta-property for the {@code maxThreads} property.
     */
    private final MetaProperty<Integer> _maxThreads = DirectMetaProperty.ofReadWrite(
        this, "maxThreads", PoolExecutorComponentFactory.class, Integer.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "poolName",
        "loadFactor",
        "maxThreads");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return _classifier;
        case 634919111:  // poolName
          return _poolName;
        case -605952555:  // loadFactor
          return _loadFactor;
        case -164000347:  // maxThreads
          return _maxThreads;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends PoolExecutorComponentFactory> builder() {
      return new DirectBeanBuilder<PoolExecutorComponentFactory>(new PoolExecutorComponentFactory());
    }

    @Override
    public Class<? extends PoolExecutorComponentFactory> beanType() {
      return PoolExecutorComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code classifier} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> classifier() {
      return _classifier;
    }

    /**
     * The meta-property for the {@code poolName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> poolName() {
      return _poolName;
    }

    /**
     * The meta-property for the {@code loadFactor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> loadFactor() {
      return _loadFactor;
    }

    /**
     * The meta-property for the {@code maxThreads} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> maxThreads() {
      return _maxThreads;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((PoolExecutorComponentFactory) bean).getClassifier();
        case 634919111:  // poolName
          return ((PoolExecutorComponentFactory) bean).getPoolName();
        case -605952555:  // loadFactor
          return ((PoolExecutorComponentFactory) bean).getLoadFactor();
        case -164000347:  // maxThreads
          return ((PoolExecutorComponentFactory) bean).getMaxThreads();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((PoolExecutorComponentFactory) bean).setClassifier((String) newValue);
          return;
        case 634919111:  // poolName
          ((PoolExecutorComponentFactory) bean).setPoolName((String) newValue);
          return;
        case -605952555:  // loadFactor
          ((PoolExecutorComponentFactory) bean).setLoadFactor((Double) newValue);
          return;
        case -164000347:  // maxThreads
          ((PoolExecutorComponentFactory) bean).setMaxThreads((Integer) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notEmpty(((PoolExecutorComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notEmpty(((PoolExecutorComponentFactory) bean)._poolName, "poolName");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
