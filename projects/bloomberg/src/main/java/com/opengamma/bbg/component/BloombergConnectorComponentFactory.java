/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.component;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
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

import com.bloomberglp.blpapi.SessionOptions;
import com.opengamma.bbg.BloombergConnector;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.referencedata.statistics.BloombergReferenceDataStatistics;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;

/**
 * Component factory for the Bloomberg connector.
 */
@BeanDefinition
public class BloombergConnectorComponentFactory extends AbstractComponentFactory {
  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;
  /**
   * The name of the data to be accessed, defaults to the classifier.
   */
  @PropertyDefinition
  private String _name;
  /**
   * The server host name.
   */
  @PropertyDefinition
  private String _host;
  /**
   * The server port number.
   */
  @PropertyDefinition
  private Integer _port;
  /**
   * The bpipe application name, if applicable.
   */
  @PropertyDefinition
  private String _applicationName;
  /**
   * The auto restart on disconnection.
   */
  @PropertyDefinition
  private boolean _autoRestartOnDisconnection = true;
  /**
   * The bloomberg reference data statistics.
   */
  @PropertyDefinition(validate = "notNull")
  private BloombergReferenceDataStatistics _referenceDataStatistics;

  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) throws Exception {
    if (getName() == null) {
      setName(getClassifier());
    }
    final ComponentInfo info = new ComponentInfo(BloombergConnector.class, getClassifier());

    final SessionOptions sessionOptions = new SessionOptions();
    if (getHost() != null) {
      sessionOptions.setServerHost(getHost());
    }
    if (getPort() != null) {
      sessionOptions.setServerPort(getPort());
    }
    final String applicationName = StringUtils.trimToNull(getApplicationName());
    if (applicationName != null) {
      sessionOptions.setAuthenticationOptions(BloombergConstants.AUTH_APP_PREFIX + applicationName);
    }
    sessionOptions.setAutoRestartOnDisconnection(isAutoRestartOnDisconnection());

    final BloombergConnector component = new BloombergConnector(getName(), sessionOptions, getReferenceDataStatistics());
    repo.registerComponent(info, component);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code BloombergConnectorComponentFactory}.
   * @return the meta-bean, not null
   */
  public static BloombergConnectorComponentFactory.Meta meta() {
    return BloombergConnectorComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(BloombergConnectorComponentFactory.Meta.INSTANCE);
  }

  @Override
  public BloombergConnectorComponentFactory.Meta metaBean() {
    return BloombergConnectorComponentFactory.Meta.INSTANCE;
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
   * Gets the name of the data to be accessed, defaults to the classifier.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name of the data to be accessed, defaults to the classifier.
   * @param name  the new value of the property
   */
  public void setName(String name) {
    this._name = name;
  }

  /**
   * Gets the the {@code name} property.
   * @return the property, not null
   */
  public final Property<String> name() {
    return metaBean().name().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the server host name.
   * @return the value of the property
   */
  public String getHost() {
    return _host;
  }

  /**
   * Sets the server host name.
   * @param host  the new value of the property
   */
  public void setHost(String host) {
    this._host = host;
  }

  /**
   * Gets the the {@code host} property.
   * @return the property, not null
   */
  public final Property<String> host() {
    return metaBean().host().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the server port number.
   * @return the value of the property
   */
  public Integer getPort() {
    return _port;
  }

  /**
   * Sets the server port number.
   * @param port  the new value of the property
   */
  public void setPort(Integer port) {
    this._port = port;
  }

  /**
   * Gets the the {@code port} property.
   * @return the property, not null
   */
  public final Property<Integer> port() {
    return metaBean().port().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bpipe application name, if applicable.
   * @return the value of the property
   */
  public String getApplicationName() {
    return _applicationName;
  }

  /**
   * Sets the bpipe application name, if applicable.
   * @param applicationName  the new value of the property
   */
  public void setApplicationName(String applicationName) {
    this._applicationName = applicationName;
  }

  /**
   * Gets the the {@code applicationName} property.
   * @return the property, not null
   */
  public final Property<String> applicationName() {
    return metaBean().applicationName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the auto restart on disconnection.
   * @return the value of the property
   */
  public boolean isAutoRestartOnDisconnection() {
    return _autoRestartOnDisconnection;
  }

  /**
   * Sets the auto restart on disconnection.
   * @param autoRestartOnDisconnection  the new value of the property
   */
  public void setAutoRestartOnDisconnection(boolean autoRestartOnDisconnection) {
    this._autoRestartOnDisconnection = autoRestartOnDisconnection;
  }

  /**
   * Gets the the {@code autoRestartOnDisconnection} property.
   * @return the property, not null
   */
  public final Property<Boolean> autoRestartOnDisconnection() {
    return metaBean().autoRestartOnDisconnection().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the bloomberg reference data statistics.
   * @return the value of the property, not null
   */
  public BloombergReferenceDataStatistics getReferenceDataStatistics() {
    return _referenceDataStatistics;
  }

  /**
   * Sets the bloomberg reference data statistics.
   * @param referenceDataStatistics  the new value of the property, not null
   */
  public void setReferenceDataStatistics(BloombergReferenceDataStatistics referenceDataStatistics) {
    JodaBeanUtils.notNull(referenceDataStatistics, "referenceDataStatistics");
    this._referenceDataStatistics = referenceDataStatistics;
  }

  /**
   * Gets the the {@code referenceDataStatistics} property.
   * @return the property, not null
   */
  public final Property<BloombergReferenceDataStatistics> referenceDataStatistics() {
    return metaBean().referenceDataStatistics().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public BloombergConnectorComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      BloombergConnectorComponentFactory other = (BloombergConnectorComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getHost(), other.getHost()) &&
          JodaBeanUtils.equal(getPort(), other.getPort()) &&
          JodaBeanUtils.equal(getApplicationName(), other.getApplicationName()) &&
          (isAutoRestartOnDisconnection() == other.isAutoRestartOnDisconnection()) &&
          JodaBeanUtils.equal(getReferenceDataStatistics(), other.getReferenceDataStatistics()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(getName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHost());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPort());
    hash = hash * 31 + JodaBeanUtils.hashCode(getApplicationName());
    hash = hash * 31 + JodaBeanUtils.hashCode(isAutoRestartOnDisconnection());
    hash = hash * 31 + JodaBeanUtils.hashCode(getReferenceDataStatistics());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("BloombergConnectorComponentFactory{");
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
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("host").append('=').append(JodaBeanUtils.toString(getHost())).append(',').append(' ');
    buf.append("port").append('=').append(JodaBeanUtils.toString(getPort())).append(',').append(' ');
    buf.append("applicationName").append('=').append(JodaBeanUtils.toString(getApplicationName())).append(',').append(' ');
    buf.append("autoRestartOnDisconnection").append('=').append(JodaBeanUtils.toString(isAutoRestartOnDisconnection())).append(',').append(' ');
    buf.append("referenceDataStatistics").append('=').append(JodaBeanUtils.toString(getReferenceDataStatistics())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code BloombergConnectorComponentFactory}.
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
        this, "classifier", BloombergConnectorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", BloombergConnectorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code host} property.
     */
    private final MetaProperty<String> _host = DirectMetaProperty.ofReadWrite(
        this, "host", BloombergConnectorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code port} property.
     */
    private final MetaProperty<Integer> _port = DirectMetaProperty.ofReadWrite(
        this, "port", BloombergConnectorComponentFactory.class, Integer.class);
    /**
     * The meta-property for the {@code applicationName} property.
     */
    private final MetaProperty<String> _applicationName = DirectMetaProperty.ofReadWrite(
        this, "applicationName", BloombergConnectorComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code autoRestartOnDisconnection} property.
     */
    private final MetaProperty<Boolean> _autoRestartOnDisconnection = DirectMetaProperty.ofReadWrite(
        this, "autoRestartOnDisconnection", BloombergConnectorComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code referenceDataStatistics} property.
     */
    private final MetaProperty<BloombergReferenceDataStatistics> _referenceDataStatistics = DirectMetaProperty.ofReadWrite(
        this, "referenceDataStatistics", BloombergConnectorComponentFactory.class, BloombergReferenceDataStatistics.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "name",
        "host",
        "port",
        "applicationName",
        "autoRestartOnDisconnection",
        "referenceDataStatistics");

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
        case 3373707:  // name
          return _name;
        case 3208616:  // host
          return _host;
        case 3446913:  // port
          return _port;
        case -1247425541:  // applicationName
          return _applicationName;
        case 1676276941:  // autoRestartOnDisconnection
          return _autoRestartOnDisconnection;
        case -1225958248:  // referenceDataStatistics
          return _referenceDataStatistics;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends BloombergConnectorComponentFactory> builder() {
      return new DirectBeanBuilder<BloombergConnectorComponentFactory>(new BloombergConnectorComponentFactory());
    }

    @Override
    public Class<? extends BloombergConnectorComponentFactory> beanType() {
      return BloombergConnectorComponentFactory.class;
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
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code host} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> host() {
      return _host;
    }

    /**
     * The meta-property for the {@code port} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> port() {
      return _port;
    }

    /**
     * The meta-property for the {@code applicationName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> applicationName() {
      return _applicationName;
    }

    /**
     * The meta-property for the {@code autoRestartOnDisconnection} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> autoRestartOnDisconnection() {
      return _autoRestartOnDisconnection;
    }

    /**
     * The meta-property for the {@code referenceDataStatistics} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BloombergReferenceDataStatistics> referenceDataStatistics() {
      return _referenceDataStatistics;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((BloombergConnectorComponentFactory) bean).getClassifier();
        case 3373707:  // name
          return ((BloombergConnectorComponentFactory) bean).getName();
        case 3208616:  // host
          return ((BloombergConnectorComponentFactory) bean).getHost();
        case 3446913:  // port
          return ((BloombergConnectorComponentFactory) bean).getPort();
        case -1247425541:  // applicationName
          return ((BloombergConnectorComponentFactory) bean).getApplicationName();
        case 1676276941:  // autoRestartOnDisconnection
          return ((BloombergConnectorComponentFactory) bean).isAutoRestartOnDisconnection();
        case -1225958248:  // referenceDataStatistics
          return ((BloombergConnectorComponentFactory) bean).getReferenceDataStatistics();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((BloombergConnectorComponentFactory) bean).setClassifier((String) newValue);
          return;
        case 3373707:  // name
          ((BloombergConnectorComponentFactory) bean).setName((String) newValue);
          return;
        case 3208616:  // host
          ((BloombergConnectorComponentFactory) bean).setHost((String) newValue);
          return;
        case 3446913:  // port
          ((BloombergConnectorComponentFactory) bean).setPort((Integer) newValue);
          return;
        case -1247425541:  // applicationName
          ((BloombergConnectorComponentFactory) bean).setApplicationName((String) newValue);
          return;
        case 1676276941:  // autoRestartOnDisconnection
          ((BloombergConnectorComponentFactory) bean).setAutoRestartOnDisconnection((Boolean) newValue);
          return;
        case -1225958248:  // referenceDataStatistics
          ((BloombergConnectorComponentFactory) bean).setReferenceDataStatistics((BloombergReferenceDataStatistics) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((BloombergConnectorComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((BloombergConnectorComponentFactory) bean)._referenceDataStatistics, "referenceDataStatistics");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
