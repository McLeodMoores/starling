/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;

/**
 * The complete set of components published in a RESTful way by JAX-RS.
 * <p>
 * Components may be managed by {@link DataComponentsResource} or by JAX-RS directly.
 */
@BeanDefinition
public class RestComponents extends DirectBean {

  /**
   * The managed components.
   * These will be controlled by {@link DataComponentsResource}.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<RestComponent> _localComponents = new ArrayList<RestComponent>();
  /**
   * The remote components.
   * These have been imported from another server and are being re-exposed.
   */
  @PropertyDefinition(validate = "notNull")
  private final List<ComponentInfo> _remoteComponents = new ArrayList<ComponentInfo>();
  /**
   * The set of root resources.
   * These are not managed by {@link DataComponentsResource}.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<Object> _rootResourceSingletons = new LinkedHashSet<Object>();
  /**
   * The set of root resource factories.
   * These are not managed by {@link DataComponentsResource}.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<RestResourceFactory> _rootResourceFactories = new LinkedHashSet<RestResourceFactory>();
  /**
   * The set of additional singleton JAX-RS helper objects that are used by JAX-RS.
   * This may include filters, providers and consumers that should be used directly by JAX-RS.
   */
  @PropertyDefinition(validate = "notNull")
  private final Set<Object> _helpers = new LinkedHashSet<Object>();

  /**
   * Creates an instance.
   */
  public RestComponents() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a managed component to the known set.
   * <p>
   * The instance is a JAX_RS class annotated with {@code Path} on the methods.
   * Any {@code Path} at the class level is ignored.
   * See {@link DataComponentsResource}.
   * 
   * @param info  the managed component info, not null
   * @param instance  the JAX-RS singleton instance, not null
   */
  public void publish(ComponentInfo info, Object instance) {
    ArgumentChecker.notNull(info, "info");
    ArgumentChecker.notNull(instance, "instance");
    if (info.getUri() != null) {
      throw new IllegalArgumentException("A managed component cannot set its own URI: " + info);
    }
    
    info.setUri(DataComponentsResource.relativeUri(info));
    getLocalComponents().add(new RestComponent(info, instance));
  }

  /**
   * Adds a JAX-RS helper instance to the known set.
   * <p>
   * This is used for JAX-RS consumers, producers and filters and unmanaged singleton resources.
   * These classes are not managed by {@code DataComponentsResource}.
   * 
   * @param instance  the JAX-RS singleton instance, not null
   */
  public void publishHelper(Object instance) {
    ArgumentChecker.notNull(instance, "instance");
    
    getHelpers().add(instance);
  }

  /**
   * Adds a JAX-RS root resource to the known set.
   * <p>
   * This is used for JAX-RS unmanaged resources.
   * The class is not managed by {@code DataComponentsResource}.
   * 
   * @param singletonInstance  the unmanaged singleton instance, not null
   */
  public void publishResource(Object singletonInstance) {
    ArgumentChecker.notNull(singletonInstance, "singletonInstance");
    
    getRootResourceSingletons().add(singletonInstance);
  }

  /**
   * Adds a JAX-RS root resource to the known set.
   * <p>
   * This is used for JAX-RS unmanaged resources.
   * These classes are not managed by {@code DataComponentsResource}.
   * 
   * @param factory  the factory for creating the resource per request, not null
   */
  public void publishResource(RestResourceFactory factory) {
    ArgumentChecker.notNull(factory, "factory");
    
    getRootResourceFactories().add(factory);
  }

