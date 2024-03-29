/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.StepInterpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;

/**
 *
 */
class SeasonalFunction extends Function1D<Double, Double> implements Bean {

  /**
   * The cumulative multiplicative seasonal factors from the reference time to the next. Array of size 12 (the 1st is 1.0, it is added to simplify the
   * implementation).
   */
  @PropertyDefinition(get = "private")
  private final double[] _monthlyCumulativeFactors;

  /**
   * The cumulative multiplicative seasonal factors from the reference time to the next. Array of size 12 (the 1st is 1.0, it is added to simplify the
   * implementation).
   */
  @PropertyDefinition(get = "private")
  private final double[] _steps;

  /**
   * The number of month in a year.
   */
  private static final int NB_MONTH = 12;

  SeasonalFunction(final double[] steps, final double[] monthlyFactors, final boolean isAdditive) {
    Validate.notNull(monthlyFactors, "Monthly factors");
    Validate.notNull(steps, "steps");
    Validate.isTrue(monthlyFactors.length == 11, "Monthly factors with incorrect length; should be 11");
    Validate.notNull(isAdditive, "isAdditive");
    _steps = steps;

    final double[] cumulativeFactors = new double[NB_MONTH];
    cumulativeFactors[0] = 1.0;

    /**
     * monthlyFactors
     */
    for (int loopmonth = 1; loopmonth < NB_MONTH; loopmonth++) {
      if (isAdditive) {
        cumulativeFactors[loopmonth] = cumulativeFactors[loopmonth - 1] + monthlyFactors[loopmonth - 1];
      } else {
        cumulativeFactors[loopmonth] = cumulativeFactors[loopmonth - 1] * monthlyFactors[loopmonth - 1];
      }
    }
    /**
     * Here we are constructing a 12-periodic vector of the same size of the step vector, and using the vector cumulative.
     */
    final int numberOfSteps = steps.length;
    _monthlyCumulativeFactors = new double[numberOfSteps];
    for (int loopmonth = 0; loopmonth < numberOfSteps; loopmonth++) {
      _monthlyCumulativeFactors[loopmonth] = cumulativeFactors[loopmonth % 12];
    }
  }

  @Override
  public Double evaluate(final Double x) {
    final StepInterpolator1D interpolator = new StepInterpolator1D();
    final Interpolator1DDataBundle dataBundle = interpolator.getDataBundleFromSortedArrays(_steps, _monthlyCumulativeFactors);
    return interpolator.interpolate(dataBundle, x);
  }

  // ------------------------- AUTOGENERATED START -------------------------
  /// CLOVER:OFF
  /**
   * The meta-bean for {@code SeasonalFunction}.
   * 
   * @return the meta-bean, not null
   */
  public static SeasonalFunction.Meta meta() {
    return SeasonalFunction.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(SeasonalFunction.Meta.INSTANCE);
  }

  @Override
  public SeasonalFunction.Meta metaBean() {
    return SeasonalFunction.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(final String propertyName) {
    return metaBean().<R> metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  // -----------------------------------------------------------------------
  /**
   * Gets the cumulative multiplicative seasonal factors from the reference time to the next. Array of size 12 (the 1st is 1.0, it is added to simplify the
   * implementation).
   * 
   * @return the value of the property
   */
  private double[] getMonthlyCumulativeFactors() {
    return _monthlyCumulativeFactors != null ? _monthlyCumulativeFactors.clone() : null;
  }

  /**
   * Gets the the {@code monthlyCumulativeFactors} property.
   * 
   * @return the property, not null
   */
  public final Property<double[]> monthlyCumulativeFactors() {
    return metaBean().monthlyCumulativeFactors().createProperty(this);
  }

  // -----------------------------------------------------------------------
  /**
   * Gets the cumulative multiplicative seasonal factors from the reference time to the next. Array of size 12 (the 1st is 1.0, it is added to simplify the
   * implementation).
   * 
   * @return the value of the property
   */
  private double[] getSteps() {
    return _steps != null ? _steps.clone() : null;
  }

  /**
   * Gets the the {@code steps} property.
   * 
   * @return the property, not null
   */
  public final Property<double[]> steps() {
    return metaBean().steps().createProperty(this);
  }

  // -----------------------------------------------------------------------
  @Override
  public SeasonalFunction clone() {
    final BeanBuilder<? extends SeasonalFunction> builder = metaBean().builder();
    for (final MetaProperty<?> mp : metaBean().metaPropertyIterable()) {
      if (mp.style().isBuildable()) {
        Object value = mp.get(this);
        if (value instanceof Bean) {
          value = JodaBeanUtils.clone((Bean) value);
        }
        builder.set(mp.name(), value);
      }
    }
    return builder.build();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      final SeasonalFunction other = (SeasonalFunction) obj;
      return JodaBeanUtils.equal(getMonthlyCumulativeFactors(), other.getMonthlyCumulativeFactors()) &&
          JodaBeanUtils.equal(getSteps(), other.getSteps());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash += hash * 31 + JodaBeanUtils.hashCode(getMonthlyCumulativeFactors());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSteps());
    return hash;
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder(96);
    buf.append("SeasonalFunction{");
    final int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(final StringBuilder buf) {
    buf.append("monthlyCumulativeFactors").append('=').append(getMonthlyCumulativeFactors()).append(',').append(' ');
    buf.append("steps").append('=').append(getSteps()).append(',').append(' ');
  }

  // -----------------------------------------------------------------------
  /**
   * The meta-bean for {@code SeasonalFunction}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code monthlyCumulativeFactors} property.
     */
    private final MetaProperty<double[]> _monthlyCumulativeFactors = DirectMetaProperty.ofReadOnly(
        this, "monthlyCumulativeFactors", SeasonalFunction.class, double[].class);
    /**
     * The meta-property for the {@code steps} property.
     */
    private final MetaProperty<double[]> _steps = DirectMetaProperty.ofReadOnly(
        this, "steps", SeasonalFunction.class, double[].class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "monthlyCumulativeFactors",
        "steps");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(final String propertyName) {
      switch (propertyName.hashCode()) {
        case 457851908: // monthlyCumulativeFactors
          return _monthlyCumulativeFactors;
        case 109761319: // steps
          return _steps;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends SeasonalFunction> builder() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Class<? extends SeasonalFunction> beanType() {
      return SeasonalFunction.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    // -----------------------------------------------------------------------
    /**
     * The meta-property for the {@code monthlyCumulativeFactors} property.
     * 
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> monthlyCumulativeFactors() {
      return _monthlyCumulativeFactors;
    }

    /**
     * The meta-property for the {@code steps} property.
     * 
     * @return the meta-property, not null
     */
    public final MetaProperty<double[]> steps() {
      return _steps;
    }

    // -----------------------------------------------------------------------
    @Override
    protected Object propertyGet(final Bean bean, final String propertyName, final boolean quiet) {
      switch (propertyName.hashCode()) {
        case 457851908: // monthlyCumulativeFactors
          return ((SeasonalFunction) bean).getMonthlyCumulativeFactors();
        case 109761319: // steps
          return ((SeasonalFunction) bean).getSteps();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(final Bean bean, final String propertyName, final Object newValue, final boolean quiet) {
      switch (propertyName.hashCode()) {
        case 457851908: // monthlyCumulativeFactors
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: monthlyCumulativeFactors");
        case 109761319: // steps
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: steps");
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  /// CLOVER:ON
  // -------------------------- AUTOGENERATED END --------------------------
}
