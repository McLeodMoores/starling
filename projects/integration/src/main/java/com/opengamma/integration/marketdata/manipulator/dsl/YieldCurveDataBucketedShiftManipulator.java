/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.ImmutableConstructor;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.Period;
import org.threeten.bp.ZonedDateTime;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.marketdata.manipulator.function.StructureManipulator;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.FixedIncomeStripWithSecurity;
import com.opengamma.financial.analytics.ircurve.StripInstrumentType;
import com.opengamma.financial.analytics.ircurve.YieldCurveData;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * A {@link StructureManipulator} which performs a list of bucketed shifts on {@link YieldCurveData}.
 */
@BeanDefinition
public final class YieldCurveDataBucketedShiftManipulator implements ImmutableBean, StructureManipulator<YieldCurveData> {

  private static final Logger LOGGER = LoggerFactory.getLogger(YieldCurveDataBucketedShiftManipulator.class);

  /** Shift type. */
  @PropertyDefinition(validate = "notNull")
  private final ScenarioShiftType _shiftType;

  /** Shifts to apply. */
  @PropertyDefinition(validate = "notNull")
  private final ImmutableList<YieldCurveBucketedShift> _shifts;

  @ImmutableConstructor
  public YieldCurveDataBucketedShiftManipulator(final ScenarioShiftType shiftType, final List<YieldCurveBucketedShift> shifts) {
    _shiftType = ArgumentChecker.notNull(shiftType, "shiftType");
    _shifts = ImmutableList.copyOf(ArgumentChecker.notEmpty(shifts, "shifts"));
  }

  @Override
  public YieldCurveData execute(final YieldCurveData curveData,
      final ValueSpecification valueSpecification,
      final FunctionExecutionContext executionContext) {
    final ZonedDateTime valuationTime = ZonedDateTime.now(executionContext.getValuationClock());
    final Map<ExternalIdBundle, Double> data = Maps.newHashMap(curveData.getDataPoints());
    final Map<ExternalId, ExternalIdBundle> index = curveData.getIndex();
    for (final YieldCurveBucketedShift shift : _shifts) {
      for (final FixedIncomeStripWithSecurity strip : curveData.getCurveSpecification().getStrips()) {
        final Period stripPeriod = strip.getTenor().getPeriod();
        final Period shiftStart = shift.getStart();
        final Period shiftEnd = shift.getEnd();
        final ZonedDateTime stripTime = valuationTime.plus(stripPeriod);
        final ZonedDateTime shiftStartTime = valuationTime.plus(shiftStart);
        final ZonedDateTime shiftEndTime = valuationTime.plus(shiftEnd);

        if (stripTime.compareTo(shiftStartTime) >= 0 && stripTime.compareTo(shiftEndTime) <= 0) {
          final ExternalIdBundle bundle = index.get(strip.getSecurityIdentifier());
          final boolean future = strip.getInstrumentType() == StripInstrumentType.FUTURE;
          final Double originalData = data.get(bundle);
          Double stripData;

          // futures are quoted the other way round from other instruments
          if (future) {
            stripData = 1 - originalData;
          } else {
            stripData = originalData;
          }
          Double shiftedData;

          if (_shiftType == ScenarioShiftType.RELATIVE) {
            // add shift amount to 1. i.e. 10.pc actualy means 'value * 1.1' and -10.pc means 'value * 0.9'
            shiftedData = stripData * (shift.getShift() + 1);
          } else {
            shiftedData = stripData + shift.getShift();
          }
          Double shiftedStripData;

          if (future) {
            shiftedStripData = 1 - shiftedData;
          } else {
            shiftedStripData = shiftedData;
          }
          data.put(bundle, shiftedStripData);
          LOGGER.debug("Shifting data {}, tenor {} by {} from {} to {}",
              strip.getSecurityIdentifier(), strip.getTenor(), shift.getShift(), originalData, shiftedStripData);
        }
      }
    }
    return new YieldCurveData(curveData.getCurveSpecification(), data);
  }

  @Override
  public Class<YieldCurveData> getExpectedType() {
    return YieldCurveData.class;
  }

  //------------------------- AUTOGENERATED START -------------------------
  /**
   * The meta-bean for {@code YieldCurveDataBucketedShiftManipulator}.
   * @return the meta-bean, not null
   */
  public static YieldCurveDataBucketedShiftManipulator.Meta meta() {
    return YieldCurveDataBucketedShiftManipulator.Meta.INSTANCE;
  }

