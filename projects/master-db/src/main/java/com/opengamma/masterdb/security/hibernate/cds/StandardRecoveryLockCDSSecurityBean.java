/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate.cds;

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

/**
 *
 */
@BeanDefinition
public class StandardRecoveryLockCDSSecurityBean extends StandardCDSSecurityBean {

  @PropertyDefinition
  private Double _recoveryRate;

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code StandardRecoveryLockCDSSecurityBean}.
   * @return the meta-bean, not null
   */
  public static StandardRecoveryLockCDSSecurityBean.Meta meta() {
    return StandardRecoveryLockCDSSecurityBean.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(StandardRecoveryLockCDSSecurityBean.Meta.INSTANCE);
  }

  @Override
  public StandardRecoveryLockCDSSecurityBean.Meta metaBean() {
    return StandardRecoveryLockCDSSecurityBean.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the recoveryRate.
   * @return the value of the property
   */
  public Double getRecoveryRate() {
    return _recoveryRate;
  }

  /**
   * Sets the recoveryRate.
   * @param recoveryRate  the new value of the property
   */
  public void setRecoveryRate(Double recoveryRate) {
    this._recoveryRate = recoveryRate;
  }

  /**
   * Gets the the {@code recoveryRate} property.
   * @return the property, not null
   */
  public final Property<Double> recoveryRate() {
    return metaBean().recoveryRate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public StandardRecoveryLockCDSSecurityBean clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      StandardRecoveryLockCDSSecurityBean other = (StandardRecoveryLockCDSSecurityBean) obj;
      return JodaBeanUtils.equal(getRecoveryRate(), other.getRecoveryRate()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getRecoveryRate());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("StandardRecoveryLockCDSSecurityBean{");
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
    buf.append("recoveryRate").append('=').append(JodaBeanUtils.toString(getRecoveryRate())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code StandardRecoveryLockCDSSecurityBean}.
   */
  public static class Meta extends StandardCDSSecurityBean.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code recoveryRate} property.
     */
    private final MetaProperty<Double> _recoveryRate = DirectMetaProperty.ofReadWrite(
        this, "recoveryRate", StandardRecoveryLockCDSSecurityBean.class, Double.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "recoveryRate");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 2002873877:  // recoveryRate
          return _recoveryRate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends StandardRecoveryLockCDSSecurityBean> builder() {
      return new DirectBeanBuilder<StandardRecoveryLockCDSSecurityBean>(new StandardRecoveryLockCDSSecurityBean());
    }

    @Override
    public Class<? extends StandardRecoveryLockCDSSecurityBean> beanType() {
      return StandardRecoveryLockCDSSecurityBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code recoveryRate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> recoveryRate() {
      return _recoveryRate;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 2002873877:  // recoveryRate
          return ((StandardRecoveryLockCDSSecurityBean) bean).getRecoveryRate();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 2002873877:  // recoveryRate
          ((StandardRecoveryLockCDSSecurityBean) bean).setRecoveryRate((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
