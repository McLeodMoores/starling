/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio.fpml5_8;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableValidator;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;

import com.opengamma.util.money.Currency;

/**
 * An object representing a FX leg defined by the value date and either one payment and an exchange rate, or two payments.
 * In the first case, the second payment is constructed using the exchange rate and first payment with the counterparties
 * reversed.
 * <p>
 * For this object to be valid, the following conditions must apply:
 * <ul>
 *  <li> If set, the counterparties of the two payments must match i.e. the payer of the first exchange must be the
 *  receiver of the second exchange and vice versa.
 *  <li> If the exchange rate is set, the currencies of the payments must match those of the exchange rate.
 *  <li> If both payments and the exchange rate are set, the exchange rate implied by the payments must match the supplied rate
 *  to within 6 decimal places.
 * </ul>
 */
//TODO if value date is not set, use trade date + conventions to work out value date?
@BeanDefinition
public class FxSingleLeg implements ImmutableBean {

  /**
   * The first payment.
   */
  @PropertyDefinition(validate = "notNull")
  private final ExchangedCurrency _exchangedCurrency1;

  /**
   * The second payment. If not set, this value is implied from the first payment and the exchange rate.
   */
  @PropertyDefinition(get = "manual")
  private final ExchangedCurrency _exchangedCurrency2;

  /**
   * The value date of the trade.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate _valueDate;

  /**
   * The exchange rate of the trade.
   */
  @PropertyDefinition(get = "manual")
  private final ExchangeRate _exchangeRate;

  /**
   * The effective exchange rate of the trade. This might be calculated from the two payments.
   */
  private ExchangeRate _effectiveExchangeRate;

  /**
   * The effective second payment. This might be calculated from the first payment and exchange rate.
   */
  private ExchangedCurrency _effectiveExchangedCurrency2;

