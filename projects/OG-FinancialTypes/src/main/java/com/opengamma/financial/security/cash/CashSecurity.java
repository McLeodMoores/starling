/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cash;

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
import org.threeten.bp.ZonedDateTime;

import com.opengamma.financial.convention.daycount.DayCount;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * A security for cash.
 */
@BeanDefinition
public class CashSecurity extends FinancialSecurity {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The security type.
   */
  public static final String SECURITY_TYPE = "CASH";

  /**
   * The currency.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;
  /**
   * The region.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _regionId;
  /**
   * The start date.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _start;
  /**
   * The maturity.
   */
  @PropertyDefinition(validate = "notNull")
  private ZonedDateTime _maturity;
  /**
   * The day count convention
   */
  @PropertyDefinition(validate = "notNull")
  private DayCount _dayCount;
  /**
   * The rate.
   */
  @PropertyDefinition
  private double _rate;
  /**
   * The amount.
   */
  @PropertyDefinition
  private double _amount;

  CashSecurity() { //For builder
    super(SECURITY_TYPE);
  }

  public CashSecurity(Currency currency, ExternalId region, ZonedDateTime start, ZonedDateTime maturity, DayCount dayCount, double rate, double amount) {
    super(SECURITY_TYPE);
    setCurrency(currency);
    setRegionId(region);
    setStart(start);
    setMaturity(maturity);
    setDayCount(dayCount);
    setRate(rate);
    setAmount(amount);
  }

  //-------------------------------------------------------------------------
  @Override
  public final <T> T accept(FinancialSecurityVisitor<T> visitor) {
    return visitor.visitCashSecurity(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CashSecurity}.
   * @return the meta-bean, not null
   */
  public static CashSecurity.Meta meta() {
    return CashSecurity.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CashSecurity.Meta.INSTANCE);
  }

