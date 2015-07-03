/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.serialization;

import java.util.Map;

import net.finmath.marketdata.model.curves.Curve.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.Curve.InterpolationEntity;
import net.finmath.marketdata.model.curves.Curve.InterpolationMethod;
import net.finmath.marketdata.model.curves.CurveInterface;
import net.finmath.marketdata.model.curves.DiscountCurve;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.util.ArgumentChecker;

/**
 * Bean for a Finmath {@link DiscountCurve}. The y value types can be represented as discount
 * factors, zero rates or forward rates.
 */
@BeanDefinition
public class DiscountCurveBean extends CurveBean {

  /** The serialization version. */
  private static final long serialVersionUID = 1L;

  /** The logger */
  private static final Logger LOGGER = LoggerFactory.getLogger(DiscountCurveBean.class);

  /**
   * The y value types.
   */
  @PropertyDefinition(validate = "notNull")
  private String _curveValueType;

  /**
   * For the builder.
   */
  /* package */DiscountCurveBean() {
    super();
  }

  /**
   * Creates an instance without interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   * @param curveValueType The type of the y values, not null
   */
  public DiscountCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter,
      final String curveValueType) {
    super(name, referenceDateString, times, values, isParameter);
    ArgumentChecker.notNull(curveValueType, "curveValueType");
    CurveValueType.valueOf(curveValueType);
    setCurveValueType(curveValueType);
  }

  /**
   * Creates an instance with interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   * @param interpolationMethod The interpolation method name, not null
   * @param extrapolationMethod The extrapolation method name, not null
   * @param interpolationEntity The interpolation entity name, not null
   * @param curveValueType The type of the y values, not null
   */
  public DiscountCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter,
      final String interpolationMethod, final String extrapolationMethod, final String interpolationEntity, final String curveValueType) {
    super(name, referenceDateString, times, values, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity);
    ArgumentChecker.notNull(curveValueType, "curveValueType");
    CurveValueType.valueOf(curveValueType);
    setCurveValueType(curveValueType);
  }

  @Override
  public CurveInterface buildCurve() {
    final CurveValueType type = CurveValueType.valueOf(_curveValueType);
    switch(type) {
      case DISCOUNT_FACTORS:
        if (getInterpolationMethod() != null) {
          final InterpolationMethod interpolationMethod = InterpolationMethod.valueOf(getInterpolationMethod());
          final ExtrapolationMethod extrapolationMethod = ExtrapolationMethod.valueOf(getExtrapolationMethod());
          final InterpolationEntity interpolationEntity = InterpolationEntity.valueOf(getInterpolationEntity());
          return DiscountCurve.createDiscountCurveFromDiscountFactors(getName(), getReferenceDate(), getTimes(), getValues(),
              getIsParameter(), interpolationMethod, extrapolationMethod, interpolationEntity);
        }
        if (getReferenceDateString() != null) {
          LOGGER.warn("Reference date is set but will not be used");
        }
        return DiscountCurve.createDiscountCurveFromDiscountFactors(getName(), getTimes(), getValues());
      case ZERO_RATES:
        if (getInterpolationMethod() != null) {
          final InterpolationMethod interpolationMethod = InterpolationMethod.valueOf(getInterpolationMethod());
          final ExtrapolationMethod extrapolationMethod = ExtrapolationMethod.valueOf(getExtrapolationMethod());
          final InterpolationEntity interpolationEntity = InterpolationEntity.valueOf(getInterpolationEntity());
          return DiscountCurve.createDiscountCurveFromZeroRates(getName(), getReferenceDate(), getTimes(), getValues(),
              getIsParameter(), interpolationMethod, extrapolationMethod, interpolationEntity);
        }
        if (getReferenceDateString() != null) {
          LOGGER.warn("Reference date is set but will not be used");
        }
        return DiscountCurve.createDiscountCurveFromZeroRates(getName(), getTimes(), getValues());
      default:
        throw new IllegalStateException("Cannot create a DiscountCurve from rates of type " + type);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code DiscountCurveBean}.
   * @return the meta-bean, not null
   */
  public static DiscountCurveBean.Meta meta() {
    return DiscountCurveBean.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(DiscountCurveBean.Meta.INSTANCE);
  }

  @Override
  public DiscountCurveBean.Meta metaBean() {
    return DiscountCurveBean.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the y value types.
   * @return the value of the property, not null
   */
  public String getCurveValueType() {
    return _curveValueType;
  }

  /**
   * Sets the y value types.
   * @param curveValueType  the new value of the property, not null
   */
  public void setCurveValueType(String curveValueType) {
    JodaBeanUtils.notNull(curveValueType, "curveValueType");
    this._curveValueType = curveValueType;
  }

  /**
   * Gets the the {@code curveValueType} property.
   * @return the property, not null
   */
  public final Property<String> curveValueType() {
    return metaBean().curveValueType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public DiscountCurveBean clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      DiscountCurveBean other = (DiscountCurveBean) obj;
      return JodaBeanUtils.equal(getCurveValueType(), other.getCurveValueType()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurveValueType());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(64);
    buf.append("DiscountCurveBean{");
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
    buf.append("curveValueType").append('=').append(JodaBeanUtils.toString(getCurveValueType())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code DiscountCurveBean}.
   */
  public static class Meta extends CurveBean.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code curveValueType} property.
     */
    private final MetaProperty<String> _curveValueType = DirectMetaProperty.ofReadWrite(
        this, "curveValueType", DiscountCurveBean.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "curveValueType");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 334835740:  // curveValueType
          return _curveValueType;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends DiscountCurveBean> builder() {
      return new DirectBeanBuilder<DiscountCurveBean>(new DiscountCurveBean());
    }

    @Override
    public Class<? extends DiscountCurveBean> beanType() {
      return DiscountCurveBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code curveValueType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> curveValueType() {
      return _curveValueType;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 334835740:  // curveValueType
          return ((DiscountCurveBean) bean).getCurveValueType();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 334835740:  // curveValueType
          ((DiscountCurveBean) bean).setCurveValueType((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((DiscountCurveBean) bean)._curveValueType, "curveValueType");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
