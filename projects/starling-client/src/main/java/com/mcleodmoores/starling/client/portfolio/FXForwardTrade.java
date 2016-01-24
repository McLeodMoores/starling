/**
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio;

import java.math.BigDecimal;
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
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalTime;
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Simplified representation of an FXForwardTrade that hides some of the less intuitive aspects of the OpenGamma 2.x data model.
 */
@BeanDefinition
public class FXForwardTrade implements ImmutableBean, Trade {
  private static final ExternalId DEFAULT_REGION = ExternalId.of(ExternalSchemes.ISO_COUNTRY_ALPHA2, "GB");
  private static final LocalTime DEFAULT_FORWARD_TIME = LocalTime.MAX;
  //TODO won't this fail when daylight savings starts or ends?
  private static final ZoneId DEFAULT_FORWARD_ZONE = ZoneId.systemDefault();
  private static final OffsetTime DEFAULT_TRADE_TIME = OffsetTime.MAX;
  private static final Counterparty DEFAULT_COUNTERPARTY = new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "DEFAULT"));

  /**
   * The date the trade was booked.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate _tradeDate;

  /**
   * The pay currency, a list of currencies can be established by using CurrencyFactory
   */
  @PropertyDefinition(validate = "notNull")
  private final Currency _payCurrency;

  /**
   * The pay currency, a list of currencies can be established by using CurrencyFactory
   */
  @PropertyDefinition(validate = "notNull")
  private final Currency _receiveCurrency;

  /**
   * The pay amount.
   */
  @PropertyDefinition
  private final double _payAmount;

  /**
   * The receive amount.
   */
  @PropertyDefinition
  private final double _receiveAmount;

  /**
   * The forward date.
   */
  @PropertyDefinition(validate = "notNull")
  private final LocalDate _forwardDate;

  /**
   * The correlation id(s) to associate this trade with the source system.
   * These <b>must</b> be unique to this trade.  There can be more than one.
   * For example if the source is actually two separate trades:
   * <pre>
   * {@code
   *   ExternalIdBundle.of(
   *     ExternalId.of("EXAMPLE_SCHEME", "TRADELEG_ID1"),
   *     ExternalId.of("EXAMPLE_SCHEME", "TRADELEG_ID2"));
   * }
   * </pre>
   * or if just one:
   * <pre>
   * {@code
   *   ExternalIdBundle.of(
   *     ExternalId.of("EXAMPLE_SCHEME", "TRADE_ID1"));
   * }
   * </pre>
   */
  @PropertyDefinition(validate = "notNull")
  private final ExternalId _correlationId;

  /**
   * The counterparty, optional.
   */
  @PropertyDefinition
  private final String _counterparty;

  /**
   * Build a position, trade and linked security that correspond to this trade.
   * @return  the position
   */
  @Override
  public Position toPosition() {
    // get one of the correlation ids, doesn't matter which as the full bundle is in the security.
    final ZonedDateTime forwardDateTime = _forwardDate.atTime(DEFAULT_FORWARD_TIME).atZone(DEFAULT_FORWARD_ZONE);
    final FXForwardSecurity fxForwardSec = new FXForwardSecurity(_payCurrency, _payAmount, _receiveCurrency, _receiveAmount, forwardDateTime, DEFAULT_REGION);
    fxForwardSec.setName(getDescription());
    fxForwardSec.setExternalIdBundle(_correlationId.toBundle());
    final Counterparty cp = _counterparty == null ? DEFAULT_COUNTERPARTY : new SimpleCounterparty(ExternalId.of("Cpty", _counterparty));
    final SimpleTrade trade = new SimpleTrade(fxForwardSec, BigDecimal.ONE, cp, _tradeDate, DEFAULT_TRADE_TIME);
    trade.addAttribute(ManageableTrade.meta().providerId().name(), _correlationId.toString());
    final SimplePosition position = new SimplePosition();
    position.addAttribute(ManageableTrade.meta().providerId().name(), _correlationId.toString());
    position.setSecurityLink(SimpleSecurityLink.of(fxForwardSec));
    position.setQuantity(BigDecimal.ONE);
    position.addTrade(trade);
    return position;
  }

  public static FXForwardTrade from(final Position position) {
    ArgumentChecker.notNull(position, "position");
    if (position.getTrades().size() != 1) {
      throw new RuntimeException("Cannot create single trade from position containing multiple or no trades");
    }
    final com.opengamma.core.position.Trade first = position.getTrades().iterator().next();
    if (!(first.getSecurity() instanceof FXForwardSecurity)) {
      throw new RuntimeException("Can't create FXForwardTrade from trade with non FXForwardSecurity link (or null)");
    }
    final FXForwardSecurity fxForwardSecurity = (FXForwardSecurity) first.getSecurity();
    return FXForwardTrade.builder()
        .receiveCurrency(fxForwardSecurity.getReceiveCurrency())
        .payCurrency(fxForwardSecurity.getPayCurrency())
        .tradeDate(first.getTradeDate())
        .counterparty(first.getCounterparty().getExternalId().getValue())
        .payAmount(fxForwardSecurity.getPayAmount())
        .receiveAmount(fxForwardSecurity.getReceiveAmount())
        .forwardDate(fxForwardSecurity.getForwardDate().toLocalDate())
        .correlationId(fxForwardSecurity.getExternalIdBundle().getExternalIds().first())
        .build();
  }

  protected String getDescription() {
    final StringBuilder sb = new StringBuilder();
    sb.append(_payCurrency);
    sb.append("/");
    sb.append(_receiveCurrency);
    sb.append(" ");
    sb.append(String.format("%.5g", Math.abs(_receiveAmount / _payAmount)));
    sb.append(" ");
    sb.append(_forwardDate);
    return sb.toString();
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FXForwardTrade}.
   * @return the meta-bean, not null
   */
  public static FXForwardTrade.Meta meta() {
    return FXForwardTrade.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FXForwardTrade.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FXForwardTrade.Builder builder() {
    return new FXForwardTrade.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected FXForwardTrade(FXForwardTrade.Builder builder) {
    JodaBeanUtils.notNull(builder._tradeDate, "tradeDate");
    JodaBeanUtils.notNull(builder._payCurrency, "payCurrency");
    JodaBeanUtils.notNull(builder._receiveCurrency, "receiveCurrency");
    JodaBeanUtils.notNull(builder._forwardDate, "forwardDate");
    JodaBeanUtils.notNull(builder._correlationId, "correlationId");
    this._tradeDate = builder._tradeDate;
    this._payCurrency = builder._payCurrency;
    this._receiveCurrency = builder._receiveCurrency;
    this._payAmount = builder._payAmount;
    this._receiveAmount = builder._receiveAmount;
    this._forwardDate = builder._forwardDate;
    this._correlationId = builder._correlationId;
    this._counterparty = builder._counterparty;
  }

  @Override
  public FXForwardTrade.Meta metaBean() {
    return FXForwardTrade.Meta.INSTANCE;
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
   * Gets the date the trade was booked.
   * @return the value of the property, not null
   */
  public LocalDate getTradeDate() {
    return _tradeDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the pay currency, a list of currencies can be established by using CurrencyFactory
   * @return the value of the property, not null
   */
  public Currency getPayCurrency() {
    return _payCurrency;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the pay currency, a list of currencies can be established by using CurrencyFactory
   * @return the value of the property, not null
   */
  public Currency getReceiveCurrency() {
    return _receiveCurrency;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the pay amount.
   * @return the value of the property
   */
  public double getPayAmount() {
    return _payAmount;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the receive amount.
   * @return the value of the property
   */
  public double getReceiveAmount() {
    return _receiveAmount;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the forward date.
   * @return the value of the property, not null
   */
  public LocalDate getForwardDate() {
    return _forwardDate;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the correlation id(s) to associate this trade with the source system.
   * These <b>must</b> be unique to this trade.  There can be more than one.
   * For example if the source is actually two separate trades:
   * <pre>
   * {@code
   * ExternalIdBundle.of(
   * ExternalId.of("EXAMPLE_SCHEME", "TRADELEG_ID1"),
   * ExternalId.of("EXAMPLE_SCHEME", "TRADELEG_ID2"));
   * }
   * </pre>
   * or if just one:
   * <pre>
   * {@code
   * ExternalIdBundle.of(
   * ExternalId.of("EXAMPLE_SCHEME", "TRADE_ID1"));
   * }
   * </pre>
   * @return the value of the property, not null
   */
  public ExternalId getCorrelationId() {
    return _correlationId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the counterparty, optional.
   * @return the value of the property
   */
  public String getCounterparty() {
    return _counterparty;
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
      FXForwardTrade other = (FXForwardTrade) obj;
      return JodaBeanUtils.equal(getTradeDate(), other.getTradeDate()) &&
          JodaBeanUtils.equal(getPayCurrency(), other.getPayCurrency()) &&
          JodaBeanUtils.equal(getReceiveCurrency(), other.getReceiveCurrency()) &&
          JodaBeanUtils.equal(getPayAmount(), other.getPayAmount()) &&
          JodaBeanUtils.equal(getReceiveAmount(), other.getReceiveAmount()) &&
          JodaBeanUtils.equal(getForwardDate(), other.getForwardDate()) &&
          JodaBeanUtils.equal(getCorrelationId(), other.getCorrelationId()) &&
          JodaBeanUtils.equal(getCounterparty(), other.getCounterparty());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPayCurrency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getReceiveCurrency());
    hash = hash * 31 + JodaBeanUtils.hashCode(getPayAmount());
    hash = hash * 31 + JodaBeanUtils.hashCode(getReceiveAmount());
    hash = hash * 31 + JodaBeanUtils.hashCode(getForwardDate());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCorrelationId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCounterparty());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
    buf.append("FXForwardTrade{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("tradeDate").append('=').append(JodaBeanUtils.toString(getTradeDate())).append(',').append(' ');
    buf.append("payCurrency").append('=').append(JodaBeanUtils.toString(getPayCurrency())).append(',').append(' ');
    buf.append("receiveCurrency").append('=').append(JodaBeanUtils.toString(getReceiveCurrency())).append(',').append(' ');
    buf.append("payAmount").append('=').append(JodaBeanUtils.toString(getPayAmount())).append(',').append(' ');
    buf.append("receiveAmount").append('=').append(JodaBeanUtils.toString(getReceiveAmount())).append(',').append(' ');
    buf.append("forwardDate").append('=').append(JodaBeanUtils.toString(getForwardDate())).append(',').append(' ');
    buf.append("correlationId").append('=').append(JodaBeanUtils.toString(getCorrelationId())).append(',').append(' ');
    buf.append("counterparty").append('=').append(JodaBeanUtils.toString(getCounterparty())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FXForwardTrade}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code tradeDate} property.
     */
    private final MetaProperty<LocalDate> _tradeDate = DirectMetaProperty.ofImmutable(
        this, "tradeDate", FXForwardTrade.class, LocalDate.class);
    /**
     * The meta-property for the {@code payCurrency} property.
     */
    private final MetaProperty<Currency> _payCurrency = DirectMetaProperty.ofImmutable(
        this, "payCurrency", FXForwardTrade.class, Currency.class);
    /**
     * The meta-property for the {@code receiveCurrency} property.
     */
    private final MetaProperty<Currency> _receiveCurrency = DirectMetaProperty.ofImmutable(
        this, "receiveCurrency", FXForwardTrade.class, Currency.class);
    /**
     * The meta-property for the {@code payAmount} property.
     */
    private final MetaProperty<Double> _payAmount = DirectMetaProperty.ofImmutable(
        this, "payAmount", FXForwardTrade.class, Double.TYPE);
    /**
     * The meta-property for the {@code receiveAmount} property.
     */
    private final MetaProperty<Double> _receiveAmount = DirectMetaProperty.ofImmutable(
        this, "receiveAmount", FXForwardTrade.class, Double.TYPE);
    /**
     * The meta-property for the {@code forwardDate} property.
     */
    private final MetaProperty<LocalDate> _forwardDate = DirectMetaProperty.ofImmutable(
        this, "forwardDate", FXForwardTrade.class, LocalDate.class);
    /**
     * The meta-property for the {@code correlationId} property.
     */
    private final MetaProperty<ExternalId> _correlationId = DirectMetaProperty.ofImmutable(
        this, "correlationId", FXForwardTrade.class, ExternalId.class);
    /**
     * The meta-property for the {@code counterparty} property.
     */
    private final MetaProperty<String> _counterparty = DirectMetaProperty.ofImmutable(
        this, "counterparty", FXForwardTrade.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "tradeDate",
        "payCurrency",
        "receiveCurrency",
        "payAmount",
        "receiveAmount",
        "forwardDate",
        "correlationId",
        "counterparty");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 752419634:  // tradeDate
          return _tradeDate;
        case -295641895:  // payCurrency
          return _payCurrency;
        case -1228590060:  // receiveCurrency
          return _receiveCurrency;
        case -1338781920:  // payAmount
          return _payAmount;
        case 984267035:  // receiveAmount
          return _receiveAmount;
        case 1652755475:  // forwardDate
          return _forwardDate;
        case -764983747:  // correlationId
          return _correlationId;
        case -1651301782:  // counterparty
          return _counterparty;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FXForwardTrade.Builder builder() {
      return new FXForwardTrade.Builder();
    }

    @Override
    public Class<? extends FXForwardTrade> beanType() {
      return FXForwardTrade.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code tradeDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> tradeDate() {
      return _tradeDate;
    }

    /**
     * The meta-property for the {@code payCurrency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> payCurrency() {
      return _payCurrency;
    }

    /**
     * The meta-property for the {@code receiveCurrency} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Currency> receiveCurrency() {
      return _receiveCurrency;
    }

    /**
     * The meta-property for the {@code payAmount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> payAmount() {
      return _payAmount;
    }

    /**
     * The meta-property for the {@code receiveAmount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> receiveAmount() {
      return _receiveAmount;
    }

    /**
     * The meta-property for the {@code forwardDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> forwardDate() {
      return _forwardDate;
    }

    /**
     * The meta-property for the {@code correlationId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> correlationId() {
      return _correlationId;
    }

    /**
     * The meta-property for the {@code counterparty} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> counterparty() {
      return _counterparty;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 752419634:  // tradeDate
          return ((FXForwardTrade) bean).getTradeDate();
        case -295641895:  // payCurrency
          return ((FXForwardTrade) bean).getPayCurrency();
        case -1228590060:  // receiveCurrency
          return ((FXForwardTrade) bean).getReceiveCurrency();
        case -1338781920:  // payAmount
          return ((FXForwardTrade) bean).getPayAmount();
        case 984267035:  // receiveAmount
          return ((FXForwardTrade) bean).getReceiveAmount();
        case 1652755475:  // forwardDate
          return ((FXForwardTrade) bean).getForwardDate();
        case -764983747:  // correlationId
          return ((FXForwardTrade) bean).getCorrelationId();
        case -1651301782:  // counterparty
          return ((FXForwardTrade) bean).getCounterparty();
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
   * The bean-builder for {@code FXForwardTrade}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<FXForwardTrade> {

    private LocalDate _tradeDate;
    private Currency _payCurrency;
    private Currency _receiveCurrency;
    private double _payAmount;
    private double _receiveAmount;
    private LocalDate _forwardDate;
    private ExternalId _correlationId;
    private String _counterparty;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(FXForwardTrade beanToCopy) {
      this._tradeDate = beanToCopy.getTradeDate();
      this._payCurrency = beanToCopy.getPayCurrency();
      this._receiveCurrency = beanToCopy.getReceiveCurrency();
      this._payAmount = beanToCopy.getPayAmount();
      this._receiveAmount = beanToCopy.getReceiveAmount();
      this._forwardDate = beanToCopy.getForwardDate();
      this._correlationId = beanToCopy.getCorrelationId();
      this._counterparty = beanToCopy.getCounterparty();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
      switch (propertyName.hashCode()) {
        case 752419634:  // tradeDate
          return _tradeDate;
        case -295641895:  // payCurrency
          return _payCurrency;
        case -1228590060:  // receiveCurrency
          return _receiveCurrency;
        case -1338781920:  // payAmount
          return _payAmount;
        case 984267035:  // receiveAmount
          return _receiveAmount;
        case 1652755475:  // forwardDate
          return _forwardDate;
        case -764983747:  // correlationId
          return _correlationId;
        case -1651301782:  // counterparty
          return _counterparty;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(String propertyName, Object newValue) {
      switch (propertyName.hashCode()) {
        case 752419634:  // tradeDate
          this._tradeDate = (LocalDate) newValue;
          break;
        case -295641895:  // payCurrency
          this._payCurrency = (Currency) newValue;
          break;
        case -1228590060:  // receiveCurrency
          this._receiveCurrency = (Currency) newValue;
          break;
        case -1338781920:  // payAmount
          this._payAmount = (Double) newValue;
          break;
        case 984267035:  // receiveAmount
          this._receiveAmount = (Double) newValue;
          break;
        case 1652755475:  // forwardDate
          this._forwardDate = (LocalDate) newValue;
          break;
        case -764983747:  // correlationId
          this._correlationId = (ExternalId) newValue;
          break;
        case -1651301782:  // counterparty
          this._counterparty = (String) newValue;
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
    public FXForwardTrade build() {
      return new FXForwardTrade(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the date the trade was booked.
     * @param tradeDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder tradeDate(LocalDate tradeDate) {
      JodaBeanUtils.notNull(tradeDate, "tradeDate");
      this._tradeDate = tradeDate;
      return this;
    }

    /**
     * Sets the pay currency, a list of currencies can be established by using CurrencyFactory
     * @param payCurrency  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder payCurrency(Currency payCurrency) {
      JodaBeanUtils.notNull(payCurrency, "payCurrency");
      this._payCurrency = payCurrency;
      return this;
    }

    /**
     * Sets the pay currency, a list of currencies can be established by using CurrencyFactory
     * @param receiveCurrency  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder receiveCurrency(Currency receiveCurrency) {
      JodaBeanUtils.notNull(receiveCurrency, "receiveCurrency");
      this._receiveCurrency = receiveCurrency;
      return this;
    }

    /**
     * Sets the pay amount.
     * @param payAmount  the new value
     * @return this, for chaining, not null
     */
    public Builder payAmount(double payAmount) {
      this._payAmount = payAmount;
      return this;
    }

    /**
     * Sets the receive amount.
     * @param receiveAmount  the new value
     * @return this, for chaining, not null
     */
    public Builder receiveAmount(double receiveAmount) {
      this._receiveAmount = receiveAmount;
      return this;
    }

    /**
     * Sets the forward date.
     * @param forwardDate  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder forwardDate(LocalDate forwardDate) {
      JodaBeanUtils.notNull(forwardDate, "forwardDate");
      this._forwardDate = forwardDate;
      return this;
    }

    /**
     * Sets the correlation id(s) to associate this trade with the source system.
     * These <b>must</b> be unique to this trade.  There can be more than one.
     * For example if the source is actually two separate trades:
     * <pre>
     * {@code
     * ExternalIdBundle.of(
     * ExternalId.of("EXAMPLE_SCHEME", "TRADELEG_ID1"),
     * ExternalId.of("EXAMPLE_SCHEME", "TRADELEG_ID2"));
     * }
     * </pre>
     * or if just one:
     * <pre>
     * {@code
     * ExternalIdBundle.of(
     * ExternalId.of("EXAMPLE_SCHEME", "TRADE_ID1"));
     * }
     * </pre>
     * @param correlationId  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder correlationId(ExternalId correlationId) {
      JodaBeanUtils.notNull(correlationId, "correlationId");
      this._correlationId = correlationId;
      return this;
    }

    /**
     * Sets the counterparty, optional.
     * @param counterparty  the new value
     * @return this, for chaining, not null
     */
    public Builder counterparty(String counterparty) {
      this._counterparty = counterparty;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      StringBuilder buf = new StringBuilder(288);
      buf.append("FXForwardTrade.Builder{");
      int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(StringBuilder buf) {
      buf.append("tradeDate").append('=').append(JodaBeanUtils.toString(_tradeDate)).append(',').append(' ');
      buf.append("payCurrency").append('=').append(JodaBeanUtils.toString(_payCurrency)).append(',').append(' ');
      buf.append("receiveCurrency").append('=').append(JodaBeanUtils.toString(_receiveCurrency)).append(',').append(' ');
      buf.append("payAmount").append('=').append(JodaBeanUtils.toString(_payAmount)).append(',').append(' ');
      buf.append("receiveAmount").append('=').append(JodaBeanUtils.toString(_receiveAmount)).append(',').append(' ');
      buf.append("forwardDate").append('=').append(JodaBeanUtils.toString(_forwardDate)).append(',').append(' ');
      buf.append("correlationId").append('=').append(JodaBeanUtils.toString(_correlationId)).append(',').append(' ');
      buf.append("counterparty").append('=').append(JodaBeanUtils.toString(_counterparty)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