  @Override
  public CashSecurity.Meta metaBean() {
    return CashSecurity.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the currency.
   * @return the value of the property, not null
   */
  public Currency getCurrency() {
    return _currency;
  }

  /**
   * Sets the currency.
   * @param currency  the new value of the property, not null
   */
  public void setCurrency(Currency currency) {
    JodaBeanUtils.notNull(currency, "currency");
    this._currency = currency;
  }

  /**
   * Gets the the {@code currency} property.
   * @return the property, not null
   */
  public final Property<Currency> currency() {
    return metaBean().currency().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the region.
   * @return the value of the property, not null
   */
  public ExternalId getRegionId() {
    return _regionId;
  }

  /**
   * Sets the region.
   * @param regionId  the new value of the property, not null
   */
  public void setRegionId(ExternalId regionId) {
    JodaBeanUtils.notNull(regionId, "regionId");
    this._regionId = regionId;
  }

  /**
   * Gets the the {@code regionId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> regionId() {
    return metaBean().regionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the start date.
   * @return the value of the property, not null
   */
  public ZonedDateTime getStart() {
    return _start;
  }

  /**
   * Sets the start date.
   * @param start  the new value of the property, not null
   */
  public void setStart(ZonedDateTime start) {
    JodaBeanUtils.notNull(start, "start");
    this._start = start;
  }

  /**
   * Gets the the {@code start} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> start() {
    return metaBean().start().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maturity.
   * @return the value of the property, not null
   */
  public ZonedDateTime getMaturity() {
    return _maturity;
  }

  /**
   * Sets the maturity.
   * @param maturity  the new value of the property, not null
   */
  public void setMaturity(ZonedDateTime maturity) {
    JodaBeanUtils.notNull(maturity, "maturity");
    this._maturity = maturity;
  }

  /**
   * Gets the the {@code maturity} property.
   * @return the property, not null
   */
  public final Property<ZonedDateTime> maturity() {
    return metaBean().maturity().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day count convention
   * @return the value of the property, not null
   */
  public DayCount getDayCount() {
    return _dayCount;
  }

  /**
   * Sets the day count convention
   * @param dayCount  the new value of the property, not null
   */
  public void setDayCount(DayCount dayCount) {
    JodaBeanUtils.notNull(dayCount, "dayCount");
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
  /**
   * Gets the rate.
   * @return the value of the property
   */
  public double getRate() {
    return _rate;
  }

  /**
   * Sets the rate.
   * @param rate  the new value of the property
   */
  public void setRate(double rate) {
    this._rate = rate;
  }

  /**
   * Gets the the {@code rate} property.
   * @return the property, not null
   */
  public final Property<Double> rate() {
    return metaBean().rate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the amount.
   * @return the value of the property
   */
  public double getAmount() {
    return _amount;
  }

  /**
   * Sets the amount.
   * @param amount  the new value of the property
   */
  public void setAmount(double amount) {
    this._amount = amount;
  }

  /**
   * Gets the the {@code amount} property.
   * @return the property, not null
   */
  public final Property<Double> amount() {
    return metaBean().amount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public CashSecurity clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      CashSecurity other = (CashSecurity) obj;
      return JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getRegionId(), other.getRegionId()) &&
          JodaBeanUtils.equal(getStart(), other.getStart()) &&
          JodaBeanUtils.equal(getMaturity(), other.getMaturity()) &&
          JodaBeanUtils.equal(getDayCount(), other.getDayCount()) &&
          JodaBeanUtils.equal(getRate(), other.getRate()) &&
          JodaBeanUtils.equal(getAmount(), other.getAmount()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRegionId());
    hash += hash * 31 + JodaBeanUtils.hashCode(getStart());
    hash += hash * 31 + JodaBeanUtils.hashCode(getMaturity());
    hash += hash * 31 + JodaBeanUtils.hashCode(getDayCount());
    hash += hash * 31 + JodaBeanUtils.hashCode(getRate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getAmount());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(256);
    buf.append("CashSecurity{");
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
    buf.append("currency").append('=').append(JodaBeanUtils.toString(getCurrency())).append(',').append(' ');
    buf.append("regionId").append('=').append(JodaBeanUtils.toString(getRegionId())).append(',').append(' ');
    buf.append("start").append('=').append(JodaBeanUtils.toString(getStart())).append(',').append(' ');
    buf.append("maturity").append('=').append(JodaBeanUtils.toString(getMaturity())).append(',').append(' ');
    buf.append("dayCount").append('=').append(JodaBeanUtils.toString(getDayCount())).append(',').append(' ');
    buf.append("rate").append('=').append(JodaBeanUtils.toString(getRate())).append(',').append(' ');
    buf.append("amount").append('=').append(JodaBeanUtils.toString(getAmount())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CashSecurity}.
   */
  public static class Meta extends FinancialSecurity.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", CashSecurity.class, Currency.class);
    /**
     * The meta-property for the {@code regionId} property.
     */
    private final MetaProperty<ExternalId> _regionId = DirectMetaProperty.ofReadWrite(
        this, "regionId", CashSecurity.class, ExternalId.class);
    /**
     * The meta-property for the {@code start} property.
     */
    private final MetaProperty<ZonedDateTime> _start = DirectMetaProperty.ofReadWrite(
        this, "start", CashSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code maturity} property.
     */
    private final MetaProperty<ZonedDateTime> _maturity = DirectMetaProperty.ofReadWrite(
        this, "maturity", CashSecurity.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code dayCount} property.
     */
    private final MetaProperty<DayCount> _dayCount = DirectMetaProperty.ofReadWrite(
        this, "dayCount", CashSecurity.class, DayCount.class);
    /**
     * The meta-property for the {@code rate} property.
     */
    private final MetaProperty<Double> _rate = DirectMetaProperty.ofReadWrite(
        this, "rate", CashSecurity.class, Double.TYPE);
    /**
     * The meta-property for the {@code amount} property.
     */
    private final MetaProperty<Double> _amount = DirectMetaProperty.ofReadWrite(
        this, "amount", CashSecurity.class, Double.TYPE);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "currency",
        "regionId",
        "start",
        "maturity",
        "dayCount",
        "rate",
        "amount");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return _currency;
        case -690339025:  // regionId
          return _regionId;
        case 109757538:  // start
          return _start;
        case 313843601:  // maturity
          return _maturity;
        case 1905311443:  // dayCount
          return _dayCount;
        case 3493088:  // rate
          return _rate;
        case -1413853096:  // amount
          return _amount;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends CashSecurity> builder() {
      return new DirectBeanBuilder<CashSecurity>(new CashSecurity());
    }

    @Override
    public Class<? extends CashSecurity> beanType() {
      return CashSecurity.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code currency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> currency() {
      return _currency;
    }

    /**
     * The meta-property for the {@code regionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> regionId() {
      return _regionId;
    }

    /**
     * The meta-property for the {@code start} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> start() {
      return _start;
    }

    /**
     * The meta-property for the {@code maturity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> maturity() {
      return _maturity;
    }

    /**
     * The meta-property for the {@code dayCount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<DayCount> dayCount() {
      return _dayCount;
    }

    /**
     * The meta-property for the {@code rate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> rate() {
      return _rate;
    }

    /**
     * The meta-property for the {@code amount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> amount() {
      return _amount;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return ((CashSecurity) bean).getCurrency();
        case -690339025:  // regionId
          return ((CashSecurity) bean).getRegionId();
        case 109757538:  // start
          return ((CashSecurity) bean).getStart();
        case 313843601:  // maturity
          return ((CashSecurity) bean).getMaturity();
        case 1905311443:  // dayCount
          return ((CashSecurity) bean).getDayCount();
        case 3493088:  // rate
          return ((CashSecurity) bean).getRate();
        case -1413853096:  // amount
          return ((CashSecurity) bean).getAmount();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          ((CashSecurity) bean).setCurrency((Currency) newValue);
          return;
        case -690339025:  // regionId
          ((CashSecurity) bean).setRegionId((ExternalId) newValue);
          return;
        case 109757538:  // start
          ((CashSecurity) bean).setStart((ZonedDateTime) newValue);
          return;
        case 313843601:  // maturity
          ((CashSecurity) bean).setMaturity((ZonedDateTime) newValue);
          return;
        case 1905311443:  // dayCount
          ((CashSecurity) bean).setDayCount((DayCount) newValue);
          return;
        case 3493088:  // rate
          ((CashSecurity) bean).setRate((Double) newValue);
          return;
        case -1413853096:  // amount
          ((CashSecurity) bean).setAmount((Double) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((CashSecurity) bean)._currency, "currency");
      JodaBeanUtils.notNull(((CashSecurity) bean)._regionId, "regionId");
      JodaBeanUtils.notNull(((CashSecurity) bean)._start, "start");
      JodaBeanUtils.notNull(((CashSecurity) bean)._maturity, "maturity");
      JodaBeanUtils.notNull(((CashSecurity) bean)._dayCount, "dayCount");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
