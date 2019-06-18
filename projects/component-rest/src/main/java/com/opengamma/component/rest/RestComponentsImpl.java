/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
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

import com.opengamma.component.ComponentInfo;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * The complete set of components published in a RESTful way by JAX-RS.
 * <p>
 * Components may be managed by {@link DataComponentServerResource} or by JAX-RS directly.
 */
@BeanDefinition
public class RestComponentsImpl extends DirectBean implements RestComponents {

  /**
   * The base URI.
   * This is normally the base URI of all the JAX-RS resources.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final URI _baseUri = URI.create("/jax");
  /**
   * The managed components.
   * These will be controlled by {@link DataComponentServerResource}.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final List<RestComponent> _localComponents = new ArrayList<>();
  /**
   * The remote components.
   * These have been imported from another server and are being re-exposed.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final List<ComponentInfo> _remoteComponents = new ArrayList<>();
  /**
   * The set of root resources.
   * These are not managed by {@link DataComponentServerResource}.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final Set<Object> _rootResourceSingletons = new LinkedHashSet<>();
  /**
   * The set of root resource factories.
   * These are not managed by {@link DataComponentServerResource}.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final Set<RestResourceFactory> _rootResourceFactories = new LinkedHashSet<>();
  /**
   * The set of additional singleton JAX-RS helper objects that are used by JAX-RS.
   * This may include filters, providers and consumers that should be used directly by JAX-RS.
   */
  @PropertyDefinition(validate = "notNull", overrideGet = true)
  private final Set<Object> _helpers = new LinkedHashSet<>();

