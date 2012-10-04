/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.config.impl.ConfigItem;
import com.opengamma.master.AbstractSearchResult;
import com.opengamma.util.PublicSPI;
import com.sun.jersey.api.client.GenericType;

/**
 * Result from searching for configuration documents.
 * <p>
 * The returned documents will match the search criteria.
 * See {@link ConfigSearchRequest} for more details.
 * 
 */
@PublicSPI
@BeanDefinition
public class ConfigSearchResult<T> extends AbstractSearchResult<ConfigDocument> {

  /**
   * Creates an instance.
   */
  public ConfigSearchResult() {
  }

  /**
   * Creates an instance.
   * @param coll  the collection of documents to add, not null
   */
  public ConfigSearchResult(Collection<ConfigDocument> coll) {
    super(coll);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the returned configuration values from within the documents.
   * 
   * @return the configuration values, not null
   */
  @SuppressWarnings("unchecked")
  public List<ConfigItem<T>> getValues() {
    GenericType<T> gt = new GenericType<T>(){};
    List<ConfigItem<T>> result = new ArrayList<ConfigItem<T>>();
    if (getDocuments() != null) {
      for (ConfigDocument doc : getDocuments()) {
        if(gt.getRawClass().isAssignableFrom(doc.getObject().getType())){
          result.add((ConfigItem<T>) doc.getObject());
        }
      }
    }
    return result;
  }

  /**
   * Gets the first configuration document value, or null if no documents.
   * 
   * @return the first configuration value, null if none
   */
  public ConfigItem<T> getFirstValue() {
    return getValues().size() > 0 ? getValues().get(0) : null;
  }

  /**
   * Gets the single result expected from a query.
   * <p>
   * This throws an exception if more than 1 result is actually available.
   * Thus, this method implies an assumption about uniqueness of the queried config.
   * 
   * @return the matching config, not null
   * @throws IllegalStateException if no config was found
   */
  public ConfigItem<T> getSingleValue() {
    if (getDocuments().size() != 1) {
      throw new OpenGammaRuntimeException("Expecting zero or single resulting match, and was " + getDocuments().size());
    } else {
      return getValues().get(0);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ConfigSearchResult}.
   * @param <R>  the bean's generic type
   * @return the meta-bean, not null
   */
  @SuppressWarnings("unchecked")
  public static <R> ConfigSearchResult.Meta<R> meta() {
    return ConfigSearchResult.Meta.INSTANCE;
  }
  static {
    JodaBeanUtils.registerMetaBean(ConfigSearchResult.Meta.INSTANCE);
  }

  @SuppressWarnings("unchecked")
  @Override
  public ConfigSearchResult.Meta<T> metaBean() {
    return ConfigSearchResult.Meta.INSTANCE;
  }

  @Override
  protected Object propertyGet(String propertyName, boolean quiet) {
    return super.propertyGet(propertyName, quiet);
  }

  @Override
  protected void propertySet(String propertyName, Object newValue, boolean quiet) {
    super.propertySet(propertyName, newValue, quiet);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      return super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    return hash ^ super.hashCode();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ConfigSearchResult}.
   */
  public static class Meta<T> extends AbstractSearchResult.Meta<ConfigDocument> {
    /**
     * The singleton instance of the meta-bean.
     */
    @SuppressWarnings("rawtypes")
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
      this, (DirectMetaPropertyMap) super.metaPropertyMap());

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    public BeanBuilder<? extends ConfigSearchResult<T>> builder() {
      return new DirectBeanBuilder<ConfigSearchResult<T>>(new ConfigSearchResult<T>());
    }

    @SuppressWarnings({"unchecked", "rawtypes" })
    @Override
    public Class<? extends ConfigSearchResult<T>> beanType() {
      return (Class) ConfigSearchResult.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
