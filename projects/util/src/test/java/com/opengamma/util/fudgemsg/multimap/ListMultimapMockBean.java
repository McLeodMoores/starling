/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.fudgemsg.multimap;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ListMultimap;

/**
 *
 */
@BeanDefinition
public class ListMultimapMockBean implements ImmutableBean {

  @PropertyDefinition(validate = "notNull")
  private final ListMultimap<String, String> _listMultimap;

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ListMultimapMockBean}.
   * @return the meta-bean, not null
   */
  public static ListMultimapMockBean.Meta meta() {
    return ListMultimapMockBean.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ListMultimapMockBean.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static ListMultimapMockBean.Builder builder() {
    return new ListMultimapMockBean.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected ListMultimapMockBean(ListMultimapMockBean.Builder builder) {
    JodaBeanUtils.notNull(builder._listMultimap, "listMultimap");
    this._listMultimap = ImmutableListMultimap.copyOf(builder._listMultimap);
  }

  @Override
  public ListMultimapMockBean.Meta metaBean() {
    return ListMultimapMockBean.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the listMultimap.
   * @return the value of the property, not null
   */
  public ListMultimap<String, String> getListMultimap() {
    return _listMultimap;
  }

  //-----------------------------------------------------------------------
  /**
   * Returns a builder that allows this bean to be mutated.
   * @return the mutable builder, not null
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ListMultimapMockBean other = (ListMultimapMockBean) obj;
      return JodaBeanUtils.equal(_listMultimap, other._listMultimap);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(_listMultimap);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("ListMultimapMockBean{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("listMultimap").append('=').append(JodaBeanUtils.toString(_listMultimap)).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ListMultimapMockBean}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code listMultimap} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ListMultimap<String, String>> _listMultimap = DirectMetaProperty.ofImmutable(
        this, "listMultimap", ListMultimapMockBean.class, (Class) ListMultimap.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "listMultimap");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1737633857:  // listMultimap
          return _listMultimap;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public ListMultimapMockBean.Builder builder() {
      return new ListMultimapMockBean.Builder();
    }

    @Override
    public Class<? extends ListMultimapMockBean> beanType() {
      return ListMultimapMockBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code listMultimap} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ListMultimap<String, String>> listMultimap() {
      return _listMultimap;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 1737633857:  // listMultimap
          return ((ListMultimapMockBean) bean).getListMultimap();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code ListMultimapMockBean}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<ListMultimapMockBean> {

    private ListMultimap<String, String> _listMultimap = ImmutableListMultimap.of();

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(ListMultimapMockBean beanToCopy) {
      this._listMultimap = ImmutableListMultimap.copyOf(beanToCopy.getListMultimap());
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 1737633857:  // listMultimap
          return _listMultimap;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 1737633857:  // listMultimap
          this._listMultimap = (ListMultimap<String, String>) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(MetaProperty<?> property, Object value) {
      super.set(property, value);
      return this;
    }

    /**
     * @deprecated Use Joda-Convert in application code
     */
    @Override
    @Deprecated
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    /**
     * @deprecated Use Joda-Convert in application code
     */
    @Override
    @Deprecated
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    /**
     * @deprecated Loop in application code
     */
    @Override
    @Deprecated
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public ListMultimapMockBean build() {
      return new ListMultimapMockBean(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the listMultimap.
     * @param listMultimap  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder listMultimap(ListMultimap<String, String> listMultimap) {
      JodaBeanUtils.notNull(listMultimap, "listMultimap");
      this._listMultimap = listMultimap;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(64);
      buf.append("ListMultimapMockBean.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("listMultimap").append('=').append(JodaBeanUtils.toString(_listMultimap)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
