/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.integration.serialization;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;

/**
 * Bean for the base Finmath {@link net.finmath.marketdata.model.curves.ForwardCurve}. The Finmath
 * class does not perform checks on the inputs (e.g. for non-null parameters). However, this bean does perform
 * these checks before constructing the curve.
 * <br>
 * It is possible to construct a discount curve in two ways: the payment offset can be calculated from
 * conventions or can be supplied directly. This class checks that only one of these methods is used during
 * a serialization round trip.
 */
@BeanDefinition
public abstract class AbstractForwardCurveBean extends CurveBean {

  /** The serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The payment offset code.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private String _paymentOffsetCode;

  /**
   * The payment business day name.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private String _paymentBusinessDayName;

  /**
   * The payment roll date convention name.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private String _paymentRollDateConventionName;

  /**
   * The discount curve name.
   */
  @PropertyDefinition()
  private String _discountCurveName;

  /**
   * The payment offset time.
   */
  @PropertyDefinition(validate = "notNull", set = "manual")
  private Double _paymentOffset;

  /**
   * For the builder.
   */
  /* package */AbstractForwardCurveBean() {
    super();
  }

  /**
   * Constructs an instance without parameter information or a discount curve name.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param paymentOffsetCode The payment offset code, not null
   * @param paymentBusinessDayName The payment business day name, not null
   * @param paymentRollDateConventionName The payment roll date convention name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values,
      final String paymentOffsetCode, final String paymentBusinessDayName, final String paymentRollDateConventionName) {
    super(name, referenceDateString, times, values);
    setPaymentOffsetCode(paymentOffsetCode);
    setPaymentBusinessDayName(paymentBusinessDayName);
    setPaymentRollDateConventionName(paymentRollDateConventionName);
    ArgumentChecker.isTrue(times.length == values.length, "Times and values arrays must have the same number of elements");
  }

  /**
   * Constructs an instance without a discount curve name.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   * @param paymentOffsetCode The payment offset code, not null
   * @param paymentBusinessDayName The payment business day name, not null
   * @param paymentRollDateConventionName The payment roll date convention name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter,
      final String paymentOffsetCode, final String paymentBusinessDayName, final String paymentRollDateConventionName) {
    super(name, referenceDateString, times, values, isParameter);
    setPaymentOffsetCode(paymentOffsetCode);
    setPaymentBusinessDayName(paymentBusinessDayName);
    setPaymentRollDateConventionName(paymentRollDateConventionName);
  }

  /**
   * Constructs an instance without parameter information and with a discount curve name.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param paymentOffsetCode The payment offset code, not null
   * @param paymentBusinessDayName The payment business day name, not null
   * @param paymentRollDateConventionName The payment roll date convention name, not null
   * @param discountCurveName The discount curve name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values,
      final String paymentOffsetCode, final String paymentBusinessDayName, final String paymentRollDateConventionName, final String discountCurveName) {
    super(name, referenceDateString, times, values);
    ArgumentChecker.notNull(discountCurveName, "discountCurveName");
    setPaymentOffsetCode(paymentOffsetCode);
    setPaymentBusinessDayName(paymentBusinessDayName);
    setPaymentRollDateConventionName(paymentRollDateConventionName);
    setDiscountCurveName(discountCurveName);
  }

  /**
   * Constructs an instance with a discount curve name.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   * @param paymentOffsetCode The payment offset code, not null
   * @param paymentBusinessDayName The payment business day name, not null
   * @param paymentRollDateConventionName The payment roll date convention name, not null
   * @param discountCurveName The discount curve name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter,
      final String paymentOffsetCode, final String paymentBusinessDayName, final String paymentRollDateConventionName, final String discountCurveName) {
    super(name, referenceDateString, times, values, isParameter);
    ArgumentChecker.notNull(discountCurveName, "discountCurveName");
    setPaymentOffsetCode(paymentOffsetCode);
    setPaymentBusinessDayName(paymentBusinessDayName);
    setPaymentRollDateConventionName(paymentRollDateConventionName);
    setDiscountCurveName(discountCurveName);
  }

  /**
   * Constructs an instance without parameter information or a discount curve name and with interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param interpolationMethod The interpolation method name, not null
   * @param extrapolationMethod The extrapolation method name, not null
   * @param interpolationEntity The interpolation entity name, not null
   * @param paymentOffsetCode The payment offset code, not null
   * @param paymentBusinessDayName The payment business day name, not null
   * @param paymentRollDateConventionName The payment roll date convention name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values,
      final String interpolationMethod, final String extrapolationMethod, final String interpolationEntity, final String paymentOffsetCode, final String paymentBusinessDayName,
      final String paymentRollDateConventionName) {
    super(name, referenceDateString, times, values, interpolationMethod, extrapolationMethod, interpolationEntity);
    setPaymentOffsetCode(paymentOffsetCode);
    setPaymentBusinessDayName(paymentBusinessDayName);
    setPaymentRollDateConventionName(paymentRollDateConventionName);
  }

  /**
   * Constructs an instance without a discount curve name and with interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   * @param interpolationMethod The interpolation method name, not null
   * @param extrapolationMethod The extrapolation method name, not null
   * @param interpolationEntity The interpolation entity name, not null
   * @param paymentOffsetCode The payment offset code, not null
   * @param paymentBusinessDayName The payment business day name, not null
   * @param paymentRollDateConventionName The payment roll date convention name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter,
      final String interpolationMethod, final String extrapolationMethod, final String interpolationEntity, final String paymentOffsetCode, final String paymentBusinessDayName,
      final String paymentRollDateConventionName) {
    super(name, referenceDateString, times, values, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity);
    setPaymentOffsetCode(paymentOffsetCode);
    setPaymentBusinessDayName(paymentBusinessDayName);
    setPaymentRollDateConventionName(paymentRollDateConventionName);
  }

  /**
   * Constructs an instance without parameter information and with a discount curve name and interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param interpolationMethod The interpolation method name, not null
   * @param extrapolationMethod The extrapolation method name, not null
   * @param interpolationEntity The interpolation entity name, not null
   * @param paymentOffsetCode The payment offset code, not null
   * @param paymentBusinessDayName The payment business day name, not null
   * @param paymentRollDateConventionName The payment roll date convention name, not null
   * @param discountCurveName The discount curve name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values,
      final String interpolationMethod, final String extrapolationMethod, final String interpolationEntity, final String paymentOffsetCode, final String paymentBusinessDayName,
      final String paymentRollDateConventionName, final String discountCurveName) {
    super(name, referenceDateString, times, values, interpolationMethod, extrapolationMethod, interpolationEntity);
    ArgumentChecker.notNull(discountCurveName, "discountCurveName");
    setPaymentOffsetCode(paymentOffsetCode);
    setPaymentBusinessDayName(paymentBusinessDayName);
    setPaymentRollDateConventionName(paymentRollDateConventionName);
    setDiscountCurveName(discountCurveName);
  }

  /**
   * Constructs an instance with a discount curve name and interpolation information.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   * @param interpolationMethod The interpolation method name, not null
   * @param extrapolationMethod The extrapolation method name, not null
   * @param interpolationEntity The interpolation entity name, not null
   * @param paymentOffsetCode The payment offset code, not null
   * @param paymentBusinessDayName The payment business day name, not null
   * @param paymentRollDateConventionName The payment roll date convention name, not null
   * @param discountCurveName The discount curve name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter,
      final String interpolationMethod, final String extrapolationMethod, final String interpolationEntity, final String paymentOffsetCode, final String paymentBusinessDayName,
      final String paymentRollDateConventionName, final String discountCurveName) {
    super(name, referenceDateString, times, values, isParameter, interpolationMethod, extrapolationMethod, interpolationEntity);
    ArgumentChecker.notNull(discountCurveName, "discountCurveName");
    setPaymentOffsetCode(paymentOffsetCode);
    setPaymentBusinessDayName(paymentBusinessDayName);
    setPaymentRollDateConventionName(paymentRollDateConventionName);
    setDiscountCurveName(discountCurveName);
  }

  /**
   * Constructs an instance without parameter information by setting the payment offset explicitly.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param paymentOffset The payment offset
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values,
      final double paymentOffset) {
    super(name, referenceDateString, times, values);
    setPaymentOffset(paymentOffset);
  }

  /**
   * Constructs an instance by setting the payment offset explicitly.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   * @param paymentOffset The payment offset
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter,
      final double paymentOffset) {
    super(name, referenceDateString, times, values, isParameter);
    setPaymentOffset(paymentOffset);
  }

  /**
   * Constructs an instance without parameter information and with a discount curve name by setting the payment offset explicitly.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param paymentOffset The payment offset
   * @param discountCurveName The discount curve name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values,
      final double paymentOffset, final String discountCurveName) {
    super(name, referenceDateString, times, values);
    ArgumentChecker.notNull(discountCurveName, "discountCurveName");
    setPaymentOffset(paymentOffset);
    setDiscountCurveName(discountCurveName);
  }

  /**
   * Constructs an instance with a discount curve name by setting the payment offset explicitly.
   * @param name The name of the curve, not null
   * @param referenceDateString The reference date as a string, not null
   * @param times The times, not null
   * @param values The values, not null. Must have the same number of entries as times array.
   * @param isParameter An element is true if the corresponding (times, values) point is a parameter. Not null, must
   * have the same number of entries as the times array.
   * @param paymentOffset The payment offset
   * @param discountCurveName The discount curve name, not null
   */
  public AbstractForwardCurveBean(final String name, final String referenceDateString, final double[] times, final double[] values, final boolean[] isParameter,
      final double paymentOffset, final String discountCurveName) {
    super(name, referenceDateString, times, values, isParameter);
    ArgumentChecker.notNull(discountCurveName, "discountCurveName");
    setPaymentOffset(paymentOffset);
    setDiscountCurveName(discountCurveName);
  }

