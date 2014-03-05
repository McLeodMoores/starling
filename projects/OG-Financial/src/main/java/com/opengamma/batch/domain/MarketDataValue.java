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

import com.opengamma.batch.BatchMaster;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;

/**
 * Data model for a market data value.
 */
@BeanDefinition
public class MarketDataValue extends DirectBean implements ObjectIdentifiable {

  @PropertyDefinition
  private long _id;

  @PropertyDefinition
  private long _marketDataId;

  /**
   * This value is not stored in db, but it is rather paired with _computationTargetId
   */
  @PropertyDefinition
  private ComputationTargetSpecification _computationTargetSpecification;

  @PropertyDefinition
  private Long _computationTargetSpecificationId;

  @PropertyDefinition
  private String _name;

  @PropertyDefinition
  private Double _value;

  public MarketDataValue() {
  }

  public MarketDataValue(ComputationTargetSpecification computationTargetSpecification, Double value, String name) {
    _computationTargetSpecification = computationTargetSpecification;
    _name = name;
    _value = value;
  }

  @Override
  public ObjectId getObjectId() {
    return ObjectId.of(BatchMaster.BATCH_IDENTIFIER_SCHEME, Long.toString(getId()));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code MarketDataValue}.
   * @return the meta-bean, not null
   */
  public static MarketDataValue.Meta meta() {
    return MarketDataValue.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(MarketDataValue.Meta.INSTANCE);
  }

