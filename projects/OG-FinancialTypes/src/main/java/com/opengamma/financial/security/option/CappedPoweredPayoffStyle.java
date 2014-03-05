/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.option;

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
 * The capped powered payoff style.
 */
@BeanDefinition
public class CappedPoweredPayoffStyle extends PayoffStyle {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The power.
   */
  @PropertyDefinition
  private double _power;
  /**
   * The cap.
   */
  @PropertyDefinition
  private double _cap;

  /**
   * Creates an instance.
   */
  private CappedPoweredPayoffStyle() {
  }

  /**
   * Creates an instance.
   * 
   * @param power  the power
   * @param cap  the cap
   */
  public CappedPoweredPayoffStyle(double power, double cap) {
    setPower(power);
    setCap(cap);
  }

  //-------------------------------------------------------------------------
  @Override
  public <T> T accept(PayoffStyleVisitor<T> visitor) {
    return visitor.visitCappedPoweredPayoffStyle(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CappedPoweredPayoffStyle}.
   * @return the meta-bean, not null
   */
  public static CappedPoweredPayoffStyle.Meta meta() {
    return CappedPoweredPayoffStyle.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CappedPoweredPayoffStyle.Meta.INSTANCE);
  }

  @Override
  public CappedPoweredPayoffStyle.Meta metaBean() {
    return CappedPoweredPayoffStyle.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the power.
   * @return the value of the property
   */
  public double getPower() {
    return _power;
  }

  /**
   * Sets the power.
   * @param power  the new value of the property
   */
  public void setPower(double power) {
    this._power = power;
  }

  /**
   * Gets the the {@code power} property.
   * @return the property, not null
   */
  public final Property<Double> power() {
    return metaBean().power().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the cap.
   * @return the value of the property
   */
  public double getCap() {
    return _cap;
  }

  /**
   * Sets the cap.
   * @param cap  the new value of the property
   */
  public void setCap(double cap) {
    this._cap = cap;
  }

  /**
   * Gets the the {@code cap} property.
   * @return the property, not null
   */
  public final Property<Double> cap() {
    return metaBean().cap().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public CappedPoweredPayoffStyle clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CappedPoweredPayoffStyle other = (CappedPoweredPayoffStyle) obj;
      return JodaBeanUtils.equal(getPower(), other.getPower()) &&
          JodaBeanUtils.equal(getCap(), other.getCap()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getPower());
    hash += hash * 31 + JodaBeanUtils.hashCode(getCap());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("CappedPoweredPayoffStyle{");
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
    buf.append("power").append('=').append(JodaBeanUtils.toString(getPower())).append(',').append(' ');
    buf.append("cap").append('=').append(JodaBeanUtils.toString(getCap())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CappedPoweredPayoffStyle}.
   */
  public static class Meta extends PayoffStyle.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code power} property.
     */
    private final MetaProperty<Double> _power = DirectMetaProperty.ofReadWrite(
        this, "power", CappedPoweredPayoffStyle.class, Double.TYPE);
    /**
     * The meta-property for the {@code cap} property.
     */
    private final MetaProperty<Double> _cap = DirectMetaProperty.ofReadWrite(
        this, "cap", CappedPoweredPayoffStyle.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "power",
        "cap");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 106858757:  // power
          return _power;
        case 98258:  // cap
          return _cap;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CappedPoweredPayoffStyle> builder() {
      return new DirectBeanBuilder<CappedPoweredPayoffStyle>(new CappedPoweredPayoffStyle());
    }

    @Override
    public Class<? extends CappedPoweredPayoffStyle> beanType() {
      return CappedPoweredPayoffStyle.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code power} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> power() {
      return _power;
    }

    /**
     * The meta-property for the {@code cap} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> cap() {
      return _cap;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 106858757:  // power
          return ((CappedPoweredPayoffStyle) bean).getPower();
        case 98258:  // cap
          return ((CappedPoweredPayoffStyle) bean).getCap();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 106858757:  // power
          ((CappedPoweredPayoffStyle) bean).setPower((Double) newValue);
          return;
        case 98258:  // cap
          ((CappedPoweredPayoffStyle) bean).setCap((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
