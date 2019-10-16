/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.component.factory.source;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.AbstractComponentFactory;
import com.opengamma.component.factory.ComponentInfoAttributes;
import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.DataHistoricalTimeSeriesSourceResource;
import com.opengamma.core.historicaltimeseries.impl.EHCachingHistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.RemoteHistoricalTimeSeriesSource;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.impl.AlwaysAvailableHistoricalTimeSeriesResolver;
import com.opengamma.master.historicaltimeseries.impl.DataHistoricalTimeSeriesResolverResource;
import com.opengamma.master.historicaltimeseries.impl.DefaultHistoricalTimeSeriesSelector;
import com.opengamma.master.historicaltimeseries.impl.MasterHistoricalTimeSeriesSource;
import com.opengamma.master.historicaltimeseries.impl.RemoteHistoricalTimeSeriesResolver;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

import net.sf.ehcache.CacheManager;
import org.joda.beans.MetaBean;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;

/**
 * A component factory that provides a {@link HistoricalTimeSeriesSource} with a {@link AlwaysAvailableHistoricalTimeSeriesResolver}.
 * This factory should be used in tests that required that time series can always be resolved.
 */
@BeanDefinition
public class AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory extends AbstractComponentFactory {

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
   * The cache manager.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;
  /**
   * The underlying hts master.
   */
  @PropertyDefinition(validate = "notNull")
  private HistoricalTimeSeriesMaster _historicalTimeSeriesMaster;
  /**
   * The config source.
   */
  @PropertyDefinition(validate = "notNull")
  private ConfigSource _configSource;

  //-------------------------------------------------------------------------
  /**
   * Initializes the HTS source, setting up component information and REST. Override using {@link #createResolver(ComponentRepository)} and
   * {@link #createSource(ComponentRepository, HistoricalTimeSeriesResolver)}.
   *
   * @param repo the component repository, not null
   * @param configuration the remaining configuration, not null
   */
  @Override
  public void init(final ComponentRepository repo, final LinkedHashMap<String, String> configuration) {
    final HistoricalTimeSeriesResolver resolver = createResolver(repo);
    final ComponentInfo infoResolver = new ComponentInfo(HistoricalTimeSeriesResolver.class, getClassifier());
    infoResolver.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    if (isPublishRest()) {
      infoResolver.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteHistoricalTimeSeriesResolver.class);
    }
    repo.registerComponent(infoResolver, resolver);

    final HistoricalTimeSeriesSource source = createSource(repo, resolver);

