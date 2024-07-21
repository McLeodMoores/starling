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
      setTradeIdSearch(getTradeIdSearch().withExternalIdsAdded(tradeIds));
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
    if (getTradeIdSearch() != null && !getTradeIdSearch().matches(trade.getExternalTradeIds())) {
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

  //-----------------------------------------------------------------------
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
   * Sets the trade id identifiers to match, null to not match on trade identifiers.
   * @param tradeIdSearch  the new value of the property
   */
  public void setTradeIdSearch(ExternalIdSearch tradeIdSearch) {
    this._tradeIdSearch = tradeIdSearch;
  }

  /**
   * Gets the the {@code tradeIdSearch} property.
   * @return the property, not null
   */
  public final Property<ExternalIdSearch> tradeIdSearch() {
    return metaBean().tradeIdSearch().createProperty(this);
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
  public String getTradeIdValue() {
    return _tradeIdValue;
  }

  /**
   * Sets the external identifier value, matching against the <b>value</b> of the identifiers,
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @param tradeIdValue  the new value of the property
   */
  public void setTradeIdValue(String tradeIdValue) {
    this._tradeIdValue = tradeIdValue;
  }

  /**
   * Gets the the {@code tradeIdValue} property.
   * null to not match by identifier value.
   * This matches against the {@link ExternalId#getValue() value} of the identifier
   * and does not match against the key. Wildcards are allowed.
   * This method is suitable for human searching, whereas the {@code externalIdSearch}
   * search is useful for exact machine searching.
   * @return the property, not null
   */
  public final Property<String> tradeIdValue() {
    return metaBean().tradeIdValue().createProperty(this);
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
          JodaBeanUtils.equal(getTradeIdSearch(), other.getTradeIdSearch()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeObjectIds());
    hash = hash * 31 + JodaBeanUtils.hashCode(getTradeIdSearch());
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
    buf.append("securityIdSearch").append('=').append(JodaBeanUtils.toString(getTradeIdSearch())).append(',').append(' ');
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
     * The meta-property for the {@code tradeObjectIds} property.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private final MetaProperty<Set<ObjectId>> _tradeObjectIds = DirectMetaProperty.ofReadWrite(
        this, "tradeObjectIds", TradeSearchRequest.class, (Class) Set.class);
    /**
     * The meta-property for the {@code securityIdSearch} property.
     */
    private final MetaProperty<ExternalIdSearch> _tradeIdSearch = DirectMetaProperty.ofReadWrite(
        this, "tradeIdSearch", TradeSearchRequest.class, ExternalIdSearch.class);
    /**
     * The meta-property for the {@code securityIdSearch} property.
     */
    private final MetaProperty<String> _tradeIdValue = DirectMetaProperty.ofReadWrite(
        this, "tradeIdValue", TradeSearchRequest.class, String.class);

    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "TradeObjectIds",
        "tradeObjectIds",
        "tradeIdSearch",
        "tradeIdValue");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 572505589:  // tradeObjectIds
          return _tradeObjectIds;
        case 1137408515:  // securityIdSearch
          return _tradeIdSearch;
        case -930478666:  // securityIdValue
          return _tradeIdValue;
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
     * The meta-property for the {@code tradeObjectIds} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Set<ObjectId>> tradeObjectIds() {
      return _tradeObjectIds;
    }

    /**
     * The meta-property for the {@code tradeIdSearch} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ExternalIdSearch> tradeIdSearch() {
      return _tradeIdSearch;
    }

    /**
     * The meta-property for the {@code tradeIdValue} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> tradeIdValue() {
      return _tradeIdValue;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 572505589:  // tradeObjectIds
          return ((TradeSearchRequest) bean).getTradeObjectIds();
        case 1137408515:  // tradeIdSearch
          return ((TradeSearchRequest) bean).getTradeIdSearch();
        case -930478666:  // tradeIdValue
          return ((TradeSearchRequest) bean).getTradeIdValue();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 572505589:  // tradeObjectIds
          ((TradeSearchRequest) bean).setTradeObjectIds((Set<ObjectId>) newValue);
          return;
        case 1137408515:  // tradeIdSearch
          ((TradeSearchRequest) bean).setTradeIdSearch((ExternalIdSearch) newValue);
          return;
        case -930478666:  // securityIdValue
          ((TradeSearchRequest) bean).setTradeIdValue((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