  /**
   * Creates an instance.
   */
  public RestComponentsImpl() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a managed component to the known set.
   * <p>
   * The instance is a JAX_RS class annotated with {@code Path} on the methods.
   * Any {@code Path} at the class level is ignored.
   * See {@link DataComponentServerResource}.
   *
   * @param info  the managed component info, not null
   * @param instance  the JAX-RS singleton instance, not null
   */
  @Override
  public void publish(final ComponentInfo info, final Object instance) {
    ArgumentChecker.notNull(info, "info");
    ArgumentChecker.notNull(instance, "instance");
    if (info.getUri() != null) {
      throw new IllegalArgumentException("A managed component cannot set its own URI: " + info);
    }

    info.setUri(DataComponentServerUris.uri(getBaseUri(), info));
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
  @Override
  public void publishHelper(final Object instance) {
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
  @Override
  public void publishResource(final Object singletonInstance) {
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
  @Override
  public void publishResource(final RestResourceFactory factory) {
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
  @Override
  public void republish(final ComponentInfo info) {
    ArgumentChecker.notNull(info, "info");

    getRemoteComponents().add(info);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the complete set of singletons, handling managed components.
   * <p>
   * This method wraps the managed components in an instance of {@link DataComponentServerResource}.
   *
   * @return the complete set of singletons, not null
   */
  @Override
  public Set<Object> buildJaxRsSingletons() {
    final AbstractDataResource dcr = new DataComponentServerResource(getLocalComponents(), getRemoteComponents());
    final Set<Object> set = new LinkedHashSet<>();
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
  @Override
  public Set<Class<?>> buildJaxRsClasses() {
    final Set<Class<?>> set = new LinkedHashSet<>();
    for (final RestResourceFactory factory : getRootResourceFactories()) {
      set.add(factory.getType());
    }
    return set;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code RestComponentsImpl}.
   * @return the meta-bean, not null
   */
  public static RestComponentsImpl.Meta meta() {
    return RestComponentsImpl.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(RestComponentsImpl.Meta.INSTANCE);
  }

  @Override
  public RestComponentsImpl.Meta metaBean() {
    return RestComponentsImpl.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the base URI.
   * This is normally the base URI of all the JAX-RS resources.
   * @return the value of the property, not null
   */
  @Override
  public URI getBaseUri() {
    return _baseUri;
  }

  /**
   * Gets the the {@code baseUri} property.
   * This is normally the base URI of all the JAX-RS resources.
   * @return the property, not null
   */
  public final Property<URI> baseUri() {
    return metaBean().baseUri().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the managed components.
   * These will be controlled by {@link DataComponentServerResource}.
   * @return the value of the property, not null
   */
  @Override
  public List<RestComponent> getLocalComponents() {
    return _localComponents;
  }

  /**
   * Sets the managed components.
   * These will be controlled by {@link DataComponentServerResource}.
   * @param localComponents  the new value of the property, not null
   */
  public void setLocalComponents(List<RestComponent> localComponents) {
    JodaBeanUtils.notNull(localComponents, "localComponents");
    this._localComponents.clear();
    this._localComponents.addAll(localComponents);
  }

  /**
   * Gets the the {@code localComponents} property.
   * These will be controlled by {@link DataComponentServerResource}.
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
  @Override
  public List<ComponentInfo> getRemoteComponents() {
    return _remoteComponents;
  }

  /**
   * Sets the remote components.
   * These have been imported from another server and are being re-exposed.
   * @param remoteComponents  the new value of the property, not null
   */
  public void setRemoteComponents(List<ComponentInfo> remoteComponents) {
    JodaBeanUtils.notNull(remoteComponents, "remoteComponents");
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
   * These are not managed by {@link DataComponentServerResource}.
   * @return the value of the property, not null
   */
  @Override
  public Set<Object> getRootResourceSingletons() {
    return _rootResourceSingletons;
  }

  /**
   * Sets the set of root resources.
   * These are not managed by {@link DataComponentServerResource}.
   * @param rootResourceSingletons  the new value of the property, not null
   */
  public void setRootResourceSingletons(Set<Object> rootResourceSingletons) {
    JodaBeanUtils.notNull(rootResourceSingletons, "rootResourceSingletons");
    this._rootResourceSingletons.clear();
    this._rootResourceSingletons.addAll(rootResourceSingletons);
  }

  /**
   * Gets the the {@code rootResourceSingletons} property.
   * These are not managed by {@link DataComponentServerResource}.
   * @return the property, not null
   */
  public final Property<Set<Object>> rootResourceSingletons() {
    return metaBean().rootResourceSingletons().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of root resource factories.
   * These are not managed by {@link DataComponentServerResource}.
   * @return the value of the property, not null
   */
  @Override
  public Set<RestResourceFactory> getRootResourceFactories() {
    return _rootResourceFactories;
  }

  /**
   * Sets the set of root resource factories.
   * These are not managed by {@link DataComponentServerResource}.
   * @param rootResourceFactories  the new value of the property, not null
   */
  public void setRootResourceFactories(Set<RestResourceFactory> rootResourceFactories) {
    JodaBeanUtils.notNull(rootResourceFactories, "rootResourceFactories");
    this._rootResourceFactories.clear();
    this._rootResourceFactories.addAll(rootResourceFactories);
  }

  /**
   * Gets the the {@code rootResourceFactories} property.
   * These are not managed by {@link DataComponentServerResource}.
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
  @Override
  public Set<Object> getHelpers() {
    return _helpers;
  }

  /**
   * Sets the set of additional singleton JAX-RS helper objects that are used by JAX-RS.
   * This may include filters, providers and consumers that should be used directly by JAX-RS.
   * @param helpers  the new value of the property, not null
   */
  public void setHelpers(Set<Object> helpers) {
    JodaBeanUtils.notNull(helpers, "helpers");
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
  @Override
  public RestComponentsImpl clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      RestComponentsImpl other = (RestComponentsImpl) obj;
      return JodaBeanUtils.equal(getBaseUri(), other.getBaseUri()) &&
          JodaBeanUtils.equal(getLocalComponents(), other.getLocalComponents()) &&
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
    hash = hash * 31 + JodaBeanUtils.hashCode(getBaseUri());
    hash = hash * 31 + JodaBeanUtils.hashCode(getLocalComponents());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRemoteComponents());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRootResourceSingletons());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRootResourceFactories());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHelpers());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("RestComponentsImpl{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("baseUri").append('=').append(JodaBeanUtils.toString(getBaseUri())).append(',').append(' ');
    buf.append("localComponents").append('=').append(JodaBeanUtils.toString(getLocalComponents())).append(',').append(' ');
    buf.append("remoteComponents").append('=').append(JodaBeanUtils.toString(getRemoteComponents())).append(',').append(' ');
    buf.append("rootResourceSingletons").append('=').append(JodaBeanUtils.toString(getRootResourceSingletons())).append(',').append(' ');
    buf.append("rootResourceFactories").append('=').append(JodaBeanUtils.toString(getRootResourceFactories())).append(',').append(' ');
    buf.append("helpers").append('=').append(JodaBeanUtils.toString(getHelpers())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code RestComponentsImpl}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code baseUri} property.
     */
    private final MetaProperty<URI> _baseUri = DirectMetaProperty.ofReadOnly(
        this, "baseUri", RestComponentsImpl.class, URI.class);
    /**
     * The meta-property for the {@code localComponents} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<RestComponent>> _localComponents = DirectMetaProperty.ofReadWrite(
        this, "localComponents", RestComponentsImpl.class, (Class) List.class);
    /**
     * The meta-property for the {@code remoteComponents} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<List<ComponentInfo>> _remoteComponents = DirectMetaProperty.ofReadWrite(
        this, "remoteComponents", RestComponentsImpl.class, (Class) List.class);
    /**
     * The meta-property for the {@code rootResourceSingletons} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Object>> _rootResourceSingletons = DirectMetaProperty.ofReadWrite(
        this, "rootResourceSingletons", RestComponentsImpl.class, (Class) Set.class);
    /**
     * The meta-property for the {@code rootResourceFactories} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<RestResourceFactory>> _rootResourceFactories = DirectMetaProperty.ofReadWrite(
        this, "rootResourceFactories", RestComponentsImpl.class, (Class) Set.class);
    /**
     * The meta-property for the {@code helpers} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<Object>> _helpers = DirectMetaProperty.ofReadWrite(
        this, "helpers", RestComponentsImpl.class, (Class) Set.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "baseUri",
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
        case -332625701:  // baseUri
          return _baseUri;
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
    public BeanBuilder<? extends RestComponentsImpl> builder() {
      return new DirectBeanBuilder<RestComponentsImpl>(new RestComponentsImpl());
    }

    @Override
    public Class<? extends RestComponentsImpl> beanType() {
      return RestComponentsImpl.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code baseUri} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<URI> baseUri() {
      return _baseUri;
    }

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

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -332625701:  // baseUri
          return ((RestComponentsImpl) bean).getBaseUri();
        case 727200993:  // localComponents
          return ((RestComponentsImpl) bean).getLocalComponents();
        case 329529340:  // remoteComponents
          return ((RestComponentsImpl) bean).getRemoteComponents();
        case -392070920:  // rootResourceSingletons
          return ((RestComponentsImpl) bean).getRootResourceSingletons();
        case -122531336:  // rootResourceFactories
          return ((RestComponentsImpl) bean).getRootResourceFactories();
        case 805824133:  // helpers
          return ((RestComponentsImpl) bean).getHelpers();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -332625701:  // baseUri
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: baseUri");
        case 727200993:  // localComponents
          ((RestComponentsImpl) bean).setLocalComponents((List<RestComponent>) newValue);
          return;
        case 329529340:  // remoteComponents
          ((RestComponentsImpl) bean).setRemoteComponents((List<ComponentInfo>) newValue);
          return;
        case -392070920:  // rootResourceSingletons
          ((RestComponentsImpl) bean).setRootResourceSingletons((Set<Object>) newValue);
          return;
        case -122531336:  // rootResourceFactories
          ((RestComponentsImpl) bean).setRootResourceFactories((Set<RestResourceFactory>) newValue);
          return;
        case 805824133:  // helpers
          ((RestComponentsImpl) bean).setHelpers((Set<Object>) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((RestComponentsImpl) bean)._baseUri, "baseUri");
      JodaBeanUtils.notNull(((RestComponentsImpl) bean)._localComponents, "localComponents");
      JodaBeanUtils.notNull(((RestComponentsImpl) bean)._remoteComponents, "remoteComponents");
      JodaBeanUtils.notNull(((RestComponentsImpl) bean)._rootResourceSingletons, "rootResourceSingletons");
      JodaBeanUtils.notNull(((RestComponentsImpl) bean)._rootResourceFactories, "rootResourceFactories");
      JodaBeanUtils.notNull(((RestComponentsImpl) bean)._helpers, "helpers");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
