/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.factory.master;

import static com.opengamma.component.factory.master.DBMasterComponentUtils.isValidJmsConfiguration;

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
import org.springframework.context.Lifecycle;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.change.BasicChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.change.JmsChangeManager;
import com.opengamma.id.ObjectIdSupplier;
import com.opengamma.master.region.RegionMaster;
import com.opengamma.master.region.impl.DataRegionMasterResource;
import com.opengamma.master.region.impl.InMemoryRegionMaster;
import com.opengamma.master.region.impl.RegionFileReader;
import com.opengamma.master.region.impl.RemoteRegionMaster;
import com.opengamma.util.jms.JmsConnector;

/**
 * Component factory for the database region master.
 */
@BeanDefinition
public class InMemoryRegionMasterComponentFactory extends AbstractComponentFactory {

  /**
   * The classifier that the factory should publish under.
   */
  @PropertyDefinition(validate = "notNull")
  private String _classifier;

  /**
   * Whether to use change management. If true, requires jms settings to be non-null.
   */
  @PropertyDefinition
  private boolean _enableChangeManagement = true;

  /**
   * The flag determining whether the component should be published by REST (default true).
   */
  @PropertyDefinition
  private boolean _publishRest = true;
  /**
   * The JMS connector.
   */
  @PropertyDefinition
  private JmsConnector _jmsConnector;
  /**
   * The JMS change manager topic.
   */
  @PropertyDefinition
  private String _jmsChangeManagerTopic;
  /**
   * The scheme used by the {@code UniqueId}.
   */
  @PropertyDefinition
  private String _uniqueIdScheme;

  //-------------------------------------------------------------------------
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final ComponentInfo info = new ComponentInfo(RegionMaster.class, getClassifier());

    // create
    final String scheme = getUniqueIdScheme() != null ? getUniqueIdScheme() : InMemoryRegionMaster.DEFAULT_OID_SCHEME;
    ChangeManager cm = new BasicChangeManager();
    if (isEnableChangeManagement() && isValidJmsConfiguration(getClassifier(), getClass(), getJmsConnector(), getJmsChangeManagerTopic())) {
      cm = new JmsChangeManager(getJmsConnector(), getJmsChangeManagerTopic());
      repo.registerLifecycle((Lifecycle) cm);
      if (getJmsConnector().getClientBrokerUri() != null) {
        info.addAttribute(ComponentInfoAttributes.JMS_BROKER_URI, getJmsConnector().getClientBrokerUri().toString());
      }
      info.addAttribute(ComponentInfoAttributes.JMS_CHANGE_MANAGER_TOPIC, getJmsChangeManagerTopic());
    }
    final InMemoryRegionMaster master = new InMemoryRegionMaster(new ObjectIdSupplier(scheme), cm);
    RegionFileReader.createPopulated(master);

