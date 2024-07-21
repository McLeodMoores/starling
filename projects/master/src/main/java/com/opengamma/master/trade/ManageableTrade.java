/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.DerivedProperty;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.google.common.collect.Maps;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.trade.Trade;
import com.opengamma.core.trade.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.MutableUniqueIdentifiable;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;
import com.opengamma.master.security.ManageableSecurityLink;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.JdkUtils;
import com.opengamma.util.PublicSPI;
import com.opengamma.util.money.Currency;

/**
 * A Trade held in a Trade master.
 * <p>
 * A Trade is fundamentally a quantity of a security.
 * For example, a Trade might be 50 shares of OpenGamma.
 * <p>
 * Trades are formed from a set of trades, however trade data may not always be available or complete.
 * Even if trade data is available, the Trade details cannot necessarily be derived from the trades.
 * Therefore the Trade holds the quantity and security reference directly, separately
 * from the underlying trades.
 * <p>
 * Trades are logically attached to nodes in the portfolio tree, however they are
 * stored and returned separately from the Trade master.
 */
@PublicSPI
@BeanDefinition
public class ManageableTrade extends DirectBean
implements Trade, MutableUniqueIdentifiable, UniqueIdentifiable, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The unique identifier of the Trade.
   * This must be null when adding to a master and not null when retrieved from a master.
   */
  @PropertyDefinition(overrideGet = true, overrideSet = true)
  private UniqueId _uniqueId;

  /**
   * The general purpose Trade attributes.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   */
  @PropertyDefinition(validate = "notNull")
  private final Map<String, String> _attributes = Maps.newHashMap();
  /**
   * The external trade identifiers for the trade.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   */
  @PropertyDefinition
  private ExternalIdBundle _externalTradeIds;

  /**
   * Construct an empty instance that must be populated via setters.
   */
  public ManageableTrade() {

  }

  /**
   * Creates a Trade from a bundle of external trade ids.
   *
   * @param externalTradeIds  the external trade identifiers, not null
   */
  public ManageableTrade(final ExternalIdBundle externalTradeIds) {
    ArgumentChecker.notNull(externalTradeIds, "externalTradeIds");
    _externalTradeIds = externalTradeIds;
  }

  /**
   * Creates a deep copy of the specified Trade.
   *
   * @param copyFrom  the Trade to copy from, not null
   */
  public ManageableTrade(final Trade copyFrom) {
    ArgumentChecker.notNull(copyFrom, "copyFrom");
    _uniqueId = copyFrom.getUniqueId();
    _externalTradeIds = JodaBeanUtils.clone(copyFrom.getExternalTradeIds());
    if (copyFrom.getAttributes() != null) {
      for (final Entry<String, String> entry : copyFrom.getAttributes().entrySet()) {
        addAttribute(entry.getKey(), entry.getValue());
      }
    }
  }

  //-------------------------------------------------------------------------

  /**
   * Gets a suitable name for the Trade.
   *
   * @return the name, not null
   */
  @DerivedProperty
  public String getName() {
    return toString();
  }

  //-------------------------------------------------------------------------


  //-------------------------------------------------------------------------
  /**
   * Adds a key value pair to attributes.
   *
   * @param key  the key to add, not null
   * @param value  the value to add, not null
   */
  public void addAttribute(final String key, final String value) {
    ArgumentChecker.notNull(key, "key");
    ArgumentChecker.notNull(value, "value");
    _attributes.put(key, value);
  }

  //-------------------------------------------------------------------------
  /**
   * Converts this Trade to an object implementing the Trade interface.
   * <p>
   * The interface contains different data to this class due to database design.
   *
   * @return the security from the link, null if not resolve
   */
  public SimpleTrade toTrade() {
    final SimpleTrade sp = new SimpleTrade();
    sp.setAttributes(this.getAttributes());

    if (this.getUniqueId() != null) { // may not have an id yet
      sp.setUniqueId(this.getUniqueId());
    }

    return sp;
  }

  //-----------------------------------------------------------------------
  @Override
  public ManageableTrade clone() {
    return new ManageableTrade(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code ManageableTrade}.
   * @return the meta-bean, not null
   */
  public static Meta meta() {
    return Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(Meta.INSTANCE);
  }

  @Override
  public Meta metaBean() {
    return Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------

  /**
   * Gets the unique identifier of the Trade.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the value of the property
   */
  @Override
  public UniqueId getUniqueId() {
    return _uniqueId;
  }

  /**
   * Sets the unique identifier of the Trade.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @param uniqueId  the new value of the property
   */
  @Override
  public void setUniqueId(UniqueId uniqueId) {
    this._uniqueId = uniqueId;
  }

  /**
   * Gets the the {@code uniqueId} property.
   * This must be null when adding to a master and not null when retrieved from a master.
   * @return the property, not null
   */
  public final Property<UniqueId> uniqueId() {
    return metaBean().uniqueId().createProperty(this);
  }


  //-----------------------------------------------------------------------
  /**
   * Gets the general purpose Trade attributes.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   * @return the value of the property, not null
   */
  public Map<String, String> getAttributes() {
    return _attributes;
  }

  /**
   * Sets the general purpose Trade attributes.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   * @param attributes  the new value of the property, not null
   */
  public void setAttributes(Map<String, String> attributes) {
    JodaBeanUtils.notNull(attributes, "attributes");
    this._attributes.clear();
    this._attributes.putAll(attributes);
  }

  /**
   * Gets the the {@code attributes} property.
   * These can be used to add arbitrary additional information to the object
   * and for aggregating in portfolios.
   * @return the property, not null
   */
  public final Property<Map<String, String>> attributes() {
    return metaBean().attributes().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the provider external identifier for the data.
   * This optional field can be used to capture the identifier used by the data provider.
   * This can be useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public ExternalIdBundle getExternalTradeIds() {
    return _externalTradeIds;
  }

  /**
   * Sets the external trade identifiers for the data.
   * This optional field can be used to capture any external trade identifiers.
   * @param externalTradeIds  the new value of the property
   */
  public void setExternalTradeIds(ExternalIdBundle externalTradeIds) {
    this._externalTradeIds = externalTradeIds;
  }

  /**
   * Gets the the {@code externalTradeIds} property.
   * This optional field can be used to capture ahy external trade identifiers.
   * This can be useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<ExternalIdBundle> externalTradeIds() {
    return metaBean()._externalTradeIds.createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      ManageableTrade other = (ManageableTrade) obj;
      return JodaBeanUtils.equal(getUniqueId(), other.getUniqueId()) &&
          JodaBeanUtils.equal(getAttributes(), other.getAttributes()) &&
          JodaBeanUtils.equal(getExternalTradeIds(), other.getExternalTradeIds());
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = getClass().hashCode();
    hash = hash * 31 + JodaBeanUtils.hashCode(getUniqueId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getAttributes());
    hash = hash * 31 + JodaBeanUtils.hashCode(getExternalTradeIds());
    return hash;
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("ManageableTrade{");
    int len = buf.length();
    toString(buf);
    if (buf.length() > len) {
      buf.setLength(buf.length() - 2);
    }
    buf.append('}');
    return buf.toString();
  }

  protected void toString(StringBuilder buf) {
    buf.append("uniqueId").append('=').append(JodaBeanUtils.toString(getUniqueId())).append(',').append(' ');
    buf.append("externalTradeIds").append('=').append(JodaBeanUtils.toString(getExternalTradeIds())).append(',').append(' ');
    buf.append("attributes").append('=').append(JodaBeanUtils.toString(getAttributes())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code ManageableTrade}.
   */
  public static class Meta extends DirectMetaBean {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code uniqueId} property.
     */
    private final MetaProperty<UniqueId> _uniqueId = DirectMetaProperty.ofReadWrite(
        this, "uniqueId", ManageableTrade.class, UniqueId.class);
    /**
     * The meta-property for the {@code attributes} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Map<String, String>> _attributes = DirectMetaProperty.ofReadWrite(
        this, "attributes", ManageableTrade.class, (Class) Map.class);
    /**
     * The meta-property for the {@code externalTradeIds} property.
     */
    private final MetaProperty<ExternalIdBundle> _externalTradeIds = DirectMetaProperty.ofReadWrite(
        this, "externalTradeIds", ManageableTrade.class, ExternalIdBundle.class);
    /**
     * The meta-property for the {@code name} property.
     */
    private final MetaProperty<String> _name = DirectMetaProperty.ofDerived(
        this, "name", ManageableTrade.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, null,
        "uniqueId",
        "externalTradeIds",
        "attributes",
        "name");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return _uniqueId;
        case 405645655:  // attributes
          return _attributes;
        case 205149932:  // externalTradeIds
          return _externalTradeIds;
        case 3373707:  // name
          return _name;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends ManageableTrade> builder() {
      return new DirectBeanBuilder<ManageableTrade>(new ManageableTrade());
    }

    @Override
    public Class<? extends ManageableTrade> beanType() {
      return ManageableTrade.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code uniqueId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<UniqueId> uniqueId() {
      return _uniqueId;
    }

    /**
     * The meta-property for the {@code attributes} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Map<String, String>> attributes() {
      return _attributes;
    }

    /**
     * The meta-property for the {@code externalTradeIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdBundle> externalTradeIds() {
      return _externalTradeIds;
    }

    /**
     * The meta-property for the {@code name} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> name() {
      return _name;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          return ((ManageableTrade) bean).getUniqueId();
        case 405645655:  // attributes
          return ((ManageableTrade) bean).getAttributes();
        case 205149932:  // providerId
          return ((ManageableTrade) bean).getExternalTradeIds();
        case 3373707:  // name
          return ((ManageableTrade) bean).getName();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -294460212:  // uniqueId
          ((ManageableTrade) bean).setUniqueId((UniqueId) newValue);
          return;
        case 405645655:  // attributes
          ((ManageableTrade) bean).setAttributes((Map<String, String>) newValue);
          return;
        case 205149932:  // providerId
          ((ManageableTrade) bean).setExternalTradeIds((ExternalIdBundle) newValue);
          return;
        case 3373707:  // name
          if (quiet) {
            return;
          }
          throw new UnsupportedOperationException("Property cannot be written: name");
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((ManageableTrade) bean)._externalTradeIds, "externalTradeIds");
      JodaBeanUtils.notNull(((ManageableTrade) bean)._attributes, "attributes");
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------

}
