/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.finmath.serialization;

import java.util.Map;

import net.finmath.marketdata.model.curves.Curve;
import net.finmath.marketdata.model.curves.Curve.CurveBuilder;
import net.finmath.marketdata.model.curves.Curve.ExtrapolationMethod;
import net.finmath.marketdata.model.curves.Curve.InterpolationEntity;
import net.finmath.marketdata.model.curves.Curve.InterpolationMethod;
import net.finmath.marketdata.model.curves.CurveInterface;

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

import com.opengamma.util.ArgumentChecker;

/**
 * Bean for the Finmath curves class {@link net.finmath.marketdata.model.curves.Curve}. The Finmath
 * class does not perform checks on the inputs (e.g. for non-null parameters). However, this bean does
 * perform these checks on construction. It is not necessary to set all of the interpolation information,
 * as null values will indicate that the defaults defined in the Finmath class will be used. However, if
 *
 */
@BeanDefinition
public class CurveBean extends AbstractCurveBean {

  /** The serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The times.
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _times;

  /**
   * The values
   */
  @PropertyDefinition(validate = "notNull")
  private double[] _values;

  /**
   * An element is true if the corresponding (times, values) point is a parameter.
   */
  @PropertyDefinition(validate = "notNull")
  private boolean[] _isParameter;

  /**
   * The interpolation method.
   */
  @PropertyDefinition
  private String _interpolationMethod;

  /**
   * The extrapolation method.
   */
  @PropertyDefinition
  private String _extrapolationMethod;

  /**
   * The interpolation entity name.
   */
  @PropertyDefinition
  private String _interpolationEntity;

  /**
   * For the builder.
   */
  /* package */CurveBean() {
    super();
  }

  /**
   * Constructs an instance without parameter or interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   */
  public CurveBean(final String name, final String referenceDateString, final double[] times, final double[] values) {
    super(name, referenceDateString);
    setTimes(times);
    setValues(values);
    ArgumentChecker.isTrue(times.length == values.length, "Times and values arrays must have the same number of elements");
  }

