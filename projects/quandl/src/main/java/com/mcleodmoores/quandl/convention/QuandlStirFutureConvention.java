/**
 * Copyright (C) 2014-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.quandl.convention;

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

import com.mcleodmoores.quandl.util.ArgumentChecker;
import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.money.Currency;
import com.opengamma.util.time.Tenor;

/**
 * Convention for short-term interest rate futures that contains the necessary meta-data to construct a
 * {@link com.opengamma.financial.security.future.InterestRateFutureSecurity} from Quandl data.
 * <p>
 * This convention contains information about the future maturity in the form of the nth day of the month
 * (e.g. the last trading date of an IMM future is the third Monday of a month).
 */
//TODO think about where currency, tenor and expiry information should go
@BeanDefinition
public class QuandlStirFutureConvention extends QuandlFutureConvention {

  /**
   * The type of the convention.
   */
  public static final ConventionType TYPE = ConventionType.of("QuandlStirFuture");

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The currency.
   */
  @PropertyDefinition(validate = "notNull")
  private Currency _currency;

  /**
   * The tenor of the future.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor _futureTenor;

  /**
   * The tenor of the underlying index.
   */
  @PropertyDefinition(validate = "notNull")
  private Tenor _underlyingTenor;

  /**
   * The nth day of the month of the future maturity.
   */
  @PropertyDefinition
  private int _nthDay;

  /**
   * The day of the week of the future maturity.
   */
  @PropertyDefinition(validate = "notNull")
  private String _dayOfWeek;

  /**
   * For the builder.
   */
  protected QuandlStirFutureConvention() {
    super();
  }

  /**
   * Creates an instance with the settlement exchange and trading exchange name created by parsing
   * the Quandl code.
   * @param name The name of the convention, not null
   * @param externalIdBundle The external ids associated with this convention, not null
   * @param currency The currency, not null
   * @param futureTenor The tenor of the future, not null
   * @param underlyingTenor The tenor of the underlying, not null
   * @param lastTradeTime The last trade time on the maturity date, not null
   * @param zoneOffsetId The time zone of the exchange where the future is traded, not null
   * @param unitAmount The unit amount of the future, not null
   * @param underlyingConventionId The id of the underlying index convention, not null
   * @param nthDay The nth day of the month of the future maturity
   * @param dayOfWeek The day of the week of the future maturity
   */
  public QuandlStirFutureConvention(final String name, final ExternalIdBundle externalIdBundle, final Currency currency,
      final Tenor futureTenor, final Tenor underlyingTenor, final String lastTradeTime, final String zoneOffsetId, final double unitAmount,
      final ExternalId underlyingConventionId, final int nthDay, final String dayOfWeek) {
    super(name, externalIdBundle, lastTradeTime, zoneOffsetId, unitAmount, underlyingConventionId);
    setCurrency(currency);
    setFutureTenor(futureTenor);
    setUnderlyingTenor(underlyingTenor);
    setNthDay(nthDay);
    setDayOfWeek(dayOfWeek);
  }

  /**
   * Creates an instance with the settlement exchange and trading exchange name set to null.
   * @param name The name of the convention, not null
   * @param externalIdBundle The external ids associated with this convention, not null
   * @param currency The currency, not null
   * @param futureTenor The tenor of the future, not null
   * @param underlyingTenor The tenor of the underlying, not null
   * @param lastTradeTime The last trade time on the maturity date, not null
   * @param zoneOffsetId The time zone of the exchange where the future is traded, not null
   * @param unitAmount The unit amount of the future, not null
   * @param underlyingConventionId The id of the underlying index convention, not null
   * @param nthDay The nth day of the month of the future maturity
   * @param dayOfWeek The day of the week of the future maturity
   * @param tradingExchange The name of the trading exchange, can be null
   * @param settlementExchange The name of the settlement exchange, can be null
   */
  public QuandlStirFutureConvention(final String name, final ExternalIdBundle externalIdBundle, final Currency currency,
      final Tenor futureTenor, final Tenor underlyingTenor, final String lastTradeTime, final String zoneOffsetId, final double unitAmount,
      final ExternalId underlyingConventionId, final int nthDay, final String dayOfWeek, final String tradingExchange,
      final String settlementExchange) {
    super(name, externalIdBundle, lastTradeTime, zoneOffsetId, unitAmount, underlyingConventionId, tradingExchange, settlementExchange);
    setCurrency(currency);
    setFutureTenor(futureTenor);
    setUnderlyingTenor(underlyingTenor);
    setNthDay(nthDay);
    setDayOfWeek(dayOfWeek);
  }

