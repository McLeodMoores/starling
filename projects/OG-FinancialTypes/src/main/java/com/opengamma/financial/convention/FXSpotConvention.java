/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 *
 * Modified by McLeod Moores Software Limited.
 *
 * Copyright (C) 2015-Present McLeod Moores Software Limited.  All rights reserved.
 */
package com.opengamma.financial.convention;

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

import com.opengamma.core.convention.ConventionType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Convention for FX spot.
 */
@BeanDefinition
public class FXSpotConvention extends FinancialConvention {

  /**
   * Type of the convention.
   */
  public static final ConventionType TYPE = ConventionType.of("FXSpot");

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The number of settlement days.
   */
  @PropertyDefinition
  private int _settlementDays;
  /**
   * The settlement region.
   * @deprecated  both regions and US holidays should be considered when calculating
   * the settlement date
   */
  @PropertyDefinition(set = "manual")
  @Deprecated
  private ExternalId _settlementRegion;
  /**
   * True if intermediate US holidays should be considered when calculating the settlement
   * date.
   */
  @PropertyDefinition(set = "manual")
  private Boolean _useIntermediateUsHolidays;

  /**
   * Creates an instance.
   */
  protected FXSpotConvention() {
  }

  /**
   * Creates an instance.
   *
   * @param name  the convention name, not null
   * @param externalIdBundle  the external identifiers for this convention, not null
   * @param settlementDays  the number of settlement days
   * @param settlementRegion  the settlement region, can be null
   * @deprecated  the settlement region should not be used, as FX settlement dates are calculated using
   * both conventions
   */
  @Deprecated
  public FXSpotConvention(final String name, final ExternalIdBundle externalIdBundle, final int settlementDays,
      final ExternalId settlementRegion) {
    super(name, externalIdBundle);
    setSettlementDays(settlementDays);
    setSettlementRegion(settlementRegion);
    setUseIntermediateUsHolidays(null);
  }

  /**
   * Creates an instance.
   *
   * @param name  the convention name, not null
   * @param externalIdBundle  the external identifiers for this convention, not null
   * @param settlementDays  the number of settlement days
   * @param useIntermediateUsHolidays  true if US holidays between the maturity date and settlement date should be 
   * considered when calculating the settlement date
   */
  public FXSpotConvention(final String name, final ExternalIdBundle externalIdBundle, final int settlementDays,
      final boolean useIntermediateUsHolidays) {
    super(name, externalIdBundle);
    setSettlementDays(settlementDays);
    setSettlementRegion(null);
    setUseIntermediateUsHolidays(useIntermediateUsHolidays);
  }

  /**
   * Tests that either the settlementRegion field or the useIntermediateUsHolidays fields are set, not
   * both.
   * @param settlementRegion  the settlementRegion
   * @param useIntermediateUsHolidays  the use intermediate US holidays flag
   */
  private static void checkConsistentSettlement(final ExternalId settlementRegion, final Boolean useIntermediateUsHolidays) {
    if (settlementRegion != null && useIntermediateUsHolidays != null) {
      throw new IllegalStateException("Cannot set settlement region and the useIntermediateUsHolidays field");
    } 
  }
  
  /**
   * Sets the settlement region.
   * @deprecated  both regions and US holidays should be considered when calculating
   * the settlement date
   * @param settlementRegion  the new value of the property
   */
  @Deprecated
  public void setSettlementRegion(final ExternalId settlementRegion) {
    checkConsistentSettlement(settlementRegion, getUseIntermediateUsHolidays());
    this._settlementRegion = settlementRegion;
  }