    // register
    info.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    if (isPublishRest()) {
      info.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteRegionMaster.class);
    }
    info.addAttribute(ComponentInfoAttributes.UNIQUE_ID_SCHEME, scheme);
    repo.registerComponent(info, master);

    // publish
    if (isPublishRest()) {
      repo.getRestComponents().publish(info, new DataRegionMasterResource(master));
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code InMemoryRegionMasterComponentFactory}.
   * @return the meta-bean, not null
   */
  public static InMemoryRegionMasterComponentFactory.Meta meta() {
    return InMemoryRegionMasterComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(InMemoryRegionMasterComponentFactory.Meta.INSTANCE);
  }

  @Override
  public InMemoryRegionMasterComponentFactory.Meta metaBean() {
    return InMemoryRegionMasterComponentFactory.Meta.INSTANCE;
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
   * Gets whether to use change management. If true, requires jms settings to be non-null.
   * @return the value of the property
   */
  public boolean isEnableChangeManagement() {
    return _enableChangeManagement;
  }

  /**
   * Sets whether to use change management. If true, requires jms settings to be non-null.
   * @param enableChangeManagement  the new value of the property
   */
  public void setEnableChangeManagement(boolean enableChangeManagement) {
    this._enableChangeManagement = enableChangeManagement;
  }

  /**
   * Gets the the {@code enableChangeManagement} property.
   * @return the property, not null
   */
  public final Property<Boolean> enableChangeManagement() {
    return metaBean().enableChangeManagement().createProperty(this);
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
   * Gets the JMS connector.
   * @return the value of the property
   */
  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  /**
   * Sets the JMS connector.
   * @param jmsConnector  the new value of the property
   */
  public void setJmsConnector(JmsConnector jmsConnector) {
    this._jmsConnector = jmsConnector;
  }

  /**
   * Gets the the {@code jmsConnector} property.
   * @return the property, not null
   */
  public final Property<JmsConnector> jmsConnector() {
    return metaBean().jmsConnector().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the JMS change manager topic.
   * @return the value of the property
   */
  public String getJmsChangeManagerTopic() {
    return _jmsChangeManagerTopic;
  }

  /**
   * Sets the JMS change manager topic.
   * @param jmsChangeManagerTopic  the new value of the property
   */
  public void setJmsChangeManagerTopic(String jmsChangeManagerTopic) {
    this._jmsChangeManagerTopic = jmsChangeManagerTopic;
  }

  /**
   * Gets the the {@code jmsChangeManagerTopic} property.
   * @return the property, not null
   */
  public final Property<String> jmsChangeManagerTopic() {
    return metaBean().jmsChangeManagerTopic().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the scheme used by the {@code UniqueId}.
   * @return the value of the property
   */
  public String getUniqueIdScheme() {
    return _uniqueIdScheme;
  }

  /**
   * Sets the scheme used by the {@code UniqueId}.
   * @param uniqueIdScheme  the new value of the property
   */
  public void setUniqueIdScheme(String uniqueIdScheme) {
    this._uniqueIdScheme = uniqueIdScheme;
  }

  /**
   * Gets the the {@code uniqueIdScheme} property.
   * @return the property, not null
   */
  public final Property<String> uniqueIdScheme() {
    return metaBean().uniqueIdScheme().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public InMemoryRegionMasterComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      InMemoryRegionMasterComponentFactory other = (InMemoryRegionMasterComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isEnableChangeManagement() == other.isEnableChangeManagement()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getJmsConnector(), other.getJmsConnector()) &&
          JodaBeanUtils.equal(getJmsChangeManagerTopic(), other.getJmsChangeManagerTopic()) &&
          JodaBeanUtils.equal(getUniqueIdScheme(), other.getUniqueIdScheme()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getClassifier());
    hash = hash * 31 + JodaBeanUtils.hashCode(isEnableChangeManagement());
    hash = hash * 31 + JodaBeanUtils.hashCode(isPublishRest());
    hash = hash * 31 + JodaBeanUtils.hashCode(getJmsConnector());
    hash = hash * 31 + JodaBeanUtils.hashCode(getJmsChangeManagerTopic());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUniqueIdScheme());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("InMemoryRegionMasterComponentFactory{");
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
    buf.append("enableChangeManagement").append('=').append(JodaBeanUtils.toString(isEnableChangeManagement())).append(',').append(' ');
    buf.append("publishRest").append('=').append(JodaBeanUtils.toString(isPublishRest())).append(',').append(' ');
    buf.append("jmsConnector").append('=').append(JodaBeanUtils.toString(getJmsConnector())).append(',').append(' ');
    buf.append("jmsChangeManagerTopic").append('=').append(JodaBeanUtils.toString(getJmsChangeManagerTopic())).append(',').append(' ');
    buf.append("uniqueIdScheme").append('=').append(JodaBeanUtils.toString(getUniqueIdScheme())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code InMemoryRegionMasterComponentFactory}.
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
        this, "classifier", InMemoryRegionMasterComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code enableChangeManagement} property.
     */
    private final MetaProperty<Boolean> _enableChangeManagement = DirectMetaProperty.ofReadWrite(
        this, "enableChangeManagement", InMemoryRegionMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", InMemoryRegionMasterComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code jmsConnector} property.
     */
    private final MetaProperty<JmsConnector> _jmsConnector = DirectMetaProperty.ofReadWrite(
        this, "jmsConnector", InMemoryRegionMasterComponentFactory.class, JmsConnector.class);
    /**
     * The meta-property for the {@code jmsChangeManagerTopic} property.
     */
    private final MetaProperty<String> _jmsChangeManagerTopic = DirectMetaProperty.ofReadWrite(
        this, "jmsChangeManagerTopic", InMemoryRegionMasterComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     */
    private final MetaProperty<String> _uniqueIdScheme = DirectMetaProperty.ofReadWrite(
        this, "uniqueIdScheme", InMemoryRegionMasterComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "enableChangeManagement",
        "publishRest",
        "jmsConnector",
        "jmsChangeManagerTopic",
        "uniqueIdScheme");

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
        case 981110710:  // enableChangeManagement
          return _enableChangeManagement;
        case -614707837:  // publishRest
          return _publishRest;
        case -1495762275:  // jmsConnector
          return _jmsConnector;
        case -758086398:  // jmsChangeManagerTopic
          return _jmsChangeManagerTopic;
        case -1737146991:  // uniqueIdScheme
          return _uniqueIdScheme;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends InMemoryRegionMasterComponentFactory> builder() {
      return new DirectBeanBuilder<InMemoryRegionMasterComponentFactory>(new InMemoryRegionMasterComponentFactory());
    }

    @Override
    public Class<? extends InMemoryRegionMasterComponentFactory> beanType() {
      return InMemoryRegionMasterComponentFactory.class;
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
     * The meta-property for the {@code enableChangeManagement} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> enableChangeManagement() {
      return _enableChangeManagement;
    }

    /**
     * The meta-property for the {@code publishRest} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> publishRest() {
      return _publishRest;
    }

    /**
     * The meta-property for the {@code jmsConnector} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<JmsConnector> jmsConnector() {
      return _jmsConnector;
    }

    /**
     * The meta-property for the {@code jmsChangeManagerTopic} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> jmsChangeManagerTopic() {
      return _jmsChangeManagerTopic;
    }

    /**
     * The meta-property for the {@code uniqueIdScheme} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> uniqueIdScheme() {
      return _uniqueIdScheme;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          return ((InMemoryRegionMasterComponentFactory) bean).getClassifier();
        case 981110710:  // enableChangeManagement
          return ((InMemoryRegionMasterComponentFactory) bean).isEnableChangeManagement();
        case -614707837:  // publishRest
          return ((InMemoryRegionMasterComponentFactory) bean).isPublishRest();
        case -1495762275:  // jmsConnector
          return ((InMemoryRegionMasterComponentFactory) bean).getJmsConnector();
        case -758086398:  // jmsChangeManagerTopic
          return ((InMemoryRegionMasterComponentFactory) bean).getJmsChangeManagerTopic();
        case -1737146991:  // uniqueIdScheme
          return ((InMemoryRegionMasterComponentFactory) bean).getUniqueIdScheme();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((InMemoryRegionMasterComponentFactory) bean).setClassifier((String) newValue);
          return;
        case 981110710:  // enableChangeManagement
          ((InMemoryRegionMasterComponentFactory) bean).setEnableChangeManagement((Boolean) newValue);
          return;
        case -614707837:  // publishRest
          ((InMemoryRegionMasterComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -1495762275:  // jmsConnector
          ((InMemoryRegionMasterComponentFactory) bean).setJmsConnector((JmsConnector) newValue);
          return;
        case -758086398:  // jmsChangeManagerTopic
          ((InMemoryRegionMasterComponentFactory) bean).setJmsChangeManagerTopic((String) newValue);
          return;
        case -1737146991:  // uniqueIdScheme
          ((InMemoryRegionMasterComponentFactory) bean).setUniqueIdScheme((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((InMemoryRegionMasterComponentFactory) bean)._classifier, "classifier");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
