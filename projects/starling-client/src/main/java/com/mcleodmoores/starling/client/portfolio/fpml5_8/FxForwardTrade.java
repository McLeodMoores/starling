/**
 * Copyright (C) 2016 - present McLeod Moores Software Limited.  All rights reserved.
 */
package com.mcleodmoores.starling.client.portfolio.fpml5_8;

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
import org.threeten.bp.OffsetTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import com.mcleodmoores.starling.client.portfolio.Trade;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.master.position.ManageableTrade;
import com.opengamma.util.i18n.Country;
import com.opengamma.util.money.Currency;

/**
 * An object representing a FX forward trade as defined in FpML consisting of a single FX leg settling on the forward rate.
 * These trades are converted to {@link FXForwardSecurity} before pricing the portfolio.
 */
@BeanDefinition
public class FxForwardTrade implements ImmutableBean, Trade {
  /** The default region to use when constructing a {@link FXForwardSecurity}. */
  private static final ExternalId DEFAULT_REGION = ExternalSchemes.countryRegionId(Country.US);
  /** The default offset time used in trades */
  private static final OffsetTime DEFAULT_TRADE_TIME = OffsetTime.MAX;

  /**
   * The creation time stamp of this trade.
   */
  @PropertyDefinition(validate = "notNull")
  private final ZonedDateTime _creationTimestamp;

  /**
   * The correlation id.
   */
  @PropertyDefinition(validate = "notNull")
  private final ExternalId _correlationId;

  /**
   * The trade header containing information about the partiels in the trade and the trade date.
   */
  @PropertyDefinition(validate = "notNull")
  private final TradeHeader _tradeHeader;

  /**
   * The FX leg containing details of the payments.
   */
  @PropertyDefinition(validate = "notNull")
  private final FxSingleLeg _fxSingleLeg;

  /**
   * The identifier of the first party in the trade.
   */
  @PropertyDefinition(validate = "notNull")
  private final ExternalId _party1Id;

  /**
   * The identifier of the second party in the trade, does not need to be set.
   */
  @PropertyDefinition
  private final ExternalId _party2Id;

  @Override
  public Position toPosition() {
    if (_tradeHeader.getParty2() != null) {
      //TODO
      throw new IllegalStateException();
    }
    final Counterparty counterparty = _tradeHeader.getParty1().getParty();
    final Currency payCurrency, receiveCurrency;
    final double payAmount, receiveAmount;
    final ExchangedCurrency exchangedCurrency1 = _fxSingleLeg.getExchangedCurrency1();
    final ExchangedCurrency exchangedCurrency2 = _fxSingleLeg.getExchangedCurrency2();
    if (exchangedCurrency1.getPayerPartyReference() != null) {
      receiveCurrency = exchangedCurrency1.getPaymentAmount().getCurrency();
      receiveAmount = exchangedCurrency1.getPaymentAmount().getAmount().doubleValue();
      payCurrency = exchangedCurrency2.getPaymentAmount().getCurrency();
      payAmount = exchangedCurrency2.getPaymentAmount().getAmount().doubleValue();
    } else {
      receiveCurrency = exchangedCurrency2.getPaymentAmount().getCurrency();
      receiveAmount = exchangedCurrency2.getPaymentAmount().getAmount().doubleValue();
      payCurrency = exchangedCurrency1.getPaymentAmount().getCurrency();
      payAmount = exchangedCurrency1.getPaymentAmount().getAmount().doubleValue();
    }
    //TODO think about the zone
    final ZonedDateTime forwardDate = _fxSingleLeg.getValueDate().atStartOfDay(ZoneId.systemDefault());
    final FXForwardSecurity security = new FXForwardSecurity(payCurrency, payAmount, receiveCurrency, receiveAmount, forwardDate, DEFAULT_REGION);
    final StringBuilder name = new StringBuilder(forwardDate.toLocalDate().toString());
    name.append(", pay ");
    name.append(payCurrency.getCode());
    name.append(" ");
    name.append(payAmount);
    name.append(", receive ");
    name.append(receiveCurrency);
    name.append(" ");
    name.append(receiveAmount);
    security.setName(name.toString());
    security.setExternalIdBundle(_correlationId.toBundle());
    final SimpleTrade trade = new SimpleTrade(security, BigDecimal.ONE, counterparty, _tradeHeader.getTradeDate(), DEFAULT_TRADE_TIME);
    final SimpleDeal deal = new SimpleDeal();
    deal.setCreationTimestamp(_creationTimestamp);
    trade.addAttribute(ManageableTrade.meta().deal().name(), deal.toString());
    trade.addAttribute(ManageableTrade.meta().providerId().name(), _correlationId.toString());
    final SimplePosition position = new SimplePosition();
    position.addAttribute(ManageableTrade.meta().providerId().name(), _correlationId.toString());
    position.setSecurityLink(SimpleSecurityLink.of(security));
    position.setQuantity(BigDecimal.ONE);
    position.addTrade(trade);
    return position;
  }

