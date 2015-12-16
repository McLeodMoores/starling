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
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.mcleodmoores.quandl.QuandlConstants;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;

/**
 * Base class for future conventions that contain sufficient meta-data to create a
 * {@link com.opengamma.financial.security.future.FutureSecurity} from Quandl data.
 */
@BeanDefinition
public abstract class QuandlFutureConvention extends QuandlFinancialConvention {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The last trade time on the last trade date of the future.
   */
  @PropertyDefinition(validate = "notNull")
  private String _lastTradeTime;

  /**
   * The time zone of the exchange.
   */
  @PropertyDefinition
  private String _zoneOffsetId;

  /**
   * The unit amount of the future.
   */
  @PropertyDefinition
  private double _unitAmount;

  /**
   * The name of the trading exchange.
   */
  @PropertyDefinition(get = "manual")
  private String _tradingExchange;

  /**
   * The name of the settlement exchange.
   */
  @PropertyDefinition(get = "manual")
  private String _settlementExchange;

  /**
   * The id of the convention of the underlying index.
   */
  @PropertyDefinition(validate = "notNull")
  private ExternalId _underlyingConventionId;

  /**
   * For the builder.
   */
  protected QuandlFutureConvention() {
    super();
  }

  /**
   * Creates an instance with the settlement exchange and trading exchange name created by parsing
   * the Quandl code.
   * @param name The name of the convention, not null
   * @param externalIdBundle The ids associated with this convention, not null
   * @param lastTradeTime The last trade time on the last trade date of the future, not null
   * @param zoneOffsetId The time zone of the exchange
   * @param unitAmount The unit amount of the future
   * @param underlyingConventionId The id of the underlying index convention, not null
   */
  public QuandlFutureConvention(final String name, final ExternalIdBundle externalIdBundle, final String lastTradeTime, final String zoneOffsetId,
      final double unitAmount, final ExternalId underlyingConventionId) {
    super(name, externalIdBundle);
    setLastTradeTime(lastTradeTime);
    setZoneOffsetId(zoneOffsetId);
    setUnitAmount(unitAmount);
    setUnderlyingConventionId(underlyingConventionId);
    final String exchangeFromCode = getExchangeFromCode(externalIdBundle);
    setTradingExchange(exchangeFromCode);
    setSettlementExchange(exchangeFromCode);
  }

  /**
   * Creates an instance.
   * @param name The name of the convention, not null
   * @param externalIdBundle The ids associated with this convention, not null
   * @param lastTradeTime The last trade time on the last trade date of the future, not null
   * @param zoneOffsetId The time zone of the exchange
   * @param unitAmount The unit amount of the future
   * @param underlyingConventionId The id of the underlying index convention, not null
   * @param tradingExchange The name of the trading exchange, can be null
   * @param settlementExchange The name of the settlement exchange, can be null
   */
  public QuandlFutureConvention(final String name, final ExternalIdBundle externalIdBundle, final String lastTradeTime, final String zoneOffsetId,
      final double unitAmount, final ExternalId underlyingConventionId, final String tradingExchange, final String settlementExchange) {
    super(name, externalIdBundle);
    setLastTradeTime(lastTradeTime);
    setZoneOffsetId(zoneOffsetId);
    setUnitAmount(unitAmount);
    setUnderlyingConventionId(underlyingConventionId);
    setTradingExchange(tradingExchange);
    setSettlementExchange(settlementExchange);
  }

  /**
   * Gets the trading exchange. If this has not been supplied, returns the string before
   * the "/" in the Quandl code, or null if there is no id in the bundle with scheme
   * {@link QuandlConstants#QUANDL_CODE}.
   * @return The name of the trading exchange
   */
  public String getTradingExchange() {
    if (_tradingExchange != null) {
      return _tradingExchange;
    }
    return getExchangeFromCode(getExternalIdBundle());
  }

  /**
   * Gets the settlement exchange. If this has not been supplied, returns the string before
   * the "/" in the Quandl code, or null if there is no id in the bundle with scheme
   * {@link QuandlConstants#QUANDL_CODE}.
   * @return The name of the settlement exchange
   */
  public String getSettlementExchange() {
    if (_settlementExchange != null) {
      return _settlementExchange;
    }
    return getExchangeFromCode(getExternalIdBundle());
  }

  /**
   * Returns the string before the first "/" in the Quandl code, which is used as the name of the exchange,
   * or null if there is no id in the bundle with scheme {@link QuandlConstants#QUANDL_CODE}.
   * @param idBundle The ids associated with this convention
   * @return The exchange code
   */
  private static String getExchangeFromCode(final ExternalIdBundle idBundle) {
    final ExternalId quandlCode = idBundle.getExternalId(QuandlConstants.QUANDL_CODE);
    if (quandlCode == null) {
      return null;
    }
    final String value = quandlCode.getValue();
    return value.substring(0, value.indexOf("/"));
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code QuandlFutureConvention}.
   * @return the meta-bean, not null
   */
  public static QuandlFutureConvention.Meta meta() {
    return QuandlFutureConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(QuandlFutureConvention.Meta.INSTANCE);
  }

