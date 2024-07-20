/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.trade;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.AbstractDocument;
import com.opengamma.master.AbstractSearchRequest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicSPI;

/**
 * Request for searching for Trades.
 * <p>
 * Documents will be returned that match the search criteria.
 * This class provides the ability to page the results and to search
 * as at a specific version and correction instant.
 * See {@link TradeHistoryRequest} for more details on how history works.
 */
@PublicSPI
@BeanDefinition
public class TradeSearchRequest extends AbstractSearchRequest {

  /**
   * The set of Trade object identifiers, null to not limit by Trade object identifiers.
   * Note that an empty set will return no Trades.
   */
  @PropertyDefinition(set = "manual")
  private Set<ObjectId> _tradeObjectIds;
  /**
   * The security external identifiers to match, null to not match on security identifiers.
   */
  @PropertyDefinition
  private ExternalIdSearch _tradeIdSearch;
  /**
   * The external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   */
  @PropertyDefinition
  private String _tradeIdValue;

  /**
   * Creates an instance.
   */
  public TradeSearchRequest() {
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single Trade object identifier to the set.
   *
   * @param tradeId  the Trade object identifier to add, not null
   */
  public void addTradeObjectId(final ObjectIdentifiable tradeId) {
    ArgumentChecker.notNull(tradeId, "TradeId");
    if (_tradeObjectIds == null) {
      _tradeObjectIds = new LinkedHashSet<>();
    }
    _tradeObjectIds.add(tradeId.getObjectId());
  }

  /**
   * Sets the set of Trade object identifiers, null to not limit by Trade object identifiers.
   * Note that an empty set will return no Trades.
   *
   * @param tradeIds  the new Trade identifiers, null clears the Trade id search
   */
  public void setTradeObjectIds(final Iterable<? extends ObjectIdentifiable> tradeIds) {
    if (tradeIds == null) {
      _tradeObjectIds = null;
    } else {
      _tradeObjectIds = new LinkedHashSet<>();
      for (final ObjectIdentifiable tradeId : tradeIds) {
        _tradeObjectIds.add(tradeId.getObjectId());
      }
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a single external trade identifier to the collection to search for.
   * Unless customized, the search will match
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   *
   * @param tradeId  the trade identifier to add, not null
   */
  public void addExternalTradeId(final ExternalId tradeId) {
    ArgumentChecker.notNull(tradeId, "tradeId");
    addExternalTradeIds(Collections.singletonList(tradeId));
  }

  /**
   * Adds a collection of security external identifiers to the collection to search for.
   * Unless customized, the search will match
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   *
   * @param tradeIds  the trade identifiers to add, not null
   */
  public void addExternalTradeIds(final ExternalId... tradeIds) {
    ArgumentChecker.notNull(tradeIds, "securityIds");
    if (getTradeIdSearch() == null) {
      setTradeIdSearch(ExternalIdSearch.of(tradeIds));
    } else {
      setTradeIdSearch(getTradeIdSearch().withExternalIdsAdded(tradeIds));
    }
  }

  /**
   * Adds a collection of security external identifiers to the collection to search for.
   * Unless customized, the search will match
   * {@link ExternalIdSearchType#ANY any} of the identifiers.
   *
   * @param tradeIds  the security key identifiers to add, not null
   */
  public void addExternalTradeIds(final Iterable<ExternalId> tradeIds) {
    ArgumentChecker.notNull(tradeIds, "tradeIds");
    if (getTradeIdSearch() == null) {
      setTradeIdSearch(ExternalIdSearch.of(tradeIds));
    } else {
      setTradeIdSearch(getSecurityIdSearch().withExternalIdsAdded(tradeIds));
    }
  }

  /**
   * Sets the search type to use in {@code ExternalIdSearch} for securities.
   *
   * @param type  the type to set, not null
   */
  public void setExternalTradeIdSearchType(final ExternalIdSearchType type) {
    if (getTradeIdSearch() == null) {
      setTradeIdSearch(ExternalIdSearch.of(type));
    } else {
      setTradeIdSearch(getTradeIdSearch().withSearchType(type));
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean matches(final AbstractDocument obj) {
    if (!(obj instanceof TradeDocument)) {
      return false;
    }
    final TradeDocument document = (TradeDocument) obj;
    final ManageableTrade trade = document.getTrade();
    if (getTradeObjectIds() != null && !getTradeObjectIds().contains(document.getObjectId())) {
      return false;
    }
    if (getSecurityIdSearch() != null && !getSecurityIdSearch().matches(trade.getExternalTradeIds())) {
      return false;
    }
    return true;
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code TradeSearchRequest}.
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
   * Gets the set of Trade object identifiers, null to not limit by Trade object identifiers.
   * Note that an empty set will return no Trades.
   * @return the value of the property
   */
  public Set<ObjectId> getTradeObjectIds() {
    return _tradeObjectIds;
  }

  /**
   * Gets the the {@code TradeObjectIds} property.
   * Note that an empty set will return no Trades.
   * @return the property, not null
   */
  public final Property<Set<ObjectId>> TradeObjectIds() {
    return metaBean().TradeObjectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the set of trade object identifiers, null to not limit by trade object identifiers.
   * Each returned Trade will contain at least one of these trades.
   * Note that an empty list will return no Trades.
   * @return the value of the property
   */
  public Set<ObjectId> getTradeObjectIds() {
    return _tradeObjectIds;
  }

  /**
   * Gets the the {@code tradeObjectIds} property.
   * Each returned Trade will contain at least one of these trades.
   * Note that an empty list will return no Trades.
   * @return the property, not null
   */
  public final Property<Set<ObjectId>> tradeObjectIds() {
    return metaBean().tradeObjectIds().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade external identifiers to match, null to not match on security identifiers.
   * @return the value of the property
   */
  public ExternalIdSearch getTradeIdSearch() {
    return _tradeIdSearch;
  }

  /**
   * Sets the security external identifiers to match, null to not match on security identifiers.
   * @param securityIdSearch  the new value of the property
   */
  public void setSecurityIdSearch(ExternalIdSearch securityIdSearch) {
    this._tradeIdSearch = tradeIdSearch;
  }

  /**
   * Gets the the {@code securityIdSearch} property.
   * @return the property, not null
   */
  public final Property<ExternalIdSearch> securityIdSearch() {
    return metaBean().securityIdSearch().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the value of the property
   */
  public String getSecurityIdValue() {
    return _securityIdValue;
  }

  /**
   * Sets the external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @param securityIdValue  the new value of the property
   */
  public void setSecurityIdValue(String securityIdValue) {
    this._securityIdValue = securityIdValue;
  }

  /**
   * Gets the the {@code securityIdValue} property.
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the property, not null
   */
  public final Property<String> securityIdValue() {
    return metaBean().securityIdValue().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the Trade data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public ExternalId getTradeProviderId() {
    return _TradeProviderId;
  }

  /**
   * Sets the Trade data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param TradeProviderId  the new value of the property
   */
  public void setTradeProviderId(ExternalId TradeProviderId) {
    this._TradeProviderId = TradeProviderId;
  }

  /**
   * Gets the the {@code TradeProviderId} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<ExternalId> TradeProviderId() {
    return metaBean().TradeProviderId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the trade data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @return the value of the property
   */
  public ExternalId getTradeProviderId() {
    return _tradeProviderId;
  }

  /**
   * Sets the trade data provider identifier to match, null to not match on provider.
   * This field is useful when receiving updates from the same provider.
   * @param tradeProviderId  the new value of the property
   */
  public void setTradeProviderId(ExternalId tradeProviderId) {
    this._tradeProviderId = tradeProviderId;
  }

  /**
   * Gets the the {@code tradeProviderId} property.
   * This field is useful when receiving updates from the same provider.
   * @return the property, not null
   */
  public final Property<ExternalId> tradeProviderId() {
    return metaBean().tradeProviderId().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the minimum quantity, inclusive, null for no minimum.
   * @return the value of the property
   */
  public BigDecimal getMinQuantity() {
    return _minQuantity;
  }

  /**
   * Sets the minimum quantity, inclusive, null for no minimum.
   * @param minQuantity  the new value of the property
   */
  public void setMinQuantity(BigDecimal minQuantity) {
    this._minQuantity = minQuantity;
  }

  /**
   * Gets the the {@code minQuantity} property.
   * @return the property, not null
   */
  public final Property<BigDecimal> minQuantity() {
    return metaBean().minQuantity().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the maximum quantity, exclusive, null for no maximum.
   * @return the value of the property
   */
  public BigDecimal getMaxQuantity() {
    return _maxQuantity;
  }

  /**
   * Sets the maximum quantity, exclusive, null for no maximum.
   * @param maxQuantity  the new value of the property
   */
  public void setMaxQuantity(BigDecimal maxQuantity) {
    this._maxQuantity = maxQuantity;
  }

  /**
   * Gets the the {@code maxQuantity} property.
   * @return the property, not null
   */
  public final Property<BigDecimal> maxQuantity() {
    return metaBean().maxQuantity().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public TradeSearchRequest clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      TradeSearchRequest other = (TradeSearchRequest) obj;
      return JodaBeanUtils.equal(getTradeObjectIds(), other.getTradeObjectIds()) &&
          JodaBeanUtils.equal(getTradeObjectIds(), other.getTradeObjectIds()) &&
          JodaBeanUtils.equal(getSecurityIdSearch(), other.getSecurityIdSearch()) &&
          JodaBeanUtils.equal(getSecurityIdValue(), other.getSecurityIdValue()) &&
          JodaBeanUtils.equal(getTradeProviderId(), other.getTradeProviderId()) &&
          JodaBeanUtils.equal(getTradeProviderId(), other.getTradeProviderId()) &&
          JodaBeanUtils.equal(getMinQuantity(), other.getMinQuantity()) &&
          JodaBeanUtils.equal(getMaxQuantity(), other.getMaxQuantity()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecurityIdSearch());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSecurityIdValue());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeProviderId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeProviderId());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMinQuantity());
    hash = hash * 31 + JodaBeanUtils.hashCode(getMaxQuantity());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(288);
    buf.append("TradeSearchRequest{");
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
    buf.append("TradeObjectIds").append('=').append(JodaBeanUtils.toString(getTradeObjectIds())).append(',').append(' ');
    buf.append("tradeObjectIds").append('=').append(JodaBeanUtils.toString(getTradeObjectIds())).append(',').append(' ');
    buf.append("securityIdSearch").append('=').append(JodaBeanUtils.toString(getSecurityIdSearch())).append(',').append(' ');
    buf.append("securityIdValue").append('=').append(JodaBeanUtils.toString(getSecurityIdValue())).append(',').append(' ');
    buf.append("TradeProviderId").append('=').append(JodaBeanUtils.toString(getTradeProviderId())).append(',').append(' ');
    buf.append("tradeProviderId").append('=').append(JodaBeanUtils.toString(getTradeProviderId())).append(',').append(' ');
    buf.append("minQuantity").append('=').append(JodaBeanUtils.toString(getMinQuantity())).append(',').append(' ');
    buf.append("maxQuantity").append('=').append(JodaBeanUtils.toString(getMaxQuantity())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code TradeSearchRequest}.
   */
  public static class Meta extends AbstractSearchRequest.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code TradeObjectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ObjectId>> _TradeObjectIds = DirectMetaProperty.ofReadWrite(
        this, "TradeObjectIds", TradeSearchRequest.class, (Class) Set.class);
    /**
     * The meta-property for the {@code tradeObjectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ObjectId>> _tradeObjectIds = DirectMetaProperty.ofReadWrite(
        this, "tradeObjectIds", TradeSearchRequest.class, (Class) Set.class);
    /**
     * The meta-property for the {@code securityIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _securityIdSearch = DirectMetaProperty.ofReadWrite(
        this, "securityIdSearch", TradeSearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code securityIdValue} property.
     */
    private final MetaProperty<String> _securityIdValue = DirectMetaProperty.ofReadWrite(
        this, "securityIdValue", TradeSearchRequest.class, String.class);
    /**
     * The meta-property for the {@code TradeProviderId} property.
     */
    private final MetaProperty<ExternalId> _TradeProviderId = DirectMetaProperty.ofReadWrite(
        this, "TradeProviderId", TradeSearchRequest.class, ExternalId.class);
    /**
     * The meta-property for the {@code tradeProviderId} property.
     */
    private final MetaProperty<ExternalId> _tradeProviderId = DirectMetaProperty.ofReadWrite(
        this, "tradeProviderId", TradeSearchRequest.class, ExternalId.class);
    /**
     * The meta-property for the {@code minQuantity} property.
     */
    private final MetaProperty<BigDecimal> _minQuantity = DirectMetaProperty.ofReadWrite(
        this, "minQuantity", TradeSearchRequest.class, BigDecimal.class);
    /**
     * The meta-property for the {@code maxQuantity} property.
     */
    private final MetaProperty<BigDecimal> _maxQuantity = DirectMetaProperty.ofReadWrite(
        this, "maxQuantity", TradeSearchRequest.class, BigDecimal.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "TradeObjectIds",
        "tradeObjectIds",
        "securityIdSearch",
        "securityIdValue",
        "TradeProviderId",
        "tradeProviderId",
        "minQuantity",
        "maxQuantity");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -88800304:  // TradeObjectIds
          return _TradeObjectIds;
        case 572505589:  // tradeObjectIds
          return _tradeObjectIds;
        case 1137408515:  // securityIdSearch
          return _securityIdSearch;
        case -930478666:  // securityIdValue
          return _securityIdValue;
        case 680799477:  // TradeProviderId
          return _TradeProviderId;
        case -293554320:  // tradeProviderId
          return _tradeProviderId;
        case 69860605:  // minQuantity
          return _minQuantity;
        case 747293199:  // maxQuantity
          return _maxQuantity;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends TradeSearchRequest> builder() {
      return new DirectBeanBuilder<TradeSearchRequest>(new TradeSearchRequest());
    }

    @Override
    public Class<? extends TradeSearchRequest> beanType() {
      return TradeSearchRequest.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code TradeObjectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ObjectId>> TradeObjectIds() {
      return _TradeObjectIds;
    }

    /**
     * The meta-property for the {@code tradeObjectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ObjectId>> tradeObjectIds() {
      return _tradeObjectIds;
    }

    /**
     * The meta-property for the {@code securityIdSearch} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdSearch> securityIdSearch() {
      return _securityIdSearch;
    }

    /**
     * The meta-property for the {@code securityIdValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> securityIdValue() {
      return _securityIdValue;
    }

    /**
     * The meta-property for the {@code TradeProviderId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> TradeProviderId() {
      return _TradeProviderId;
    }

    /**
     * The meta-property for the {@code tradeProviderId} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalId> tradeProviderId() {
      return _tradeProviderId;
    }

    /**
     * The meta-property for the {@code minQuantity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> minQuantity() {
      return _minQuantity;
    }

    /**
     * The meta-property for the {@code maxQuantity} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> maxQuantity() {
      return _maxQuantity;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -88800304:  // TradeObjectIds
          return ((TradeSearchRequest) bean).getTradeObjectIds();
        case 572505589:  // tradeObjectIds
          return ((TradeSearchRequest) bean).getTradeObjectIds();
        case 1137408515:  // securityIdSearch
          return ((TradeSearchRequest) bean).getSecurityIdSearch();
        case -930478666:  // securityIdValue
          return ((TradeSearchRequest) bean).getSecurityIdValue();
        case 680799477:  // TradeProviderId
          return ((TradeSearchRequest) bean).getTradeProviderId();
        case -293554320:  // tradeProviderId
          return ((TradeSearchRequest) bean).getTradeProviderId();
        case 69860605:  // minQuantity
          return ((TradeSearchRequest) bean).getMinQuantity();
        case 747293199:  // maxQuantity
          return ((TradeSearchRequest) bean).getMaxQuantity();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -88800304:  // TradeObjectIds
          ((TradeSearchRequest) bean).setTradeObjectIds((Set<ObjectId>) newValue);
          return;
        case 572505589:  // tradeObjectIds
          ((TradeSearchRequest) bean).setTradeObjectIds((Set<ObjectId>) newValue);
          return;
        case 1137408515:  // securityIdSearch
          ((TradeSearchRequest) bean).setSecurityIdSearch((ExternalIdSearch) newValue);
          return;
        case -930478666:  // securityIdValue
          ((TradeSearchRequest) bean).setSecurityIdValue((String) newValue);
          return;
        case 680799477:  // TradeProviderId
          ((TradeSearchRequest) bean).setTradeProviderId((ExternalId) newValue);
          return;
        case -293554320:  // tradeProviderId
          ((TradeSearchRequest) bean).setTradeProviderId((ExternalId) newValue);
          return;
        case 69860605:  // minQuantity
          ((TradeSearchRequest) bean).setMinQuantity((BigDecimal) newValue);
          return;
        case 747293199:  // maxQuantity
          ((TradeSearchRequest) bean).setMaxQuantity((BigDecimal) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