  public static FxSpotTrade from(final Position position) {
    //TODO
    return null;
  }
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FxForwardTrade}.
   * @return the meta-bean, not null
   */
  public static FxForwardTrade.Meta meta() {
    return FxForwardTrade.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FxForwardTrade.Meta.INSTANCE);
  }

  /**
   * Returns a builder used to create an instance of the bean.
   * @return the builder, not null
   */
  public static FxForwardTrade.Builder builder() {
    return new FxForwardTrade.Builder();
  }

  /**
   * Restricted constructor.
   * @param builder  the builder to copy from, not null
   */
  protected FxForwardTrade(final FxForwardTrade.Builder builder) {
    JodaBeanUtils.notNull(builder._creationTimestamp, "creationTimestamp");
    JodaBeanUtils.notNull(builder._correlationId, "correlationId");
    JodaBeanUtils.notNull(builder._tradeHeader, "tradeHeader");
    JodaBeanUtils.notNull(builder._fxSingleLeg, "fxSingleLeg");
    JodaBeanUtils.notNull(builder._party1Id, "party1Id");
    this._creationTimestamp = builder._creationTimestamp;
    this._correlationId = builder._correlationId;
    this._tradeHeader = builder._tradeHeader;
    this._fxSingleLeg = builder._fxSingleLeg;
    this._party1Id = builder._party1Id;
    this._party2Id = builder._party2Id;
  }

  @Override
  public FxForwardTrade.Meta metaBean() {
    return FxForwardTrade.Meta.INSTANCE;
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
   * Gets the creationTimestamp.
   * @return the value of the property, not null
   */
  public ZonedDateTime getCreationTimestamp() {
    return _creationTimestamp;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the correlationId.
   * @return the value of the property, not null
   */
  public ExternalId getCorrelationId() {
    return _correlationId;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the tradeHeader.
   * @return the value of the property, not null
   */
  public TradeHeader getTradeHeader() {
    return _tradeHeader;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the fxSingleLeg.
   * @return the value of the property, not null
   */
  public FxSingleLeg getFxSingleLeg() {
    return _fxSingleLeg;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the party1Id.
   * @return the value of the property, not null
   */
  public ExternalId getParty1Id() {
    return _party1Id;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the party2Id.
   * @return the value of the property
   */
  public ExternalId getParty2Id() {
    return _party2Id;
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
      final FxForwardTrade other = (FxForwardTrade) obj;
      return JodaBeanUtils.equal(getCreationTimestamp(), other.getCreationTimestamp()) &&
          JodaBeanUtils.equal(getCorrelationId(), other.getCorrelationId()) &&
          JodaBeanUtils.equal(getTradeHeader(), other.getTradeHeader()) &&
          JodaBeanUtils.equal(getFxSingleLeg(), other.getFxSingleLeg()) &&
          JodaBeanUtils.equal(getParty1Id(), other.getParty1Id()) &&
          JodaBeanUtils.equal(getParty2Id(), other.getParty2Id());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getCreationTimestamp());
    hash = hash * 31 + JodaBeanUtils.hashCode(getCorrelationId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeHeader());
    hash = hash * 31 + JodaBeanUtils.hashCode(getFxSingleLeg());
    hash = hash * 31 + JodaBeanUtils.hashCode(getParty1Id());
    hash = hash * 31 + JodaBeanUtils.hashCode(getParty2Id());
    return hash;
  }

  @Override
  public String toString() {
    final StringBuilder buf = new StringBuilder(224);
    buf.append("FxForwardTrade{");
    final int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(final StringBuilder buf) {
    buf.append("creationTimestamp").append('=').append(JodaBeanUtils.toString(getCreationTimestamp())).append(',').append(' ');
    buf.append("correlationId").append('=').append(JodaBeanUtils.toString(getCorrelationId())).append(',').append(' ');
    buf.append("tradeHeader").append('=').append(JodaBeanUtils.toString(getTradeHeader())).append(',').append(' ');
    buf.append("fxSingleLeg").append('=').append(JodaBeanUtils.toString(getFxSingleLeg())).append(',').append(' ');
    buf.append("party1Id").append('=').append(JodaBeanUtils.toString(getParty1Id())).append(',').append(' ');
    buf.append("party2Id").append('=').append(JodaBeanUtils.toString(getParty2Id())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FxForwardTrade}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code creationTimestamp} property.
     */
    private final MetaProperty<ZonedDateTime> _creationTimestamp = DirectMetaProperty.ofImmutable(
        this, "creationTimestamp", FxForwardTrade.class, ZonedDateTime.class);
    /**
     * The meta-property for the {@code correlationId} property.
     */
    private final MetaProperty<ExternalId> _correlationId = DirectMetaProperty.ofImmutable(
        this, "correlationId", FxForwardTrade.class, ExternalId.class);
    /**
     * The meta-property for the {@code tradeHeader} property.
     */
    private final MetaProperty<TradeHeader> _tradeHeader = DirectMetaProperty.ofImmutable(
        this, "tradeHeader", FxForwardTrade.class, TradeHeader.class);
    /**
     * The meta-property for the {@code fxSingleLeg} property.
     */
    private final MetaProperty<FxSingleLeg> _fxSingleLeg = DirectMetaProperty.ofImmutable(
        this, "fxSingleLeg", FxForwardTrade.class, FxSingleLeg.class);
    /**
     * The meta-property for the {@code party1Id} property.
     */
    private final MetaProperty<ExternalId> _party1Id = DirectMetaProperty.ofImmutable(
        this, "party1Id", FxForwardTrade.class, ExternalId.class);
    /**
     * The meta-property for the {@code party2Id} property.
     */
    private final MetaProperty<ExternalId> _party2Id = DirectMetaProperty.ofImmutable(
        this, "party2Id", FxForwardTrade.class, ExternalId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "creationTimestamp",
        "correlationId",
        "tradeHeader",
        "fxSingleLeg",
        "party1Id",
        "party2Id");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(final String propertyName) {
      switch (propertyName.hashCode()) {
        case -370203401:  // creationTimestamp
          return _creationTimestamp;
        case -764983747:  // correlationId
          return _correlationId;
        case 1638409489:  // tradeHeader
          return _tradeHeader;
        case -202817548:  // fxSingleLeg
          return _fxSingleLeg;
        case 1189278854:  // party1Id
          return _party1Id;
        case 1189279815:  // party2Id
          return _party2Id;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public FxForwardTrade.Builder builder() {
      return new FxForwardTrade.Builder();
    }

    @Override
    public Class<? extends FxForwardTrade> beanType() {
      return FxForwardTrade.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code creationTimestamp} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ZonedDateTime> creationTimestamp() {
      return _creationTimestamp;
    }

    /**
     * The meta-property for the {@code correlationId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> correlationId() {
      return _correlationId;
    }

    /**
     * The meta-property for the {@code tradeHeader} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<TradeHeader> tradeHeader() {
      return _tradeHeader;
    }

    /**
     * The meta-property for the {@code fxSingleLeg} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<FxSingleLeg> fxSingleLeg() {
      return _fxSingleLeg;
    }

    /**
     * The meta-property for the {@code party1Id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> party1Id() {
      return _party1Id;
    }

    /**
     * The meta-property for the {@code party2Id} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> party2Id() {
      return _party2Id;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(final Bean bean, final String propertyName, final boolean quiet) {
      switch (propertyName.hashCode()) {
        case -370203401:  // creationTimestamp
          return ((FxForwardTrade) bean).getCreationTimestamp();
        case -764983747:  // correlationId
          return ((FxForwardTrade) bean).getCorrelationId();
        case 1638409489:  // tradeHeader
          return ((FxForwardTrade) bean).getTradeHeader();
        case -202817548:  // fxSingleLeg
          return ((FxForwardTrade) bean).getFxSingleLeg();
        case 1189278854:  // party1Id
          return ((FxForwardTrade) bean).getParty1Id();
        case 1189279815:  // party2Id
          return ((FxForwardTrade) bean).getParty2Id();
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
   * The bean-builder for {@code FxForwardTrade}.
   */
  public static class Builder extends DirectFieldsBeanBuilder<FxForwardTrade> {

    private ZonedDateTime _creationTimestamp;
    private ExternalId _correlationId;
    private TradeHeader _tradeHeader;
    private FxSingleLeg _fxSingleLeg;
    private ExternalId _party1Id;
    private ExternalId _party2Id;

    /**
     * Restricted constructor.
     */
    protected Builder() {
    }

    /**
     * Restricted copy constructor.
     * @param beanToCopy  the bean to copy from, not null
     */
    protected Builder(final FxForwardTrade beanToCopy) {
      this._creationTimestamp = beanToCopy.getCreationTimestamp();
      this._correlationId = beanToCopy.getCorrelationId();
      this._tradeHeader = beanToCopy.getTradeHeader();
      this._fxSingleLeg = beanToCopy.getFxSingleLeg();
      this._party1Id = beanToCopy.getParty1Id();
      this._party2Id = beanToCopy.getParty2Id();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(final String propertyName) {
      switch (propertyName.hashCode()) {
        case -370203401:  // creationTimestamp
          return _creationTimestamp;
        case -764983747:  // correlationId
          return _correlationId;
        case 1638409489:  // tradeHeader
          return _tradeHeader;
        case -202817548:  // fxSingleLeg
          return _fxSingleLeg;
        case 1189278854:  // party1Id
          return _party1Id;
        case 1189279815:  // party2Id
          return _party2Id;
        default:
          throw new NoSuchElementException("Unknown property: " + propertyName);
      }
    }

    @Override
    public Builder set(final String propertyName, final Object newValue) {
      switch (propertyName.hashCode()) {
        case -370203401:  // creationTimestamp
          this._creationTimestamp = (ZonedDateTime) newValue;
          break;
        case -764983747:  // correlationId
          this._correlationId = (ExternalId) newValue;
          break;
        case 1638409489:  // tradeHeader
          this._tradeHeader = (TradeHeader) newValue;
          break;
        case -202817548:  // fxSingleLeg
          this._fxSingleLeg = (FxSingleLeg) newValue;
          break;
        case 1189278854:  // party1Id
          this._party1Id = (ExternalId) newValue;
          break;
        case 1189279815:  // party2Id
          this._party2Id = (ExternalId) newValue;
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
    public FxForwardTrade build() {
      return new FxForwardTrade(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the creationTimestamp.
     * @param creationTimestamp  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder creationTimestamp(final ZonedDateTime creationTimestamp) {
      JodaBeanUtils.notNull(creationTimestamp, "creationTimestamp");
      this._creationTimestamp = creationTimestamp;
      return this;
    }

    /**
     * Sets the correlationId.
     * @param correlationId  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder correlationId(final ExternalId correlationId) {
      JodaBeanUtils.notNull(correlationId, "correlationId");
      this._correlationId = correlationId;
      return this;
    }

    /**
     * Sets the tradeHeader.
     * @param tradeHeader  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder tradeHeader(final TradeHeader tradeHeader) {
      JodaBeanUtils.notNull(tradeHeader, "tradeHeader");
      this._tradeHeader = tradeHeader;
      return this;
    }

    /**
     * Sets the fxSingleLeg.
     * @param fxSingleLeg  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder fxSingleLeg(final FxSingleLeg fxSingleLeg) {
      JodaBeanUtils.notNull(fxSingleLeg, "fxSingleLeg");
      this._fxSingleLeg = fxSingleLeg;
      return this;
    }

    /**
     * Sets the party1Id.
     * @param party1Id  the new value, not null
     * @return this, for chaining, not null
     */
    public Builder party1Id(final ExternalId party1Id) {
      JodaBeanUtils.notNull(party1Id, "party1Id");
      this._party1Id = party1Id;
      return this;
    }

    /**
     * Sets the party2Id.
     * @param party2Id  the new value
     * @return this, for chaining, not null
     */
    public Builder party2Id(final ExternalId party2Id) {
      this._party2Id = party2Id;
      return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
      final StringBuilder buf = new StringBuilder(224);
      buf.append("FxForwardTrade.Builder{");
      final int len = buf.length();
      toString(buf);
      if (buf.length() > len) {
        buf.setLength(buf.length() - 2);
      }
      buf.append('}');
      return buf.toString();
    }

    protected void toString(final StringBuilder buf) {
      buf.append("creationTimestamp").append('=').append(JodaBeanUtils.toString(_creationTimestamp)).append(',').append(' ');
      buf.append("correlationId").append('=').append(JodaBeanUtils.toString(_correlationId)).append(',').append(' ');
      buf.append("tradeHeader").append('=').append(JodaBeanUtils.toString(_tradeHeader)).append(',').append(' ');
      buf.append("fxSingleLeg").append('=').append(JodaBeanUtils.toString(_fxSingleLeg)).append(',').append(' ');
      buf.append("party1Id").append('=').append(JodaBeanUtils.toString(_party1Id)).append(',').append(' ');
      buf.append("party2Id").append('=').append(JodaBeanUtils.toString(_party2Id)).append(',').append(' ');
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