  /**
   * Constructs an instance without interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   */
  public CurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter) {
    super(name, referenceDateString);
    setTimes(times);
    setValues(values);
    setIsParameter(isParameter);
    ArgumentChecker.isTrue(times.length == values.length, "Times and values arrays must have the same number of elements");
    ArgumentChecker.isTrue(times.length == isParameter.length, "Times and isParameter arrays must have the same number of elements");
  }

  /**
   * Constructs an instance without parameter information and with interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * have the same number of entries as the times array.
   * @param interpolationMethod The interpolation method name, not null
   * @param extrapolationMethod The extrapolation method name, not null
   * @param interpolationEntity The interpolation entity name, not null
   */
  public CurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final String interpolationMethod,
      final String extrapolationMethod, final String interpolationEntity) {
    super(name, referenceDateString);
    ArgumentChecker.notNull(interpolationMethod, "interpolationMethod");
    ArgumentChecker.notNull(extrapolationMethod, "extrapolationMethod");
    ArgumentChecker.notNull(interpolationEntity, "interpolationEntity");
    setTimes(times);
    setValues(values);
    ArgumentChecker.isTrue(times.length == values.length, "Times and values arrays must have the same number of elements");
    setInterpolationMethod(interpolationMethod);
    setExtrapolationMethod(extrapolationMethod);
    setInterpolationEntity(interpolationEntity);
    InterpolationMethod.valueOf(interpolationMethod);
    ExtrapolationMethod.valueOf(extrapolationMethod);
    InterpolationEntity.valueOf(interpolationEntity);
  }

  /**
   * Constructs an instance with interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   * @param interpolationMethod The interpolation method name, not null
   * @param extrapolationMethod The extrapolation method name, not null
   * @param interpolationEntity The interpolation entity name, not null
   */
  public CurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter,
      final String interpolationMethod, final String extrapolationMethod, final String interpolationEntity) {
    super(name, referenceDateString);
    ArgumentChecker.notNull(interpolationMethod, "interpolationMethod");
    ArgumentChecker.notNull(extrapolationMethod, "extrapolationMethod");
    ArgumentChecker.notNull(interpolationEntity, "interpolationEntity");
    setTimes(times);
    setValues(values);
    setIsParameter(isParameter);
    ArgumentChecker.isTrue(times.length == values.length, "Times and values arrays must have the same number of elements");
    ArgumentChecker.isTrue(times.length == isParameter.length, "Times and isParameter arrays must have the same number of elements");
    setInterpolationMethod(interpolationMethod);
    setExtrapolationMethod(extrapolationMethod);
    setInterpolationEntity(interpolationEntity);
    InterpolationMethod.valueOf(interpolationMethod);
    ExtrapolationMethod.valueOf(extrapolationMethod);
    InterpolationEntity.valueOf(interpolationEntity);
  }

  @Override
  public CurveInterface buildCurve() {
    final CurveBuilder builder = new Curve.CurveBuilder(getName(), getReferenceDate());
    if (getInterpolationMethod() != null) {
      builder.setInterpolationMethod(InterpolationMethod.valueOf(getInterpolationMethod()));
    }
    if (getExtrapolationMethod() != null) {
      builder.setExtrapolationMethod(ExtrapolationMethod.valueOf(getExtrapolationMethod()));
    }
    if (getInterpolationEntity() != null) {
      builder.setInterpolationEntity(InterpolationEntity.valueOf(getInterpolationEntity()));
    }
    final int length = getTimes().length;
    for (int i = 0; i < length; i++) {
      builder.addPoint(getTimes()[i], getValues()[i], getIsParameter()[i]);
    }
    try {
      return builder.build();
    } catch (final CloneNotSupportedException e) {
      throw new IllegalStateException(e);
    }
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CurveBean}.
   * @return the meta-bean, not null
   */
  public static CurveBean.Meta meta() {
    return CurveBean.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CurveBean.Meta.INSTANCE);
  }

  @Override
  public CurveBean.Meta metaBean() {
    return CurveBean.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the times.
   * @return the value of the property, not null
   */
  public double[] getTimes() {
    return _times;
  }

  /**
   * Sets the times.
   * @param times  the new value of the property, not null
   */
  public void setTimes(double[] times) {
    JodaBeanUtils.notNull(times, "times");
    this._times = times;
  }

  /**
   * Gets the the {@code times} property.
   * @return the property, not null
   */
  public final Property<double[]> times() {
    return metaBean().times().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the values
   * @return the value of the property, not null
   */
  public double[] getValues() {
    return _values;
  }

  /**
   * Sets the values
   * @param values  the new value of the property, not null
   */
  public void setValues(double[] values) {
    JodaBeanUtils.notNull(values, "values");
    this._values = values;
  }

  /**
   * Gets the the {@code values} property.
   * @return the property, not null
   */
  public final Property<double[]> values() {
    return metaBean().values().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets an element is true if the corresponding (times, values) point is a parameter.
   * @return the value of the property, not null
   */
  public boolean[] getIsParameter() {
    return _isParameter;
  }

  /**
   * Sets an element is true if the corresponding (times, values) point is a parameter.
   * @param isParameter  the new value of the property, not null
   */
  public void setIsParameter(boolean[] isParameter) {
    JodaBeanUtils.notNull(isParameter, "isParameter");
    this._isParameter = isParameter;
  }

  /**
   * Gets the the {@code isParameter} property.
   * @return the property, not null
   */
  public final Property<boolean[]> isParameter() {
    return metaBean().isParameter().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the interpolation method.
   * @return the value of the property
   */
  public String getInterpolationMethod() {
    return _interpolationMethod;
  }

  /**
   * Sets the interpolation method.
   * @param interpolationMethod  the new value of the property
   */
  public void setInterpolationMethod(String interpolationMethod) {
    this._interpolationMethod = interpolationMethod;
  }

  /**
   * Gets the the {@code interpolationMethod} property.
   * @return the property, not null
   */
  public final Property<String> interpolationMethod() {
    return metaBean().interpolationMethod().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the extrapolation method.
   * @return the value of the property
   */
  public String getExtrapolationMethod() {
    return _extrapolationMethod;
  }

  /**
   * Sets the extrapolation method.
   * @param extrapolationMethod  the new value of the property
   */
  public void setExtrapolationMethod(String extrapolationMethod) {
    this._extrapolationMethod = extrapolationMethod;
  }

  /**
   * Gets the the {@code extrapolationMethod} property.
   * @return the property, not null
   */
  public final Property<String> extrapolationMethod() {
    return metaBean().extrapolationMethod().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the interpolation entity name.
   * @return the value of the property
   */
  public String getInterpolationEntity() {
    return _interpolationEntity;
  }

  /**
   * Sets the interpolation entity name.
   * @param interpolationEntity  the new value of the property
   */
  public void setInterpolationEntity(String interpolationEntity) {
    this._interpolationEntity = interpolationEntity;
  }

  /**
   * Gets the the {@code interpolationEntity} property.
   * @return the property, not null
   */
  public final Property<String> interpolationEntity() {
    return metaBean().interpolationEntity().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public CurveBean clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CurveBean other = (CurveBean) obj;
      return JodaBeanUtils.equal(getTimes(), other.getTimes()) &&
          JodaBeanUtils.equal(getValues(), other.getValues()) &&
          JodaBeanUtils.equal(getIsParameter(), other.getIsParameter()) &&
          JodaBeanUtils.equal(getInterpolationMethod(), other.getInterpolationMethod()) &&
          JodaBeanUtils.equal(getExtrapolationMethod(), other.getExtrapolationMethod()) &&
          JodaBeanUtils.equal(getInterpolationEntity(), other.getInterpolationEntity()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getTimes());
    hash = hash * 31 + JodaBeanUtils.hashCode(getValues());
    hash = hash * 31 + JodaBeanUtils.hashCode(getIsParameter());
    hash = hash * 31 + JodaBeanUtils.hashCode(getInterpolationMethod());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExtrapolationMethod());
    hash = hash * 31 + JodaBeanUtils.hashCode(getInterpolationEntity());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("CurveBean{");
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
    buf.append("times").append('=').append(JodaBeanUtils.toString(getTimes())).append(',').append(' ');
    buf.append("values").append('=').append(JodaBeanUtils.toString(getValues())).append(',').append(' ');
    buf.append("isParameter").append('=').append(JodaBeanUtils.toString(getIsParameter())).append(',').append(' ');
    buf.append("interpolationMethod").append('=').append(JodaBeanUtils.toString(getInterpolationMethod())).append(',').append(' ');
    buf.append("extrapolationMethod").append('=').append(JodaBeanUtils.toString(getExtrapolationMethod())).append(',').append(' ');
    buf.append("interpolationEntity").append('=').append(JodaBeanUtils.toString(getInterpolationEntity())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CurveBean}.
   */
  public static class Meta extends AbstractCurveBean.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code times} property.
     */
    private final MetaProperty<double[]> _times = DirectMetaProperty.ofReadWrite(
        this, "times", CurveBean.class, double[].class);
    /**
     * The meta-property for the {@code values} property.
     */
    private final MetaProperty<double[]> _values = DirectMetaProperty.ofReadWrite(
        this, "values", CurveBean.class, double[].class);
    /**
     * The meta-property for the {@code isParameter} property.
     */
    private final MetaProperty<boolean[]> _isParameter = DirectMetaProperty.ofReadWrite(
        this, "isParameter", CurveBean.class, boolean[].class);
    /**
     * The meta-property for the {@code interpolationMethod} property.
     */
    private final MetaProperty<String> _interpolationMethod = DirectMetaProperty.ofReadWrite(
        this, "interpolationMethod", CurveBean.class, String.class);
    /**
     * The meta-property for the {@code extrapolationMethod} property.
     */
    private final MetaProperty<String> _extrapolationMethod = DirectMetaProperty.ofReadWrite(
        this, "extrapolationMethod", CurveBean.class, String.class);
    /**
     * The meta-property for the {@code interpolationEntity} property.
     */
    private final MetaProperty<String> _interpolationEntity = DirectMetaProperty.ofReadWrite(
        this, "interpolationEntity", CurveBean.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "times",
        "values",
        "isParameter",
        "interpolationMethod",
        "extrapolationMethod",
        "interpolationEntity");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 110364486:  // times
          return _times;
        case -823812830:  // values
          return _values;
        case -1162087009:  // isParameter
          return _isParameter;
        case 374385573:  // interpolationMethod
          return _interpolationMethod;
        case 170930137:  // extrapolationMethod
          return _extrapolationMethod;
        case 153665191:  // interpolationEntity
          return _interpolationEntity;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CurveBean> builder() {
      return new DirectBeanBuilder<CurveBean>(new CurveBean());
    }

    @Override
    public Class<? extends CurveBean> beanType() {
      return CurveBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code times} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> times() {
      return _times;
    }

    /**
     * The meta-property for the {@code values} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> values() {
      return _values;
    }

    /**
     * The meta-property for the {@code isParameter} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<boolean[]> isParameter() {
      return _isParameter;
    }

    /**
     * The meta-property for the {@code interpolationMethod} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> interpolationMethod() {
      return _interpolationMethod;
    }

    /**
     * The meta-property for the {@code extrapolationMethod} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> extrapolationMethod() {
      return _extrapolationMethod;
    }

    /**
     * The meta-property for the {@code interpolationEntity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> interpolationEntity() {
      return _interpolationEntity;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 110364486:  // times
          return ((CurveBean) bean).getTimes();
        case -823812830:  // values
          return ((CurveBean) bean).getValues();
        case -1162087009:  // isParameter
          return ((CurveBean) bean).getIsParameter();
        case 374385573:  // interpolationMethod
          return ((CurveBean) bean).getInterpolationMethod();
        case 170930137:  // extrapolationMethod
          return ((CurveBean) bean).getExtrapolationMethod();
        case 153665191:  // interpolationEntity
          return ((CurveBean) bean).getInterpolationEntity();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 110364486:  // times
          ((CurveBean) bean).setTimes((double[]) newValue);
          return;
        case -823812830:  // values
          ((CurveBean) bean).setValues((double[]) newValue);
          return;
        case -1162087009:  // isParameter
          ((CurveBean) bean).setIsParameter((boolean[]) newValue);
          return;
        case 374385573:  // interpolationMethod
          ((CurveBean) bean).setInterpolationMethod((String) newValue);
          return;
        case 170930137:  // extrapolationMethod
          ((CurveBean) bean).setExtrapolationMethod((String) newValue);
          return;
        case 153665191:  // interpolationEntity
          ((CurveBean) bean).setInterpolationEntity((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((CurveBean) bean)._times, "times");
      JodaBeanUtils.notNull(((CurveBean) bean)._values, "values");
      JodaBeanUtils.notNull(((CurveBean) bean)._isParameter, "isParameter");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
