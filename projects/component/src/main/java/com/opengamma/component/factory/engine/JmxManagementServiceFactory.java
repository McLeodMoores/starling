/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.engine;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.management.MBeanServer;

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
import com.opengamma.component.factory.AbstractSpringComponentFactory;
import com.opengamma.engine.exec.stats.TotallingGraphStatisticsGathererProvider;
import com.opengamma.engine.management.ManagementService;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.engine.view.impl.ViewProcessorImpl;

/**
 * Factory class which allows MBeans for view processors, view processes and view clients to
 * be automatically registered with the MBean server.
 */
@BeanDefinition
public class JmxManagementServiceFactory extends AbstractSpringComponentFactory {

  /**
   * The view processor for which to register MBeans.
   */
  @PropertyDefinition(validate = "notNull")
  private ViewProcessor _viewProcessor;

  /**
   * MBean server to register with.
   */
  @PropertyDefinition(validate = "notNull")
  private MBeanServer _mBeanServer;

  /**
   * Indicates if MBeans should be categorized by view processor
   * or not. Default is false.
   */
  @PropertyDefinition
  private boolean _splitByViewProcessor;

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {

    if (_viewProcessor instanceof ViewProcessorImpl) {
      new ManagementService((ViewProcessorImpl) _viewProcessor,
                            new TotallingGraphStatisticsGathererProvider(),
                            _mBeanServer,
                            _splitByViewProcessor).init();
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code JmxManagementServiceFactory}.
   * @return the meta-bean, not null
   */
  public static JmxManagementServiceFactory.Meta meta() {
    return JmxManagementServiceFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(JmxManagementServiceFactory.Meta.INSTANCE);
  }

  @Override
  public JmxManagementServiceFactory.Meta metaBean() {
    return JmxManagementServiceFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the view processor for which to register MBeans.
   * @return the value of the property, not null
   */
  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  /**
   * Sets the view processor for which to register MBeans.
   * @param viewProcessor  the new value of the property, not null
   */
  public void setViewProcessor(ViewProcessor viewProcessor) {
    JodaBeanUtils.notNull(viewProcessor, "viewProcessor");
    this._viewProcessor = viewProcessor;
  }

  /**
   * Gets the the {@code viewProcessor} property.
   * @return the property, not null
   */
  public final Property<ViewProcessor> viewProcessor() {
    return metaBean().viewProcessor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets mBean server to register with.
   * @return the value of the property, not null
   */
  public MBeanServer getMBeanServer() {
    return _mBeanServer;
  }

  /**
   * Sets mBean server to register with.
   * @param mBeanServer  the new value of the property, not null
   */
  public void setMBeanServer(MBeanServer mBeanServer) {
    JodaBeanUtils.notNull(mBeanServer, "mBeanServer");
    this._mBeanServer = mBeanServer;
  }

  /**
   * Gets the the {@code mBeanServer} property.
   * @return the property, not null
   */
  public final Property<MBeanServer> mBeanServer() {
    return metaBean().mBeanServer().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets indicates if MBeans should be categorized by view processor
   * or not. Default is false.
   * @return the value of the property
   */
  public boolean isSplitByViewProcessor() {
    return _splitByViewProcessor;
  }

  /**
   * Sets indicates if MBeans should be categorized by view processor
   * or not. Default is false.
   * @param splitByViewProcessor  the new value of the property
   */
  public void setSplitByViewProcessor(boolean splitByViewProcessor) {
    this._splitByViewProcessor = splitByViewProcessor;
  }

  /**
   * Gets the the {@code splitByViewProcessor} property.
   * or not. Default is false.
   * @return the property, not null
   */
  public final Property<Boolean> splitByViewProcessor() {
    return metaBean().splitByViewProcessor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public JmxManagementServiceFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      JmxManagementServiceFactory other = (JmxManagementServiceFactory) obj;
      return JodaBeanUtils.equal(getViewProcessor(), other.getViewProcessor()) &&
          JodaBeanUtils.equal(getMBeanServer(), other.getMBeanServer()) &&
          (isSplitByViewProcessor() == other.isSplitByViewProcessor()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getViewProcessor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMBeanServer());
    hash = hash * 31 + JodaBeanUtils.hashCode(isSplitByViewProcessor());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("JmxManagementServiceFactory{");
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
    buf.append("viewProcessor").append('=').append(JodaBeanUtils.toString(getViewProcessor())).append(',').append(' ');
    buf.append("mBeanServer").append('=').append(JodaBeanUtils.toString(getMBeanServer())).append(',').append(' ');
    buf.append("splitByViewProcessor").append('=').append(JodaBeanUtils.toString(isSplitByViewProcessor())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code JmxManagementServiceFactory}.
   */
  public static class Meta extends AbstractSpringComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code viewProcessor} property.
     */
    private final MetaProperty<ViewProcessor> _viewProcessor = DirectMetaProperty.ofReadWrite(
        this, "viewProcessor", JmxManagementServiceFactory.class, ViewProcessor.class);
    /**
     * The meta-property for the {@code mBeanServer} property.
     */
    private final MetaProperty<MBeanServer> _mBeanServer = DirectMetaProperty.ofReadWrite(
        this, "mBeanServer", JmxManagementServiceFactory.class, MBeanServer.class);
    /**
     * The meta-property for the {@code splitByViewProcessor} property.
     */
    private final MetaProperty<Boolean> _splitByViewProcessor = DirectMetaProperty.ofReadWrite(
        this, "splitByViewProcessor", JmxManagementServiceFactory.class, Boolean.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "viewProcessor",
        "mBeanServer",
        "splitByViewProcessor");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1697555603:  // viewProcessor
          return _viewProcessor;
        case -1980390560:  // mBeanServer
          return _mBeanServer;
        case -127645220:  // splitByViewProcessor
          return _splitByViewProcessor;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends JmxManagementServiceFactory> builder() {
      return new DirectBeanBuilder<JmxManagementServiceFactory>(new JmxManagementServiceFactory());
    }

    @Override
    public Class<? extends JmxManagementServiceFactory> beanType() {
      return JmxManagementServiceFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code viewProcessor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ViewProcessor> viewProcessor() {
      return _viewProcessor;
    }

    /**
     * The meta-property for the {@code mBeanServer} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<MBeanServer> mBeanServer() {
      return _mBeanServer;
    }

    /**
     * The meta-property for the {@code splitByViewProcessor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> splitByViewProcessor() {
      return _splitByViewProcessor;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1697555603:  // viewProcessor
          return ((JmxManagementServiceFactory) bean).getViewProcessor();
        case -1980390560:  // mBeanServer
          return ((JmxManagementServiceFactory) bean).getMBeanServer();
        case -127645220:  // splitByViewProcessor
          return ((JmxManagementServiceFactory) bean).isSplitByViewProcessor();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1697555603:  // viewProcessor
          ((JmxManagementServiceFactory) bean).setViewProcessor((ViewProcessor) newValue);
          return;
        case -1980390560:  // mBeanServer
          ((JmxManagementServiceFactory) bean).setMBeanServer((MBeanServer) newValue);
          return;
        case -127645220:  // splitByViewProcessor
          ((JmxManagementServiceFactory) bean).setSplitByViewProcessor((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((JmxManagementServiceFactory) bean)._viewProcessor, "viewProcessor");
      JodaBeanUtils.notNull(((JmxManagementServiceFactory) bean)._mBeanServer, "mBeanServer");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}