  static {
    MetaBean.register(YieldCurveDataBucketedShiftManipulator.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static YieldCurveDataBucketedShiftManipulator.Builder builder() {
    return new YieldCurveDataBucketedShiftManipulator.Builder();
  }

  @Override
  public YieldCurveDataBucketedShiftManipulator.Meta metaBean() {
    return YieldCurveDataBucketedShiftManipulator.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets shift type.
   * @return the value of the property, not null
   */
  public ScenarioShiftType getShiftType() {
    return _shiftType;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets shifts to apply.
   * @return the value of the property, not null
   */
  public ImmutableList<YieldCurveBucketedShift> getShifts() {
    return _shifts;
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
      YieldCurveDataBucketedShiftManipulator other = (YieldCurveDataBucketedShiftManipulator) obj;
      return JodaBeanUtils.equal(_shiftType, other._shiftType) &&
          JodaBeanUtils.equal(_shifts, other._shifts);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(_shiftType);
    hash = hash * 31 + JodaBeanUtils.hashCode(_shifts);
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(96);
    buf.append("YieldCurveDataBucketedShiftManipulator{");
    buf.append("shiftType").append('=').append(_shiftType).append(',').append(' ');
    buf.append("shifts").append('=').append(JodaBeanUtils.toString(_shifts));
    buf.append('}');
    return buf.toString();
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code YieldCurveDataBucketedShiftManipulator}.
   */
  public static final class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code shiftType} property.
     */
    private final MetaProperty<ScenarioShiftType> _shiftType = DirectMetaProperty.ofImmutable(
        this, "shiftType", YieldCurveDataBucketedShiftManipulator.class, ScenarioShiftType.class);
    /**
     * The meta-property for the {@code shifts} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<ImmutableList<YieldCurveBucketedShift>> _shifts = DirectMetaProperty.ofImmutable(
        this, "shifts", YieldCurveDataBucketedShiftManipulator.class, (Class) ImmutableList.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "shiftType",
        "shifts");

    /**
     * Restricted constructor.
     */
    private Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return _shiftType;
        case -903338959:  // shifts
          return _shifts;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public YieldCurveDataBucketedShiftManipulator.Builder builder() {
      return new YieldCurveDataBucketedShiftManipulator.Builder();
    }

    @Override
    public Class<? extends YieldCurveDataBucketedShiftManipulator> beanType() {
      return YieldCurveDataBucketedShiftManipulator.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code shiftType} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ScenarioShiftType> shiftType() {
      return _shiftType;
    }

    /**
     * The meta-property for the {@code shifts} property.
     * @return the meta-property, not null
     */
    public MetaProperty<ImmutableList<YieldCurveBucketedShift>> shifts() {
      return _shifts;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return ((YieldCurveDataBucketedShiftManipulator) bean).getShiftType();
        case -903338959:  // shifts
          return ((YieldCurveDataBucketedShiftManipulator) bean).getShifts();
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
   * The bean-builder for {@code YieldCurveDataBucketedShiftManipulator}.
   */
  public static final class Builder extends DirectFieldsBeanBuilder<YieldCurveDataBucketedShiftManipulator> {

    private ScenarioShiftType _shiftType;
    private List<YieldCurveBucketedShift> _shifts = ImmutableList.of();

    /**
     * Restricted constructor.
     */
    private Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    private Builder(YieldCurveDataBucketedShiftManipulator beanToCopy) {
      this._shiftType = beanToCopy.getShiftType();
      this._shifts = beanToCopy.getShifts();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          return _shiftType;
        case -903338959:  // shifts
          return _shifts;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 893345500:  // shiftType
          this._shiftType = (ScenarioShiftType) newValue;
          break;
        case -903338959:  // shifts
          this._shifts = (List<YieldCurveBucketedShift>) newValue;
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

    @Override
    public YieldCurveDataBucketedShiftManipulator build() {
      return new YieldCurveDataBucketedShiftManipulator(
          _shiftType,
          _shifts);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets shift type.
     * @param shiftType  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shiftType(ScenarioShiftType shiftType) {
      JodaBeanUtils.notNull(shiftType, "shiftType");
      this._shiftType = shiftType;
      return this;
    }

    /**
     * Sets shifts to apply.
     * @param shifts  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shifts(List<YieldCurveBucketedShift> shifts) {
      JodaBeanUtils.notNull(shifts, "shifts");
      this._shifts = shifts;
      return this;
    }

    /**
     * Sets the {@code shifts} property in the builder
     * from an array of objects.
     * @param shifts  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder shifts(YieldCurveBucketedShift... shifts) {
      return shifts(ImmutableList.copyOf(shifts));
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(96);
      buf.append("YieldCurveDataBucketedShiftManipulator.Builder{");
      buf.append("shiftType").append('=').append(JodaBeanUtils.toString(_shiftType)).append(',').append(' ');
      buf.append("shifts").append('=').append(JodaBeanUtils.toString(_shifts));
      buf.append('}');
      return buf.toString();
    }

  }

  //-------------------------- AUTOGENERATED END --------------------------
}