  @Override
  public MarketDataValue.Meta metaBean() {
    return MarketDataValue.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the id.
   * @return the value of the property
   */
  public long getId() {
    return _id;
  }

  /**
   * Sets the id.
   * @param id  the new value of the property
   */
  public void setId(long id) {
    this._id = id;
  }

  /**
   * Gets the the {@code id} property.
   * @return the property, not null
   */
  public final Property<Long> id() {
    return metaBean().id().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the marketDataId.
   * @return the value of the property
   */
  public long getMarketDataId() {
    return _marketDataId;
  }

  /**
   * Sets the marketDataId.
   * @param marketDataId  the new value of the property
   */
  public void setMarketDataId(long marketDataId) {
    this._marketDataId = marketDataId;
  }

  /**
   * Gets the the {@code marketDataId} property.
   * @return the property, not null
   */
  public final Property<Long> marketDataId() {
    return metaBean().marketDataId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets this value is not stored in db, but it is rather paired with _computationTargetId
   * @return the value of the property
   */
  public ComputationTargetSpecification getComputationTargetSpecification() {
    return _computationTargetSpecification;
  }

  /**
   * Sets this value is not stored in db, but it is rather paired with _computationTargetId
   * @param computationTargetSpecification  the new value of the property
   */
  public void setComputationTargetSpecification(ComputationTargetSpecification computationTargetSpecification) {
    this._computationTargetSpecification = computationTargetSpecification;
  }

  /**
   * Gets the the {@code computationTargetSpecification} property.
   * @return the property, not null
   */
  public final Property<ComputationTargetSpecification> computationTargetSpecification() {
    return metaBean().computationTargetSpecification().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the computationTargetSpecificationId.
   * @return the value of the property
   */
  public Long getComputationTargetSpecificationId() {
    return _computationTargetSpecificationId;
  }

  /**
   * Sets the computationTargetSpecificationId.
   * @param computationTargetSpecificationId  the new value of the property
   */
  public void setComputationTargetSpecificationId(Long computationTargetSpecificationId) {
    this._computationTargetSpecificationId = computationTargetSpecificationId;
  }

  /**
   * Gets the the {@code computationTargetSpecificationId} property.
   * @return the property, not null
   */
  public final Property<Long> computationTargetSpecificationId() {
    return metaBean().computationTargetSpecificationId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the name.
   * @return the value of the property
   */
  public String getName() {
    return _name;
  }

  /**
   * Sets the name.
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
   * Gets the value.
   * @return the value of the property
   */
  public Double getValue() {
    return _value;
  }

  /**
   * Sets the value.
   * @param value  the new value of the property
   */
  public void setValue(Double value) {
    this._value = value;
  }

  /**
   * Gets the the {@code value} property.
   * @return the property, not null
   */
  public final Property<Double> value() {
    return metaBean().value().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public MarketDataValue clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      MarketDataValue other = (MarketDataValue) obj;
      return (getId() == other.getId()) &&
          (getMarketDataId() == other.getMarketDataId()) &&
          JodaBeanUtils.equal(getComputationTargetSpecification(), other.getComputationTargetSpecification()) &&
          JodaBeanUtils.equal(getComputationTargetSpecificationId(), other.getComputationTargetSpecificationId()) &&
          JodaBeanUtils.equal(getName(), other.getName()) &&
          JodaBeanUtils.equal(getValue(), other.getValue());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMarketDataId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getComputationTargetSpecification());
    hash += hash * 31 + JodaBeanUtils.hashCode(getComputationTargetSpecificationId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getName());
    hash += hash * 31 + JodaBeanUtils.hashCode(getValue());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("MarketDataValue{");
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
    buf.append("marketDataId").append('=').append(JodaBeanUtils.toString(getMarketDataId())).append(',').append(' ');
    buf.append("computationTargetSpecification").append('=').append(JodaBeanUtils.toString(getComputationTargetSpecification())).append(',').append(' ');
    buf.append("computationTargetSpecificationId").append('=').append(JodaBeanUtils.toString(getComputationTargetSpecificationId())).append(',').append(' ');
    buf.append("name").append('=').append(JodaBeanUtils.toString(getName())).append(',').append(' ');
    buf.append("value").append('=').append(JodaBeanUtils.toString(getValue())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code MarketDataValue}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code id} property.
     */
    private final MetaProperty<Long> _id = DirectMetaProperty.ofReadWrite(
        this, "id", MarketDataValue.class, Long.TYPE);
    /**
     * The meta-property for the {@code marketDataId} property.
     */
    private final MetaProperty<Long> _marketDataId = DirectMetaProperty.ofReadWrite(
        this, "marketDataId", MarketDataValue.class, Long.TYPE);
    /**
     * The meta-property for the {@code computationTargetSpecification} property.
     */
    private final MetaProperty<ComputationTargetSpecification> _computationTargetSpecification = DirectMetaProperty.ofReadWrite(
        this, "computationTargetSpecification", MarketDataValue.class, ComputationTargetSpecification.class);
    /**
     * The meta-property for the {@code computationTargetSpecificationId} property.
     */
    private final MetaProperty<Long> _computationTargetSpecificationId = DirectMetaProperty.ofReadWrite(
        this, "computationTargetSpecificationId", MarketDataValue.class, Long.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofReadWrite(
        this, "name", MarketDataValue.class, String.class);
    /**
     * The meta-property for the {@code value} property.
     */
    private final MetaProperty<Double> _value = DirectMetaProperty.ofReadWrite(
        this, "value", MarketDataValue.class, Double.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "id",
        "marketDataId",
        "computationTargetSpecification",
        "computationTargetSpecificationId",
        "name",
        "value");

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
        case -530966079:  // marketDataId
          return _marketDataId;
        case -1157884501:  // computationTargetSpecification
          return _computationTargetSpecification;
        case -330473434:  // computationTargetSpecificationId
          return _computationTargetSpecificationId;
        case 3373707:  // name
          return _name;
        case 111972721:  // value
          return _value;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends MarketDataValue> builder() {
      return new DirectBeanBuilder<MarketDataValue>(new MarketDataValue());
    }

    @Override
    public Class<? extends MarketDataValue> beanType() {
      return MarketDataValue.class;
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
    public final MetaProperty<Long> id() {
      return _id;
    }

    /**
     * The meta-property for the {@code marketDataId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> marketDataId() {
      return _marketDataId;
    }

    /**
     * The meta-property for the {@code computationTargetSpecification} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ComputationTargetSpecification> computationTargetSpecification() {
      return _computationTargetSpecification;
    }

    /**
     * The meta-property for the {@code computationTargetSpecificationId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Long> computationTargetSpecificationId() {
      return _computationTargetSpecificationId;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    /**
     * The meta-property for the {@code value} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> value() {
      return _value;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          return ((MarketDataValue) bean).getId();
        case -530966079:  // marketDataId
          return ((MarketDataValue) bean).getMarketDataId();
        case -1157884501:  // computationTargetSpecification
          return ((MarketDataValue) bean).getComputationTargetSpecification();
        case -330473434:  // computationTargetSpecificationId
          return ((MarketDataValue) bean).getComputationTargetSpecificationId();
        case 3373707:  // name
          return ((MarketDataValue) bean).getName();
        case 111972721:  // value
          return ((MarketDataValue) bean).getValue();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 3355:  // id
          ((MarketDataValue) bean).setId((Long) newValue);
          return;
        case -530966079:  // marketDataId
          ((MarketDataValue) bean).setMarketDataId((Long) newValue);
          return;
        case -1157884501:  // computationTargetSpecification
          ((MarketDataValue) bean).setComputationTargetSpecification((ComputationTargetSpecification) newValue);
          return;
        case -330473434:  // computationTargetSpecificationId
          ((MarketDataValue) bean).setComputationTargetSpecificationId((Long) newValue);
          return;
        case 3373707:  // name
          ((MarketDataValue) bean).setName((String) newValue);
          return;
        case 111972721:  // value
          ((MarketDataValue) bean).setValue((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
