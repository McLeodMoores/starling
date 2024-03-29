/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.source;

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

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.financial.analytics.volatility.cube.ConfigDBVolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.financial.analytics.volatility.cube.rest.DataVolatilityCubeDefinitionSourceResource;
import com.opengamma.financial.analytics.volatility.cube.rest.RemoteVolatilityCubeDefinitionSource;

/**
 * Component factory providing the {@code VolatilityCubeDefinitionSource}.
 */
@BeanDefinition
@SuppressWarnings("deprecation")
public class VolatilityCubeDefinitionSourceComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The config source to wrap.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigSource _configSource;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final ComponentInfo info = new ComponentInfo(VolatilityCubeDefinitionSource.class, getClassifier());
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    if (isPublishRest()) {
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteVolatilityCubeDefinitionSource.class);
    }

    final VolatilityCubeDefinitionSource base = new ConfigDBVolatilityCubeDefinitionSource(getConfigSource());

    repo.registerComponent(info, base);
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataVolatilityCubeDefinitionSourceResource(base));
    }

  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code VolatilityCubeDefinitionSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static VolatilityCubeDefinitionSourceComponentFactory.Meta meta() {
    return VolatilityCubeDefinitionSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(VolatilityCubeDefinitionSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public VolatilityCubeDefinitionSourceComponentFactory.Meta metaBean() {
    return VolatilityCubeDefinitionSourceComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the classifier that the factory should publish under.
   * @return the value of the property, not null
   */
  public String getClassifier() {
    return _classifier;
  }

  /**
   * Sets the classifier that the factory should publish under.
   * @param classifier  the new value of the property, not null
   */
  public void setClassifier(String classifier) {
    JodaBeanUtils.notNull(classifier, "classifier");
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
   * Gets the flag determining whether the component should be published by REST (default true).
   * @return the value of the property
   */
  public boolean isPublishRest() {
    return _publishRest;
  }

  /**
   * Sets the flag determining whether the component should be published by REST (default true).
   * @param publishRest  the new value of the property
   */
  public void setPublishRest(boolean publishRest) {
    this._publishRest = publishRest;
  }

  /**
   * Gets the the {@code publishRest} property.
   * @return the property, not null
   */
  public final Property<Boolean> publishRest() {
    return metaBean().publishRest().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config source to wrap.
   * @return the value of the property, not null
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets the config source to wrap.
   * @param configSource  the new value of the property, not null
   */
  public void setConfigSource(ConfigSource configSource) {
    JodaBeanUtils.notNull(configSource, "configSource");
    this._configSource = configSource;
  }

  /**
   * Gets the the {@code configSource} property.
   * @return the property, not null
   */
  public final Property<ConfigSource> configSource() {
    return metaBean().configSource().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public VolatilityCubeDefinitionSourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      VolatilityCubeDefinitionSourceComponentFactory other = (VolatilityCubeDefinitionSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getConfigSource(), other.getConfigSource()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("VolatilityCubeDefinitionSourceComponentFactory{");
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
    buf.append("publishRest").append('=').append(JodaBeanUtils.toString(isPublishRest())).append(',').append(' ');
    buf.append("configSource").append('=').append(JodaBeanUtils.toString(getConfigSource())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code VolatilityCubeDefinitionSourceComponentFactory}.
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
        this, "classifier", VolatilityCubeDefinitionSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", VolatilityCubeDefinitionSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", VolatilityCubeDefinitionSourceComponentFactory.class, ConfigSource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "configSource");

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
        case -614707837:  // publishRest
          return _publishRest;
        case 195157501:  // configSource
          return _configSource;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends VolatilityCubeDefinitionSourceComponentFactory> builder() {
      return new DirectBeanBuilder<VolatilityCubeDefinitionSourceComponentFactory>(new VolatilityCubeDefinitionSourceComponentFactory());
    }

    @Override
    public Class<? extends VolatilityCubeDefinitionSourceComponentFactory> beanType() {
      return VolatilityCubeDefinitionSourceComponentFactory.class;
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
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code configSource} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ConfigSource> configSource() {
      return _configSource;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((VolatilityCubeDefinitionSourceComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((VolatilityCubeDefinitionSourceComponentFactory) bean).isPublishRest();
        case 195157501:  // configSource
          return ((VolatilityCubeDefinitionSourceComponentFactory) bean).getConfigSource();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((VolatilityCubeDefinitionSourceComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((VolatilityCubeDefinitionSourceComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case 195157501:  // configSource
          ((VolatilityCubeDefinitionSourceComponentFactory) bean).setConfigSource((ConfigSource) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((VolatilityCubeDefinitionSourceComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((VolatilityCubeDefinitionSourceComponentFactory) bean)._configSource, "configSource");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