  /**
   * Validates this leg.
   */
  @ImmutableValidator
  private void validate() {
    final BigDecimal paymentAmount1 = _exchangedCurrency1.getPaymentAmount().getAmount();
    if (_exchangedCurrency2 != null) {
      _effectiveExchangedCurrency2 = _exchangedCurrency2;
      // check that the payer and receiver references on both legs match
      // using Objects.equals to handle null counterparties
      if (!Objects.equals(_exchangedCurrency1.getPayerPartyReference(), _exchangedCurrency2.getReceiverPartyReference())) {
        throw new IllegalStateException("Payer reference 1 " + _exchangedCurrency1.getPayerPartyReference()
          + " does not match receiver reference 2 " + _exchangedCurrency2.getReceiverPartyReference());
      }
      if (!Objects.equals(_exchangedCurrency1.getReceiverPartyReference(), _exchangedCurrency2.getPayerPartyReference())) {
        throw new IllegalStateException("Payer reference 2 " + _exchangedCurrency2.getPayerPartyReference()
          + " does not match payer reference 1 " + _exchangedCurrency1.getReceiverPartyReference());
      }
      if (_exchangeRate != null) {
        // check for consistent currencies and exchange rates
        final BigDecimal paymentAmount2 = _exchangedCurrency2.getPaymentAmount().getAmount();
        // check that the currencies are consistent
        final Currency paymentCurrency1 = _exchangedCurrency1.getPaymentAmount().getCurrency();
        final Currency paymentCurrency2 = _exchangedCurrency2.getPaymentAmount().getCurrency();
        final Currency exchangeRateCurrency1 = _exchangeRate.getQuotedCurrencyPair().getCurrency1();
        final Currency exchangeRateCurrency2 = _exchangeRate.getQuotedCurrencyPair().getCurrency2();
        if (!(paymentCurrency1.equals(exchangeRateCurrency1) && paymentCurrency2.equals(exchangeRateCurrency2)
            || paymentCurrency1.equals(exchangeRateCurrency2) && paymentCurrency2.equals(exchangeRateCurrency1))) {
          throw new IllegalStateException("Inconsistent exchanged currencies and exchange rate: have (" + paymentCurrency1
              + ", " + paymentCurrency2 + ") and (" + exchangeRateCurrency1 + ", " + exchangeRateCurrency2 + ")");
        }
        final BigDecimal impliedRate6dp, rate6dp;
        // get the exchange rate in CCY1/CCY2 form and round
        switch (_exchangeRate.getQuotedCurrencyPair().getQuoteBasis()) {
          case CURRENCY2_PER_CURRENCY1:
            rate6dp = _exchangeRate.getRate().setScale(6, RoundingMode.HALF_DOWN);
            break;
          case CURRENCY1_PER_CURRENCY2:
            rate6dp = BigDecimal.valueOf(1 / _exchangeRate.getRate().doubleValue()).setScale(6, RoundingMode.HALF_DOWN);
            break;
          default:
            throw new IllegalStateException("Unrecognized quote basis " + _exchangeRate.getQuotedCurrencyPair().getQuoteBasis());
        }
        if (paymentCurrency1.equals(exchangeRateCurrency1)) {
          impliedRate6dp = BigDecimal.valueOf(paymentAmount2.doubleValue() / paymentAmount1.doubleValue()).setScale(6, RoundingMode.HALF_UP);
        } else {
          impliedRate6dp = BigDecimal.valueOf(paymentAmount1.doubleValue() / paymentAmount2.doubleValue()).setScale(6, RoundingMode.HALF_UP);
        }
        if (rate6dp.compareTo(impliedRate6dp) != 0) {
          throw new IllegalStateException("Implied rate " + impliedRate6dp + " does not match provided rate " + rate6dp
            + " for " + exchangeRateCurrency1 + "/" + exchangeRateCurrency2);
        }
        _effectiveExchangeRate = _exchangeRate;
      } else {
        // construct the exchange rate
        final BigDecimal paymentAmount2 = _exchangedCurrency2.getPaymentAmount().getAmount();
        final BigDecimal impliedRate = BigDecimal.valueOf(paymentAmount2.doubleValue()
            / paymentAmount1.doubleValue());
        final QuotedCurrencyPair impliedQuotedCurrencyPair = QuotedCurrencyPair.builder()
            .currency1(_exchangedCurrency1.getPaymentAmount().getCurrency())
            .currency2(_exchangedCurrency2.getPaymentAmount().getCurrency())
            .quoteBasis(QuoteBasis.CURRENCY2_PER_CURRENCY1)
            .build();
        _effectiveExchangeRate = ExchangeRate.builder()
            .rate(impliedRate)
            .quotedCurrencyPair(impliedQuotedCurrencyPair)
            .build();
      }
    } else if (_exchangeRate != null) {
      _effectiveExchangeRate = _exchangeRate;
      // construct the second payment
      final Currency exchangeRateCurrency1 = _exchangeRate.getQuotedCurrencyPair().getCurrency1();
      final Currency exchangeRateCurrency2 = _exchangeRate.getQuotedCurrencyPair().getCurrency2();
      final Currency paymentCurrency = _exchangedCurrency1.getPaymentAmount().getCurrency();
      final PaymentAmount.Builder paymentBuilder = PaymentAmount.builder();
      final double exchangeRate;
      // get rate as currency2 / currency1
      switch (_exchangeRate.getQuotedCurrencyPair().getQuoteBasis()) {
        case CURRENCY1_PER_CURRENCY2:
          exchangeRate = 1 / _exchangeRate.getRate().doubleValue();
          break;
        case CURRENCY2_PER_CURRENCY1:
          exchangeRate = _exchangeRate.getRate().doubleValue();
          break;
        default:
          throw new IllegalStateException("Unrecognized quote basis " + _exchangeRate.getQuotedCurrencyPair().getQuoteBasis());
      }
      final ExchangedCurrency.Builder exchangedCurrencyBuilder = ExchangedCurrency.builder()
          .payerPartyReference(_exchangedCurrency1.getReceiverPartyReference())
          .receiverPartyReference(_exchangedCurrency1.getPayerPartyReference());
      if (paymentCurrency.equals(exchangeRateCurrency1)) {
        paymentBuilder.currency(exchangeRateCurrency2)
          .amount(BigDecimal.valueOf(_exchangedCurrency1.getPaymentAmount().getAmount().doubleValue() * exchangeRate));
      } else if (paymentCurrency.equals(exchangeRateCurrency2)) {
        paymentBuilder.currency(exchangeRateCurrency1)
          .amount(BigDecimal.valueOf(_exchangedCurrency1.getPaymentAmount().getAmount().doubleValue() / exchangeRate));
      } else {
        throw new IllegalStateException("Exchange rate currencies (" + exchangeRateCurrency1 + ", "
            + exchangeRateCurrency2 + " not compatible with first payment currency " + paymentCurrency);
      }
      _effectiveExchangedCurrency2 = exchangedCurrencyBuilder.paymentAmount(paymentBuilder.build()).build();
    } else {
      throw new IllegalStateException("Must set either the exchangeRate or exchangedCurrency2 fields");
    }
  }