  @Override
  public ConventionType getConventionType() {
    return TYPE;
  }

  @Override
  public <T> T accept(final QuandlFinancialConventionVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitQuandlStirFutureConvention(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code QuandlStirFutureConvention}.
   * @return the meta-bean, not null
   */
  public static QuandlStirFutureConvention.Meta meta() {
    return QuandlStirFutureConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(QuandlStirFutureConvention.Meta.INSTANCE);
  }

  @Override
  public QuandlStirFutureConvention.Meta metaBean() {
    return QuandlStirFutureConvention.Meta.INSTANCE;
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
   * Gets the tenor of the future.
   * @return the value of the property, not null
   */
  public Tenor getFutureTenor() {
    return _futureTenor;
  }

  /**
   * Sets the tenor of the future.
   * @param futureTenor  the new value of the property, not null
   */
  public void setFutureTenor(Tenor futureTenor) {
    JodaBeanUtils.notNull(futureTenor, "futureTenor");
    this._futureTenor = futureTenor;
  }

  /**
   * Gets the the {@code futureTenor} property.
   * @return the property, not null
   */
  public final Property<Tenor> futureTenor() {
    return metaBean().futureTenor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the tenor of the underlying index.
   * @return the value of the property, not null
   */
  public Tenor getUnderlyingTenor() {
    return _underlyingTenor;
  }

  /**
   * Sets the tenor of the underlying index.
   * @param underlyingTenor  the new value of the property, not null
   */
  public void setUnderlyingTenor(Tenor underlyingTenor) {
    JodaBeanUtils.notNull(underlyingTenor, "underlyingTenor");
    this._underlyingTenor = underlyingTenor;
  }

  /**
   * Gets the the {@code underlyingTenor} property.
   * @return the property, not null
   */
  public final Property<Tenor> underlyingTenor() {
    return metaBean().underlyingTenor().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the nth day of the month of the future maturity.
   * @return the value of the property
   */
  public int getNthDay() {
    return _nthDay;
  }

  /**
   * Sets the nth day of the month of the future maturity.
   * @param nthDay  the new value of the property
   */
  public void setNthDay(int nthDay) {
    this._nthDay = nthDay;
  }

  /**
   * Gets the the {@code nthDay} property.
   * @return the property, not null
   */
  public final Property<Integer> nthDay() {
    return metaBean().nthDay().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the day of the week of the future maturity.
   * @return the value of the property, not null
   */
  public String getDayOfWeek() {
    return _dayOfWeek;
  }

  /**
   * Sets the day of the week of the future maturity.
   * @param dayOfWeek  the new value of the property, not null
   */
  public void setDayOfWeek(String dayOfWeek) {
    JodaBeanUtils.notNull(dayOfWeek, "dayOfWeek");
    this._dayOfWeek = dayOfWeek;
  }

  /**
   * Gets the the {@code dayOfWeek} property.
   * @return the property, not null
   */
  public final Property<String> dayOfWeek() {
    return metaBean().dayOfWeek().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public QuandlStirFutureConvention clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      QuandlStirFutureConvention other = (QuandlStirFutureConvention) obj;
      return JodaBeanUtils.equal(getCurrency(), other.getCurrency()) &&
          JodaBeanUtils.equal(getFutureTenor(), other.getFutureTenor()) &&
          JodaBeanUtils.equal(getUnderlyingTenor(), other.getUnderlyingTenor()) &&
          (getNthDay() == other.getNthDay()) &&
          JodaBeanUtils.equal(getDayOfWeek(), other.getDayOfWeek()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getCurrency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFutureTenor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingTenor());
    hash = hash * 31 + JodaBeanUtils.hashCode(getNthDay());
    hash = hash * 31 + JodaBeanUtils.hashCode(getDayOfWeek());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(192);
    buf.append("QuandlStirFutureConvention{");
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
    buf.append("futureTenor").append('=').append(JodaBeanUtils.toString(getFutureTenor())).append(',').append(' ');
    buf.append("underlyingTenor").append('=').append(JodaBeanUtils.toString(getUnderlyingTenor())).append(',').append(' ');
    buf.append("nthDay").append('=').append(JodaBeanUtils.toString(getNthDay())).append(',').append(' ');
    buf.append("dayOfWeek").append('=').append(JodaBeanUtils.toString(getDayOfWeek())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code QuandlStirFutureConvention}.
   */
  public static class Meta extends QuandlFutureConvention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code currency} property.
     */
    private final MetaProperty<Currency> _currency = DirectMetaProperty.ofReadWrite(
        this, "currency", QuandlStirFutureConvention.class, Currency.class);
    /**
     * The meta-property for the {@code futureTenor} property.
     */
    private final MetaProperty<Tenor> _futureTenor = DirectMetaProperty.ofReadWrite(
        this, "futureTenor", QuandlStirFutureConvention.class, Tenor.class);
    /**
     * The meta-property for the {@code underlyingTenor} property.
     */
    private final MetaProperty<Tenor> _underlyingTenor = DirectMetaProperty.ofReadWrite(
        this, "underlyingTenor", QuandlStirFutureConvention.class, Tenor.class);
    /**
     * The meta-property for the {@code nthDay} property.
     */
    private final MetaProperty<Integer> _nthDay = DirectMetaProperty.ofReadWrite(
        this, "nthDay", QuandlStirFutureConvention.class, Integer.TYPE);
    /**
     * The meta-property for the {@code dayOfWeek} property.
     */
    private final MetaProperty<String> _dayOfWeek = DirectMetaProperty.ofReadWrite(
        this, "dayOfWeek", QuandlStirFutureConvention.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "currency",
        "futureTenor",
        "underlyingTenor",
        "nthDay",
        "dayOfWeek");

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
        case -515187011:  // futureTenor
          return _futureTenor;
        case -824175261:  // underlyingTenor
          return _underlyingTenor;
        case -1035465510:  // nthDay
          return _nthDay;
        case -730552025:  // dayOfWeek
          return _dayOfWeek;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends QuandlStirFutureConvention> builder() {
      return new DirectBeanBuilder<QuandlStirFutureConvention>(new QuandlStirFutureConvention());
    }

    @Override
    public Class<? extends QuandlStirFutureConvention> beanType() {
      return QuandlStirFutureConvention.class;
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
     * The meta-property for the {@code futureTenor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Tenor> futureTenor() {
      return _futureTenor;
    }

    /**
     * The meta-property for the {@code underlyingTenor} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Tenor> underlyingTenor() {
      return _underlyingTenor;
    }

    /**
     * The meta-property for the {@code nthDay} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> nthDay() {
      return _nthDay;
    }

    /**
     * The meta-property for the {@code dayOfWeek} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> dayOfWeek() {
      return _dayOfWeek;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          return ((QuandlStirFutureConvention) bean).getCurrency();
        case -515187011:  // futureTenor
          return ((QuandlStirFutureConvention) bean).getFutureTenor();
        case -824175261:  // underlyingTenor
          return ((QuandlStirFutureConvention) bean).getUnderlyingTenor();
        case -1035465510:  // nthDay
          return ((QuandlStirFutureConvention) bean).getNthDay();
        case -730552025:  // dayOfWeek
          return ((QuandlStirFutureConvention) bean).getDayOfWeek();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 575402001:  // currency
          ((QuandlStirFutureConvention) bean).setCurrency((Currency) newValue);
          return;
        case -515187011:  // futureTenor
          ((QuandlStirFutureConvention) bean).setFutureTenor((Tenor) newValue);
          return;
        case -824175261:  // underlyingTenor
          ((QuandlStirFutureConvention) bean).setUnderlyingTenor((Tenor) newValue);
          return;
        case -1035465510:  // nthDay
          ((QuandlStirFutureConvention) bean).setNthDay((Integer) newValue);
          return;
        case -730552025:  // dayOfWeek
          ((QuandlStirFutureConvention) bean).setDayOfWeek((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((QuandlStirFutureConvention) bean)._currency, "currency");
      JodaBeanUtils.notNull(((QuandlStirFutureConvention) bean)._futureTenor, "futureTenor");
      JodaBeanUtils.notNull(((QuandlStirFutureConvention) bean)._underlyingTenor, "underlyingTenor");
      JodaBeanUtils.notNull(((QuandlStirFutureConvention) bean)._dayOfWeek, "dayOfWeek");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