  /**
   * Sets the paymentOffsetCode.
   * @param paymentOffsetCode  the new value of the property
   */
  public void setPaymentOffsetCode(final String paymentOffsetCode) {
    if (_paymentOffset != null) {
      throw new IllegalStateException("Payment offset has been set directly");
    }
    this._paymentOffsetCode = paymentOffsetCode;
  }

  /**
   * Sets the paymentBusinessDayName.
   * @param paymentBusinessDayName  the new value of the property
   */
  public void setPaymentBusinessDayName(final String paymentBusinessDayName) {
    if (_paymentOffset != null) {
      throw new IllegalStateException("Payment offset has been set directly");
    }
    this._paymentBusinessDayName = paymentBusinessDayName;
  }

  /**
   * Sets the paymentRollDateConventionName.
   * @param paymentRollDateConventionName  the new value of the property
   */
  public void setPaymentRollDateConventionName(final String paymentRollDateConventionName) {
    if (_paymentOffset != null) {
      throw new IllegalStateException("Payment offset has been set directly");
    }
    this._paymentRollDateConventionName = paymentRollDateConventionName;
  }

  /**
   * Sets the paymentOffset.
   * @param paymentOffset  the new value of the property
   */
  public void setPaymentOffset(final Double paymentOffset) {
    if (_paymentOffsetCode != null || _paymentBusinessDayName != null || _paymentRollDateConventionName != null) {
      throw new IllegalStateException("Payment offset has been set using market convention information");
    }
    this._paymentOffset = paymentOffset;
  }


  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code AbstractForwardCurveBean}.
   * @return the meta-bean, not null
   */
  public static AbstractForwardCurveBean.Meta meta() {
    return AbstractForwardCurveBean.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(AbstractForwardCurveBean.Meta.INSTANCE);
  }

  @Override
  public AbstractForwardCurveBean.Meta metaBean() {
    return AbstractForwardCurveBean.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment offset code.
   * @return the value of the property, not null
   */
  public String getPaymentOffsetCode() {
    return _paymentOffsetCode;
  }

  /**
   * Gets the the {@code paymentOffsetCode} property.
   * @return the property, not null
   */
  public final Property<String> paymentOffsetCode() {
    return metaBean().paymentOffsetCode().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment business day name.
   * @return the value of the property, not null
   */
  public String getPaymentBusinessDayName() {
    return _paymentBusinessDayName;
  }

  /**
   * Gets the the {@code paymentBusinessDayName} property.
   * @return the property, not null
   */
  public final Property<String> paymentBusinessDayName() {
    return metaBean().paymentBusinessDayName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment roll date convention name.
   * @return the value of the property, not null
   */
  public String getPaymentRollDateConventionName() {
    return _paymentRollDateConventionName;
  }

  /**
   * Gets the the {@code paymentRollDateConventionName} property.
   * @return the property, not null
   */
  public final Property<String> paymentRollDateConventionName() {
    return metaBean().paymentRollDateConventionName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the discount curve name.
   * @return the value of the property
   */
  public String getDiscountCurveName() {
    return _discountCurveName;
  }

  /**
   * Sets the discount curve name.
   * @param discountCurveName  the new value of the property
   */
  public void setDiscountCurveName(String discountCurveName) {
    this._discountCurveName = discountCurveName;
  }

  /**
   * Gets the the {@code discountCurveName} property.
   * @return the property, not null
   */
  public final Property<String> discountCurveName() {
    return metaBean().discountCurveName().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the payment offset time.
   * @return the value of the property, not null
   */
  public Double getPaymentOffset() {
    return _paymentOffset;
  }

  /**
   * Gets the the {@code paymentOffset} property.
   * @return the property, not null
   */
  public final Property<Double> paymentOffset() {
    return metaBean().paymentOffset().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      AbstractForwardCurveBean other = (AbstractForwardCurveBean) obj;
      return JodaBeanUtils.equal(getPaymentOffsetCode(), other.getPaymentOffsetCode()) &&
          JodaBeanUtils.equal(getPaymentBusinessDayName(), other.getPaymentBusinessDayName()) &&
          JodaBeanUtils.equal(getPaymentRollDateConventionName(), other.getPaymentRollDateConventionName()) &&
          JodaBeanUtils.equal(getDiscountCurveName(), other.getDiscountCurveName()) &&
          JodaBeanUtils.equal(getPaymentOffset(), other.getPaymentOffset()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentOffsetCode());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentBusinessDayName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentRollDateConventionName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDiscountCurveName());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPaymentOffset());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("AbstractForwardCurveBean{");
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
    buf.append("paymentOffsetCode").append('=').append(JodaBeanUtils.toString(getPaymentOffsetCode())).append(',').append(' ');
    buf.append("paymentBusinessDayName").append('=').append(JodaBeanUtils.toString(getPaymentBusinessDayName())).append(',').append(' ');
    buf.append("paymentRollDateConventionName").append('=').append(JodaBeanUtils.toString(getPaymentRollDateConventionName())).append(',').append(' ');
    buf.append("discountCurveName").append('=').append(JodaBeanUtils.toString(getDiscountCurveName())).append(',').append(' ');
    buf.append("paymentOffset").append('=').append(JodaBeanUtils.toString(getPaymentOffset())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code AbstractForwardCurveBean}.
   */
  public static class Meta extends CurveBean.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code paymentOffsetCode} property.
     */
    private final MetaProperty<String> _paymentOffsetCode = DirectMetaProperty.ofReadWrite(
        this, "paymentOffsetCode", AbstractForwardCurveBean.class, String.class);
    /**
     * The meta-property for the {@code paymentBusinessDayName} property.
     */
    private final MetaProperty<String> _paymentBusinessDayName = DirectMetaProperty.ofReadWrite(
        this, "paymentBusinessDayName", AbstractForwardCurveBean.class, String.class);
    /**
     * The meta-property for the {@code paymentRollDateConventionName} property.
     */
    private final MetaProperty<String> _paymentRollDateConventionName = DirectMetaProperty.ofReadWrite(
        this, "paymentRollDateConventionName", AbstractForwardCurveBean.class, String.class);
    /**
     * The meta-property for the {@code discountCurveName} property.
     */
    private final MetaProperty<String> _discountCurveName = DirectMetaProperty.ofReadWrite(
        this, "discountCurveName", AbstractForwardCurveBean.class, String.class);
    /**
     * The meta-property for the {@code paymentOffset} property.
     */
    private final MetaProperty<Double> _paymentOffset = DirectMetaProperty.ofReadWrite(
        this, "paymentOffset", AbstractForwardCurveBean.class, Double.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "paymentOffsetCode",
        "paymentBusinessDayName",
        "paymentRollDateConventionName",
        "discountCurveName",
        "paymentOffset");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1773091898:  // paymentOffsetCode
          return _paymentOffsetCode;
        case 627793281:  // paymentBusinessDayName
          return _paymentBusinessDayName;
        case -782579219:  // paymentRollDateConventionName
          return _paymentRollDateConventionName;
        case -42343463:  // discountCurveName
          return _discountCurveName;
        case 1303406137:  // paymentOffset
          return _paymentOffset;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends AbstractForwardCurveBean> builder() {
      throw new UnsupportedOperationException("AbstractForwardCurveBean is an abstract class");
    }

    @Override
    public Class<? extends AbstractForwardCurveBean> beanType() {
      return AbstractForwardCurveBean.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code paymentOffsetCode} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> paymentOffsetCode() {
      return _paymentOffsetCode;
    }

    /**
     * The meta-property for the {@code paymentBusinessDayName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> paymentBusinessDayName() {
      return _paymentBusinessDayName;
    }

    /**
     * The meta-property for the {@code paymentRollDateConventionName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> paymentRollDateConventionName() {
      return _paymentRollDateConventionName;
    }

    /**
     * The meta-property for the {@code discountCurveName} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> discountCurveName() {
      return _discountCurveName;
    }

    /**
     * The meta-property for the {@code paymentOffset} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> paymentOffset() {
      return _paymentOffset;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1773091898:  // paymentOffsetCode
          return ((AbstractForwardCurveBean) bean).getPaymentOffsetCode();
        case 627793281:  // paymentBusinessDayName
          return ((AbstractForwardCurveBean) bean).getPaymentBusinessDayName();
        case -782579219:  // paymentRollDateConventionName
          return ((AbstractForwardCurveBean) bean).getPaymentRollDateConventionName();
        case -42343463:  // discountCurveName
          return ((AbstractForwardCurveBean) bean).getDiscountCurveName();
        case 1303406137:  // paymentOffset
          return ((AbstractForwardCurveBean) bean).getPaymentOffset();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1773091898:  // paymentOffsetCode
          ((AbstractForwardCurveBean) bean).setPaymentOffsetCode((String) newValue);
          return;
        case 627793281:  // paymentBusinessDayName
          ((AbstractForwardCurveBean) bean).setPaymentBusinessDayName((String) newValue);
          return;
        case -782579219:  // paymentRollDateConventionName
          ((AbstractForwardCurveBean) bean).setPaymentRollDateConventionName((String) newValue);
          return;
        case -42343463:  // discountCurveName
          ((AbstractForwardCurveBean) bean).setDiscountCurveName((String) newValue);
          return;
        case 1303406137:  // paymentOffset
          ((AbstractForwardCurveBean) bean).setPaymentOffset((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((AbstractForwardCurveBean) bean)._paymentOffsetCode, "paymentOffsetCode");
      JodaBeanUtils.notNull(((AbstractForwardCurveBean) bean)._paymentBusinessDayName, "paymentBusinessDayName");
      JodaBeanUtils.notNull(((AbstractForwardCurveBean) bean)._paymentRollDateConventionName, "paymentRollDateConventionName");
      JodaBeanUtils.notNull(((AbstractForwardCurveBean) bean)._paymentOffset, "paymentOffset");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