  /**
   * Gets the second payment. If not set, this value is implied from the first payment and the exchange rate.
   * @return the value of the property
   */
  public ExchangedCurrency getExchangedCurrency2() {
    return _effectiveExchangedCurrency2;
  }

  /**
   * Gets the exchange rate of the trade.
   * @return the value of the property
   */
  public ExchangeRate getExchangeRate() {
    return _effectiveExchangeRate;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FxSingleLeg}.
   * @return the meta-bean, not null
   */
  public static FxSingleLeg.Meta meta() {
    return FxSingleLeg.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FxSingleLeg.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FxSingleLeg.Builder builder() {
    return new FxSingleLeg.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected FxSingleLeg(FxSingleLeg.Builder builder) {
    JodaBeanUtils.notNull(builder._exchangedCurrency1, "exchangedCurrency1");
    JodaBeanUtils.notNull(builder._valueDate, "valueDate");
    this._exchangedCurrency1 = builder._exchangedCurrency1;
    this._exchangedCurrency2 = builder._exchangedCurrency2;
    this._valueDate = builder._valueDate;
    this._exchangeRate = builder._exchangeRate;
    validate();
  }

  @Override
  public FxSingleLeg.Meta metaBean() {
    return FxSingleLeg.Meta.INSTANCE;
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    return metaBean().<R>metaProperty(propertyName).createProperty(this);
  }

  @Override
  public Set<String> propertyNames() {
    return metaBean().metaPropertyMap().keySet();
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the first payment.
   * @return the value of the property, not null
   */
  public ExchangedCurrency getExchangedCurrency1() {
    return _exchangedCurrency1;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the value date of the trade.
   * @return the value of the property, not null
   */
  public LocalDate getValueDate() {
    return _valueDate;
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
      FxSingleLeg other = (FxSingleLeg) obj;
      return JodaBeanUtils.equal(getExchangedCurrency1(), other.getExchangedCurrency1()) &&
          JodaBeanUtils.equal(getExchangedCurrency2(), other.getExchangedCurrency2()) &&
          JodaBeanUtils.equal(getValueDate(), other.getValueDate()) &&
          JodaBeanUtils.equal(getExchangeRate(), other.getExchangeRate());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getExchangedCurrency1());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExchangedCurrency2());
    hash = hash * 31 + JodaBeanUtils.hashCode(getValueDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExchangeRate());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(160);
    buf.append("FxSingleLeg{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("exchangedCurrency1").append('=').append(JodaBeanUtils.toString(getExchangedCurrency1())).append(',').append(' ');
    buf.append("exchangedCurrency2").append('=').append(JodaBeanUtils.toString(getExchangedCurrency2())).append(',').append(' ');
    buf.append("valueDate").append('=').append(JodaBeanUtils.toString(getValueDate())).append(',').append(' ');
    buf.append("exchangeRate").append('=').append(JodaBeanUtils.toString(getExchangeRate())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FxSingleLeg}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code exchangedCurrency1} property.
     */
    private final MetaProperty<ExchangedCurrency> _exchangedCurrency1 = DirectMetaProperty.ofImmutable(
        this, "exchangedCurrency1", FxSingleLeg.class, ExchangedCurrency.class);
    /**
     * The meta-property for the {@code exchangedCurrency2} property.
     */
    private final MetaProperty<ExchangedCurrency> _exchangedCurrency2 = DirectMetaProperty.ofImmutable(
        this, "exchangedCurrency2", FxSingleLeg.class, ExchangedCurrency.class);
    /**
     * The meta-property for the {@code valueDate} property.
     */
    private final MetaProperty<LocalDate> _valueDate = DirectMetaProperty.ofImmutable(
        this, "valueDate", FxSingleLeg.class, LocalDate.class);
    /**
     * The meta-property for the {@code exchangeRate} property.
     */
    private final MetaProperty<ExchangeRate> _exchangeRate = DirectMetaProperty.ofImmutable(
        this, "exchangeRate", FxSingleLeg.class, ExchangeRate.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "exchangedCurrency1",
        "exchangedCurrency2",
        "valueDate",
        "exchangeRate");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 219734175:  // exchangedCurrency1
          return _exchangedCurrency1;
        case 219734176:  // exchangedCurrency2
          return _exchangedCurrency2;
        case -766192449:  // valueDate
          return _valueDate;
        case 1429636515:  // exchangeRate
          return _exchangeRate;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FxSingleLeg.Builder builder() {
      return new FxSingleLeg.Builder();
    }

    @Override
    public Class<? extends FxSingleLeg> beanType() {
      return FxSingleLeg.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code exchangedCurrency1} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExchangedCurrency> exchangedCurrency1() {
      return _exchangedCurrency1;
    }

    /**
     * The meta-property for the {@code exchangedCurrency2} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExchangedCurrency> exchangedCurrency2() {
      return _exchangedCurrency2;
    }

    /**
     * The meta-property for the {@code valueDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> valueDate() {
      return _valueDate;
    }

    /**
     * The meta-property for the {@code exchangeRate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExchangeRate> exchangeRate() {
      return _exchangeRate;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 219734175:  // exchangedCurrency1
          return ((FxSingleLeg) bean).getExchangedCurrency1();
        case 219734176:  // exchangedCurrency2
          return ((FxSingleLeg) bean).getExchangedCurrency2();
        case -766192449:  // valueDate
          return ((FxSingleLeg) bean).getValueDate();
        case 1429636515:  // exchangeRate
          return ((FxSingleLeg) bean).getExchangeRate();
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
   * The bean-builder for {@code FxSingleLeg}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<FxSingleLeg> {

    private ExchangedCurrency _exchangedCurrency1;
    private ExchangedCurrency _exchangedCurrency2;
    private LocalDate _valueDate;
    private ExchangeRate _exchangeRate;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(FxSingleLeg beanToCopy) {
      this._exchangedCurrency1 = beanToCopy.getExchangedCurrency1();
      this._exchangedCurrency2 = beanToCopy.getExchangedCurrency2();
      this._valueDate = beanToCopy.getValueDate();
      this._exchangeRate = beanToCopy.getExchangeRate();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 219734175:  // exchangedCurrency1
          return _exchangedCurrency1;
        case 219734176:  // exchangedCurrency2
          return _exchangedCurrency2;
        case -766192449:  // valueDate
          return _valueDate;
        case 1429636515:  // exchangeRate
          return _exchangeRate;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 219734175:  // exchangedCurrency1
          this._exchangedCurrency1 = (ExchangedCurrency) newValue;
          break;
        case 219734176:  // exchangedCurrency2
          this._exchangedCurrency2 = (ExchangedCurrency) newValue;
          break;
        case -766192449:  // valueDate
          this._valueDate = (LocalDate) newValue;
          break;
        case 1429636515:  // exchangeRate
          this._exchangeRate = (ExchangeRate) newValue;
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
    public Builder setString(String propertyName, String value) {
      setString(meta().metaProperty(propertyName), value);
      return this;
    }

    @Override
    public Builder setString(MetaProperty<?> property, String value) {
      super.setString(property, value);
      return this;
    }

    @Override
    public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
      super.setAll(propertyValueMap);
      return this;
    }

    @Override
    public FxSingleLeg build() {
      return new FxSingleLeg(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the first payment.
     * @param exchangedCurrency1  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder exchangedCurrency1(ExchangedCurrency exchangedCurrency1) {
      JodaBeanUtils.notNull(exchangedCurrency1, "exchangedCurrency1");
      this._exchangedCurrency1 = exchangedCurrency1;
      return this;
    }

    /**
     * Sets the second payment. If not set, this value is implied from the first payment and the exchange rate.
     * @param exchangedCurrency2  the new value
     * @return this, for chaining, not null
     */
    public Builder exchangedCurrency2(ExchangedCurrency exchangedCurrency2) {
      this._exchangedCurrency2 = exchangedCurrency2;
      return this;
    }

    /**
     * Sets the value date of the trade.
     * @param valueDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder valueDate(LocalDate valueDate) {
      JodaBeanUtils.notNull(valueDate, "valueDate");
      this._valueDate = valueDate;
      return this;
    }

    /**
     * Sets the exchange rate of the trade.
     * @param exchangeRate  the new value
     * @return this, for chaining, not null
     */
    public Builder exchangeRate(ExchangeRate exchangeRate) {
      this._exchangeRate = exchangeRate;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(160);
      buf.append("FxSingleLeg.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("exchangedCurrency1").append('=').append(JodaBeanUtils.toString(_exchangedCurrency1)).append(',').append(' ');
      buf.append("exchangedCurrency2").append('=').append(JodaBeanUtils.toString(_exchangedCurrency2)).append(',').append(' ');
      buf.append("valueDate").append('=').append(JodaBeanUtils.toString(_valueDate)).append(',').append(' ');
      buf.append("exchangeRate").append('=').append(JodaBeanUtils.toString(_exchangeRate)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