  @Override
  public QuandlFutureConvention.Meta metaBean() {
    return QuandlFutureConvention.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the last trade time on the last trade date of the future.
   * @return the value of the property, not null
   */
  public String getLastTradeTime() {
    return _lastTradeTime;
  }

  /**
   * Sets the last trade time on the last trade date of the future.
   * @param lastTradeTime  the new value of the property, not null
   */
  public void setLastTradeTime(String lastTradeTime) {
    JodaBeanUtils.notNull(lastTradeTime, "lastTradeTime");
    this._lastTradeTime = lastTradeTime;
  }

  /**
   * Gets the the {@code lastTradeTime} property.
   * @return the property, not null
   */
  public final Property<String> lastTradeTime() {
    return metaBean().lastTradeTime().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the time zone of the exchange.
   * @return the value of the property
   */
  public String getZoneOffsetId() {
    return _zoneOffsetId;
  }

  /**
   * Sets the time zone of the exchange.
   * @param zoneOffsetId  the new value of the property
   */
  public void setZoneOffsetId(String zoneOffsetId) {
    this._zoneOffsetId = zoneOffsetId;
  }

  /**
   * Gets the the {@code zoneOffsetId} property.
   * @return the property, not null
   */
  public final Property<String> zoneOffsetId() {
    return metaBean().zoneOffsetId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the unit amount of the future.
   * @return the value of the property
   */
  public double getUnitAmount() {
    return _unitAmount;
  }

  /**
   * Sets the unit amount of the future.
   * @param unitAmount  the new value of the property
   */
  public void setUnitAmount(double unitAmount) {
    this._unitAmount = unitAmount;
  }

  /**
   * Gets the the {@code unitAmount} property.
   * @return the property, not null
   */
  public final Property<Double> unitAmount() {
    return metaBean().unitAmount().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the name of the trading exchange.
   * @param tradingExchange  the new value of the property
   */
  public void setTradingExchange(String tradingExchange) {
    this._tradingExchange = tradingExchange;
  }

  /**
   * Gets the the {@code tradingExchange} property.
   * @return the property, not null
   */
  public final Property<String> tradingExchange() {
    return metaBean().tradingExchange().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Sets the name of the settlement exchange.
   * @param settlementExchange  the new value of the property
   */
  public void setSettlementExchange(String settlementExchange) {
    this._settlementExchange = settlementExchange;
  }

  /**
   * Gets the the {@code settlementExchange} property.
   * @return the property, not null
   */
  public final Property<String> settlementExchange() {
    return metaBean().settlementExchange().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the id of the convention of the underlying index.
   * @return the value of the property, not null
   */
  public ExternalId getUnderlyingConventionId() {
    return _underlyingConventionId;
  }

  /**
   * Sets the id of the convention of the underlying index.
   * @param underlyingConventionId  the new value of the property, not null
   */
  public void setUnderlyingConventionId(ExternalId underlyingConventionId) {
    JodaBeanUtils.notNull(underlyingConventionId, "underlyingConventionId");
    this._underlyingConventionId = underlyingConventionId;
  }

  /**
   * Gets the the {@code underlyingConventionId} property.
   * @return the property, not null
   */
  public final Property<ExternalId> underlyingConventionId() {
    return metaBean().underlyingConventionId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      QuandlFutureConvention other = (QuandlFutureConvention) obj;
      return JodaBeanUtils.equal(getLastTradeTime(), other.getLastTradeTime()) &&
          JodaBeanUtils.equal(getZoneOffsetId(), other.getZoneOffsetId()) &&
          JodaBeanUtils.equal(getUnitAmount(), other.getUnitAmount()) &&
          JodaBeanUtils.equal(getTradingExchange(), other.getTradingExchange()) &&
          JodaBeanUtils.equal(getSettlementExchange(), other.getSettlementExchange()) &&
          JodaBeanUtils.equal(getUnderlyingConventionId(), other.getUnderlyingConventionId()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getLastTradeTime());
    hash = hash * 31 + JodaBeanUtils.hashCode(getZoneOffsetId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnitAmount());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradingExchange());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSettlementExchange());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUnderlyingConventionId());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("QuandlFutureConvention{");
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
    buf.append("lastTradeTime").append('=').append(JodaBeanUtils.toString(getLastTradeTime())).append(',').append(' ');
    buf.append("zoneOffsetId").append('=').append(JodaBeanUtils.toString(getZoneOffsetId())).append(',').append(' ');
    buf.append("unitAmount").append('=').append(JodaBeanUtils.toString(getUnitAmount())).append(',').append(' ');
    buf.append("tradingExchange").append('=').append(JodaBeanUtils.toString(getTradingExchange())).append(',').append(' ');
    buf.append("settlementExchange").append('=').append(JodaBeanUtils.toString(getSettlementExchange())).append(',').append(' ');
    buf.append("underlyingConventionId").append('=').append(JodaBeanUtils.toString(getUnderlyingConventionId())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code QuandlFutureConvention}.
   */
  public static class Meta extends QuandlFinancialConvention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code lastTradeTime} property.
     */
    private final MetaProperty<String> _lastTradeTime = DirectMetaProperty.ofReadWrite(
        this, "lastTradeTime", QuandlFutureConvention.class, String.class);
    /**
     * The meta-property for the {@code zoneOffsetId} property.
     */
    private final MetaProperty<String> _zoneOffsetId = DirectMetaProperty.ofReadWrite(
        this, "zoneOffsetId", QuandlFutureConvention.class, String.class);
    /**
     * The meta-property for the {@code unitAmount} property.
     */
    private final MetaProperty<Double> _unitAmount = DirectMetaProperty.ofReadWrite(
        this, "unitAmount", QuandlFutureConvention.class, Double.TYPE);
    /**
     * The meta-property for the {@code tradingExchange} property.
     */
    private final MetaProperty<String> _tradingExchange = DirectMetaProperty.ofReadWrite(
        this, "tradingExchange", QuandlFutureConvention.class, String.class);
    /**
     * The meta-property for the {@code settlementExchange} property.
     */
    private final MetaProperty<String> _settlementExchange = DirectMetaProperty.ofReadWrite(
        this, "settlementExchange", QuandlFutureConvention.class, String.class);
    /**
     * The meta-property for the {@code underlyingConventionId} property.
     */
    private final MetaProperty<ExternalId> _underlyingConventionId = DirectMetaProperty.ofReadWrite(
        this, "underlyingConventionId", QuandlFutureConvention.class, ExternalId.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "lastTradeTime",
        "zoneOffsetId",
        "unitAmount",
        "tradingExchange",
        "settlementExchange",
        "underlyingConventionId");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -1041466277:  // lastTradeTime
          return _lastTradeTime;
        case -2027179814:  // zoneOffsetId
          return _zoneOffsetId;
        case 1673913084:  // unitAmount
          return _unitAmount;
        case -661485980:  // tradingExchange
          return _tradingExchange;
        case 389497452:  // settlementExchange
          return _settlementExchange;
        case -162478999:  // underlyingConventionId
          return _underlyingConventionId;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends QuandlFutureConvention> builder() {
      throw new UnsupportedOperationException("QuandlFutureConvention is an abstract class");
    }

    @Override
    public Class<? extends QuandlFutureConvention> beanType() {
      return QuandlFutureConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code lastTradeTime} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> lastTradeTime() {
      return _lastTradeTime;
    }

    /**
     * The meta-property for the {@code zoneOffsetId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> zoneOffsetId() {
      return _zoneOffsetId;
    }

    /**
     * The meta-property for the {@code unitAmount} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Double> unitAmount() {
      return _unitAmount;
    }

    /**
     * The meta-property for the {@code tradingExchange} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> tradingExchange() {
      return _tradingExchange;
    }

    /**
     * The meta-property for the {@code settlementExchange} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> settlementExchange() {
      return _settlementExchange;
    }

    /**
     * The meta-property for the {@code underlyingConventionId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> underlyingConventionId() {
      return _underlyingConventionId;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1041466277:  // lastTradeTime
          return ((QuandlFutureConvention) bean).getLastTradeTime();
        case -2027179814:  // zoneOffsetId
          return ((QuandlFutureConvention) bean).getZoneOffsetId();
        case 1673913084:  // unitAmount
          return ((QuandlFutureConvention) bean).getUnitAmount();
        case -661485980:  // tradingExchange
          return ((QuandlFutureConvention) bean).getTradingExchange();
        case 389497452:  // settlementExchange
          return ((QuandlFutureConvention) bean).getSettlementExchange();
        case -162478999:  // underlyingConventionId
          return ((QuandlFutureConvention) bean).getUnderlyingConventionId();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -1041466277:  // lastTradeTime
          ((QuandlFutureConvention) bean).setLastTradeTime((String) newValue);
          return;
        case -2027179814:  // zoneOffsetId
          ((QuandlFutureConvention) bean).setZoneOffsetId((String) newValue);
          return;
        case 1673913084:  // unitAmount
          ((QuandlFutureConvention) bean).setUnitAmount((Double) newValue);
          return;
        case -661485980:  // tradingExchange
          ((QuandlFutureConvention) bean).setTradingExchange((String) newValue);
          return;
        case 389497452:  // settlementExchange
          ((QuandlFutureConvention) bean).setSettlementExchange((String) newValue);
          return;
        case -162478999:  // underlyingConventionId
          ((QuandlFutureConvention) bean).setUnderlyingConventionId((ExternalId) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((QuandlFutureConvention) bean)._lastTradeTime, "lastTradeTime");
      JodaBeanUtils.notNull(((QuandlFutureConvention) bean)._underlyingConventionId, "underlyingConventionId");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
