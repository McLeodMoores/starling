/**
 * Copyright (C) 2014 - present by McLeod Moores Software Limited
 * Derived from Apache 2 code Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.mcleodmoores.quandl.component;

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

import com.mcleodmoores.quandl.QuandlConstants;
import com.mcleodmoores.quandl.historicaltimeseries.QuandlHistoricalTimeSeriesProvider;
import com.opengamma.component.ComponentRepository;
import com.opengamma.component.factory.provider.HistoricalTimeSeriesProviderComponentFactory;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.impl.EHCachingHistoricalTimeSeriesProvider;

import net.sf.ehcache.CacheManager;

/**
 * Component factory for the Quandl time-series provider.
 */
@BeanDefinition
public class QuandlHistoricalTimeSeriesProviderComponentFactory extends HistoricalTimeSeriesProviderComponentFactory {

  /**
   * The optional cache manager.
   * Caching will be added if this field is set.
   */
  @PropertyDefinition
  private CacheManager _cacheManager;

  /**
   * The authorization token.
   */
  @PropertyDefinition
  private String _authToken;

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesProvider initHistoricalTimeSeriesProvider(final ComponentRepository repo) {
    final QuandlHistoricalTimeSeriesProvider provider = new QuandlHistoricalTimeSeriesProvider(getAuthToken());
    if (getCacheManager() == null) {
      return provider;
    }
    repo.registerLifecycle(provider);
    return new EHCachingHistoricalTimeSeriesProvider(provider, getCacheManager());
  }

  @Override
  protected String getAcceptedTypes() {
    return QuandlConstants.QUANDL_DATA_SOURCE_NAME;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code QuandlHistoricalTimeSeriesProviderComponentFactory}.
   * @return the meta-bean, not null
   */
  public static QuandlHistoricalTimeSeriesProviderComponentFactory.Meta meta() {
    return QuandlHistoricalTimeSeriesProviderComponentFactory.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(QuandlHistoricalTimeSeriesProviderComponentFactory.Meta.INSTANCE);
  }

  @Override
  public QuandlHistoricalTimeSeriesProviderComponentFactory.Meta metaBean() {
    return QuandlHistoricalTimeSeriesProviderComponentFactory.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the optional cache manager.
   * Caching will be added if this field is set.
   * @return the value of the property
   */
  public CacheManager getCacheManager() {
    return _cacheManager;
  }

  /**
   * Sets the optional cache manager.
   * Caching will be added if this field is set.
   * @param cacheManager  the new value of the property
   */
  public void setCacheManager(CacheManager cacheManager) {
    this._cacheManager = cacheManager;
  }

  /**
   * Gets the the {@code cacheManager} property.
   * Caching will be added if this field is set.
   * @return the property, not null
   */
  public final Property<CacheManager> cacheManager() {
    return metaBean().cacheManager().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the authorization token.
   * @return the value of the property
   */
  public String getAuthToken() {
    return _authToken;
  }

  /**
   * Sets the authorization token.
   * @param authToken  the new value of the property
   */
  public void setAuthToken(String authToken) {
    this._authToken = authToken;
  }

  /**
   * Gets the the {@code authToken} property.
   * @return the property, not null
   */
  public final Property<String> authToken() {
    return metaBean().authToken().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public QuandlHistoricalTimeSeriesProviderComponentFactory clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      QuandlHistoricalTimeSeriesProviderComponentFactory other = (QuandlHistoricalTimeSeriesProviderComponentFactory) obj;
      return JodaBeanUtils.equal(getCacheManager(), other.getCacheManager()) &&
          JodaBeanUtils.equal(getAuthToken(), other.getAuthToken()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getCacheManager());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAuthToken());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("QuandlHistoricalTimeSeriesProviderComponentFactory{");
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
    buf.append("cacheManager").append('=').append(JodaBeanUtils.toString(getCacheManager())).append(',').append(' ');
    buf.append("authToken").append('=').append(JodaBeanUtils.toString(getAuthToken())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code QuandlHistoricalTimeSeriesProviderComponentFactory}.
   */
  public static class Meta extends HistoricalTimeSeriesProviderComponentFactory.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code cacheManager} property.
     */
    private final MetaProperty<CacheManager> _cacheManager = DirectMetaProperty.ofReadWrite(
        this, "cacheManager", QuandlHistoricalTimeSeriesProviderComponentFactory.class, CacheManager.class);
    /**
     * The meta-property for the {@code authToken} property.
     */
    private final MetaProperty<String> _authToken = DirectMetaProperty.ofReadWrite(
        this, "authToken", QuandlHistoricalTimeSeriesProviderComponentFactory.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "cacheManager",
        "authToken");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1452875317:  // cacheManager
          return _cacheManager;
        case 1450587441:  // authToken
          return _authToken;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends QuandlHistoricalTimeSeriesProviderComponentFactory> builder() {
      return new DirectBeanBuilder<QuandlHistoricalTimeSeriesProviderComponentFactory>(new QuandlHistoricalTimeSeriesProviderComponentFactory());
    }

    @Override
    public Class<? extends QuandlHistoricalTimeSeriesProviderComponentFactory> beanType() {
      return QuandlHistoricalTimeSeriesProviderComponentFactory.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code cacheManager} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<CacheManager> cacheManager() {
      return _cacheManager;
    }

    /**
     * The meta-property for the {@code authToken} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> authToken() {
      return _authToken;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1452875317:  // cacheManager
          return ((QuandlHistoricalTimeSeriesProviderComponentFactory) bean).getCacheManager();
        case 1450587441:  // authToken
          return ((QuandlHistoricalTimeSeriesProviderComponentFactory) bean).getAuthToken();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1452875317:  // cacheManager
          ((QuandlHistoricalTimeSeriesProviderComponentFactory) bean).setCacheManager((CacheManager) newValue);
          return;
        case 1450587441:  // authToken
          ((QuandlHistoricalTimeSeriesProviderComponentFactory) bean).setAuthToken((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
