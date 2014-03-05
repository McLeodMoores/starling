/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.tool.portfolio.xml.v1_0.jaxb;

import java.math.BigDecimal;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

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
import org.threeten.bp.LocalDate;
import org.threeten.bp.YearMonth;

import com.opengamma.integration.tool.portfolio.xml.v1_0.conversion.ListedFutureSecurityExtractor;
import com.opengamma.integration.tool.portfolio.xml.v1_0.conversion.ListedSecurityExtractor;

@XmlRootElement(name = "futureSecurity")
@BeanDefinition
public class FutureSecurityDefinition extends ListedSecurityDefinition {

  public enum ListedFutureType {
    @XmlEnumValue(value = "equityIndexFuture")
    EQUITY_INDEX_FUTURE,
    @XmlEnumValue(value = "equityDividendFuture")
    EQUITY_DIVIDEND_FUTURE
  }

  @XmlAttribute(name = "type", required = true)
  @PropertyDefinition(validate = "notNull")
  private ListedFutureType _futureType;

  @XmlAttribute(name = "price", required = true)
  @PropertyDefinition(validate = "notNull")
  private BigDecimal _price;

  @XmlAttribute(name = "futureExpiry", required = true)
  @XmlJavaTypeAdapter(DerivativeExpiryDateAdapter.class)
  @PropertyDefinition(validate = "notNull")
  private YearMonth _futureExpiry;

  @XmlAttribute(name = "settlementExchange")
  @PropertyDefinition()
  private String _settlementExchange;

  @XmlAttribute(name = "settlemenDate")
  @PropertyDefinition()
  private LocalDate _settlementDate;

  @XmlAttribute(name = "futureCategory")
  @PropertyDefinition()
  private String _futureCategory;