  /**
   * Sets true if intermediate US holidays should be used to calculate the settlement date.
   * @param useIntermediateUsHolidays  the new value of the property
   */
  public void setUseIntermediateUsHolidays(final Boolean useIntermediateUsHolidays) {
    checkConsistentSettlement(getSettlementRegion(), useIntermediateUsHolidays);
    this._useIntermediateUsHolidays = useIntermediateUsHolidays;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Gets the type identifying this convention.
   *
   * @return the {@link #TYPE} constant, not null
   */
  @Override
  public ConventionType getConventionType() {
    return TYPE;
  }

  /**
   * Accepts a visitor to manage traversal of the hierarchy.
   *
   * @param <T>  the result type of the visitor
   * @param visitor  the visitor, not null
   * @return the result
   */
  @Override
  public <T> T accept(final FinancialConventionVisitor<T> visitor) {
    ArgumentChecker.notNull(visitor, "visitor");
    return visitor.visitFXSpotConvention(this);
  }

  //------------------------- AUTOGENERATED START -------------------------
  ///CLOVER:OFF
  /**
   * The meta-bean for {@code FXSpotConvention}.
   * @return the meta-bean, not null
   */
  public static FXSpotConvention.Meta meta() {
    return FXSpotConvention.Meta.INSTANCE;
  }

  static {
    JodaBeanUtils.registerMetaBean(FXSpotConvention.Meta.INSTANCE);
  }

  @Override
  public FXSpotConvention.Meta metaBean() {
    return FXSpotConvention.Meta.INSTANCE;
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the number of settlement days.
   * @return the value of the property
   */
  public int getSettlementDays() {
    return _settlementDays;
  }

  /**
   * Sets the number of settlement days.
   * @param settlementDays  the new value of the property
   */
  public void setSettlementDays(int settlementDays) {
    this._settlementDays = settlementDays;
  }

  /**
   * Gets the the {@code settlementDays} property.
   * @return the property, not null
   */
  public final Property<Integer> settlementDays() {
    return metaBean().settlementDays().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets the settlement region.
   * @deprecated  both regions and US holidays should be considered when calculating
   * the settlement date
   * @return the value of the property
   */
  @Deprecated
  public ExternalId getSettlementRegion() {
    return _settlementRegion;
  }

  /**
   * Gets the the {@code settlementRegion} property.
   * @deprecated  both regions and US holidays should be considered when calculating
   * the settlement date
   * @return the property, not null
   */
  @Deprecated
  public final Property<ExternalId> settlementRegion() {
    return metaBean().settlementRegion().createProperty(this);
  }

  //-----------------------------------------------------------------------
  /**
   * Gets true if intermediate US holidays should be considered when calculating the settlement
   * date.
   * @return the value of the property
   */
  public Boolean getUseIntermediateUsHolidays() {
    return _useIntermediateUsHolidays;
  }

  /**
   * Gets the the {@code useIntermediateUsHolidays} property.
   * date.
   * @return the property, not null
   */
  public final Property<Boolean> useIntermediateUsHolidays() {
    return metaBean().useIntermediateUsHolidays().createProperty(this);
  }

  //-----------------------------------------------------------------------
  @Override
  public FXSpotConvention clone() {
    return JodaBeanUtils.cloneAlways(this);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj != null && obj.getClass() == this.getClass()) {
      FXSpotConvention other = (FXSpotConvention) obj;
      return (getSettlementDays() == other.getSettlementDays()) &&
          JodaBeanUtils.equal(getSettlementRegion(), other.getSettlementRegion()) &&
          JodaBeanUtils.equal(getUseIntermediateUsHolidays(), other.getUseIntermediateUsHolidays()) &&
          super.equals(obj);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = hash * 31 + JodaBeanUtils.hashCode(getSettlementDays());
    hash = hash * 31 + JodaBeanUtils.hashCode(getSettlementRegion());
    hash = hash * 31 + JodaBeanUtils.hashCode(getUseIntermediateUsHolidays());
    return hash ^ super.hashCode();
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder(128);
    buf.append("FXSpotConvention{");
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
    buf.append("settlementDays").append('=').append(JodaBeanUtils.toString(getSettlementDays())).append(',').append(' ');
    buf.append("settlementRegion").append('=').append(JodaBeanUtils.toString(getSettlementRegion())).append(',').append(' ');
    buf.append("useIntermediateUsHolidays").append('=').append(JodaBeanUtils.toString(getUseIntermediateUsHolidays())).append(',').append(' ');
  }

  //-----------------------------------------------------------------------
  /**
   * The meta-bean for {@code FXSpotConvention}.
   */
  public static class Meta extends FinancialConvention.Meta {
    /**
     * The singleton instance of the meta-bean.
     */
    static final Meta INSTANCE = new Meta();

    /**
     * The meta-property for the {@code settlementDays} property.
     */
    private final MetaProperty<Integer> _settlementDays = DirectMetaProperty.ofReadWrite(
        this, "settlementDays", FXSpotConvention.class, Integer.TYPE);
    /**
     * The meta-property for the {@code settlementRegion} property.
     */
    private final MetaProperty<ExternalId> _settlementRegion = DirectMetaProperty.ofReadWrite(
        this, "settlementRegion", FXSpotConvention.class, ExternalId.class);
    /**
     * The meta-property for the {@code useIntermediateUsHolidays} property.
     */
    private final MetaProperty<Boolean> _useIntermediateUsHolidays = DirectMetaProperty.ofReadWrite(
        this, "useIntermediateUsHolidays", FXSpotConvention.class, Boolean.class);
    /**
     * The meta-properties.
     */
    private final Map<String, MetaProperty<?>> _metaPropertyMap$ = new DirectMetaPropertyMap(
        this, (DirectMetaPropertyMap) super.metaPropertyMap(),
        "settlementDays",
        "settlementRegion",
        "useIntermediateUsHolidays");

    /**
     * Restricted constructor.
     */
    protected Meta() {
    }

    @Override
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
      switch (propertyName.hashCode()) {
        case -295948000:  // settlementDays
          return _settlementDays;
        case -534226563:  // settlementRegion
          return _settlementRegion;
        case -1741761511:  // useIntermediateUsHolidays
          return _useIntermediateUsHolidays;
      }
      return super.metaPropertyGet(propertyName);
    }

    @Override
    public BeanBuilder<? extends FXSpotConvention> builder() {
      return new DirectBeanBuilder<FXSpotConvention>(new FXSpotConvention());
    }

    @Override
    public Class<? extends FXSpotConvention> beanType() {
      return FXSpotConvention.class;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
      return _metaPropertyMap$;
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-property for the {@code settlementDays} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Integer> settlementDays() {
      return _settlementDays;
    }

    /**
     * The meta-property for the {@code settlementRegion} property.
     * @deprecated  both regions and US holidays should be considered when calculating
     * @return the meta-property, not null
     */
    @Deprecated
    public final MetaProperty<ExternalId> settlementRegion() {
      return _settlementRegion;
    }

    /**
     * The meta-property for the {@code useIntermediateUsHolidays} property.
     * @return the meta-property, not null
     */
    public final MetaProperty<Boolean> useIntermediateUsHolidays() {
      return _useIntermediateUsHolidays;
    }

    //-----------------------------------------------------------------------
    @Override
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -295948000:  // settlementDays
          return ((FXSpotConvention) bean).getSettlementDays();
        case -534226563:  // settlementRegion
          return ((FXSpotConvention) bean).getSettlementRegion();
        case -1741761511:  // useIntermediateUsHolidays
          return ((FXSpotConvention) bean).getUseIntermediateUsHolidays();
      }
      return super.propertyGet(bean, propertyName, quiet);
    }

    @Override
    protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
      switch (propertyName.hashCode()) {
        case -295948000:  // settlementDays
          ((FXSpotConvention) bean).setSettlementDays((Integer) newValue);
          return;
        case -534226563:  // settlementRegion
          ((FXSpotConvention) bean).setSettlementRegion((ExternalId) newValue);
          return;
        case -1741761511:  // useIntermediateUsHolidays
          ((FXSpotConvention) bean).setUseIntermediateUsHolidays((Boolean) newValue);
          return;
      }
      super.propertySet(bean, propertyName, newValue, quiet);
    }

  }

  ///CLOVER:ON
  //-------------------------- AUTOGENERATED END --------------------------
}
