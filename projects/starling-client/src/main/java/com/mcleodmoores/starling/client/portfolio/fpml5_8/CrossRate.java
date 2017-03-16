/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio.fpml5_8;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * An object containing information about an FX cross rate. This object contains a {@link QuotedCurrencyPair}, which defines the currency
 * pair and the quote basis, and the FX rate.
 * <p>
 * This class also contains a method that creates an {@link ExchangeRate} from this cross rate and another.
 */
@BeanDefinition
public class CrossRate implements ImmutableBean {

  /**
   * Defines the currency pair, including information about the quote basis.
   */
  @PropertyDefinition(validate = "notNull")
  private final QuotedCurrencyPair _quotedCurrencyPair;

  //TODO 4dp (or 2 for JPY)
  /**
   * The FX exchange rate expressed in the units of the quote basis.
   */
  @PropertyDefinition(validate = "notNull")
  private final BigDecimal _rate;

  /**
   * This method uses two FX crosses to produce another cross. The rules are:
   * <ul>
   *   <li> If the provided cross rate has the same currencies e.g. USD/GBP and GBP/USD, or USD/GBP and USD/GBP, a copy of this cross rate is returned.
   *        If the rates are not equivalent to within 7 decimal places an exception is thrown.
   *   <li> This cross rate is multiplied or divided by the provided rate e.g. GBP/EUR = GBP/USD * USD/EUR or GBP/EUR = GBP/USD / EUR/USD. The quote
   *        basis of the result is always {@link QuoteBasis#CURRENCY1_PER_CURRENCY2}.
   *   <li> If the cross rates are incompatible, e.g. GBP/USD and EUR/CHF, an exception is thrown.
   * </ul>
   * @param otherRate  the other cross rate, not null
   * @return  the exchange rate
   */
  public ExchangeRate toExchangeRate(final CrossRate otherRate) {
    ArgumentChecker.notNull(otherRate, "otherRate");
    final Currency crossRateNumerator1, crossRateDenominator1;
    final double rate1, rate2;
    final Currency currency1 = _quotedCurrencyPair.getCurrency1();
    final Currency currency2 = _quotedCurrencyPair.getCurrency2();
    final QuoteBasis quoteBasis = _quotedCurrencyPair.getQuoteBasis();
    final Currency otherCurrency1 = otherRate.getQuotedCurrencyPair().getCurrency1();
    final Currency otherCurrency2 = otherRate.getQuotedCurrencyPair().getCurrency2();
    final QuoteBasis otherQuoteBasis = otherRate.getQuotedCurrencyPair().getQuoteBasis();
    switch (quoteBasis) {
      case CURRENCY2_PER_CURRENCY1:
        crossRateNumerator1 = currency1;
        crossRateDenominator1 = currency2;
        rate1 = _rate.doubleValue();
        break;
      case CURRENCY1_PER_CURRENCY2:
        crossRateDenominator1 = currency2;
        crossRateNumerator1 = currency1;
        rate1 = 1. / _rate.doubleValue();
        break;
      default:
        throw new IllegalStateException("Unrecognized quote basis " + quoteBasis);
    }
    final Currency crossRateNumerator2, crossRateDenominator2;
    switch (otherQuoteBasis) {
      case CURRENCY2_PER_CURRENCY1:
        crossRateNumerator2 = otherCurrency1;
        crossRateDenominator2 = otherCurrency2;
        rate2 = otherRate._rate.doubleValue();
        break;
      case CURRENCY1_PER_CURRENCY2:
        crossRateDenominator2 = otherCurrency2;
        crossRateNumerator2 = otherCurrency1;
        rate2 = 1. / otherRate._rate.doubleValue();
        break;
      default:
        throw new IllegalStateException("Unrecognized quote basis " + quoteBasis);
    }
    if (crossRateNumerator1.equals(crossRateNumerator2) && crossRateDenominator1.equals(crossRateDenominator2)) {
      // same cross rates
      final BigDecimal rate7dp = BigDecimal.valueOf(_rate.doubleValue()).setScale(7);
      final BigDecimal otherRate7dp = BigDecimal.valueOf(otherRate.getRate().doubleValue()).setScale(7, RoundingMode.HALF_DOWN);
      if (rate7dp.compareTo(otherRate7dp) != 0) {
        throw new IllegalStateException("Incompatible rates for " + crossRateNumerator1 + "/" + crossRateDenominator1
            + ": have " + rate7dp + " and " + otherRate7dp);
      }
      return ExchangeRate.builder()
          .quotedCurrencyPair(_quotedCurrencyPair)
          .rate(_rate)
          .build();
    } else if (crossRateNumerator1.equals(crossRateDenominator2) && crossRateDenominator1.equals(crossRateNumerator2)) {
      // inverted cross rates
      final BigDecimal rate7dp = BigDecimal.valueOf(_rate.doubleValue()).setScale(7);
      final BigDecimal otherRate7dp = BigDecimal.valueOf(1. / otherRate.getRate().doubleValue()).setScale(7, RoundingMode.HALF_DOWN);
      if (rate7dp.compareTo(otherRate7dp) != 0) {
        throw new IllegalStateException("Incompatible rates for " + crossRateNumerator1 + "/" + crossRateDenominator1
            + ": have " + rate7dp + " and " + otherRate7dp);
      }
      return ExchangeRate.builder()
          .quotedCurrencyPair(_quotedCurrencyPair)
          .rate(_rate)
          .build();
    }
    final Currency numerator, denominator;
    final double cross;
    if (crossRateNumerator1.equals(crossRateDenominator2)) {
      numerator = crossRateNumerator2;
      denominator = crossRateDenominator1;
      cross = rate1 * rate2;
    } else if (crossRateNumerator2.equals(crossRateDenominator1)) {
      numerator = crossRateNumerator1;
      denominator = crossRateDenominator2;
      cross = rate1 * rate2;
    } else if (crossRateNumerator1.equals(crossRateNumerator2)) {
      numerator = crossRateDenominator2;
      denominator = crossRateDenominator1;
      cross = rate1 / rate2;
    } else if (crossRateDenominator1.equals(crossRateDenominator2)) {
      numerator = crossRateNumerator1;
      denominator = crossRateNumerator2;
      cross = rate1 / rate2;
    } else {
      throw new IllegalStateException("Unmatched currencies: have " + crossRateNumerator1 + "/" + crossRateDenominator1
          + " and " + crossRateNumerator2 + "/" + crossRateDenominator2);
    }
    final QuotedCurrencyPair newCrossQuotedPair = QuotedCurrencyPair.builder()
        .currency1(numerator)
        .currency2(denominator)
        .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
        .build();
    return ExchangeRate.builder()
        .quotedCurrencyPair(newCrossQuotedPair)
        .rate(BigDecimal.valueOf(cross))
        .build();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code CrossRate}.
   * @return the meta-bean, not null
   */
  public static CrossRate.Meta meta() {
    return CrossRate.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(CrossRate.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static CrossRate.Builder builder() {
    return new CrossRate.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected CrossRate(final CrossRate.Builder builder) {
    JodaBeanUtils.notNull(builder._quotedCurrencyPair, "quotedCurrencyPair");
    JodaBeanUtils.notNull(builder._rate, "rate");
    this._quotedCurrencyPair = builder._quotedCurrencyPair;
    this._rate = builder._rate;
  }

  @Override
  public CrossRate.Meta metaBean() {
    return CrossRate.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(final String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets defines the currency pair, including information about the quote basis.
   * @return the value of the property, not null
   */
  public QuotedCurrencyPair getQuotedCurrencyPair() {
    return _quotedCurrencyPair;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the FX exchange rate expressed in the units of the quote basis.
   * @return the value of the property, not null
   */
  public BigDecimal getRate() {
    return _rate;
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
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      final CrossRate other = (CrossRate) obj;
      return JodaBeanUtils.equal(getQuotedCurrencyPair(), other.getQuotedCurrencyPair()) &&
          JodaBeanUtils.equal(getRate(), other.getRate());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getQuotedCurrencyPair());
    hash = hash * 31 + JodaBeanUtils.hashCode(getRate());
    return hash;
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder(96);
    buf.append("CrossRate{");
    final int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(final StringBuilder buf) {
    buf.append("quotedCurrencyPair").append('=').append(JodaBeanUtils.toString(getQuotedCurrencyPair())).append(',').append(' ');
    buf.append("rate").append('=').append(JodaBeanUtils.toString(getRate())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code CrossRate}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code quotedCurrencyPair} property.
     */
    private final MetaProperty<QuotedCurrencyPair> _quotedCurrencyPair = DirectMetaProperty.ofImmutable(
        this, "quotedCurrencyPair", CrossRate.class, QuotedCurrencyPair.class);
    /**
     * The meta-property for the {@code rate} property.
     */
    private final MetaProperty<BigDecimal> _rate = DirectMetaProperty.ofImmutable(
        this, "rate", CrossRate.class, BigDecimal.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "quotedCurrencyPair",
        "rate");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(final String propertyName) {
      switch (propertyName.hashCode()) {
        case -1256824813:  // quotedCurrencyPair
          return _quotedCurrencyPair;
        case 3493088:  // rate
          return _rate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public CrossRate.Builder builder() {
      return new CrossRate.Builder();
    }

    @Override
    public Class<? extends CrossRate> beanType() {
      return CrossRate.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code quotedCurrencyPair} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<QuotedCurrencyPair> quotedCurrencyPair() {
      return _quotedCurrencyPair;
    }

    /**
     * The meta-property for the {@code rate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> rate() {
      return _rate;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(final Bean bean, final String propertyName, final boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1256824813:  // quotedCurrencyPair
          return ((CrossRate) bean).getQuotedCurrencyPair();
        case 3493088:  // rate
          return ((CrossRate) bean).getRate();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(final Bean bean, final String propertyName, final Object newValue, final boolean quiet) {
      metaProperty(propertyName);
      if (quiet) {
        return;
      }
      throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
    }

  }

  //-----------------------------------------------------------------------
  /**
   * The bean-builder for {@code CrossRate}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<CrossRate> {

    private QuotedCurrencyPair _quotedCurrencyPair;
    private BigDecimal _rate;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(final CrossRate beanToCopy) {
      this._quotedCurrencyPair = beanToCopy.getQuotedCurrencyPair();
      this._rate = beanToCopy.getRate();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(final String propertyName) {
      switch (propertyName.hashCode()) {
        case -1256824813:  // quotedCurrencyPair
          return _quotedCurrencyPair;
        case 3493088:  // rate
          return _rate;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(final String propertyName, final Object newValue) {
      switch (propertyName.hashCode()) {
        case -1256824813:  // quotedCurrencyPair
          this._quotedCurrencyPair = (QuotedCurrencyPair) newValue;
          break;
        case 3493088:  // rate
          this._rate = (BigDecimal) newValue;
          break;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
      return this;
    }

    @Override
    public Builder set(final MetaProperty<?> property, final Object value) {
      super.set(property, value);
      return this;
    }

    @Override
    public Builder setString(final String propertyName, final String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(final MetaProperty<?> property, final String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(final Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public CrossRate build() {
      return new CrossRate(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets defines the currency pair, including information about the quote basis.
     * @param quotedCurrencyPair  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder quotedCurrencyPair(final QuotedCurrencyPair quotedCurrencyPair) {
      JodaBeanUtils.notNull(quotedCurrencyPair, "quotedCurrencyPair");
      this._quotedCurrencyPair = quotedCurrencyPair;
      return this;
    }

    /**
     * Sets the FX exchange rate expressed in the units of the quote basis.
     * @param rate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder rate(final BigDecimal rate) {
      JodaBeanUtils.notNull(rate, "rate");
      this._rate = rate;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      final StringBuilder buf = new StringBuilder(96);
      buf.append("CrossRate.Builder{");
      final int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(final StringBuilder buf) {
      buf.append("quotedCurrencyPair").append('=').append(JodaBeanUtils.toString(_quotedCurrencyPair)).append(',').append(' ');
      buf.append("rate").append('=').append(JodaBeanUtils.toString(_rate)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