  @Override
  public ListedSecurityExtractor getSecurityExtractor() {
    return new ListedFutureSecurityExtractor(this);
  }
  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FutureSecurityDefinition}.
   * @return the meta-bean, not null
   */
  public static FutureSecurityDefinition.Meta meta() {
    return FutureSecurityDefinition.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FutureSecurityDefinition.Meta.INSTANCE);
  }

  @Override
  public FutureSecurityDefinition.Meta metaBean() {
    return FutureSecurityDefinition.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the futureType.
   * @return the value of the property, not null
   */
  public ListedFutureType getFutureType() {
    return _futureType;
  }

  /**
   * Sets the futureType.
   * @param futureType  the new value of the property, not null
   */
  public void setFutureType(ListedFutureType futureType) {
    JodaBeanUtils.notNull(futureType, "futureType");
    this._futureType = futureType;
  }

  /**
   * Gets the the {@code futureType} property.
   * @return the property, not null
   */
  public final Property<ListedFutureType> futureType() {
    return metaBean().futureType().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the price.
   * @return the value of the property, not null
   */
  public BigDecimal getPrice() {
    return _price;
  }

  /**
   * Sets the price.
   * @param price  the new value of the property, not null
   */
  public void setPrice(BigDecimal price) {
    JodaBeanUtils.notNull(price, "price");
    this._price = price;
  }

  /**
   * Gets the the {@code price} property.
   * @return the property, not null
   */
  public final Property<BigDecimal> price() {
    return metaBean().price().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the futureExpiry.
   * @return the value of the property, not null
   */
  public YearMonth getFutureExpiry() {
    return _futureExpiry;
  }

  /**
   * Sets the futureExpiry.
   * @param futureExpiry  the new value of the property, not null
   */
  public void setFutureExpiry(YearMonth futureExpiry) {
    JodaBeanUtils.notNull(futureExpiry, "futureExpiry");
    this._futureExpiry = futureExpiry;
  }

  /**
   * Gets the the {@code futureExpiry} property.
   * @return the property, not null
   */
  public final Property<YearMonth> futureExpiry() {
    return metaBean().futureExpiry().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the settlementExchange.
   * @return the value of the property
   */
  public String getSettlementExchange() {
    return _settlementExchange;
  }

  /**
   * Sets the settlementExchange.
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
   * Gets the settlementDate.
   * @return the value of the property
   */
  public LocalDate getSettlementDate() {
    return _settlementDate;
  }

  /**
   * Sets the settlementDate.
   * @param settlementDate  the new value of the property
   */
  public void setSettlementDate(LocalDate settlementDate) {
    this._settlementDate = settlementDate;
  }

  /**
   * Gets the the {@code settlementDate} property.
   * @return the property, not null
   */
  public final Property<LocalDate> settlementDate() {
    return metaBean().settlementDate().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the futureCategory.
   * @return the value of the property
   */
  public String getFutureCategory() {
    return _futureCategory;
  }

  /**
   * Sets the futureCategory.
   * @param futureCategory  the new value of the property
   */
  public void setFutureCategory(String futureCategory) {
    this._futureCategory = futureCategory;
  }

  /**
   * Gets the the {@code futureCategory} property.
   * @return the property, not null
   */
  public final Property<String> futureCategory() {
    return metaBean().futureCategory().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FutureSecurityDefinition clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FutureSecurityDefinition other = (FutureSecurityDefinition) obj;
      return JodaBeanUtils.equal(getFutureType(), other.getFutureType()) &&
          JodaBeanUtils.equal(getPrice(), other.getPrice()) &&
          JodaBeanUtils.equal(getFutureExpiry(), other.getFutureExpiry()) &&
          JodaBeanUtils.equal(getSettlementExchange(), other.getSettlementExchange()) &&
          JodaBeanUtils.equal(getSettlementDate(), other.getSettlementDate()) &&
          JodaBeanUtils.equal(getFutureCategory(), other.getFutureCategory()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash += hash * 31 + JodaBeanUtils.hashCode(getFutureType());
    hash += hash * 31 + JodaBeanUtils.hashCode(getPrice());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFutureExpiry());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSettlementExchange());
    hash += hash * 31 + JodaBeanUtils.hashCode(getSettlementDate());
    hash += hash * 31 + JodaBeanUtils.hashCode(getFutureCategory());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(224);
    buf.append("FutureSecurityDefinition{");
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
    buf.append("futureType").append('=').append(JodaBeanUtils.toString(getFutureType())).append(',').append(' ');
    buf.append("price").append('=').append(JodaBeanUtils.toString(getPrice())).append(',').append(' ');
    buf.append("futureExpiry").append('=').append(JodaBeanUtils.toString(getFutureExpiry())).append(',').append(' ');
    buf.append("settlementExchange").append('=').append(JodaBeanUtils.toString(getSettlementExchange())).append(',').append(' ');
    buf.append("settlementDate").append('=').append(JodaBeanUtils.toString(getSettlementDate())).append(',').append(' ');
    buf.append("futureCategory").append('=').append(JodaBeanUtils.toString(getFutureCategory())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FutureSecurityDefinition}.
   */
  public static class Meta extends ListedSecurityDefinition.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code futureType} property.
     */
    private final MetaProperty<ListedFutureType> _futureType = DirectMetaProperty.ofReadWrite(
        this, "futureType", FutureSecurityDefinition.class, ListedFutureType.class);
    /**
     * The meta-property for the {@code price} property.
     */
    private final MetaProperty<BigDecimal> _price = DirectMetaProperty.ofReadWrite(
        this, "price", FutureSecurityDefinition.class, BigDecimal.class);
    /**
     * The meta-property for the {@code futureExpiry} property.
     */
    private final MetaProperty<YearMonth> _futureExpiry = DirectMetaProperty.ofReadWrite(
        this, "futureExpiry", FutureSecurityDefinition.class, YearMonth.class);
    /**
     * The meta-property for the {@code settlementExchange} property.
     */
    private final MetaProperty<String> _settlementExchange = DirectMetaProperty.ofReadWrite(
        this, "settlementExchange", FutureSecurityDefinition.class, String.class);
    /**
     * The meta-property for the {@code settlementDate} property.
     */
    private final MetaProperty<LocalDate> _settlementDate = DirectMetaProperty.ofReadWrite(
        this, "settlementDate", FutureSecurityDefinition.class, LocalDate.class);
    /**
     * The meta-property for the {@code futureCategory} property.
     */
    private final MetaProperty<String> _futureCategory = DirectMetaProperty.ofReadWrite(
        this, "futureCategory", FutureSecurityDefinition.class, String.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "futureType",
        "price",
        "futureExpiry",
        "settlementExchange",
        "settlementDate",
        "futureCategory");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case 537589661:  // futureType
          return _futureType;
        case 106934601:  // price
          return _price;
        case 797235414:  // futureExpiry
          return _futureExpiry;
        case 389497452:  // settlementExchange
          return _settlementExchange;
        case -295948169:  // settlementDate
          return _settlementDate;
        case -673825823:  // futureCategory
          return _futureCategory;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FutureSecurityDefinition> builder() {
      return new DirectBeanBuilder<FutureSecurityDefinition>(new FutureSecurityDefinition());
    }

    @Override
    public Class<? extends FutureSecurityDefinition> beanType() {
      return FutureSecurityDefinition.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code futureType} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<ListedFutureType> futureType() {
      return _futureType;
    }

    /**
     * The meta-property for the {@code price} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<BigDecimal> price() {
      return _price;
    }

    /**
     * The meta-property for the {@code futureExpiry} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<YearMonth> futureExpiry() {
      return _futureExpiry;
    }

    /**
     * The meta-property for the {@code settlementExchange} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> settlementExchange() {
      return _settlementExchange;
    }

    /**
     * The meta-property for the {@code settlementDate} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<LocalDate> settlementDate() {
      return _settlementDate;
    }

    /**
     * The meta-property for the {@code futureCategory} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<String> futureCategory() {
      return _futureCategory;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 537589661:  // futureType
          return ((FutureSecurityDefinition) bean).getFutureType();
        case 106934601:  // price
          return ((FutureSecurityDefinition) bean).getPrice();
        case 797235414:  // futureExpiry
          return ((FutureSecurityDefinition) bean).getFutureExpiry();
        case 389497452:  // settlementExchange
          return ((FutureSecurityDefinition) bean).getSettlementExchange();
        case -295948169:  // settlementDate
          return ((FutureSecurityDefinition) bean).getSettlementDate();
        case -673825823:  // futureCategory
          return ((FutureSecurityDefinition) bean).getFutureCategory();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case 537589661:  // futureType
          ((FutureSecurityDefinition) bean).setFutureType((ListedFutureType) newValue);
          return;
        case 106934601:  // price
          ((FutureSecurityDefinition) bean).setPrice((BigDecimal) newValue);
          return;
        case 797235414:  // futureExpiry
          ((FutureSecurityDefinition) bean).setFutureExpiry((YearMonth) newValue);
          return;
        case 389497452:  // settlementExchange
          ((FutureSecurityDefinition) bean).setSettlementExchange((String) newValue);
          return;
        case -295948169:  // settlementDate
          ((FutureSecurityDefinition) bean).setSettlementDate((LocalDate) newValue);
          return;
        case -673825823:  // futureCategory
          ((FutureSecurityDefinition) bean).setFutureCategory((String) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

    @Override
    protected void validate(Bean bean) {
      JodaBeanUtils.notNull(((FutureSecurityDefinition) bean)._futureType, "futureType");
      JodaBeanUtils.notNull(((FutureSecurityDefinition) bean)._price, "price");
      JodaBeanUtils.notNull(((FutureSecurityDefinition) bean)._futureExpiry, "futureExpiry");
      super.validate(bean);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
