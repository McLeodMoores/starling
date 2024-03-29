/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.batch.domain;


import java.util.Map;

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

@BeanDefinition
public class TargetProperty extends DirectBean {

  @PropertyDefinition
  private int _id;

  @PropertyDefinition
  private HbComputationTargetSpecification _targetSpecification;

  @PropertyDefinition
  private String _propertyKey;

  @PropertyDefinition
  private String _propertyValue;

  public TargetProperty() {
  }

  public TargetProperty(final String propertyKey, final String propertyValue) {
    this._propertyKey = propertyKey;
    this._propertyValue = propertyValue;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TargetProperty}.
   * @return the meta-bean, not null
   */
  public static TargetProperty.Meta meta() {
    return TargetProperty.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(TargetProperty.Meta.INSTANCE);
  }

  @Override
  public TargetProperty.Meta metaBean() {
    return TargetProperty.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the id.
   * @return the value of the property
   */
  public int getId() {
    return _id;
  }

  /**
   * Sets the id.
   * @param id  the new value of the property
   */
  public void setId(int id) {
    this._id = id;
  }

  /**
   * Gets the the {@code id} property.
   * @return the property, not null
   */
  public final Property<Integer> id() {
    return metaBean().id().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the targetSpecification.
   * @return the value of the property
   */
  public HbComputationTargetSpecification getTargetSpecification() {
    return _targetSpecification;
  }

  /**
   * Sets the targetSpecification.
   * @param targetSpecification  the new value of the property
   */
  public void setTargetSpecification(HbComputationTargetSpecification targetSpecification) {
    this._targetSpecification = targetSpecification;
  }

  /**
   * Gets the the {@code targetSpecification} property.
   * @return the property, not null
   */
  public final Property<HbComputationTargetSpecification> targetSpecification() {
    return metaBean().targetSpecification().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the propertyKey.
   * @return the value of the property
   */
  public String getPropertyKey() {
    return _propertyKey;
  }

  /**
   * Sets the propertyKey.
   * @param propertyKey  the new value of the property
   */
  public void setPropertyKey(String propertyKey) {
    this._propertyKey = propertyKey;
  }

  /**
   * Gets the the {@code propertyKey} property.
   * @return the property, not null
   */
  public final Property<String> propertyKey() {
    return metaBean().propertyKey().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the propertyValue.
   * @return the value of the property
   */
  public String getPropertyValue() {
    return _propertyValue;
  }

  /**
   * Sets the propertyValue.
   * @param propertyValue  the new value of the property
   */
  public void setPropertyValue(String propertyValue) {
    this._propertyValue = propertyValue;
  }

  /**
   * Gets the the {@code propertyValue} property.
   * @return the property, not null
   */
  public final Property<String> propertyValue() {
    return metaBean().propertyValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public TargetProperty clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      TargetProperty other = (TargetProperty) obj;
      return (getId() == other.getId()) &&
          JodaBeanUtils.equal(getTargetSpecification(), other.getTargetSpecification()) &&
          JodaBeanUtils.equal(getPropertyKey(), other.getPropertyKey()) &&
          JodaBeanUtils.equal(getPropertyValue(), other.getPropertyValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTargetSpecification());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPropertyKey());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPropertyValue());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("TargetProperty{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("id").append('=').append(JodaBeanUtils.toString(getId())).append(',').append(' ');
    buf.append("targetSpecification").append('=').append(JodaBeanUtils.toString(getTargetSpecification())).append(',').append(' ');
    buf.append("propertyKey").append('=').append(JodaBeanUtils.toString(getPropertyKey())).append(',').append(' ');
    buf.append("propertyValue").append('=').append(JodaBeanUtils.toString(getPropertyValue())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TargetProperty}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<Integer> _id = DirectMetaProperty.ofReadWrite(
        this, "id", TargetProperty.class, Integer.TYPE);
    /**
     * The meta-property for the {@code targetSpecification} property.
     */
    private final MetaProperty<HbComputationTargetSpecification> _targetSpecification = DirectMetaProperty.ofReadWrite(
        this, "targetSpecification", TargetProperty.class, HbComputationTargetSpecification.class);
    /**
     * The meta-property for the {@code propertyKey} property.
     */
    private final MetaProperty<String> _propertyKey = DirectMetaProperty.ofReadWrite(
        this, "propertyKey", TargetProperty.class, String.class);
    /**
     * The meta-property for the {@code propertyValue} property.
     */
    private final MetaProperty<String> _propertyValue = DirectMetaProperty.ofReadWrite(
        this, "propertyValue", TargetProperty.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "id",
        "targetSpecification",
        "propertyKey",
        "propertyValue");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return _id;
        case -1553345806:  // targetSpecification
          return _targetSpecification;
        case 1357577290:  // propertyKey
          return _propertyKey;
        case -1028251492:  // propertyValue
          return _propertyValue;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends TargetProperty> builder() {
      return new DirectBeanBuilder<TargetProperty>(new TargetProperty());
    }

    @Override
    public Class<? extends TargetProperty> beanType() {
      return TargetProperty.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code targetSpecification} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<HbComputationTargetSpecification> targetSpecification() {
      return _targetSpecification;
    }

    /**
     * The meta-property for the {@code propertyKey} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> propertyKey() {
      return _propertyKey;
    }

    /**
     * The meta-property for the {@code propertyValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> propertyValue() {
      return _propertyValue;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return ((TargetProperty) bean).getId();
        case -1553345806:  // targetSpecification
          return ((TargetProperty) bean).getTargetSpecification();
        case 1357577290:  // propertyKey
          return ((TargetProperty) bean).getPropertyKey();
        case -1028251492:  // propertyValue
          return ((TargetProperty) bean).getPropertyValue();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          ((TargetProperty) bean).setId((Integer) newValue);
          return;
        case -1553345806:  // targetSpecification
          ((TargetProperty) bean).setTargetSpecification((HbComputationTargetSpecification) newValue);
          return;
        case 1357577290:  // propertyKey
          ((TargetProperty) bean).setPropertyKey((String) newValue);
          return;
        case -1028251492:  // propertyValue
          ((TargetProperty) bean).setPropertyValue((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
