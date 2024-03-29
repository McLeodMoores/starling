/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.credit.isdastandardmodel;

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
import org.threeten.bp.LocalDate;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.convention.daycount.DayCounts;
import com.opengamma.util.ArgumentChecker;

/**
 * An ISDA compliant date curve.
 */
@BeanDefinition
public class ISDACompliantDateCurve
    extends ISDACompliantCurve
    implements ISDACompliantCurveWithDates {

  /**
   * The standard ACT/365 day count.
   */
  private static final DayCount ACT_365 = DayCounts.ACT_365;

  /**
   * The base date.
   */
  @PropertyDefinition(set = "private")
  private LocalDate _baseDate;
  /**
   * The knot dates on the curve.
   */
  @PropertyDefinition(get = "private", set = "private")
  private LocalDate[] _dates;
  /**
   * The day count.
   */
  @PropertyDefinition(get = "private", set = "private")
  private DayCount _dayCount;

  //-------------------------------------------------------------------------
  //  protected static ISDACompliantCurve makeISDACompliantCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
  //    return makeISDACompliantCurve(baseDate, dates, rates, ACT_365);
  //  }

  protected static ISDACompliantCurve makeISDACompliantCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    final double[] t = checkAndGetTimes(baseDate, dates, rates, dayCount);
    return new ISDACompliantCurve(t, rates);
  }

  //-------------------------------------------------------------------------
  /**
   * Constructor for Joda-Beans.
   */
  protected ISDACompliantDateCurve() {
  }

  /**
   * Builds a curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates
   * The times (year-fractions) between the baseDate and the knot dates is calculated using ACT/365.
   *
   * @param baseDate  the base date for the curve (i.e. this is time zero), not null
   * @param dates  the knot dates on the curve. These must be ascending with the first date after the baseDate, not null
   * @param rates  the continually compounded zero rates at given knot dates, not null
   */
  public ISDACompliantDateCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    this(baseDate, dates, rates, ACT_365);
  }

  /**
   * Builds a curve from a baseDate with a set of <b>continually compounded</b> zero rates at given knot dates.
   * The times (year-fractions) between the baseDate and the knot dates is calculated using the specified day-count-convention.
   *
   * @param baseDate  the base date for the curve (i.e. this is time zero), not null
   * @param dates  the knot dates on the curve. These must be ascending with the first date after the baseDate, not null
   * @param rates  the continually compounded zero rates at given knot dates, not null
   * @param dayCount  the day-count-convention, not null
   */
  public ISDACompliantDateCurve(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    this(baseDate, dates, dayCount, makeISDACompliantCurve(baseDate, dates, rates, dayCount));
  }

  private ISDACompliantDateCurve(final LocalDate baseDate, final LocalDate[] dates, final DayCount dayCount, final ISDACompliantCurve baseCurve) {
    super(baseCurve);
    _baseDate = baseDate;
    _dates = dates;
    _dayCount = dayCount;
  }

  //-------------------------------------------------------------------------
  @Override
  public final LocalDate getCurveDate(final int index) {
    return _dates[index];
  }

  @Override
  public final LocalDate[] getCurveDates() {
    return _dates.clone();
  }

  @Override
  public ISDACompliantDateCurve withRate(final double rate, final int index) {
    final ISDACompliantCurve temp = super.withRate(rate, index);
    return new ISDACompliantDateCurve(_baseDate, _dates, _dayCount, temp);
  }

  protected static double[] checkAndGetTimes(final LocalDate baseDate, final LocalDate[] dates, final double[] rates) {
    return checkAndGetTimes(baseDate, dates, rates, ACT_365);
  }

  protected static double[] checkAndGetTimes(final LocalDate baseDate, final LocalDate[] dates, final double[] rates, final DayCount dayCount) {
    ArgumentChecker.notNull(baseDate, "null baseDate");
    ArgumentChecker.notNull(dayCount, "null dayCount");
    ArgumentChecker.noNulls(dates, "null dates");
    ArgumentChecker.notEmpty(rates, "empty rates");
    ArgumentChecker.isTrue(dates[0].isAfter(baseDate), "first date is not after base date");
    final int n = dates.length;
    ArgumentChecker.isTrue(rates.length == n, "rates and dates different lengths");
    final double[] t = new double[n];
    for (int i = 0; i < n; i++) {
      t[i] = dayCount.getDayCountFraction(baseDate, dates[i]);
      if (i > 0) {
        ArgumentChecker.isTrue(t[i] > t[i - 1], "dates are not ascending");
      }
    }
    return t;
  }

  @Override
  public double getZeroRate(final LocalDate date) {
    final double t = _dayCount.getDayCountFraction(_baseDate, date);
    return getZeroRate(t);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ISDACompliantDateCurve}.
   * @return the meta-bean, not null
   */
  public static ISDACompliantDateCurve.Meta meta() {
    return ISDACompliantDateCurve.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(ISDACompliantDateCurve.Meta.INSTANCE);
  }

  @Override
  public ISDACompliantDateCurve.Meta metaBean() {
    return ISDACompliantDateCurve.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the base date.
   * @return the value of the property
   */
  public LocalDate getBaseDate() {
    return _baseDate;
  }

  /**
   * Sets the base date.
   * @param baseDate  the new value of the property
   */
  private void setBaseDate(LocalDate baseDate) {
    this._baseDate = baseDate;
  }

  /**
   * Gets the the {@code baseDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> baseDate() {
    return metaBean().baseDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the knot dates on the curve.
   * @return the value of the property
   */
  private LocalDate[] getDates() {
    return _dates;
  }

  /**
   * Sets the knot dates on the curve.
   * @param dates  the new value of the property
   */
  private void setDates(LocalDate[] dates) {
    this._dates = dates;
  }

  /**
   * Gets the the {@code dates} property.
   * @return the property, not null
   */
  public final Property<LocalDate[]> dates() {
    return metaBean().dates().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count.
   * @return the value of the property
   */
  private DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the day count.
   * @param dayCount  the new value of the property
   */
  private void setDayCount(DayCount dayCount) {
    this._dayCount = dayCount;
  }

  /**
   * Gets the the {@code dayCount} property.
   * @return the property, not null
   */
  public final Property<DayCount> dayCount() {
    return metaBean().dayCount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public ISDACompliantDateCurve clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ISDACompliantDateCurve other = (ISDACompliantDateCurve) obj;
      return JodaBeanUtils.equal(getBaseDate(), other.getBaseDate()) &&
          JodaBeanUtils.equal(getDates(), other.getDates()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getBaseDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDates());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("ISDACompliantDateCurve{");
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
    buf.append("baseDate").append('=').append(JodaBeanUtils.toString(getBaseDate())).append(',').append(' ');
    buf.append("dates").append('=').append(JodaBeanUtils.toString(getDates())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ISDACompliantDateCurve}.
   */
  public static class Meta extends ISDACompliantCurve.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code baseDate} property.
     */
    private final MetaProperty<LocalDate> _baseDate = DirectMetaProperty.ofReadWrite(
        this, "baseDate", ISDACompliantDateCurve.class, LocalDate.class);
    /**
     * The meta-property for the {@code dates} property.
     */
    private final MetaProperty<LocalDate[]> _dates = DirectMetaProperty.ofReadWrite(
        this, "dates", ISDACompliantDateCurve.class, LocalDate[].class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadWrite(
        this, "dayCount", ISDACompliantDateCurve.class, DayCount.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "baseDate",
        "dates",
        "dayCount");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1721984481:  // baseDate
          return _baseDate;
        case 95356549:  // dates
          return _dates;
        case 1905311443:  // dayCount
          return _dayCount;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ISDACompliantDateCurve> builder() {
      return new DirectBeanBuilder<ISDACompliantDateCurve>(new ISDACompliantDateCurve());
    }

    @Override
    public Class<? extends ISDACompliantDateCurve> beanType() {
      return ISDACompliantDateCurve.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code baseDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> baseDate() {
      return _baseDate;
    }

    /**
     * The meta-property for the {@code dates} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate[]> dates() {
      return _dates;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCount() {
      return _dayCount;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1721984481:  // baseDate
          return ((ISDACompliantDateCurve) bean).getBaseDate();
        case 95356549:  // dates
          return ((ISDACompliantDateCurve) bean).getDates();
        case 1905311443:  // dayCount
          return ((ISDACompliantDateCurve) bean).getDayCount();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1721984481:  // baseDate
          ((ISDACompliantDateCurve) bean).setBaseDate((LocalDate) newValue);
          return;
        case 95356549:  // dates
          ((ISDACompliantDateCurve) bean).setDates((LocalDate[]) newValue);
          return;
        case 1905311443:  // dayCount
          ((ISDACompliantDateCurve) bean).setDayCount((DayCount) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