  /**
   * Re-publishes the component.
   * <p>
   * This is used when a component is read in from a remote location and is then re-published.
   * 
   * @param info  the component information, not null
   */
  public void republish(ComponentInfo info) {
    ArgumentChecker.notNull(info, "info");
    
    getRemoteComponents().add(info);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the complete set of singletons, handling managed components.
   * <p>
   * This method wraps the managed components in an instance of {@link DataComponentsResource}.
   * 
   * @return the complete set of singletons, not null
   */
  public Set<Object> getJaxRsSingletons() {
    DataComponentsResource dcr = new DataComponentsResource(getLocalComponents(), getRemoteComponents());
    Set<Object> set = new LinkedHashSet<Object>();
    set.add(dcr);
    set.addAll(getHelpers());
    set.addAll(getRootResourceSingletons());
    set.addAll(getRootResourceFactories());
    return set;
  }

  /**
   * Gets the complete set of JaxRs classes.
   * 
   * @return the complete set of classes, not null
   */
  public Set<Class<?>> getJaxRsClasses() {
    Set<Class<?>> set = new LinkedHashSet<Class<?>>();
    for (RestResourceFactory factory : getRootResourceFactories()) {
      set.add(factory.getType());
    }
    return set;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RestComponents}.
   * @return the meta-bean, not null
   */
  public static RestComponents.Meta meta() {
    return RestComponents.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(RestComponents.Meta.INSTANCE);
  }

  @Override
  public RestComponents.Meta metaBean() {
    return RestComponents.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 727200993:  // localComponents
        return getLocalComponents();
      case 329529340:  // remoteComponents
        return getRemoteComponents();
      case -392070920:  // rootResourceSingletons
        return getRootResourceSingletons();
      case -122531336:  // rootResourceFactories
        return getRootResourceFactories();
      case 805824133:  // helpers
        return getHelpers();
    }
    return super.propertyGet(propertyName, quiet);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    switch (propertyName.hashCode()) {
      case 727200993:  // localComponents
        setLocalComponents((List<RestComponent>) newValue);
        return;
      case 329529340:  // remoteComponents
        setRemoteComponents((List<ComponentInfo>) newValue);
        return;
      case -392070920:  // rootResourceSingletons
        setRootResourceSingletons((Set<Object>) newValue);
        return;
      case -122531336:  // rootResourceFactories
        setRootResourceFactories((Set<RestResourceFactory>) newValue);
        return;
      case 805824133:  // helpers
        setHelpers((Set<Object>) newValue);
        return;
    }
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  protected void validate() {
    JodaBeanUtils.notNull(_localComponents, "localComponents");
    JodaBeanUtils.notNull(_remoteComponents, "remoteComponents");
    JodaBeanUtils.notNull(_rootResourceSingletons, "rootResourceSingletons");
    JodaBeanUtils.notNull(_rootResourceFactories, "rootResourceFactories");
    JodaBeanUtils.notNull(_helpers, "helpers");
    super.validate();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RestComponents other = (RestComponents) obj;
      return JodaBeanUtils.equal(getLocalComponents(), other.getLocalComponents()) &&
          JodaBeanUtils.equal(getRemoteComponents(), other.getRemoteComponents()) &&
          JodaBeanUtils.equal(getRootResourceSingletons(), other.getRootResourceSingletons()) &&
          JodaBeanUtils.equal(getRootResourceFactories(), other.getRootResourceFactories()) &&
          JodaBeanUtils.equal(getHelpers(), other.getHelpers());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getLocalComponents());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRemoteComponents());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRootResourceSingletons());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRootResourceFactories());
    hash += hash * 31 + JodaBeanUtils.hashCode(getHelpers());
    return hash;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the managed components.
   * These will be controlled by {@link DataComponentsResource}.
   * @return the value of the property, not null
   */
  public List<RestComponent> getLocalComponents() {
    return _localComponents;
  }

  /**
   * Sets the managed components.
   * These will be controlled by {@link DataComponentsResource}.
   * @param localComponents  the new value of the property
   */
  public void setLocalComponents(List<RestComponent> localComponents) {
    this._localComponents.clear();
    this._localComponents.addAll(localComponents);
  }

  /**
   * Gets the the {@code localComponents} property.
   * These will be controlled by {@link DataComponentsResource}.
   * @return the property, not null
   */
  public final Property<List<RestComponent>> localComponents() {
    return metaBean().localComponents().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the remote components.
   * These have been imported from another server and are being re-exposed.
   * @return the value of the property, not null
   */
  public List<ComponentInfo> getRemoteComponents() {
    return _remoteComponents;
  }

  /**
   * Sets the remote components.
   * These have been imported from another server and are being re-exposed.
   * @param remoteComponents  the new value of the property
   */
  public void setRemoteComponents(List<ComponentInfo> remoteComponents) {
    this._remoteComponents.clear();
    this._remoteComponents.addAll(remoteComponents);
  }

  /**
   * Gets the the {@code remoteComponents} property.
   * These have been imported from another server and are being re-exposed.
   * @return the property, not null
   */
  public final Property<List<ComponentInfo>> remoteComponents() {
    return metaBean().remoteComponents().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of root resources.
   * These are not managed by {@link DataComponentsResource}.
   * @return the value of the property, not null
   */
  public Set<Object> getRootResourceSingletons() {
    return _rootResourceSingletons;
  }

  /**
   * Sets the set of root resources.
   * These are not managed by {@link DataComponentsResource}.
   * @param rootResourceSingletons  the new value of the property
   */
  public void setRootResourceSingletons(Set<Object> rootResourceSingletons) {
    this._rootResourceSingletons.clear();
    this._rootResourceSingletons.addAll(rootResourceSingletons);
  }

  /**
   * Gets the the {@code rootResourceSingletons} property.
   * These are not managed by {@link DataComponentsResource}.
   * @return the property, not null
   */
  public final Property<Set<Object>> rootResourceSingletons() {
    return metaBean().rootResourceSingletons().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of root resource factories.
   * These are not managed by {@link DataComponentsResource}.
   * @return the value of the property, not null
   */
  public Set<RestResourceFactory> getRootResourceFactories() {
    return _rootResourceFactories;
  }

  /**
   * Sets the set of root resource factories.
   * These are not managed by {@link DataComponentsResource}.
   * @param rootResourceFactories  the new value of the property
   */
  public void setRootResourceFactories(Set<RestResourceFactory> rootResourceFactories) {
    this._rootResourceFactories.clear();
    this._rootResourceFactories.addAll(rootResourceFactories);
  }

  /**
   * Gets the the {@code rootResourceFactories} property.
   * These are not managed by {@link DataComponentsResource}.
   * @return the property, not null
   */
  public final Property<Set<RestResourceFactory>> rootResourceFactories() {
    return metaBean().rootResourceFactories().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of additional singleton JAX-RS helper objects that are used by JAX-RS.
   * This may include filters, providers and consumers that should be used directly by JAX-RS.
   * @return the value of the property, not null
   */
  public Set<Object> getHelpers() {
    return _helpers;
  }

  /**
   * Sets the set of additional singleton JAX-RS helper objects that are used by JAX-RS.
   * This may include filters, providers and consumers that should be used directly by JAX-RS.
   * @param helpers  the new value of the property
   */
  public void setHelpers(Set<Object> helpers) {
    this._helpers.clear();
    this._helpers.addAll(helpers);
  }

  /**
   * Gets the the {@code helpers} property.
   * This may include filters, providers and consumers that should be used directly by JAX-RS.
   * @return the property, not null
   */
  public final Property<Set<Object>> helpers() {
    return metaBean().helpers().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RestComponents}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code localComponents} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<RestComponent>> _localComponents = DirectMetaProperty.ofReadWrite(
        this, "localComponents", RestComponents.class, (Class) List.class);
    /**
     * The meta-property for the {@code remoteComponents} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ComponentInfo>> _remoteComponents = DirectMetaProperty.ofReadWrite(
        this, "remoteComponents", RestComponents.class, (Class) List.class);
    /**
     * The meta-property for the {@code rootResourceSingletons} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Object>> _rootResourceSingletons = DirectMetaProperty.ofReadWrite(
        this, "rootResourceSingletons", RestComponents.class, (Class) Set.class);
    /**
     * The meta-property for the {@code rootResourceFactories} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<RestResourceFactory>> _rootResourceFactories = DirectMetaProperty.ofReadWrite(
        this, "rootResourceFactories", RestComponents.class, (Class) Set.class);
    /**
     * The meta-property for the {@code helpers} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Object>> _helpers = DirectMetaProperty.ofReadWrite(
        this, "helpers", RestComponents.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<Object>> _map = new DirectMetaPropertyMap(
        this, null,
        "localComponents",
        "remoteComponents",
        "rootResourceSingletons",
        "rootResourceFactories",
        "helpers");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 727200993:  // localComponents
          return _localComponents;
        case 329529340:  // remoteComponents
          return _remoteComponents;
        case -392070920:  // rootResourceSingletons
          return _rootResourceSingletons;
        case -122531336:  // rootResourceFactories
          return _rootResourceFactories;
        case 805824133:  // helpers
          return _helpers;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends RestComponents> builder() {
      return new DirectBeanBuilder<RestComponents>(new RestComponents());
    }

    @Override
    public Class<? extends RestComponents> beanType() {
      return RestComponents.class;
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
      return _map;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code localComponents} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<RestComponent>> localComponents() {
      return _localComponents;
    }

    /**
     * The meta-property for the {@code remoteComponents} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<List<ComponentInfo>> remoteComponents() {
      return _remoteComponents;
    }

    /**
     * The meta-property for the {@code rootResourceSingletons} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<Object>> rootResourceSingletons() {
      return _rootResourceSingletons;
    }

    /**
     * The meta-property for the {@code rootResourceFactories} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<RestResourceFactory>> rootResourceFactories() {
      return _rootResourceFactories;
    }

    /**
     * The meta-property for the {@code helpers} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<Object>> helpers() {
      return _helpers;
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