    final ComponentInfo infoSource = new ComponentInfo(HistoricalTimeSeriesSource.class, getClassifier());
    infoSource.addAttribute(ComponentInfoAttributes.LEVEL, 1);
    if (isPublishRest()) {
      infoSource.addAttribute(ComponentInfoAttributes.REMOTE_CLIENT_JAVA, RemoteHistoricalTimeSeriesSource.class);
    }
    repo.registerComponent(infoSource, source);
    if (isPublishRest()) {
      repo.getRestComponents().publish(infoResolver, new DataHistoricalTimeSeriesResolverResource(resolver, OpenGammaFudgeContext.getInstance()));
      repo.getRestComponents().publish(infoSource, new DataHistoricalTimeSeriesSourceResource(source));
    }
  }

  /**
   * Creates the HTS resolver without registering it.
   *
   * @param repo the component repository, only used to register secondary items like lifecycle, not null
   * @return the resolver, not null
   */
  protected HistoricalTimeSeriesResolver createResolver(final ComponentRepository repo) {
    final HistoricalTimeSeriesSelector selector = new DefaultHistoricalTimeSeriesSelector(getConfigSource());
    return new AlwaysAvailableHistoricalTimeSeriesResolver(selector, getHistoricalTimeSeriesMaster());
  }

  /**
   * Creates the HTS source without registering it.
   * <p>
   * This calls {@link #createSourcePreCaching(ComponentRepository, HistoricalTimeSeriesResolver)}.
   *
   * @param repo the component repository, only used to register secondary items like lifecycle, not null
   * @param resolver the resolver, not null
   * @return the source, not null
   */
  protected HistoricalTimeSeriesSource createSource(final ComponentRepository repo, final HistoricalTimeSeriesResolver resolver) {
    HistoricalTimeSeriesSource source = createSourcePreCaching(repo, resolver);
    if (getCacheManager() != null) {
      source = new EHCachingHistoricalTimeSeriesSource(source, getCacheManager());
    }
    return source;
  }

  /**
   * Creates the HTS source without registering it before caching.
   *
   * @param repo the component repository, only used to register secondary items like lifecycle, not null
   * @param resolver the resolver, not null
   * @return the source, not null
   */
  protected HistoricalTimeSeriesSource createSourcePreCaching(final ComponentRepository repo, final HistoricalTimeSeriesResolver resolver) {
    return new MasterHistoricalTimeSeriesSource(getHistoricalTimeSeriesMaster(), resolver);
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory}.
   * @return the meta-bean, not null
   */
  public static AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.Meta meta() {
    return AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.Meta.INSTANCE;
  }

  static {
    MetaBean.register(AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.Meta.INSTANCE);
  }

  @Override
  public AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.Meta metaBean() {
    return AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.Meta.INSTANCE;
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
   * Gets the cache manager.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the cache manager.
   * @param cacheManager  the new value of the property
   */
  public void setCacheManager(CacheManager cacheManager) {
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the underlying hts master.
   * @return the value of the property, not null
   */
  public HistoricalTimeSeriesMaster getHistoricalTimeSeriesMaster() {
    return _historicalTimeSeriesMaster;
  }

  /**
   * Sets the underlying hts master.
   * @param historicalTimeSeriesMaster  the new value of the property, not null
   */
  public void setHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster historicalTimeSeriesMaster) {
    JodaBeanUtils.notNull(historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
    this._historicalTimeSeriesMaster = historicalTimeSeriesMaster;
  }

  /**
   * Gets the the {@code historicalTimeSeriesMaster} property.
   * @return the property, not null
   */
  public final Property<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
    return metaBean().historicalTimeSeriesMaster().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the config source.
   * @return the value of the property, not null
   */
  public ConfigSource getConfigSource() {
    return _configSource;
  }

  /**
   * Sets the config source.
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
  public AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory other = (AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) obj;
      return JodaBeanUtils.equal(getClassifier(), other.getClassifier()) &&
          (isPublishRest() == other.isPublishRest()) &&
          JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getHistoricalTimeSeriesMaster(), other.getHistoricalTimeSeriesMaster()) &&
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
    hash = hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash = hash * 31 + JodaBeanUtils.hashCode(getHistoricalTimeSeriesMaster());
    hash = hash * 31 + JodaBeanUtils.hashCode(getConfigSource());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory{");
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
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
    buf.append("historicalTimeSeriesMaster").append('=').append(JodaBeanUtils.toString(getHistoricalTimeSeriesMaster())).append(',').append(' ');
    buf.append("configSource").append('=').append(JodaBeanUtils.toString(getConfigSource())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory}.
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
        this, "classifier", AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.class, String.class);
    /**
     * The meta-property for the {@code publishRest} property.
     */
    private final MetaProperty<Boolean> _publishRest = DirectMetaProperty.ofReadWrite(
        this, "publishRest", AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.class, Boolean.TYPE);
    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     */
    private final MetaProperty<HistoricalTimeSeriesMaster> _historicalTimeSeriesMaster = DirectMetaProperty.ofReadWrite(
        this, "historicalTimeSeriesMaster", AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.class, HistoricalTimeSeriesMaster.class);
    /**
     * The meta-property for the {@code configSource} property.
     */
    private final MetaProperty<ConfigSource> _configSource = DirectMetaProperty.ofReadWrite(
        this, "configSource", AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.class, ConfigSource.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "classifier",
        "publishRest",
        "cacheManager",
        "historicalTimeSeriesMaster",
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
        case -1452875317:  // cacheManager
          return _cacheManager;
        case 173967376:  // historicalTimeSeriesMaster
          return _historicalTimeSeriesMaster;
        case 195157501:  // configSource
          return _configSource;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory> builder() {
      return new DirectBeanBuilder<>(new AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory());
    }

    @Override
    public Class<? extends AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory> beanType() {
      return AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory.class;
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
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    /**
     * The meta-property for the {@code historicalTimeSeriesMaster} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HistoricalTimeSeriesMaster> historicalTimeSeriesMaster() {
      return _historicalTimeSeriesMaster;
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
          return ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).getClassifier();
        case -614707837:  // publishRest
          return ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).isPublishRest();
        case -1452875317:  // cacheManager
          return ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).getCacheManager();
        case 173967376:  // historicalTimeSeriesMaster
          return ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).getHistoricalTimeSeriesMaster();
        case 195157501:  // configSource
          return ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).getConfigSource();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -281470431:  // classifier
          ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).setClassifier((String) newValue);
          return;
        case -614707837:  // publishRest
          ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).setPublishRest((Boolean) newValue);
          return;
        case -1452875317:  // cacheManager
          ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case 173967376:  // historicalTimeSeriesMaster
          ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).setHistoricalTimeSeriesMaster((HistoricalTimeSeriesMaster) newValue);
          return;
        case 195157501:  // configSource
          ((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean).setConfigSource((ConfigSource) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean)._classifier, "classifier");
      JodaBeanUtils.notNull(((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean)._historicalTimeSeriesMaster, "historicalTimeSeriesMaster");
      JodaBeanUtils.notNull(((AlwaysAvailableHistoricalTimeSeriesSourceComponentFactory) bean)._configSource, "configSource");
      super.validate(bean);
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
